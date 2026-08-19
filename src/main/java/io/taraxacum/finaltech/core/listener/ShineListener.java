package io.taraxacum.finaltech.core.listener;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.task.effect.VoidCurse;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.setup.FinalTechItems;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.libs.plugin.task.TaskTicker;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Final_ROOT
 * @see io.taraxacum.finaltech.core.item.unusable.Shine
 * @since 2.0
 */
public class ShineListener implements Listener {
    private final double damage = ConfigUtil.getOrDefaultItemSetting(0.3, FinalTechItemStacks.SHINE.getItemId(), "damage");
    private final int liveTime = ConfigUtil.getOrDefaultItemSetting(40, FinalTechItemStacks.SHINE.getItemId(), "live-time");
    private final int baseEffectTime = ConfigUtil.getOrDefaultItemSetting(100, FinalTechItemStacks.SHINE.getItemId(), "effect-time-base");
    private final int deathMulEffectTime = ConfigUtil.getOrDefaultItemSetting(40, FinalTechItemStacks.SHINE.getItemId(), "effect-time-mul-death");
    private final int obtainMulEffectTime = ConfigUtil.getOrDefaultItemSetting(2, FinalTechItemStacks.SHINE.getItemId(), "effect-time-mul-obtain");
    private final double baseTeleportRange = ConfigUtil.getOrDefaultItemSetting(16, FinalTechItemStacks.SHINE.getItemId(), "teleport-range-base");
    private final double mulTeleportRange = ConfigUtil.getOrDefaultItemSetting(0.25, FinalTechItemStacks.SHINE.getItemId(), "teleport-range-mul");

    private final Map<Player, Integer> deathCount = new HashMap<>();
    private final Map<Player, Integer> obtainCount = new HashMap<>();

    /**
     * Player will get a shine while holding item box.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent entityDamageEvent) {
        if (EntityDamageEvent.DamageCause.VOID.equals(entityDamageEvent.getCause()) && EntityType.PLAYER.equals(entityDamageEvent.getEntityType())) {
            Entity entity = entityDamageEvent.getEntity();
            Location location = entity.getLocation();
            if (entity instanceof Player player && location.getWorld() != null && location.getY() < location.getWorld().getMinHeight()) {
                int boxAmount = 0;
                int orderedDustAmount = 0;
                boolean haveShine = false;
                boolean haveUnorderedDust = false;
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (boxAmount == 0 && FinalTechItems.BOX.verifyItem(itemStack)) {
                        boxAmount += itemStack.getAmount();
                        itemStack.setAmount(0);
                    } else if (FinalTechItems.SHINE.verifyItem(itemStack)) {
                        haveShine = true;
                    } else if (FinalTechItems.UNORDERED_DUST.verifyItem(itemStack)) {
                        haveUnorderedDust = true;
                    } else if (FinalTechItems.ORDERED_DUST.verifyItem(itemStack)) {
                        orderedDustAmount += itemStack.getAmount();
                    }
                }
                if (boxAmount > 0 && !haveShine && !haveUnorderedDust) {
                    Research research = FinalTechItems.SHINE.getResearch();
                    Optional<PlayerProfile> playerProfile = PlayerProfile.find(player);
                    boolean unlock; // 是否解锁物品的研究
                    if (research == null) {
                        unlock = true;
                    } else unlock = playerProfile.isPresent() && playerProfile.get().getResearches().contains(research);

                    int obtain = this.obtainCount.getOrDefault(player, 1); // 获得耀的次数加上1
                    if (!player.isFlying() && boxAmount > 0 && !player.isDead() && unlock) {
                        Vector nowVector = player.getVelocity().clone();
                        Location nowLocation = player.getLocation();
                        Location newLocation = nowLocation.add( // x&z随机运动，获得耀的次数越多，范围越大
                                FinalTechChanged.getRandom().nextDouble() * this.baseTeleportRange * (1 + obtain * this.mulTeleportRange) * 2 - this.baseTeleportRange * (1 + obtain * this.mulTeleportRange),
                                0,
                                FinalTechChanged.getRandom().nextDouble() * this.baseTeleportRange * (1 + obtain * this.mulTeleportRange) - this.baseTeleportRange * (1 + obtain * this.mulTeleportRange));
                        player.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        player.setVelocity(nowVector);

                        PlayerInventory playerInventory = player.getInventory();
                        int itemAmount = 0;
                        if (orderedDustAmount > 0) {
                            itemAmount = Math.min(player.getLevel(), orderedDustAmount);
                            itemAmount = Math.min(itemAmount, 64);
                            itemAmount = Math.min(itemAmount, boxAmount);
                            player.giveExpLevels(-itemAmount);
                        }
                        itemAmount += 1;
                        ItemStack items = FinalTechItems.SHINE.getValidItem();
                        items.setAmount(itemAmount);
                        playerInventory.addItem(items);
                            
                        obtain++;
                        this.obtainCount.put(player, obtain);
                    }

                    int deathCount = this.deathCount.getOrDefault(player, 0);

                    TaskTicker.applyOrAddTo(new VoidCurse(this.baseEffectTime + deathCount * this.deathMulEffectTime + obtain * this.obtainMulEffectTime, 1), player, LivingEntity.class);

                    double health = player.getHealth();
                    double expectedHealth = Math.max(0, player.getHealth() - player.getMaxHealth() * this.damage);
                    entityDamageEvent.setDamage(player.getMaxHealth() * this.damage);
                    FinalTechChanged.getInstance().getServer().getScheduler().runTaskLater(FinalTechChanged.getInstance(), () -> {
                        if (player.isDead()) {
                            return;
                        }

                        if (player.getNoDamageTicks() == 0 || expectedHealth == 0 || player.getHealth() >= health) {
                            player.setHealth(0);
                            return;
                        }

                        player.setMaximumNoDamageTicks(this.liveTime);
                        player.setNoDamageTicks(Math.min(player.getNoDamageTicks() + this.liveTime - 11, this.liveTime - 1));

                        double nowHealth = player.getHealth();

                        if (nowHealth > expectedHealth) {
                            player.setHealth(Math.max(expectedHealth * 2 - nowHealth, 0));
                        }
                    }, 1);

                }
            }
        }
    }
}
