package fr.loxoz.mods.betterwaystonesmenu.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CText {
    public static MutableComponent literal(String string) {
        return Component.literal(string);
    }
    public static MutableComponent translatable(String key) {
        return Component.translatable(key);
    }
    public static MutableComponent translatable(String key, Object ...args) {
        return Component.translatable(key, args);
    }
    public static MutableComponent empty() {
        return Component.literal("");
    }
}
