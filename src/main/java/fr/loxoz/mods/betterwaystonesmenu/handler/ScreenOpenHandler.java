package fr.loxoz.mods.betterwaystonesmenu.handler;

import fr.loxoz.mods.betterwaystonesmenu.BetterWaystonesMenu;
import fr.loxoz.mods.betterwaystonesmenu.gui.BWMScreenUpgrader;
import net.blay09.mods.waystones.client.gui.screen.SharestoneSelectionScreen;
import net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.ScreenEvent; // ScreenOpenEvent → ScreenEvent.Opening
import net.neoforged.bus.api.SubscribeEvent;             // eventbus movido para bus.api

import java.util.HashMap;
import java.util.Map;

public class ScreenOpenHandler {
    public boolean ignoreNextMenu = false;
    private final Map<Class<?>, ScreenSupplier> supplierMap = new HashMap<>() {{
        put(WaystoneSelectionScreen.class, (original, playerInventory) -> BWMScreenUpgrader.createFrom((WaystoneSelectionScreen) original, playerInventory));
        put(SharestoneSelectionScreen.class, (original, playerInventory) -> BWMScreenUpgrader.createFrom((SharestoneSelectionScreen) original, playerInventory));
    }};

    @SubscribeEvent
    public void onScreenOpen(ScreenEvent.Opening e) { // ScreenOpenEvent → ScreenEvent.Opening
        if (e.getScreen() == null) return;
        if (BetterWaystonesMenu.inst().config().disabled.get()) return;
        ScreenSupplier supplier = supplierMap.get(e.getScreen().getClass());
        if (supplier == null) return;
        if (ignoreNextMenu) {
            ignoreNextMenu = false;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        try {
            var screen = supplier.supply(e.getScreen(), client.player.getInventory());
            e.setScreen(screen); // setScreen ainda funciona igual
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface ScreenSupplier {
        Screen supply(Screen original, Inventory playerInventory);
    }
}
