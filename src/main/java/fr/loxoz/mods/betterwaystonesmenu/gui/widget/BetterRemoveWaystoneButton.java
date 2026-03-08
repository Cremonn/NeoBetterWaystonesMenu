package fr.loxoz.mods.betterwaystonesmenu.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.loxoz.mods.betterwaystonesmenu.compat.CText;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.IPositionedTooltipProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BetterRemoveWaystoneButton extends Button implements IPositionedTooltipProvider {
    private static final ResourceLocation BEACON_GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/beacon.png");
    private static final ResourceLocation WIDGETS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/widgets.png");

    public final boolean global;
    private boolean clickable = false;

    public BetterRemoveWaystoneButton(int x, int y, int width, int height, boolean global, OnPress onPress) {
        super(x, y, width, height, CText.empty(), onPress, Button.DEFAULT_NARRATION);
        this.global = global;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        clickable = Screen.hasShiftDown() && isHoveredOrFocused();
        RenderSystem.enableDepthTest();

        // botão base: hovered=2, normal=1 (substitui getYImage)
        int iv = isHoveredOrFocused() ? 2 : 1;
        guiGraphics.blit(WIDGETS_TEXTURE, getX(), getY(), 0, 46 + iv * 20, width / 2, height, 256, 256);
        guiGraphics.blit(WIDGETS_TEXTURE, getX() + width / 2, getY(), 200 - width / 2, 46 + iv * 20, width / 2, height, 256, 256);

        int color_over = clickable ? 0x33dc2626 : 0x99262626;
        int pad = clickable ? 1 : 0;
        guiGraphics.fill(getX() + pad, getY() + pad, getX() + width - pad, getY() + height - pad, color_over);

        float color = clickable ? 1f : 0.5f;
        RenderSystem.setShaderColor(color, color, color, color);
        int icon_w = 13;
        guiGraphics.blit(BEACON_GUI_TEXTURE, getX() + (width - icon_w) / 2, getY() + (height - icon_w) / 2, 114, 223, icon_w, icon_w, 256, 256);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clickable) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        if (!clickable) return;
        super.onPress();
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        if (!clickable) return;
        super.playDownSound(soundManager);
    }

    @Override
    public @NotNull Component getMessage() {
        if (clickable) return CText.translatable("gui.waystones.waystone_selection.click_to_delete");
        return CText.translatable("gui.waystones.waystone_selection.hold_shift_to_delete");
    }

    @Override
    public boolean shouldShowTooltip() {
        return visible && isHoveredOrFocused();
    }

    @Override
    public List<Component> getTooltipComponents() {
        List<Component> tooltip = Lists.newArrayList(getMessage());
        if (global) tooltip.add(CText.translatable("gui.waystones.waystone_selection.deleting_global_for_all"));
        return tooltip;
    }
}
