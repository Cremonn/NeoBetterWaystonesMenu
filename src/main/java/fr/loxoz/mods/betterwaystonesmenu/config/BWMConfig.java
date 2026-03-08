package fr.loxoz.mods.betterwaystonesmenu.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BWMConfig {
    public final ModConfigSpec.EnumValue<BWMSortMode> sortMode;
    public final ModConfigSpec.BooleanValue reducedMotion;
    public final ModConfigSpec.BooleanValue focusSearch;
    public final ModConfigSpec.DoubleValue menuHeightScale;
    // advanced
    public final ModConfigSpec.BooleanValue specialCharsFirst;
    public final ModConfigSpec.BooleanValue weightedSearch;
    // disabled
    public final ModConfigSpec.BooleanValue disabled;

    public BWMConfig(ModConfigSpec.Builder builder) {
        sortMode = builder
                .comment("Waystone List Sorting Mode")
                .defineEnum("sortMode", BWMSortMode.NAME);
        focusSearch = builder
                .comment("Focus Search bar when the menu opens")
                .define("focusSearch", true);
        reducedMotion = builder
                .comment("Disable scrollbar animation")
                .define("reducedMotion", false);
        menuHeightScale = builder
                .comment("Menu height scale in percentage")
                .defineInRange("menuHeightScale", 0.66d, 0.4d, 0.8d);
        builder.push("advanced");
        specialCharsFirst = builder
                .comment("Put Special Characters at first for Sort by Name mode")
                .define("specialCharsFirst", true);
        weightedSearch = builder
                .comment("Show most relevant search results first")
                .define("weightedSearch", true);
        builder.pop();
        disabled = builder
                .comment("Completely disable the menu")
                .define("disabled", false);
    }
}
