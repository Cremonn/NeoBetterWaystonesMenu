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
