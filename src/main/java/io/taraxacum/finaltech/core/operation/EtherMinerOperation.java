package io.taraxacum.finaltech.core.operation;

import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;

/**
 * @author Final_ROOT
 * @since 2.4
 */
public class EtherMinerOperation implements MachineOperation {
    public static final int TOTAL_TICKS = 2*60 * 5;
    private int tick;

    public EtherMinerOperation() {
    }

    @Override
    public int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    public void addProgress(int ticks) {
        this.tick += ticks;
    }

    @Override
    public int getProgress() {
        return this.tick;
    }
}
