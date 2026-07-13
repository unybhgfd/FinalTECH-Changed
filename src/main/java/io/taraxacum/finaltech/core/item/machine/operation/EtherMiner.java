package io.taraxacum.finaltech.core.item.machine.operation;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.common.util.MathUtil;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.MenuUpdater;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.machine.EtherMinerMenu;
import io.taraxacum.finaltech.core.operation.EtherMinerOperation;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.setup.FinalTechItems;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.MachineUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalInt;

/**
 * @author Final_ROOT
 * @since 2.4
 */
public class EtherMiner extends AbstractOperationMachine implements RecipeItem, MenuUpdater {
    // time = baseTime / logN(supplies * mul + 1)
    private final double baseTime = ConfigUtil.getOrDefaultItemSetting(12, this, "time");
    private final double logN = ConfigUtil.getOrDefaultItemSetting(8, this, "logN");
    private final double mul = ConfigUtil.getOrDefaultItemSetting(0.15, this, "mul");
    private final double add = ConfigUtil.getOrDefaultItemSetting(1.2, this, "add");
    private final double random = ConfigUtil.getOrDefaultItemSetting(0.08, this, "random");

    public EtherMiner(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent blockBreakEvent, @Nonnull ItemStack itemStack, @Nonnull List<ItemStack> drops) {
                Location location = blockBreakEvent.getBlock().getLocation();
                BlockMenu blockMenu = BlockStorage.getInventory(location);
                blockMenu.dropItems(location, EtherMiner.this.getInputSlot());
                blockMenu.dropItems(location, EtherMiner.this.getOutputSlot());

                EtherMiner.this.getMachineProcessor().endOperation(location);
            }
        };
    }

    @Nullable
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new EtherMinerMenu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        Location location = block.getLocation();
        BlockMenu blockMenu = BlockStorage.getInventory(location);

        EtherMinerOperation etherMinerOperation = (EtherMinerOperation) this.getMachineProcessor().getOperation(location);
        if (etherMinerOperation != null) {
            etherMinerOperation.addProgress(1);
            if (etherMinerOperation.isFinished()) {
                ItemStack outputItemStack = FinalTechItems.ETHER.getValidItem();
                if (MachineUtil.calMaxMatch(blockMenu.toInventory(), this.getOutputSlot(), outputItemStack) > 0) {
                    this.getMachineProcessor().endOperation(location);
                    blockMenu.pushItem(outputItemStack, this.getOutputSlot());
                    etherMinerOperation = null;
                }
            }
        } else {
            etherMinerOperation = new EtherMinerOperation();
            boolean startOperation = this.getMachineProcessor().startOperation(location, etherMinerOperation);
            if (startOperation) {
            } else {
                etherMinerOperation = null;
            }
        }

        if (blockMenu.hasViewer()) {
            int progress = 0;
            int totalTicks = 0;
            if (etherMinerOperation != null) {
                progress = etherMinerOperation.getProgress();
                totalTicks = etherMinerOperation.getTotalTicks();
            }
            this.updateMenu(blockMenu, EtherMinerMenu.STATUS_SLOT, this,
                    String.valueOf(progress),
                    String.valueOf(totalTicks));
        }
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipeWithBorder(FinalTechChanged.getLanguageManager(), this,
                String.valueOf(this.baseTime));

        // this.registerRecipe(FinalTechItemStacks.UNORDERED_DUST, FinalTechItemStacks.ETHER);
    }
}
