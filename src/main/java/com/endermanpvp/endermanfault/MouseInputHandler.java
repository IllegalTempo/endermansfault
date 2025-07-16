package com.endermanpvp.endermanfault;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

public class MouseInputHandler {

    private final PlushRenderer plushRenderer;

    public MouseInputHandler(PlushRenderer plushRenderer) {
        this.plushRenderer = plushRenderer;
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        // This event fires for any mouse button state change.
        // We only care about left and right clicks.
        if (Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1) {
            if (Mouse.getEventButtonState()) { // Button was pressed
                plushRenderer.startSqueezing();
            } else { // Button was released
                plushRenderer.stopSqueezing();
            }
        }
    }
}
