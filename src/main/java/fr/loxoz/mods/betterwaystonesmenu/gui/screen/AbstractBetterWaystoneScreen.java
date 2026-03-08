package fr.loxoz.mods.betterwaystonesmenu.gui.screen;

import fr.loxoz.mods.betterwaystonesmenu.BetterWaystonesMenu;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.ITooltipProviderParent;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.PositionedTooltip;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.TooltipPos;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBetterWaystoneScreen extends AbstractContainerScreen<WaystoneSelectionMenu> implements ITooltipProviderParent {
    public static int WAYSTONE_NAME_MAX_WIDTH = 260;
    public static int CONTENT_WIDTH = 200;
    public static int BTN_GAP = 2;
    public static int UI_GAP = 8;
    // ResourceLocation.fromNamespaceAndPath no 1.21
    public static final ResourceLocation MENU_TEXTURE = ResourceLocation.fromNamespaceAndPath(BetterWaystonesMenu.MOD_ID, "textures/gui/menu.png");
    public static float menuHeightScale = 0.66f;

    public AbstractBetterWaystoneScreen(WaystoneSelectionMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        menuHeightScale = BetterWaystonesMenu.inst().config().menuHeightScale.get().floatValue();
    }

    // PoseStack → GuiGraphics em todos os métodos de render
    protected void renderChildrenTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (var provider : getTooltips()) {
            renderPositionedTooltip(provider, guiGraphics, mouseX, mouseY);
        }
    }

    protected BetterWaystonesMenu inst() { return BetterWaystonesMenu.inst(); }

    protected void drawVersionInfo(GuiGraphics guiGraphics) {
        var info = inst().getModInfo();
        if (info != null) {
            // drawString → guiGraphics.drawString no 1.21
            guiGraphics.drawString(font, String.format("%s v%s", info.getDisplayName(), info.getVersion()), 32, height - font.lineHeight - UI_GAP, 0x33ffffff);
        }
    }

    protected void renderPositionedTooltip(PositionedTooltip tooltip, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TooltipPos pos = tooltip.getTooltipPos(mouseX, mouseY);
        // renderTooltip no 1.21 recebe GuiGraphics + lista de ClientTooltipComponent + x + y
        guiGraphics.renderComponentTooltip(font, tooltip.getTooltip(), pos.x(), pos.y());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        float mhs = BetterWaystonesMenu.inst().config().menuHeightScale.get().floatValue();
        if (mhs != menuHeightScale) {
            menuHeightScale = mhs;
            //noinspection ConstantConditions
            init(minecraft, width, height);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (getFocused() != null && isDragging() && button == 0) {
            if (getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    // renderBg agora recebe GuiGraphics
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {}
}
