package fr.loxoz.mods.betterwaystonesmenu.compat;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.literal;
import net.minecraft.network.chat.Component.translatable;

/**
 * I usually make this utility file for large projects that could support multiple versions of Minecraft to make
 * maintenance easier across multiple versions of Minecraft and also because I like the Minecraft 1.19+ unique Text
 * class for all text components.
 */
public class CText {
    public static MutableComponent literal(String string) {
        return new Component.literal(string);
    }

    public static MutableComponent translatable(String key) {
        return new Component.translatable(key);
    }

    public static MutableComponent translatable(String key, Object ...args) {
        return new Component.translatable(key, args);
    }

    public static MutableComponent empty() {
        return new Component.literal("");
    }
}
