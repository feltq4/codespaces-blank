package com.extranightvision.gui;

import com.extranightvision.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Tap an entity to cycle:  default/off  ->  ENLARGE  ->  BLACKLIST  ->  default/off
 * ENLARGE list empty = every living entity is enlarged. BLACKLIST is always normal size.
 */
public class EntityListScreen extends Screen {
    private static List<String> allIds;

    private final Screen parent;
    private String query = "";
    private int page = 0;
    private int perPage = 4;
    private List<String> filtered = new ArrayList<>();
    private final List<Button> rowButtons = new ArrayList<>();
    private EditBox search;
    private Button pageLabel;
    private int rowsTop;
    private int rowsLeft;
    private int rowsWidth;

    public EntityListScreen(Screen parent) {
        super(Component.literal("Entity lists"));
        this.parent = parent;
    }

    private static List<String> allIds() {
        if (allIds == null) {
            List<String> l = new ArrayList<>();
            for (var id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                l.add(id.toString());
            }
            Collections.sort(l);
            allIds = l;
        }
        return allIds;
    }

    @Override
    protected void init() {
        rowButtons.clear();
        int cx = this.width / 2;
        rowsWidth = Math.min(280, this.width - 16);
        rowsLeft = cx - rowsWidth / 2;
        int rowH = 24;
        rowsTop = 52;
        int navY = this.height - 26;
        perPage = Math.max(2, (navY - rowsTop - 4) / rowH);

        Button header = Button.builder(Component.literal("Tap: ENLARGE > BLACKLIST > off"), b -> {})
                .bounds(rowsLeft, 4, rowsWidth, 20).build();
        header.active = false;
        this.addRenderableWidget(header);

        search = new EditBox(this.font, rowsLeft, 28, rowsWidth, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search entity..."));
        search.setValue(query);
        search.setResponder(s -> {
            query = s.toLowerCase(Locale.ROOT).trim();
            page = 0;
            refreshRows();
        });
        this.addRenderableWidget(search);

        int backW = rowsWidth * 25 / 100;
        int arrowW = rowsWidth * 20 / 100;
        int labelW = rowsWidth - backW - arrowW * 2 - 6;
        int x = rowsLeft;
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b ->
                Minecraft.getInstance().gui.setScreen(parent)).bounds(x, navY, backW, 20).build());
        x += backW + 2;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (page > 0) {
                page--;
                refreshRows();
            }
        }).bounds(x, navY, arrowW, 20).build());
        x += arrowW + 2;
        pageLabel = Button.builder(Component.literal("Page 1/1"), b -> {})
                .bounds(x, navY, labelW, 20).build();
        pageLabel.active = false;
        this.addRenderableWidget(pageLabel);
        x += labelW + 2;
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if ((page + 1) * perPage < filtered.size()) {
                page++;
                refreshRows();
            }
        }).bounds(x, navY, arrowW, 20).build());

        query = search.getValue().toLowerCase(Locale.ROOT).trim();
        refreshRows();
    }

    private void refreshRows() {
        for (Button b : rowButtons) {
            this.removeWidget(b);
        }
        rowButtons.clear();

        filtered = new ArrayList<>();
        for (String id : allIds()) {
            if (query.isEmpty() || id.contains(query)) {
                filtered.add(id);
            }
        }
        int pages = Math.max(1, (filtered.size() + perPage - 1) / perPage);
        if (page >= pages) {
            page = pages - 1;
        }
        if (pageLabel != null) {
            pageLabel.setMessage(Component.literal("Page " + (page + 1) + "/" + pages));
        }

        int start = page * perPage;
        int end = Math.min(filtered.size(), start + perPage);
        int y = rowsTop;
        for (int i = start; i < end; i++) {
            final String id = filtered.get(i);
            Button btn = Button.builder(rowText(id), b -> {
                cycle(id);
                b.setMessage(rowText(id));
            }).bounds(rowsLeft, y, rowsWidth, 22).build();
            rowButtons.add(btn);
            this.addRenderableWidget(btn);
            y += 24;
        }
    }

    private static Component rowText(String id) {
        String state;
        if (Config.blacklist.contains(id)) {
            state = "BLACKLIST";
        } else if (Config.include.contains(id)) {
            state = "ENLARGE";
        } else {
            state = Config.include.isEmpty() ? "default" : "off";
        }
        return Component.literal(id.replace("minecraft:", "") + "  [" + state + "]");
    }

    private static void cycle(String id) {
        if (Config.blacklist.contains(id)) {
            Config.blacklist.remove(id);
        } else if (Config.include.contains(id)) {
            Config.include.remove(id);
            Config.blacklist.add(id);
        } else {
            Config.include.add(id);
        }
        Config.bump();
    }

    @Override
    public void removed() {
        Config.save();
        super.removed();
    }
}
