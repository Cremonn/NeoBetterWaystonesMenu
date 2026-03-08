package fr.loxoz.mods.betterwaystonesmenu.gui.screen;

import fr.loxoz.mods.betterwaystonesmenu.compat.CText;
import fr.loxoz.mods.betterwaystonesmenu.compat.widget.TexturedButtonTooltipWidget;
import fr.loxoz.mods.betterwaystonesmenu.config.BWMSortMode;
import fr.loxoz.mods.betterwaystonesmenu.gui.widget.BetterTextFieldWidget;
import fr.loxoz.mods.betterwaystonesmenu.gui.widget.BetterWaystoneButton;
import fr.loxoz.mods.betterwaystonesmenu.gui.widget.ScrollableContainerWidget;
import fr.loxoz.mods.betterwaystonesmenu.gui.widget.TexturedEnumButtonWidget;
import fr.loxoz.mods.betterwaystonesmenu.util.WaystoneUtils;
import fr.loxoz.mods.betterwaystonesmenu.util.query.IQueryMatcher;
import fr.loxoz.mods.betterwaystonesmenu.util.query.PartsQueryMatcher;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.Waystone;              // IWaystone → Waystone
import net.blay09.mods.waystones.api.WaystoneTypes;         // core → api
import net.blay09.mods.waystones.api.WaystoneVisibility;    // novo
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.blay09.mods.waystones.network.message.RequestEditWaystoneMessage;
import net.blay09.mods.waystones.network.message.SelectWaystoneMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.util.*;
import java.util.function.Predicate;

public abstract class BetterWaystoneSelectionScreenBase extends AbstractBetterWaystoneScreen {
    public static BWMSortMode sortMode = BWMSortMode.INDEX;
    protected final List<Waystone> waystones;
    protected Rect2i area_heading = new Rect2i(0, 0, 0, 0);
    protected Rect2i area_title = new Rect2i(0, 0, 0, 0);
    protected Rect2i area_query = new Rect2i(0, 0, 0, 0);
    protected ScrollableContainerWidget scrollable;
    protected BetterTextFieldWidget queryField;
    protected IQueryMatcher queryMatcher = new PartsQueryMatcher();
    private Screen originalScreen = null;
    private Component heading_title;
    private final List<Waystone> visibleWaystones = new ArrayList<>();

    public BetterWaystoneSelectionScreenBase(WaystoneSelectionMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.waystones = container.getWaystones();
        //noinspection SuspiciousNameCombination
        imageWidth = imageHeight = CONTENT_WIDTH;
    }

    public void setOriginalScreen(Screen originalScreen) { this.originalScreen = originalScreen; }
    public Screen getOriginalScreen() { return originalScreen; }

    protected @Nullable Vec3 getOriginPos() {
        if (menu.getWaystoneFrom() != null) {
            return Vec3.atBottomCenterOf(menu.getWaystoneFrom().getPos());
        }
        if (minecraft != null && minecraft.player != null) {
            return minecraft.player.position();
        }
        return null;
    }

    protected @Nullable ResourceKey<Level> getOriginDim() {
        if (menu.getWaystoneFrom() != null) {
            return menu.getWaystoneFrom().getDimension();
        }
        if (minecraft != null && minecraft.player != null) {
            return minecraft.player.clientLevel.dimension();
        }
        return null;
    }

    protected void updateFilters() {
        visibleWaystones.clear();
        List<Waystone> list;

        Map<Waystone, Float> resultScores = inst().config().weightedSearch.get() ? new HashMap<>() : null;
        if (queryMatcher.isBlank()) {
            list = waystones;
        } else {
            // getName() agora retorna Component — usar .getString()
            Predicate<Waystone> predicate = waystone -> queryMatcher.match(waystone.getName().getString());
            if (resultScores != null) {
                predicate = waystone -> resultScores.compute(waystone, ($, score) ->
                        score == null ? queryMatcher.matchScore(waystone.getName().getString()) : score) > 0;
            }
            // hasName() substitui !getName().isBlank()
            list = waystones.stream().filter(Waystone::hasName).filter(predicate).toList();
        }

        final Vec3 origin = Optional.ofNullable(getOriginPos()).orElse(new Vec3(0, 0, 0));
        Comparator<Waystone> sortComparator = getSortComparator(sortMode, origin);
        Comparator<Waystone> comparator = sortComparator;
        if (resultScores != null) {
            comparator = (w1, w2) -> {
                float score = (resultScores.getOrDefault(w2, 0f)) - (resultScores.getOrDefault(w1, 0f));
                if (score != 0) return score > 0 ? 1 : -1;
                return sortComparator != null ? sortComparator.compare(w1, w2) : 0;
            };
        }
        if (comparator != null) {
            list = list.stream().sorted(comparator).toList();
        }

        visibleWaystones.addAll(list);
    }

