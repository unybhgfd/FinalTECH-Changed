package io.taraxacum.finaltech.util;

public class ConstantTableUtil {
    public static final String CONFIG_ID = "id";

    public static final String CONFIG_CHARGE = "energy-charge";

    public static final String CONFIG_SLEEP = "sleep";

    public static final String CONFIG_UUID = "owner";

    public static final int ITEM_COPY_CARD_AMOUNT = ConfigUtil.getOrDefaultItemSetting(16777216, "COPY_CARD", "amount");

    public static final int ITEM_SPIROCHETE_AMOUNT = ConfigUtil.getOrDefaultItemSetting(128, "SPIROCHETE", "amount");

    public static final int ITEM_SINGULARITY_AMOUNT = ConfigUtil.getOrDefaultItemSetting(512+ITEM_SPIROCHETE_AMOUNT, "SINGULARITY", "amount");

    public static final int ITEM_MAX_STACK = 64;

    public static final double WARNING_TPS = 10.5;
}
