package fr.loxoz.mods.betterwaystonesmenu;

import fr.loxoz.mods.betterwaystonesmenu.config.BWMConfig;
import fr.loxoz.mods.betterwaystonesmenu.handler.ScreenOpenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.forgespi.language.IModInfo;

import java.util.Optional;

@Mod(BetterWaystonesMenu.MOD_ID)
public class BetterWaystonesMenu {
    public static final String MOD_ID = "betterwaystonesmenu";
    private static BetterWaystonesMenu instance = null;
    private ScreenOpenHandler screenOpenHandler = null;
    private final BWMConfig config;
    private final ModConfigSpec spec;           // ForgeConfigSpec → ModConfigSpec
    private final ModContainer modContainer;

    public static BetterWaystonesMenu inst() { return instance; }

    // NeoForge injeta o IEventBus direto no construtor
    public BetterWaystonesMenu(IEventBus modEventBus) {
        instance = this;

        if (FMLEnvironment.dist == Dist.CLIENT) {
            screenOpenHandler = new ScreenOpenHandler();
            NeoForge.EVENT_BUS.register(screenOpenHandler); // MinecraftForge → NeoForge
            var builder = new ModConfigSpec.Builder();       // ForgeConfigSpec.Builder → ModConfigSpec.Builder
            config = new BWMConfig(builder);
            spec = builder.build();
            modContainer.registerConfig(ModConfig.Type.CLIENT, spec);
        } else {
            config = null;
            spec = null;
        }

        modContainer = ModList.get().getModContainerById(MOD_ID).orElse(null);
    }

    public void openOriginalScreen(Screen screen) {
        screenOpenHandler.ignoreNextMenu = true;
        Minecraft.getInstance().setScreen(screen);
    }

    public BWMConfig config() { return config; }
    public ModConfigSpec configSpec() { return spec; }
    public ModContainer getModContainer() { return modContainer; }
    public IModInfo getModInfo() { return modContainer != null ? modContainer.getModInfo() : null; }

    public Optional<Screen> getConfigScreen(Minecraft minecraft, Screen parent) {
        var info = getModInfo();
        if (info == null) return Optional.empty();
        // ConfigGuiHandler → ConfigScreenHandler
        return ConfigScreenHandler.getGuiFactoryFor(info).map(f -> f.apply(minecraft, parent));
    }

    public Optional<Screen> getConfigScreen(Minecraft minecraft) {
        return getConfigScreen(minecraft, minecraft.screen);
    }
}
