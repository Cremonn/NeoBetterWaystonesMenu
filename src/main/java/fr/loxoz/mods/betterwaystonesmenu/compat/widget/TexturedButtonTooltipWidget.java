package fr.loxoz.mods.betterwaystonesmenu.compat.widget;

import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.IPositionedTooltipProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TexturedButtonTooltipWidget extends Button implements IPositionedTooltipProvider {
    private final ResourceLocation texture;
    private final int u, v, hoverVOffset, textureWidth, textureHeight;

    public TexturedButtonTooltipWidget(int x, int y, int width, int height, int u, int v, int hoverVOffset, ResourceLocation texture, int textureWidth, int textureHeight, OnPress onClick, Component message) {
        super(x, y, width, height, message, onClick, Button.DEFAULT_NARRATION);
        this.u = u;
        this.v = v;
        this.hoverVOffset = hoverVOffset;
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int vOffset = isHoveredOrFocused() ? v + hoverVOffset : v;
        guiGraphics.blit(texture, getX(), getY(), u, vOffset, width, height, textureWidth, textureHeight);
    }

    @Override
    public boolean shouldShowTooltip() { return isHoveredOrFocused(); }

    @Override
    public List<Component> getTooltipComponents() { return List.of(getMessage()); }
}
