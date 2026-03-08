package fr.loxoz.mods.betterwaystonesmenu.gui.screen;

import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * The upgraded version of {@link net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreen}
 */
public class BetterWaystoneSelectionScreen extends BetterWaystoneSelectionScreenBase {
    public BetterWaystoneSelectionScreen(WaystoneSelectionMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    // AbstractContainerScreen exige essa implementação no 1.21
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {}
}
