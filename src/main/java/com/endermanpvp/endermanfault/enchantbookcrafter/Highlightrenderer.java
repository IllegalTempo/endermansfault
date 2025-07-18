package com.endermanpvp.endermanfault.enchantbookcrafter;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;

public class Highlightrenderer {
    /**
     * Call this from your GUI's drawScreen or equivalent method after slots are rendered.
     * @param gui The GuiContainer instance
     */
    public static void renderHighlightedSlots(GuiContainer gui) {

        // Enable blending for proper overlay effect
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Draw lit highlights

        for (int i = 0 ; i < gui.inventorySlots.inventorySlots.size(); i++) {
            Slot slot = gui.inventorySlots.getSlot(i);
            if (slot == null) continue;
            // Correct offset: add guiLeft and guiTop
            int x = (gui.width - 176) / 2 + slot.xDisplayPosition;
            int y = (gui.height - 222) / 2 + slot.yDisplayPosition;

            if(HighlightSameEnchantClickListener.highlightedSlots.contains(i)) {
                gui.drawRect(x, y, x + 16, y + 16, 0x6AFFFFFF); // Semi-transparent bright white
            } else {
                gui.drawRect(x, y, x + 16, y + 16, 0x6A000000); // Semi-transparent dark
            }
            // Render the slot index 'i' at the given x and y
        }

        // Restore default blending
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
