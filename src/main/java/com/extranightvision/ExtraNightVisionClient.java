package com.extranightvision;

import com.extranightvision.gui.MainScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExtraNightVisionClient implements ClientModInitializer {
    public static final String MOD_ID = "extranightvision";

    private static KeyMapping openGuiKey;
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.extranightvision.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.extranightvision.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category));

        FriendCommands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                Config.enabled = !Config.enabled;
                Config.bump();
                Config.save();
            }
            while (openGuiKey.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new MainScreen());
                }
            }
        });
    }
}
