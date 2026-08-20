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
        if (world != null && location.getY() < location.getWorld().getMinHeight()) {
            ItemStack[] playerItems = player.getInventory().getContents();

            boolean haveUnorderedDust = false;
            boolean haveBox = false;
            int orderedDustAmount = 0;
            for (ItemStack itemStack : playerItems) {
                if (FinalTechItems.ORDERED_DUST.verifyItem(itemStack)) {
                    orderedDustAmount += itemStack.getAmount();
                } else if (FinalTechItems.BOX.verifyItem(itemStack)) {
                    haveBox = true;
                } else if (FinalTechItems.UNORDERED_DUST.verifyItem(itemStack)) {
                    haveUnorderedDust = true;
                }
            }

            if (haveUnorderedDust && !haveBox) {
                // world.dropItem(location, FinalTechItems.BOX.getValidItem());
                player.getInventory().addItem(FinalTechItems.BOX.getValidItem());
                if (orderedDustAmount > 0) {
                    int playerExpLevel = player.getLevel();
                    int extraItemAmount = Math.min(orderedDustAmount, playerExpLevel);
                    extraItemAmount = Math.min(extraItemAmount, 64);
                    player.giveExpLevels(-extraItemAmount);
                    ItemStack items = FinalTechItems.BOX.getValidItem();
                    items.setAmount(extraItemAmount);
                    player.getInventory().addItem(items);
                }
            }
        }
    }
}
