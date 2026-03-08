package fr.loxoz.mods.betterwaystonesmenu.gui.widget;

import fr.loxoz.mods.betterwaystonesmenu.compat.ScissorCompat;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.ITooltipProviderParent;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.PositionedTooltip;
import fr.loxoz.mods.betterwaystonesmenu.compat.tooltip.TooltipOffset;
import fr.loxoz.mods.betterwaystonesmenu.compat.widget.WidgetCompat;
import fr.loxoz.mods.betterwaystonesmenu.util.Easing;
import fr.loxoz.mods.betterwaystonesmenu.util.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics; // PoseStack → GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;  // Widget → Renderable
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScrollableContainerWidget extends AbstractContainerEventHandler implements WidgetCompat, Renderable, NarratableEntry, ITooltipProviderParent {
    private final List<GuiEventListener> children = new ArrayList<>();
    private int width;
    private int height;
    private int x;
    private int y;
    private boolean hovered;
    private boolean active = true;
    private boolean visible = true;
    public int scrollbarBg = 0xff111111;
    public boolean alwaysRenderBg = true;
    protected boolean scrollbarHovered = false;
    private boolean scrollbarYDragged;
    public double scrollDeltaY = 32;
    public int animationDuration = 800;
    public Easing animationEasing = Easings.EASE_OUT_QUAD;
    private double scrollY;
    private double targetScrollY;
    private double contentHeight = 0;
    private boolean animated;
    private long scrollYStart = 0;

    public ScrollableContainerWidget(int x, int y, int width, int height) {
        this(x, y, width, height, true);
    }

    public ScrollableContainerWidget(int x, int y, int width, int height, boolean animated) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.animated = animated;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) {
        boolean bottom = getTargetScrollY() >= getMaxScrollY();
        this.height = height;
        scrollTo(bottom ? getMaxScrollY() : getTargetScrollY());
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public boolean isAnimated() { return animated; }
    public void setAnimated(boolean animated) { this.animated = animated; }

    public void scrollTo(double targetY) { scrollTo(targetY, isAnimated()); }
    public void scrollTo(double targetY, boolean animated) {
        double clampedTargetY = Mth.clamp(targetY, 0.0, getMaxScrollY());
        if (clampedTargetY == targetScrollY) return;
        update();
        targetScrollY = clampedTargetY;
        if (!animated || !isAnimated()) {
            scrollY = targetScrollY;
            return;
        }
        if (scrollY != targetScrollY) {
            scrollYStart = System.currentTimeMillis();
        }
    }

    public void scrollBy(double amount) { scrollBy(amount, true); }
    public void scrollBy(double amount, boolean animated) { scrollTo(targetScrollY - amount, animated); }

    public void setContentHeight(double height) {
        contentHeight = height;
        scrollTo(getTargetScrollY());
    }
    public double getContentHeight() { return contentHeight; }

    public double getScrollY() { return scrollY; }
    public void setScrollY(double scrollY) { scrollTo(scrollY, false); }
    public double getTargetScrollY() { return targetScrollY; }

    public double getMaxScrollY() { return Math.max(0, contentHeight - getHeight()); }

    public boolean isHovered() { return hovered; }
    public boolean isFocused() { return false; }

    @Override
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean canInteract() { return isActive() && isVisible(); }

    public int getScrollbarWidth() { return 8; }

    public boolean isScrollbarAt(double mouseX, double mouseY) {
        return mouseX >= (getX() + getWidth() - getScrollbarWidth()) && mouseX <= (getX() + getWidth()) && mouseY >= getY() && mouseY < (getY() + getHeight());
    }

    public int getInnerWidth() { return getWidth() - getScrollbarWidth(); }
    public int getInnerHeight() { return getHeight(); }

    public void update() {
        if (!isAnimated() || (scrollY == targetScrollY)) return;
        long currentTime = System.currentTimeMillis();
        double deltaTime = currentTime - scrollYStart;
        double t = Math.min(deltaTime / animationDuration, 1);
        if (t >= 1) {
            scrollY = targetScrollY;
            scrollYStart = currentTime;
        } else {
            scrollY = ease(scrollY, targetScrollY, t);
        }
    }

    // render helpers atualizados para GuiGraphics
    public void applyTranslate(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.pose().translate(getX(), getY() - getScrollY(), 0);
    }

    public void enableContentScissor() {
        ScissorCompat.enableScissor(Minecraft.getInstance(), getX(), getY(), getInnerWidth(), getInnerHeight());
    }

    public void renderBackground(@NotNull GuiGraphics guiGraphics) {}

    public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int childMouseX = mouseX - getX();
        int childMouseY = (int) (mouseY - getY() + getScrollY());
        guiGraphics.pose().pushPose();
        enableContentScissor();
        applyTranslate(guiGraphics);
        for (var child : children) {
            if (!(child instanceof Renderable drawable)) continue;
            int mx = childMouseX;
            int my = childMouseY;
            if (child instanceof AbstractWidget widget) {
                int ey = getY() + widget.getY();
                if (!isElementVisible(ey, ey + widget.getHeight())) continue;
                if (!hovered) { mx = -1; my = -1; }
            }
            drawable.render(guiGraphics, mx, my, partialTicks);
        }
        ScissorCompat.disableScissor();
        guiGraphics.pose().popPose();
    }

    public void drawScrollbarBg(@NotNull GuiGraphics guiGraphics) {
        if (!alwaysRenderBg && !isOverflowing()) return;
        guiGraphics.fill(getX() + getWidth() - getScrollbarWidth(), getY(), getX() + getWidth(), getY() + getHeight(), scrollbarBg);
    }

    public void drawScrollbarThumb(@NotNull GuiGraphics guiGraphics) {
        if (!isOverflowing()) return;
        int thumbHeight = getScrollbarThumbHeight();
        int x1 = getX() + getWidth() - getScrollbarWidth();
        int x2 = getX() + getWidth();
        int y1 = Math.max(getY(), (int) scrollY * (getHeight() - thumbHeight) / (int) getMaxScrollY() + getY());
        if ((y1 - getY()) > getMaxScrollY()) return;
        int y2 = Math.min(y1 + thumbHeight, getY() + getHeight());
        boolean hoveredOrDragged = scrollbarHovered || scrollbarYDragged;
        guiGraphics.fill(x1, y1, x2, y2, hoveredOrDragged ? 0xffa0a0a0 : 0xff808080);
        guiGraphics.fill(x1, y1, x2 - 1, y2 - 1, hoveredOrDragged ? 0xffe0e0e0 : 0xffC0C0C0);
    }

    public void renderScrollbar(@NotNull GuiGraphics guiGraphics) {
        drawScrollbarBg(guiGraphics);
        drawScrollbarThumb(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            hovered = false;
            return;
        }
        hovered = isMouseInside(mouseX, mouseY);
        scrollbarHovered = isScrollbarAt(mouseX, mouseY);
        update();
        renderBackground(guiGraphics);
        renderContent(guiGraphics, mouseX, mouseY, partialTicks);
        renderScrollbar(guiGraphics);
    }

    @Override
    public @NotNull Optional<GuiEventListener> getChildAt(double x, double y) {
        double childMouseX = x - getX();
        double childMouseY = y - getY() + getScrollY();
        return super.getChildAt(childMouseX, childMouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!canInteract()) return false;
        if (isScrollbarAt(mouseX, mouseY)) {
            if (button == 0 && isOverflowing()) {
                scrollbarYDragged = true;
            }
            return true;
        }
        if (!isMouseInside(mouseX, mouseY)) return false;
        double childMouseX = mouseX - getX();
        double childMouseY = mouseY - getY() + getScrollY();
        return super.mouseClicked(childMouseX, childMouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && scrollbarYDragged) {
            scrollbarYDragged = false;
            return true;
        }
        if (!canInteract()) return false;
        double childMouseX = mouseX - getX();
        double childMouseY = mouseY - getY() + getScrollY();
        return super.mouseReleased(childMouseX, childMouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!canInteract()) {
            if (scrollbarYDragged) scrollbarYDragged = false;
            return false;
        }
        if (scrollbarYDragged) {
            if (mouseY < getY()) setScrollY(0);
            else if (mouseY > getY() + getHeight()) setScrollY(getMaxScrollY());
            else {
                double d = Math.max(1, getMaxScrollY() / (getHeight() - getScrollbarThumbHeight()));
                setScrollY(getScrollY() + deltaY * d);
            }
            return true;
        }
        double childMouseX = mouseX - getX();
        double childMouseY = mouseY - getY() + getScrollY();
        return super.mouseDragged(childMouseX, childMouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // 1.21: mouseScrolled agora recebe deltaX e deltaY separados
        if (!canInteract()) return false;
        if (!isMouseInside(mouseX, mouseY)) return false;
        if (!isScrollbarAt(mouseX, mouseY)) {
            double childMouseX = mouseX - getX();
            double childMouseY = mouseY - getY() + getScrollY();
            if (super.mouseScrolled(childMouseX, childMouseY, deltaX, deltaY)) return true;
        }
        scrollBy(deltaY * scrollDeltaY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == GLFW.GLFW_KEY_UP) { scrollBy(scrollDeltaY); return true; }
        if (keyCode == GLFW.GLFW_KEY_DOWN) { scrollBy(-scrollDeltaY); return true; }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) { scrollBy(scrollDeltaY * 6); return true; }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) { scrollBy(-scrollDeltaY * 6); return true; }
        return false;
    }
