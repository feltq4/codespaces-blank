package com.extranightvision.gui;

import com.extranightvision.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Main menu: ON/OFF, width, height, entity lists. Big buttons only, so it is easy to tap on a phone. */
public class MainScreen extends Screen {
    private Button toggleButton;
    private Button widthLabel;
    private Button heightLabel;

    public MainScreen() {
        super(Component.literal("Extra Night Vision"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int rowH = 26;
        int rows = 6;
        int y = Math.max(6, (this.height - rows * rowH) / 2);

        Button header = Button.builder(Component.literal("Extra Night Vision"), b -> {})
                .bounds(cx - 122, y, 244, 22).build();
        header.active = false;
        this.addRenderableWidget(header);
        y += rowH;

        toggleButton = Button.builder(toggleText(), b -> {
            Config.enabled = !Config.enabled;
            Config.bump();
            b.setMessage(toggleText());
        }).bounds(cx - 122, y, 244, 22).build();
        this.addRenderableWidget(toggleButton);
        y += rowH;

        widthLabel = addSizeRow(y, true);
        y += rowH;
        heightLabel = addSizeRow(y, false);
        y += rowH;

        this.addRenderableWidget(Button.builder(Component.literal("Entity lists..."), b ->
                Minecraft.getInstance().gui.setScreen(new EntityListScreen(this)))
                .bounds(cx - 122, y, 244, 22).build());
        y += rowH;

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(cx - 122, y, 244, 22).build());
    }

    private Component toggleText() {
        return Component.literal("Enlarged hitboxes: " + (Config.enabled ? "ON" : "OFF"));
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.2fx", v);
    }

    private Button addSizeRow(int y, boolean isWidth) {
        int x = this.width / 2 - 122;
        String name = isWidth ? "Width" : "Height";
        addStep(x, y, 36, "-1", isWidth, -1.0);
        addStep(x + 38, y, 36, "-.1", isWidth, -0.1);
        Button label = Button.builder(Component.literal(name + ": " + fmt(isWidth ? Config.width : Config.height)), b -> {})
                .bounds(x + 76, y, 92, 22).build();
        label.active = false;
        this.addRenderableWidget(label);
        addStep(x + 170, y, 36, "+.1", isWidth, 0.1);
        addStep(x + 208, y, 36, "+1", isWidth, 1.0);
        return label;
    }

    private void addStep(int x, int y, int w, String text, boolean isWidth, double delta) {
        this.addRenderableWidget(Button.builder(Component.literal(text), b -> {
            if (isWidth) {
                Config.width = Config.clamp(Config.width + delta);
            } else {
                Config.height = Config.clamp(Config.height + delta);
            }
            Config.bump();
            widthLabel.setMessage(Component.literal("Width: " + fmt(Config.width)));
            heightLabel.setMessage(Component.literal("Height: " + fmt(Config.height)));
        }).bounds(x, y, w, 22).build());
    }

    /** Called whenever this screen is closed or replaced: save settings to disk. */
    @Override
    public void removed() {
        Config.save();
        super.removed();
    }
}
