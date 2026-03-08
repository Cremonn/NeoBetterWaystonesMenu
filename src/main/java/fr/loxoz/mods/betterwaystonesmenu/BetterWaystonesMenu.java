package fr.loxoz.mods.betterwaystonesmenu;

import fr.loxoz.mods.betterwaystonesmenu.config.BWMConfig;
import fr.loxoz.mods.betterwaystonesmenu.handler.ScreenOpenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

@Mod(BetterWaystonesMenu.MOD_ID)
public class BetterWaystonesMenu {
    public static final String MOD_ID = "betterwaystonesmenu";
    private static BetterWaystonesMenu instance = null;
    private ScreenOpenHandler screenOpenHandler = null;
    private final BWMConfig config;
    private final ModConfigSpec spec;
    private final ModContainer modContainer;

    public static BetterWaystonesMenu inst() { return instance; }

    public BetterWaystonesMenu(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        this.modContainer = modContainer;

        if (FMLEnvironment.dist == Dist.CLIENT) {
            screenOpenHandler = new ScreenOpenHandler();
            NeoForge.EVENT_BUS.register(screenOpenHandler);
            var builder = new ModConfigSpec.Builder();
            config = new BWMConfig(builder);
            spec = builder.build();
            modContainer.registerConfig(ModConfig.Type.CLIENT, spec);
        } else {
            config = null;
            spec = null;
        }
    }

    public void openOriginalScreen(Screen screen) {
        screenOpenHandler.ignoreNextMenu = true;
        Minecraft.getInstance().setScreen(screen);
    }

    public BWMConfig config() { return config; }
    public ModConfigSpec configSpec() { return spec; }
    public ModContainer getModContainer() { return modContainer; }

    // Config screen desativado — ConfigScreenHandler foi removido em 1.21.1
    // Para reativar, registre um IConfigScreenFactory no seu mod
    public Optional<Screen> getConfigScreen(Minecraft minecraft, Screen parent) {
        return Optional.empty();
    }

    public Optional<Screen> getConfigScreen(Minecraft minecraft) {
        return getConfigScreen(minecraft, minecraft.screen);
    }
}
