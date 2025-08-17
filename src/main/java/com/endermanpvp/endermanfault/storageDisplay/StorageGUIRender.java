package com.endermanpvp.endermanfault.storageDisplay;

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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class StorageGUIRender extends GuiScreen {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final StorageGUIData storageData;
    private int scrollOffset = 0;
    private final int ITEMS_PER_ROW = 9;
    private final int SLOT_SIZE = 18;
    private final int STORAGE_HEIGHT = 120;
    private final int STORAGES_PER_ROW = 5;
    private int maxScroll = 0;
    private ItemStack hoveredItem = null;
    private StorageGUIData.Storage clickedStorage = null;

    public StorageGUIRender(StorageGUIData storageData) {
        this.storageData = storageData;
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    public void initGui() {
        super.initGui();
        calculateMaxScroll();
    }

    private void calculateMaxScroll() {
        int storageRows = (storageData.storages.size() + STORAGES_PER_ROW - 1) / STORAGES_PER_ROW;
        int totalStorageHeight = storageRows * STORAGE_HEIGHT;
        int availableHeight = height - 140; // Reserve space for title and player inventory
        maxScroll = Math.max(0, totalStorageHeight - availableHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        hoveredItem = null;
        clickedStorage = null;

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // Calculate layout areas
        int titleHeight = 20;
        int playerInventoryHeight = 100;
        int scrollableAreaHeight = screenHeight - titleHeight - playerInventoryHeight;
        int scrollableAreaY = titleHeight;

        // Draw title
        String title = "Storage Overview (" + storageData.storages.size() + " storages)";
        int titleWidth = fontRendererObj.getStringWidth(title);
        fontRendererObj.drawString(title, (screenWidth - titleWidth) / 2, 5, 0xFFFFFF);

        // Enable scissoring for scrollable area
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0,
                      (screenHeight - scrollableAreaY - scrollableAreaHeight) * sr.getScaleFactor(),
                      screenWidth * sr.getScaleFactor(),
                      scrollableAreaHeight * sr.getScaleFactor());

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffset, 0);

        // Draw storages with proper mouse coordinate adjustment
        int adjustedMouseY = mouseY + scrollOffset;
        drawStorages(screenWidth, scrollableAreaY, mouseX, adjustedMouseY);

        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scroll indicator
        if (maxScroll > 0) {
            drawScrollBar(screenWidth - 10, scrollableAreaY, scrollableAreaHeight);
        }

        // Draw player inventory
        drawPlayerInventory(mouseX, mouseY, screenHeight - playerInventoryHeight);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw tooltip last to ensure it's on top
        if (hoveredItem != null) {
            drawHoveringText(mouseX, mouseY);
        }
    }

    private void drawStorages(int screenWidth, int startY, int mouseX, int mouseY) {
        int storageWidth = (screenWidth - 60) / STORAGES_PER_ROW;
        int currentRow = 0;
        int currentCol = 0;

        for (int i = 0; i < storageData.storages.size(); i++) {
            StorageGUIData.Storage storage = storageData.storages.get(i);

            int storageX = 20 + currentCol * storageWidth;
            int storageY = startY + currentRow * STORAGE_HEIGHT;

            drawStorage(storage, storageX, storageY, storageWidth, mouseX, mouseY);

            currentCol++;
            if (currentCol >= STORAGES_PER_ROW) {
                currentCol = 0;
                currentRow++;
            }
        }
    }

    private void drawStorage(StorageGUIData.Storage storage, int x, int y, int width, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

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
        int titleWidth = fontRendererObj.getStringWidth(storageTitle);
        int titleX = x + (width - titleWidth) / 2;
        fontRendererObj.drawString(storageTitle, titleX, y + 5, storage.IsEnderChest ? 0x800080 : 0x404040);

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

        for (int i = 0; i < storage.contents.length; i++) {
            int slotX = startX + (i % ITEMS_PER_ROW) * SLOT_SIZE;
            int slotY = startY + (i / ITEMS_PER_ROW) * SLOT_SIZE;

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
                mc.getRenderItem().renderItemOverlayIntoGUI(fontRendererObj, stack, slotX, slotY, null);
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void drawScrollBar(int x, int y, int height) {
        // Draw scrollbar background
        drawRect(x - 5, y, x, y + height, 0x88000000);

        if (maxScroll > 0) {
            // Calculate handle
            int handleHeight = Math.max(10, (height * height) / (height + maxScroll));
            int handleY = y + (int)((height - handleHeight) * ((float)scrollOffset / maxScroll));

            // Draw handle
            drawRect(x - 4, handleY, x - 1, handleY + handleHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public void handleMouseInput() {
        try {
            super.handleMouseInput();
        } catch (java.io.IOException e) {
            System.err.println("Error handling mouse input: " + e.getMessage());
        }

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            handleScrolling(wheel);
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

    private void drawHoveringText(int mouseX, int mouseY) {
        if (hoveredItem == null) return;

        List<String> tooltip = hoveredItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
        if (tooltip.isEmpty()) return;

        // Calculate tooltip dimensions
        int tooltipWidth = 0;
        for (String line : tooltip) {
            int lineWidth = fontRendererObj.getStringWidth(line);
            if (lineWidth > tooltipWidth) {
                tooltipWidth = lineWidth;
            }
        }

        int tooltipHeight = tooltip.size() * 10;

        // Position tooltip to avoid screen edges
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        if (tooltipX + tooltipWidth + 6 > width) {
            tooltipX = mouseX - tooltipWidth - 12;
        }
        if (tooltipY + tooltipHeight + 6 > height) {
            tooltipY = mouseY - tooltipHeight - 12;
        }

        // Ensure tooltip is on screen
        tooltipX = Math.max(4, Math.min(tooltipX, width - tooltipWidth - 4));
        tooltipY = Math.max(4, Math.min(tooltipY, height - tooltipHeight - 4));

        // Draw tooltip with proper z-level
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();

        // Background
        drawRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 0xF0100010);
        drawRect(tooltipX - 2, tooltipY - 2, tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 0x505000FF);

        // Text
        for (int i = 0; i < tooltip.size(); i++) {
            fontRendererObj.drawStringWithShadow(tooltip.get(i), tooltipX, tooltipY + i * 10, 0xFFFFFF);
        }

        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    private void drawPlayerInventory(int mouseX, int mouseY, int yPosition) {

    }

    private void renderInventorySlot(int slotIndex, int slotX, int slotY, int mouseX, int mouseY, boolean drawHover) {
        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slotIndex);
        if (stack != null) {
            // Check hover
            if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                hoveredItem = stack;
                if (drawHover) {
                    this.drawTexturedModalRect(slotX, slotY, 240, 240, 16, 16);
                }
            }

            // Render item
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, slotX, slotY);
            mc.getRenderItem().renderItemOverlayIntoGUI(fontRendererObj, stack, slotX, slotY, null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (java.io.IOException e) {
            System.err.println("Error handling mouse click: " + e.getMessage());
        }

        // Handle storage click
        if (mouseButton == 0 && clickedStorage != null) {
            String command = clickedStorage.IsEnderChest
                ? "/ec " + clickedStorage.StorageNum
                : "/backpack " + clickedStorage.StorageNum;

            mc.thePlayer.sendChatMessage(command);
            closeGUI();
            return;
        }

        // Close on right-click
        if (mouseButton == 1) {
            closeGUI();
        }
    }

    private void closeGUI() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // ESC
            closeGUI();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            if (container.inventorySlots != null &&
                !container.inventorySlots.inventorySlots.isEmpty() &&
                container.inventorySlots.getSlot(0).inventory != null) {

                String invName = container.inventorySlots.getSlot(0).inventory.getName();

                if (invName != null && invName.equals("Storage")) {
                    event.setCanceled(true);
                    Minecraft.getMinecraft().displayGuiScreen(new StorageGUIRender(storageData));
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
