package com.endermanpvp.endermanfault.storageDisplay;

import com.endermanpvp.endermanfault.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class StorageGUIRender {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final StorageGUIData storageData;
    private final Minecraft mc;
    private int scrollOffset = 0;
    private final int STORAGE_HEIGHT = 120;
    private int maxScroll = 0;
    private ItemStack hoveredItem = null;
    private StorageGUIData.Storage clickedStorage = null;
    private boolean isVisible = false;
    private boolean shouldClose = false;

    public StorageGUIRender(StorageGUIData storageData) {
        this.storageData = storageData;
        this.mc = Minecraft.getMinecraft();
    }

    public void show() {
        isVisible = true;
        calculateMaxScroll();
    }

    public void hide() {
        isVisible = false;
        shouldClose = false;
    }

    private void calculateMaxScroll() {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenHeight = sr.getScaledHeight();
        int leftPanelWidth = Math.min(sr.getScaledWidth() / 2 - 30, 370); // Increase to half screen minus 30px, or 350px max

        // Calculate storages per row based on panel width (same logic as in drawStorages)
        int adjustedStoragesPerRow = Math.max(1, Math.min(2, leftPanelWidth / 150)); // Keep max 2 storages per row
        int storageRows = (storageData.storages.size() + adjustedStoragesPerRow - 1) / adjustedStoragesPerRow;
        int totalStorageHeight = storageRows * (STORAGE_HEIGHT + 15); // Use consistent spacing
        int availableHeight = screenHeight - 40; // Reserve space for title and margin
        maxScroll = Math.max(0, totalStorageHeight - availableHeight);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        // Check if storage display is enabled in config
        if (!ModConfig.getInstance().getBoolean("toggle_storage", true)) {
            return; // Exit early if storage system is disabled
        }

        if (!isVisible) {
            return;
        }

        // Only render when a GUI container is open
        if (!(mc.currentScreen instanceof GuiContainer)) {
            hide();
            return;
        }

        // Remove per-frame input draining; handled in MouseInputEvent now
        // handleInput();

        if (shouldClose) {
            hide();
            return;
        }

        // Push matrix and set up rendering state for overlay
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Render the storage overlay on top
        renderStorageOverlay();

        // Restore rendering state
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    // Helper to find which storage (if any) is under a given mouse position
    private StorageGUIData.Storage findStorageAt(int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);
        int leftPanelWidth = Math.min(sr.getScaledWidth() / 2 - 30, 375);
        int titleHeight = 20;
        int scrollableAreaY = titleHeight;
        int adjustedMouseY = mouseY + scrollOffset;

        int adjustedStoragesPerRow = Math.max(1, Math.min(2, leftPanelWidth / 150));
        int storageWidth = (leftPanelWidth - 30) / adjustedStoragesPerRow;

        int currentRow = 0;
        int currentCol = 0;
        for (int i = 0; i < storageData.storages.size(); i++) {
            StorageGUIData.Storage storage = storageData.storages.get(i);
            int storageX = 15 + currentCol * (storageWidth + 10);
            int storageY = scrollableAreaY + currentRow * (STORAGE_HEIGHT + 15);

            // Compute container area
            final int ITEMS_PER_ROW = 9;
            final int SLOT_SIZE = 18;
            int itemsInStorage = storage.contents.length;
            int rows = Math.max(1, (itemsInStorage + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
            int itemAreaWidth = Math.min(ITEMS_PER_ROW, itemsInStorage) * SLOT_SIZE + 10;
            int itemAreaHeight = rows * SLOT_SIZE + 10;
            int containerX = storageX + (storageWidth - itemAreaWidth) / 2;
            int containerY = storageY + 20;

            if (mouseX >= containerX && mouseX <= containerX + itemAreaWidth &&
                adjustedMouseY >= containerY && adjustedMouseY <= containerY + itemAreaHeight) {
                return storage;
            }

            currentCol++;
            if (currentCol >= adjustedStoragesPerRow) {
                currentCol = 0;
                currentRow++;
            }
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        // Respect config and visibility
        if (!ModConfig.getInstance().getBoolean("enable_storage", true)) return;
        if (!isVisible) return;
        if (!(event.gui instanceof GuiContainer)) return;

        // Compute mouse relative to current screen
        int mouseX = org.lwjgl.input.Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - org.lwjgl.input.Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        int leftPanelWidth = Math.min(sr.getScaledWidth() / 2 - 30, 375);

        // Handle scroll wheel over our overlay only
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0 && mouseX <= leftPanelWidth) {
            handleScrolling(wheel);
            event.setCanceled(true); // we've handled it
            return;
        }

        // Handle left-click on a storage only; let everything else pass through
        int button = org.lwjgl.input.Mouse.getEventButton();
        boolean pressed = org.lwjgl.input.Mouse.getEventButtonState();
        if (pressed && button == 0 && mouseX <= leftPanelWidth) {
            StorageGUIData.Storage target = findStorageAt(mouseX, mouseY);
            if (target != null) {
                this.clickedStorage = target;
                handleMouseClick(mouseX, mouseY, 0);
                event.setCanceled(true); // consume only when we actually click a storage
            }
        }
    }

    private void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        // Only handle left click (button 0) and only if we have a storage to click
        if (mouseButton == 0 && clickedStorage != null) {
            // Play click sound when storage is clicked
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(
                new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));

            String command = clickedStorage.IsEnderChest
                ? "/ec " + clickedStorage.StorageNum
                : "/backpack " + clickedStorage.StorageNum;

            mc.thePlayer.sendChatMessage(command);
            return;
        }

    }

    private void handleScrolling(int wheel) {
        int scrollAmount = 20;
        if (wheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - scrollAmount);
        } else if (wheel < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + scrollAmount);
        }
    }

    private void renderStorageOverlay() {
        System.out.println("rendering storage");

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        hoveredItem = null;
        clickedStorage = null;

        // Calculate left panel dimensions - increase size slightly
        int leftPanelWidth = Math.min(screenWidth / 2 - 30, 375); // Half screen minus 30px, or 350px max
        int titleHeight = 20;
        int scrollableAreaHeight = screenHeight - titleHeight - 20;
        int scrollableAreaY = titleHeight;

        // Draw semi-transparent background only for the left panel
        drawRect(0, 0, leftPanelWidth, screenHeight, 0x30000000);

        // Draw title in the left panel
        String title = "Storage (" + storageData.storages.size() + ")";
        int titleWidth = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawStringWithShadow(title, (leftPanelWidth - titleWidth) / 2, 5, 0xFFFFFF);

        // Enable scissoring for scrollable area (only left panel)
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = sr.getScaleFactor();
        GL11.glScissor(0,
                      (sr.getScaledHeight() - scrollableAreaY - scrollableAreaHeight) * scaleFactor,
                      leftPanelWidth * scaleFactor,
                      scrollableAreaHeight * scaleFactor);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffset, 0);

        // Get mouse coordinates
        int mouseX = Mouse.getX() * screenWidth / mc.displayWidth;
        int mouseY = screenHeight - Mouse.getY() * screenHeight / mc.displayHeight - 1;
        int adjustedMouseY = mouseY + scrollOffset;

        drawStorages(leftPanelWidth, scrollableAreaY, mouseX, adjustedMouseY);

        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scroll indicator just to the right of the GUI panel
        if (maxScroll > 0) {
            // Position scroll bar just outside the right edge of the panel
            int scrollBarX = leftPanelWidth + 5; // 5 pixels to the right of the panel edge
            drawScrollBar(scrollBarX, scrollableAreaY, scrollableAreaHeight);
        }

        // Draw tooltip last to ensure it's on top
        if (hoveredItem != null) {
            drawHoveringText(mouseX, mouseY);
        }

    }

    private void drawStorages(int panelWidth, int startY, int mouseX, int mouseY) {
        // Limit to maximum 2 storages per row for smaller panel
        int adjustedStoragesPerRow = Math.max(1, Math.min(2, panelWidth / 150)); // Max 2 storages, need 150px each
        int storageWidth = (panelWidth - 30) / adjustedStoragesPerRow; // Appropriate margin space
        int currentRow = 0;
        int currentCol = 0;

        for (int i = 0; i < storageData.storages.size(); i++) {
            StorageGUIData.Storage storage = storageData.storages.get(i);

            int storageX = 15 + currentCol * (storageWidth + 10); // Proper spacing between storages
            int storageY = startY + currentRow * (STORAGE_HEIGHT + 15); // Consistent vertical spacing

            drawStorage(storage, storageX, storageY, storageWidth, mouseX, mouseY);

            currentCol++;
            if (currentCol >= adjustedStoragesPerRow) {
                currentCol = 0;
                currentRow++;
            }
        }
    }

    private void drawStorage(StorageGUIData.Storage storage, int x, int y, int width, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Define constants locally
        final int ITEMS_PER_ROW = 9;
        final int SLOT_SIZE = 18;

        // Calculate dimensions
        int itemsInStorage = storage.contents.length;
        int rows = Math.max(1, (itemsInStorage + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
        int itemAreaWidth = Math.min(ITEMS_PER_ROW, itemsInStorage) * SLOT_SIZE + 10;
        int itemAreaHeight = rows * SLOT_SIZE + 10;

        // Center the item area
        int containerX = x + (width - itemAreaWidth) / 2;
        int containerY = y + 20;

        // Draw title
        String storageTitle = (storage.IsEnderChest ? "Ender Chest " : "Backpack ") + storage.StorageNum;
        int titleWidth = mc.fontRendererObj.getStringWidth(storageTitle);
        int titleX = x + (width - titleWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(storageTitle, titleX, y + 5, storage.IsEnderChest ? 0x800080 : 0x404040);

        // Draw container background
        drawRect(containerX, containerY, containerX + itemAreaWidth, containerY + itemAreaHeight, 0x88000000);
        drawRect(containerX + 1, containerY + 1, containerX + itemAreaWidth - 1, containerY + itemAreaHeight - 1, 0x44FFFFFF);

        // Check for storage click (store for mouseClicked event)
        if (mouseX >= containerX && mouseX <= containerX + itemAreaWidth &&
            mouseY >= containerY && mouseY <= containerY + itemAreaHeight) {
            clickedStorage = storage;
        }

        // Draw items
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        int startX = containerX + 5;
        int startY = containerY + 5;

        for (int i = 9; i < storage.contents.length; i++) {
            int slotX = startX + ((i-9) % ITEMS_PER_ROW) * SLOT_SIZE;
            int slotY = startY + ((i-9) / ITEMS_PER_ROW) * SLOT_SIZE;

            // Draw slot background
            drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x88888888);

            Slot slot = storage.contents[i];
            if (slot != null && slot.getStack() != null) {
                ItemStack stack = slot.getStack();

                // Check for item hover
                if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                    hoveredItem = stack;
                    drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
                }

                // Render item
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, slotX, slotY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, slotX, slotY, null);
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void drawScrollBar(int x, int y, int height) {
        // Draw scrollbar background - ensure it's within the panel
        drawRect(x - 6, y, x - 1, y + height, 0x88000000);

        if (maxScroll > 0) {
            // Calculate handle size and position
            int handleHeight = Math.max(10, (height * height) / (height + maxScroll));
            int handleY = y + (int)((height - handleHeight) * ((float)scrollOffset / maxScroll));

            // Draw handle - make it slightly smaller than the background
            drawRect(x - 5, handleY, x - 2, handleY + handleHeight, 0xFFAAAAAA);
        }
    }

    private void drawHoveringText(int mouseX, int mouseY) {
        if (hoveredItem == null) return;

        List<String> tooltip = hoveredItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
        if (tooltip.isEmpty()) return;

        // Calculate tooltip dimensions
        int tooltipWidth = 0;
        for (String line : tooltip) {
            int lineWidth = mc.fontRendererObj.getStringWidth(line);
            if (lineWidth > tooltipWidth) {
                tooltipWidth = lineWidth;
            }
        }

        int tooltipHeight = tooltip.size() * 10;
        ScaledResolution sr = new ScaledResolution(mc);

        // Position tooltip to avoid screen edges
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        if (tooltipX + tooltipWidth + 6 > sr.getScaledWidth()) {
            tooltipX = mouseX - tooltipWidth - 12;
        }
        if (tooltipY + tooltipHeight + 6 > sr.getScaledHeight()) {
            tooltipY = mouseY - tooltipHeight - 12;
        }

        // Ensure tooltip is on screen
        tooltipX = Math.max(4, Math.min(tooltipX, sr.getScaledWidth() - tooltipWidth - 4));
        tooltipY = Math.max(4, Math.min(tooltipY, sr.getScaledHeight() - tooltipHeight - 4));

        // Draw tooltip with proper z-level
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();

        // Background
        drawRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 0xF0100010);
        drawRect(tooltipX - 2, tooltipY - 2, tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 0x11111111);

        // Text
        for (int i = 0; i < tooltip.size(); i++) {
            mc.fontRendererObj.drawStringWithShadow(tooltip.get(i), tooltipX, tooltipY + i * 10, 0xFFFFFF);
        }

        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    // Helper method for drawing rectangles (since we're no longer extending GuiScreen)
    private void drawRect(int left, int top, int right, int bottom, int color) {
        GuiScreen.drawRect(left, top, right, bottom, color);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        // Check if storage display is enabled in config
        if (!ModConfig.getInstance().getBoolean("enable_storage", true)) {
            return; // Exit early if storage system is disabled
        }

        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            // Show storage overlay for any container GUI that includes player inventory
            if (container.inventorySlots != null && !container.inventorySlots.inventorySlots.isEmpty()) {
                if ((container.inventorySlots.getSlot(9).inventory == mc.thePlayer.inventory && ModConfig.getInstance().getBoolean("toggle_storageInInventory", true)) ||
                     "Storage".equals(container.inventorySlots.getSlot(0).inventory.getName()))
                     {
                     show();

                 }


             }
         }
     }

    // Static instance management
    private static StorageGUIRender instance;

    public static void registerEvents(StorageGUIData storageData) {
        if (instance != null) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
        }
        instance = new StorageGUIRender(storageData);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
    }
}
