package com.endermanpvp.endermanfault.equipment;

import com.endermanpvp.endermanfault.DataType.Toggle;
import com.endermanpvp.endermanfault.config.AllConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderEquipmentInventory {

    // Storage for equipment items from "Your Equipment and Stats" GUI
    private static ItemStack[] savedEquipmentItems = new ItemStack[4];
    private static boolean hasRecordedItems = false;

    // Storage for equipment slot positions for mouse interaction
    private static int[] equipmentSlotX = new int[4];
    private static int[] equipmentSlotY = new int[4];
    private static boolean slotsPositionsCalculated = false;

    // Track if we've already recorded equipment this session
    private static boolean recordedThisSession = false;
    private static String lastInventoryName = "";

    private final Toggle Toggle = AllConfig.INSTANCE.BooleanConfig.get("toggle_equipmentInInventory");

    // Load equipment data when the class is first used
    static {
        EquipmentFileManager.EquipmentData data = EquipmentFileManager.loadEquipmentFromFile();
        savedEquipmentItems = data.equipmentItems;
        hasRecordedItems = data.hasRecordedItems;
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if(!Toggle.data) return;
        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            // Check if we have enough slots to get inventory name safely
            if (container.inventorySlots.inventorySlots.size() > 0) {
                String inventoryName = container.inventorySlots.getSlot(0).inventory.getName();

                // Record equipment items when "Your Equipment and Stats" is opened - only on first open or GUI change
                if (inventoryName.equals("Your Equipment and Stats")) {
                    if (!inventoryName.equals(lastInventoryName) || !recordedThisSession) {
                        recordEquipmentItems(container);
                        recordedThisSession = true;
                        System.out.println("Recorded equipment on GUI open");
                    }
                }

                // Only render equipment overlay in player's regular inventory
                if (isPlayerInventoryOnly(container, inventoryName)) {
                    renderEquipmentOverlay(container);
                    // Render equipment tooltips on hover
                    renderEquipmentTooltips(container, event.mouseX, event.mouseY);
                }

                lastInventoryName = inventoryName;
            }
        }
    }

    @SubscribeEvent
    public void onGuiMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            if (container.inventorySlots.inventorySlots.size() > 0) {
                String inventoryName = container.inventorySlots.getSlot(0).inventory.getName();

                // Record equipment when clicked in equipment stats GUI
                if (inventoryName.equals("Your Equipment and Stats") && org.lwjgl.input.Mouse.getEventButtonState()) {
                    recordEquipmentItems(container);
                    System.out.println("Recorded equipment on click");
                }

                // Handle clicks in player's regular inventory for /eq command
                if (isPlayerInventoryOnly(container, inventoryName) && hasRecordedItems && slotsPositionsCalculated) {
                    if (org.lwjgl.input.Mouse.getEventButtonState()) {
                        int mouseX = org.lwjgl.input.Mouse.getX() * container.width / container.mc.displayWidth;
                        int mouseY = container.height - org.lwjgl.input.Mouse.getY() * container.height / container.mc.displayHeight - 1;
                        handleEquipmentClick(mouseX, mouseY);
                    }
                }
            }
        }
    }

    /**
     * Handles mouse clicks on equipment slots
     */
    private void handleEquipmentClick(int mouseX, int mouseY) {
        for (int i = 0; i < 4; i++) {
            if (savedEquipmentItems[i] != null) {
                // Check if mouse is within the equipment slot bounds
                if (mouseX >= equipmentSlotX[i] && mouseX <= equipmentSlotX[i] + 18 &&
                        mouseY >= equipmentSlotY[i] && mouseY <= equipmentSlotY[i] + 18) {

                    // Execute /eq command
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/eq");
                    break;
                }
            }
        }
    }

    /**
     * Checks if the current GUI is only the player's inventory (not any other container)
     * @param container The GUI container
     * @param inventoryName The name of the inventory
     * @return true if this is only the player's inventory
     */
    private boolean isPlayerInventoryOnly(GuiContainer container, String inventoryName) {
        // Check the class type - GuiInventory is the player's regular inventory
        if (container.getClass().getSimpleName().equals("GuiInventory")) {
            return true;
        }

        // Additional checks for inventory name
        if (inventoryName.equals("container.inventory") ||
                inventoryName.equals("Inventory") ||
                inventoryName.equals("inventory.player")) {

            // Make sure it's not a container with extra slots (like chests, crafting tables, etc.)
            // Player inventory typically has 36 slots (27 main + 9 hotbar) + 4 armor slots + 1 offhand = 41 total
            // But we'll be more lenient and check for common container sizes to exclude
            int totalSlots = container.inventorySlots.inventorySlots.size();

            // Exclude common container sizes:
            // Chests: 54+ slots, Crafting tables: 45+ slots, etc.
            if (totalSlots <= 45) { // Allow some flexibility for modded inventories
                return true;
            }
        }

        return false;
    }

    /**
     * Records equipment items from slots 10, 19, 28, 37 in "Your Equipment and Stats" GUI
     * @param container The equipment stats GUI container
     */
    private void recordEquipmentItems(GuiContainer container) {
        int[] equipmentSlots = {10, 19, 28, 37};

        for (int i = 0; i < equipmentSlots.length; i++) {
            int slotIndex = equipmentSlots[i];

            if (slotIndex < container.inventorySlots.inventorySlots.size()) {
                Slot slot = container.inventorySlots.getSlot(slotIndex);
                ItemStack itemStack = slot.getStack();

                // Save the item (copy it to prevent reference issues)
                if (itemStack != null) {
                    savedEquipmentItems[i] = itemStack.copy();
                } else {
                    savedEquipmentItems[i] = null;
                }
            } else {
                savedEquipmentItems[i] = null;
            }
        }

        hasRecordedItems = true;

        // Save to file whenever equipment is recorded using the new file manager
        EquipmentFileManager.saveEquipmentToFile(savedEquipmentItems, hasRecordedItems);
    }

    /**
     * Renders custom overlay for equipment inventory
     * @param container The equipment GUI container
     */
    private void renderEquipmentOverlay(GuiContainer container) {
        // Only render if we have recorded items
        if (!hasRecordedItems) {
            return;
        }

        // Enable proper rendering states
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();

        // Render equipment slots above inventory slots 14, 15, 16, 17 (only once per draw)
        renderEquipmentSlotsAboveInventory(container);

        // Restore rendering states
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Renders tooltips for equipment items when hovering
     */
    private void renderEquipmentTooltips(GuiContainer container, int mouseX, int mouseY) {
        if (!hasRecordedItems || !slotsPositionsCalculated) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (savedEquipmentItems[i] != null) {
                // Check if mouse is hovering over the equipment slot
                if (mouseX >= equipmentSlotX[i] && mouseX <= equipmentSlotX[i] + 18 &&
                        mouseY >= equipmentSlotY[i] && mouseY <= equipmentSlotY[i] + 18) {

                    // Get the tooltip lines from the item
                    List<String> tooltip = savedEquipmentItems[i].getTooltip(
                            Minecraft.getMinecraft().thePlayer,
                            Minecraft.getMinecraft().gameSettings.advancedItemTooltips
                    );

                    // Render the tooltip with background
                    if (!tooltip.isEmpty()) {
                        renderTooltipWithBackground(container, tooltip, mouseX, mouseY);
                    }

                    break; // Only show one tooltip at a time
                }
            }
        }
    }

    /**
     * Renders tooltip text with a background rectangle
     */
    private void renderTooltipWithBackground(GuiContainer container, List<String> tooltip, int mouseX, int mouseY) {
        if (tooltip.isEmpty()) return;

        // Calculate tooltip dimensions
        int maxWidth = 0;
        for (String line : tooltip) {
            int lineWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int tooltipHeight = tooltip.size() * 10; // 10 pixels per line
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        // Adjust position if tooltip would go off screen
        if (tooltipX + maxWidth + 8 > container.width) {
            tooltipX = mouseX - maxWidth - 16;
        }
        if (tooltipY < 8) {
            tooltipY = mouseY + 16;
        }

        // Push matrix and translate forward to render above items
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 300.0F);
        GlStateManager.disableDepth();

        // Render background rectangle
        renderTooltipBackground(tooltipX - 4, tooltipY - 4, maxWidth + 8, tooltipHeight + 8);

        // Render text lines
        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i);
            container.drawString(
                    Minecraft.getMinecraft().fontRendererObj,
                    line,
                    tooltipX,
                    tooltipY + (i * 10),
                    0xFFFFFF // White color for text
            );
        }

        // Restore matrix
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    /**
     * Renders a background rectangle for tooltips
     */
    private void renderTooltipBackground(int x, int y, int width, int height) {
        // Disable texture rendering to draw solid colors
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw background with gradient effect (darker at edges)
        // Main background
        GlStateManager.color(0.0f, 0.0f, 0.0f, 0.8f); // Semi-transparent black
        drawRect(x, y, x + width, y + height);

        // Border
        GlStateManager.color(0.3f, 0.3f, 0.3f, 0.9f); // Lighter border
        drawRect(x - 1, y - 1, x + width + 1, y); // Top border
        drawRect(x - 1, y + height, x + width + 1, y + height + 1); // Bottom border
        drawRect(x - 1, y, x, y + height); // Left border
        drawRect(x + width, y, x + width + 1, y + height); // Right border

        // Re-enable texture rendering
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Helper method to draw a rectangle
     */
    private void drawRect(int left, int top, int right, int bottom) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
    }

    /**
     * Renders 4 equipment slots above inventory slots 14, 15, 16, 17
     * @param container The GUI container
     */
    private void renderEquipmentSlotsAboveInventory(GuiContainer container) {
        // Calculate base position for equipment slots
        int guiLeft = (container.width - 176) / 2;
        int guiTop = (container.height - 166) / 2;

        // Calculate positions for each equipment slot
        for (int i = 0; i < 4; i++) {
            // Find the corresponding inventory slot (14 + i)
            int inventorySlotIndex = 14 + i;
            if (inventorySlotIndex < container.inventorySlots.inventorySlots.size()) {
                Slot inventorySlot = container.inventorySlots.getSlot(inventorySlotIndex);

                // Position equipment slot above the inventory slot
                equipmentSlotX[i] = guiLeft + inventorySlot.xDisplayPosition;
                equipmentSlotY[i] = guiTop + inventorySlot.yDisplayPosition - 20; // 20 pixels above
            } else {
                // Fallback positioning if slots don't exist
                equipmentSlotX[i] = guiLeft + 62 + (i * 18); // Standard spacing
                equipmentSlotY[i] = guiTop + 20; // Above inventory area
            }
        }

        // Mark positions as calculated
        slotsPositionsCalculated = true;

        // Bind the GUI texture (contains slot backgrounds)

        // Render each equipment slot
        for (int i = 0; i < 4; i++) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("textures/gui/container/inventory.png"));

            renderEquipmentSlotBackground(container, equipmentSlotX[i], equipmentSlotY[i], i);

            // Render the saved equipment item if it exists
            if (savedEquipmentItems[i] != null) {
                renderEquipmentItem(container, equipmentSlotX[i], equipmentSlotY[i], savedEquipmentItems[i]);
            }
        }
    }

    /**
     * Renders the background image for an equipment slot
     * @param container The GUI container
     * @param x X position of the slot
     * @param y Y position of the slot
     * @param slotIndex Index of the equipment slot (0-3)
     */
    private void renderEquipmentSlotBackground(GuiContainer container, int x, int y, int slotIndex) {
        // Enable proper rendering state
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw the slot background (18x18 slot from inventory texture)
        // The inventory.png texture has slot backgrounds we can use
        // Standard slot texture coordinates in inventory.png: (7, 83, 25, 101)
        container.drawTexturedModalRect(x, y, 7, 83, 18, 18);

        // Optional: Add equipment type icons or overlays based on slot index
    }

    /**
     * Renders equipment type icons for each slot
     * @param container The GUI container
     * @param x X position of the slot
     * @param y Y position of the slot
     * @param slotIndex Index of the equipment slot (0-3)
     */
    private void renderEquipmentSlotIcon(GuiContainer container, int x, int y, int slotIndex) {
        // Circle rendering removed - equipment slots now have clean slot backgrounds only
    }

    /**
     * Gets the text color for each equipment slot
     */
    private int getEquipmentSlotTextColor(int slotIndex) {
        switch (slotIndex) {
            case 0: // Equipment slot 0 (from slot 10)
                return 0xFFFF0000; // Red
            case 1: // Equipment slot 1 (from slot 19)
                return 0xFF00FF00; // Green
            case 2: // Equipment slot 2 (from slot 28)
                return 0xFF0000FF; // Blue
            case 3: // Equipment slot 3 (from slot 37)
                return 0xFFFFFF00; // Yellow
            default:
                return 0xFFFFFFFF; // White
        }
    }


    /**
     * Renders the actual equipment item in the slot
     * @param container The GUI container
     * @param x X position of the slot
     * @param y Y position of the slot
     * @param itemStack The item to render
     */
    private void renderEquipmentItem(GuiContainer container, int x, int y, ItemStack itemStack) {
        // Enable proper rendering for items
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();

        // Render the item
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemStack, x + 1, y + 1);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(
                Minecraft.getMinecraft().fontRendererObj, itemStack, x + 1, y + 1, null);

        // Disable lighting
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
    }
}