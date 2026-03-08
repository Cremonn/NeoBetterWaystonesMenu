package fr.loxoz.mods.betterwaystonesmenu.compat.tooltip;

import net.blay09.mods.waystones.client.gui.widget.ITooltipProvider;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

public interface IPositionedTooltipProvider extends ITooltipProvider {

    @Override
    boolean shouldShowTooltip();

    @Override
    List<net.minecraft.network.chat.Component> getTooltipComponents();

    default TooltipPos getTooltipPos(int mouseX, int mouseY, TooltipOffset offset) {
        if (this instanceof AbstractWidget widget) {
            return new TooltipPos(widget.getX() + offset.x(), widget.getY() + offset.y());
        }
        return new TooltipPos(mouseX, mouseY);
    }
}
