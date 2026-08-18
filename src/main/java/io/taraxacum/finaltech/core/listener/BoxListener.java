package io.taraxacum.finaltech.core.listener;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.setup.FinalTechItems;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Final_ROOT
 * @see io.taraxacum.finaltech.core.item.unusable.Box
 */
public class BoxListener implements Listener {
    private final double height;

    public BoxListener(double height) {
        this.height = height;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        Player player = playerDeathEvent.getEntity();
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world != null) {
            ItemStack[] playerItems = player.getInventory().getContents();

            boolean haveUnorderedDust = Arrays.stream(playerItems)
                    .anyMatch(FinalTechItems.UNORDERED_DUST::verifyItem); // 假设开了死亡不掉落
            int orderedDustAmount = 0;
            for (ItemStack itemStack : playerItems) {
                if (FinalTechItems.UNORDERED_DUST.verifyItem(itemStack)) {
                    orderedDustAmount += itemStack.getAmount();
                }
            }

            if (location.getY() < location.getWorld().getMinHeight()) {
                if (haveUnorderedDust) {
                    // world.dropItem(location, FinalTechItems.BOX.getValidItem());
                    player.getInventory().addItem(FinalTechItems.BOX.getValidItem());
                    if (orderedDustAmount > 0) {
                        int playerExpLevel = player.getExpToLevel();
                        int extraItemAmount = Math.min(orderedDustAmount, playerExpLevel);
                        player.giveExpLevels(-extraItemAmount);
                        ItemStack items = FinalTechItems.BOX.getValidItem();
                        items.setAmount(extraItemAmount);
                        player.getInventory().addItem(items);
                    }
                }
            }
        }
    }
}