    protected int getSpecialCharWeight(String str) {
        if (str.isBlank()) return 0;
        char c = str.trim().charAt(0);
        return switch (c) {
            case '-' -> 10;
            case '=' -> 5;
            case '~' -> 2;
            default -> 0;
        };
    }

    public @Nullable Comparator<Waystone> getSortComparator(BWMSortMode mode, Vec3 origin) {
        return switch (mode) {
            case NAME -> (w1, w2) -> {
                // hasName() substitui !getName().isBlank()
                int unnamedDiff = (w2.hasName() ? 0 : -1) - (w1.hasName() ? 0 : -1);
                if (unnamedDiff != 0) return unnamedDiff;
                if (inst().config().specialCharsFirst.get()) {
                    int specialCharWeight = getSpecialCharWeight(w2.getName().getString()) - getSpecialCharWeight(w1.getName().getString());
                    if (specialCharWeight != 0) return specialCharWeight;
                }
                return Collator.getInstance().compare(w1.getName().getString(), w2.getName().getString());
            };
            case DISTANCE -> (w1, w2) -> (int) (
                    origin.distanceToSqr(Vec3.atBottomCenterOf(w1.getPos())) -
                    origin.distanceToSqr(Vec3.atBottomCenterOf(w2.getPos()))
            );
            default -> null;
        };
    }

    public void setSortMode(BWMSortMode mode) {
        sortMode = mode;
        inst().config().sortMode.set(sortMode);
        updateFilters();
        updateList();
    }

    protected boolean isIconHeading() {
        return switch (menu.getWarpMode()) {
            case WARP_STONE, WARP_SCROLL -> true;
            default -> false;
        };
    }

    protected boolean shouldShownHeading() {
        return menu.getWaystoneFrom() != null || isIconHeading();
    }