// changeFocus removido em 1.21.1 — tratado via keyPressed (TAB)
@Override
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
    if (keyCode == GLFW.GLFW_KEY_TAB) {
        var children = children();
        if (children.isEmpty()) return false;
        var focused = getFocused();
        int idx = focused != null ? children.indexOf(focused) : -1;
        int next = Screen.hasShiftDown() ? idx - 1 : idx + 1;
        if (next >= 0 && next < children.size()) {
            var nextWidget = children.get(next);
            setFocused(nextWidget);
            if (nextWidget instanceof AbstractWidget w) scrollElementIntoView(w);
        }
        return true;
    }
    if (keyCode == GLFW.GLFW_KEY_UP) { scrollBy(scrollDeltaY); return true; }
    if (keyCode == GLFW.GLFW_KEY_DOWN) { scrollBy(-scrollDeltaY); return true; }
    if (keyCode == GLFW.GLFW_KEY_PAGE_UP) { scrollBy(scrollDeltaY * 6); return true; }
    if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) { scrollBy(-scrollDeltaY * 6); return true; }
    return false;
}

    public void scrollElementIntoView(AbstractWidget widget) {
        scrollElementIntoView(widget.getY(), widget.getHeight()); // widget.y → widget.getY()
    }
    public void scrollElementIntoView(int elementY, int elementHeight) {
        if (isElementFullyVisible(getY() + elementY, getY() + elementY + elementHeight)) return;
        scrollTo(elementY - (getHeight() - elementHeight) / 2f);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isActive() && isVisible() && isMouseInside(mouseX, mouseY);
    }

    public boolean isMouseInside(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseY >= getY() && mouseX < (getX() + getWidth()) && mouseY < (getY() + getHeight());
    }

    public boolean isOverflowing() { return contentHeight > getHeight(); }

    public boolean isElementVisible(int elementTop, int elementBottom) {
        return elementBottom - getScrollY() >= getY() && elementTop - getScrollY() <= (getY() + getHeight());
    }

    public boolean isElementFullyVisible(int elementTop, int elementBottom) {
        return elementBottom - getScrollY() <= (getY() + getHeight()) && elementTop - getScrollY() >= getY();
    }

    public @NotNull List<GuiEventListener> contents() { return children; }

    @Override
    public @NotNull List<? extends GuiEventListener> children() { return children; }

    public void replaceChildren(List<? extends GuiEventListener> children) {
        this.children.clear();
        this.children.addAll(children);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        if (isFocused()) return NarratableEntry.NarrationPriority.FOCUSED;
        return isHovered() ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput out) {}

    @Override
    public @NotNull TooltipOffset getWidgetsTooltipOffset() {
        return new TooltipOffset(getX(), (int) (getY() - getScrollY()));
    }

    @Override
    public List<PositionedTooltip> getTooltips(boolean visible) {
        return ITooltipProviderParent.super.getTooltips(visible).stream()
                .filter(tooltip -> {
                    if (!(tooltip.getTarget() instanceof AbstractWidget widget)) return true;
                    return isElementVisible(getY() + widget.getY(), getY() + widget.getY() + widget.getHeight());
                })
                .collect(Collectors.toList());
    }

    private int getScrollbarThumbHeight() {
        return Mth.clamp((int) ((float) (height * height) / contentHeight), 32, height);
    }

    public double ease(double start, double end, double t) {
        return start + (end - start) * animationEasing.getProgressSafe(t);
    }
}
