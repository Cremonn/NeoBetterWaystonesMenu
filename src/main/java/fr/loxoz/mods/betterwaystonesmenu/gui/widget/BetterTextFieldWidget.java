package fr.loxoz.mods.betterwaystonesmenu.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BetterTextFieldWidget extends EditBox {
    public BetterTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component message) {
        super(textRenderer, x, y, width, height, message);
    }

    @Override
    public boolean keyPressed(int p_94132_, int p_94133_, int p_94134_) {
        super.keyPressed(p_94132_, p_94133_, p_94134_);
        return canConsumeInput();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            setValue("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        if (!isVisible()) return;
        if (!getValue().isEmpty()) return;
        // drawShadow → drawString com shadow=true; x/y → getX()/getY()
        guiGraphics.drawString(font, getMessage(), getX() + 4, (int) (getY() + (height - 8f) / 2f), 0xff262626, false);
    }

    // Em 1.21.1 setX/setY/getX/getY já existem no parent — sem necessidade de wrappers
    public void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }
}