    @Override
    protected void init() {
        int cw = CONTENT_WIDTH;
        int cx = (width - cw) / 2;
        heading_title = menu.getWaystoneFrom() == null ? null : WaystoneUtils.getTrimmedWaystoneName(menu.getWaystoneFrom(), font, getMaxNameWidth());
        int title_w = heading_title != null ? (font.width(heading_title) + 36) : 0;
        int sbw = 20 + UI_GAP;
        int cbw = cw + sbw * 2;
        int cbx = (width - cbw) / 2;
        imageWidth = Math.max(cbw, title_w);
        imageHeight = (int) (height * menuHeightScale) + (UI_GAP * 2);
        super.init();
        int hw = Math.max(title_w, cw);
        int hx = (width - hw) / 2;
        int ry = topPos;
        int rh = imageHeight;
        int rb = ry + rh;

        area_heading = new Rect2i(hx, ry, hw, 18);
        area_title = new Rect2i(cx, area_heading.getY() + area_heading.getHeight() + UI_GAP, cw, font.lineHeight);
        area_query = new Rect2i(cx, area_title.getY() + area_title.getHeight() + UI_GAP, cw, 20);
        int aq_bpos = area_query.getY() + area_query.getHeight() + UI_GAP;

        sortMode = inst().config().sortMode.get();

        if (menu.getWaystoneFrom() != null) {
            addRenderableWidget(new TexturedButtonTooltipWidget(
                    area_heading.getX() + area_heading.getWidth() - 18, area_heading.getY(), 18, 18,
                    0, 40, 18, MENU_TEXTURE, 256, 256,
                    $ -> Balm.getNetworking().sendToServer(new RequestEditWaystoneMessage(menu.getWaystoneFrom().getWaystoneUid())),
                    CText.translatable("gui.betterwaystonesmenu.waystone_selection.rename")));
        }

        int sby = area_query.getY();

        if (allowSorting() || allowDeletion()) {
            addRenderableWidget(new TexturedButtonTooltipWidget(cbx, sby, 20, 20, 20, 0, 20, MENU_TEXTURE, 256, 256, $ -> {
                Objects.requireNonNull(minecraft);
                minecraft.setScreen(new BetterWaystoneRearrangeScreen(menu, minecraft.player.getInventory(), this, new BetterWaystoneRearrangeScreen.Allowed(allowSorting(), allowDeletion())));
            }, CText.translatable("gui.betterwaystonesmenu.waystone_selection.rearrange")));
            sby += 20 + UI_GAP;
        }

        addRenderableWidget(new ConfigButtonWidget(cbx, sby, CText.translatable("gui.betterwaystonesmenu.waystone_selection.open_config"), inst().getConfigScreen(minecraft, this).orElse(null)));
        sby += 20 + UI_GAP;

        addRenderableWidget(new TexturedButtonTooltipWidget(cbx, sby, 20, 20, 0, 0, 20, MENU_TEXTURE, 256, 256, $ -> {
            if (originalScreen == null) return;
            inst().openOriginalScreen(originalScreen);
        }, CText.translatable("gui.betterwaystonesmenu.waystone_selection.return_to_original")));

        if (queryField == null) {
            queryField = new BetterTextFieldWidget(font, 0, 0, 100, area_query.getHeight(), CText.translatable("gui.betterwaystonesmenu.waystone_selection.query_waystones"));
            queryField.setMaxLength(128);
        }
        queryField.setPosition(area_query.getX(), area_query.getY());
        queryField.setWidth(area_query.getWidth() - 20 - UI_GAP);
        addRenderableWidget(queryField);
        if (inst().config().focusSearch.get()) {
            setInitialFocus(queryField);
        }

        TexturedEnumButtonWidget<BWMSortMode> sortModeBtn = new TexturedEnumButtonWidget<>(
                area_query.getX() + area_query.getWidth() - 20, area_query.getY(), 20, 20,
                BWMSortMode.values(), sortMode,
                mode -> CText.translatable("gui.betterwaystonesmenu.waystone_selection.sort_mode_prefix",
                        CText.translatable("gui.betterwaystonesmenu.waystone_selection.sort_modes." + mode.getId())),
                MENU_TEXTURE, 0, 92, 256, 256);
        sortModeBtn.onChange(this::setSortMode);
        addRenderableWidget(sortModeBtn);

        if (scrollable == null) {
            scrollable = new ScrollableContainerWidget(0, 0, cw, 0);
            if (inst().config().reducedMotion.get()) {
                scrollable.setAnimated(false);
            }
        }
        scrollable.setPosition(cx, aq_bpos);
        scrollable.setHeight(rb - aq_bpos);
        addRenderableWidget(scrollable);

        updateFilters();
        updateList();
    }

    public void updateList() {
        scrollable.contents().clear();
        int y = 0;
        int content_h = 0;
        for (Waystone waystone : visibleWaystones) {
            var btn = createWaystoneButton(y, waystone);
            scrollable.contents().add(btn);
            y += 20 + BTN_GAP;
            int ch = btn.getY() + btn.getHeight();
            if (ch > content_h) content_h = ch;
        }
        scrollable.setContentHeight(content_h);
    }

    private BetterWaystoneButton createWaystoneButton(int y, Waystone waystone) {
        Waystone waystoneFrom = menu.getWaystoneFrom();
        Player player = Minecraft.getInstance().player;
        int xpLevelCost = Math.round((float) PlayerWaystoneManager.predictExperienceLevelCost(
                Objects.requireNonNull(player), waystone, menu.getWarpMode(), waystoneFrom));
        BetterWaystoneButton btnWaystone = new BetterWaystoneButton(0, y, waystone, xpLevelCost,
                $ -> onWaystoneSelected(waystone), getOriginPos(), getOriginDim());
        btnWaystone.setWidth(scrollable.getInnerWidth() - BTN_GAP);
        if (waystoneFrom != null && waystone.getWaystoneUid().equals(waystoneFrom.getWaystoneUid())) {
            btnWaystone.active = false;
        }
        return btnWaystone;
    }

    protected void onWaystoneSelected(Waystone waystone) {
        Balm.getNetworking().sendToServer(new SelectWaystoneMessage(waystone.getWaystoneUid()));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrollable.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        queryField.tick();
        if (!queryMatcher.getQuery().equals(queryField.getValue())) {
            queryMatcher.setQuery(queryField.getValue());
            updateFilters();
            updateList();
        }
        boolean reducedMotion = inst().config().reducedMotion.get();
        if (reducedMotion == scrollable.isAnimated()) {
            scrollable.setAnimated(!reducedMotion);
        }
    }

