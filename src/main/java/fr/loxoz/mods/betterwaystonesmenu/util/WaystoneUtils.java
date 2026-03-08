package fr.loxoz.mods.betterwaystonesmenu.util;

import fr.loxoz.mods.betterwaystonesmenu.compat.CText;
import net.blay09.mods.waystones.api.Waystone; // IWaystone → Waystone
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;

public class WaystoneUtils {
    public static MutableComponent getTrimmedWaystoneName(Waystone waystone, Font font, int maxWidth) {
        if (!waystone.hasName()) return CText.translatable("gui.waystones.waystone_selection.unnamed_waystone").withStyle(s -> s.withColor(ChatFormatting.GRAY));
        return Utils.trimTextWidth(waystone.getName().getString(), font, maxWidth); // getName() → getName().getString()
    }
}