    public int getMaxNameWidth() {
        return Math.min(WAYSTONE_NAME_MAX_WIDTH, width - 36);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        if (shouldShownHeading()) {
            Waystone fromWaystone = menu.getWaystoneFrom();
            boolean iconHeading = fromWaystone == null;

            int hx1 = area_heading.getX() + (!iconHeading ? 0 : (area_heading.getWidth() - 16) / 2 - 1);
            int hx2 = !iconHeading ? (area_heading.getX() + area_heading.getWidth()) : (hx1 + 18);

            guiGraphics.fill(hx1, area_heading.getY(), hx2, area_heading.getY() + area_heading.getHeight(), 0x66000000);

            if (!iconHeading) {
                guiGraphics.drawCenteredString(font, heading_title,
                        area_heading.getX() + area_heading.getWidth() / 2,
                        area_heading.getY() + area_heading.getHeight() / 2 - font.lineHeight / 2,
                        0xffffff);
                int u = 0;
                // WaystoneTypes agora está em api, não em core
                if (fromWaystone.getWaystoneType().equals(WaystoneTypes.WAYSTONE)) u = 16;
                guiGraphics.blit(MENU_TEXTURE, area_heading.getX() + 1, area_heading.getY() + 1, u, 76, 16, 16);
            } else {
                ResourceLocation item_key = switch (menu.getWarpMode()) {
                    case WARP_STONE -> ResourceLocation.fromNamespaceAndPath("waystones", "warp_stone");
                    case WARP_SCROLL -> ResourceLocation.fromNamespaceAndPath("waystones", "warp_scroll");
                    default -> null;
                };

                Item item = null;
                if (item_key != null) {
                    item = ForgeRegistries.ITEMS.getValue(item_key);
                }
                if (item != null) {
                    guiGraphics.renderItem(new ItemStack(item),
                            area_heading.getX() + area_heading.getWidth() / 2 - 8,
                            area_heading.getY() + area_heading.getHeight() / 2 - 8);
                }
            }
        }

        drawVersionInfo(guiGraphics);

        guiGraphics.drawCenteredString(font,
                CText.translatable("gui.betterwaystonesmenu.waystone_selection.showing", visibleWaystones.size(), waystones.size()),
                width / 2, scrollable.getY() + scrollable.getHeight() + UI_GAP, 0xff737373);

        if (visibleWaystones.isEmpty()) {
            var message = queryMatcher.isBlank() ?
                    CText.translatable("gui.waystones.waystone_selection.no_waystones_activated").withStyle(style -> style.withColor(ChatFormatting.RED)) :
                    CText.translatable("gui.betterwaystonesmenu.waystone_selection.no_results").withStyle(style -> style.withColor(ChatFormatting.GRAY));
            guiGraphics.drawCenteredString(font, message,
                    scrollable.getX() + scrollable.getWidth() / 2,
                    (scrollable.getY() + scrollable.getHeight() / 2) - (font.lineHeight / 2),
                    0xffffff);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderChildrenTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(-leftPos, -topPos, 0);
        guiGraphics.drawCenteredString(font,
                getTitle().copy().withStyle(style -> style.withColor(ChatFormatting.GRAY)),
                area_title.getX() + area_title.getWidth() / 2,
                area_title.getY(),
                0xffffff);
        guiGraphics.pose().popPose();
    }

    protected boolean allowSorting() { return true; }
    protected boolean allowDeletion() { return true; }

    protected class ConfigButtonWidget extends TexturedButtonTooltipWidget {
        public ConfigButtonWidget(int x, int y, Component message, Screen configScreen) {
            super(x, y, 20, 20, 60, 0, 20, MENU_TEXTURE, 256, 256, $ -> {
                if (configScreen == null) return;
                //noinspection ConstantConditions
                minecraft.setScreen(configScreen);
            }, message);
            if (configScreen == null) active = false;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
            if (!active) guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xcc141414);
        }

        @Override
        public List<Component> getTooltip() {
            var list = new ArrayList<>(super.getTooltip());
            if (!active) list.add(CText.translatable("gui.betterwaystonesmenu.waystone_selection.config_requires_configured"));
            return Collections.unmodifiableList(list);
        }
    }
}
