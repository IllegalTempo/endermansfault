package com.endermanpvp.endermanfault.storageDisplay;

import com.endermanpvp.endermanfault.DataType.Toggle;
import com.endermanpvp.endermanfault.config.AllConfig;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StorageGUIRender {
    public final Toggle Toggle = AllConfig.INSTANCE.BooleanConfig.get("toggle_storage");
    public final Toggle Inv = AllConfig.INSTANCE.BooleanConfig.get("toggle_storageInInventory");

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final StorageGUIData storageData;
    private final Minecraft mc;
    private int scrollOffset = 0;
    private final int STORAGE_HEIGHT = 120;
    private int maxScroll = 0;
    private ItemStack hoveredItem = null;
//    private StorageGUIData.Storage clickedStorage = null;
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
        if (!Toggle.data) {
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

    // Encapsulates a hovered overlay slot
    private static class HoverTarget {
        StorageGUIData.Storage storage;
        int slotId; // container slot index (0..containerSlots-1)
        HoverTarget(StorageGUIData.Storage s, int id) { storage = s; slotId = id; }
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

    // Find hovered slot inside storages (returns container slot index i for that storage)
    private HoverTarget findOverlaySlotAt(int mouseX, int mouseY) {
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

            // Container area
            final int ITEMS_PER_ROW = 9;
            final int SLOT_SIZE = 18;
            int itemsInStorage = storage.contents.length;
            int rows = Math.max(1, (itemsInStorage + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
            int itemAreaWidth = Math.min(ITEMS_PER_ROW, itemsInStorage) * SLOT_SIZE + 10;
            int itemAreaHeight = rows * SLOT_SIZE + 10;
            int containerX = storageX + (storageWidth - itemAreaWidth) / 2;
            int containerY = storageY + 20;

            // Slots start coordinates
            int startX = containerX + 5;
            int startY = containerY + 5;

            // Check slot rects (only indices 9..length-1 are items for storage)
            for (int idx = 9; idx < storage.contents.length; idx++) {
                int slotX = startX + ((idx - 9) % ITEMS_PER_ROW) * SLOT_SIZE;
                int slotY = startY + ((idx - 9) / ITEMS_PER_ROW) * SLOT_SIZE;
                if (mouseX >= slotX && mouseX <= slotX + 16 &&
                    adjustedMouseY >= slotY && adjustedMouseY <= slotY + 16) {
                    return new HoverTarget(storage, idx);
                }
            }

            currentCol++;
            if (currentCol >= adjustedStoragesPerRow) { currentCol = 0; currentRow++; }
        }
        return null;
    }

    // Check if the hovered slot belongs to the currently open container (so we can windowClick)
    private boolean isActiveContainerSlot(HoverTarget target) {
        if (!(mc.currentScreen instanceof GuiContainer)) return false;
        if (target == null || target.storage == null) return false;

        GuiContainer container = (GuiContainer) mc.currentScreen;
        if (container.inventorySlots == null || container.inventorySlots.inventorySlots.isEmpty()) return false;
        String invName = container.inventorySlots.getSlot(0).inventory.getName();

        // Determine open container identity
        boolean openIsEnder = false;
        int openNum = -1;
        try {
            if (invName.startsWith("Ender Chest")) {
                openIsEnder = true;
                openNum = Integer.parseInt(invName.substring(invName.indexOf("(") + 1, invName.indexOf("/")));
            } else if (invName.contains("Backpack")) {
                openIsEnder = false;
                openNum = Integer.parseInt(invName.substring(invName.indexOf("#") + 1, invName.length() - 1));
            } else {
                return false; // Not a storage container
            }
        } catch (Exception e) {
            return false; // Parsing failed; treat as not active
        }

        // Must match the hovered storage identity
        if (openIsEnder != target.storage.IsEnderChest || openNum != target.storage.StorageNum) return false;

        // Ensure slot index is inside current container slot range (exclude player inv)
        int totalSlots = container.inventorySlots.inventorySlots.size();
        int containerSlots = Math.max(0, totalSlots - 36);
        return target.slotId >= 0 && target.slotId < containerSlots;
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        // Respect config and visibility
        if (!Toggle.data) return;
        if (!isVisible) return;
        if (!(event.gui instanceof GuiContainer)) return;

        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        int leftPanelWidth = Math.min(sr.getScaledWidth() / 2 - 30, 375);

        // Handle scroll only within overlay area
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && mouseX <= leftPanelWidth) {
            handleScrolling(wheel);
            event.setCanceled(true);
            return;
        }

        // Handle mouse button presses
        int button = Mouse.getEventButton();
        boolean pressed = Mouse.getEventButtonState();
        if (!pressed) return;

        if (mouseX <= leftPanelWidth) {
            HoverTarget target = findOverlaySlotAt(mouseX, mouseY);
            if (target != null) {

                if(!isActiveContainerSlot(target))
                {

                    int Clickedcontainernum = target.storage.StorageNum;
                    boolean Clickedcontainertype = target.storage.IsEnderChest;

                    GuiContainer OpenedContainer = (GuiContainer) mc.currentScreen;
                    String invName = OpenedContainer.inventorySlots.getSlot(0).inventory.getName();
                    int opencontainernum = -1;
                    boolean opencontainertype = false;
                    if(invName.startsWith("Ender Chest"))
                    {
                        opencontainertype = true;
                        opencontainernum = Integer.parseInt(invName.substring(invName.indexOf("(")+1, invName.indexOf("/")));

                    } else
                    if(invName.contains("Backpack"))
                    {
                        opencontainertype = false;
                        opencontainernum = Integer.parseInt(invName.substring(invName.indexOf("#")+1,invName.length()-1));


                    }
                    //log open container info and click container info
                    System.out.println("Open container: " + opencontainernum + " type: " + opencontainertype);
                    System.out.println("Clicked container: " + Clickedcontainernum + " type: " + Clickedcontainertype);
                    if(opencontainernum != Clickedcontainernum || opencontainertype != Clickedcontainertype)
                    {
                        // Use the hovered storage from the target directly
                        StorageGUIData.Storage hoveredStorage = target.storage;
                        String command = hoveredStorage.IsEnderChest
                                ? "/ec " + hoveredStorage.StorageNum
                                : "/backpack " + hoveredStorage.StorageNum;

                        // Play click sound when attempting to open a storage via command
                        playClick();
                        mc.thePlayer.sendChatMessage(command);
                        event.setCanceled(true); // swallow so base GUI doesn't also process this click
                        return;
                    }


                } else {
                    // Determine click mode
                    boolean shift = GuiScreen.isShiftKeyDown();
                    
                    int windowId = ((GuiContainer) event.gui).inventorySlots.windowId;


                        // Normal pick/place: left or right
                        mc.playerController.windowClick(windowId, target.slotId, 1, 1, mc.thePlayer);
                        playClick();
                        // Prevent base GUI from interpreting this as an outside click (which can drop items)
                        event.setCanceled(true);
                        return;
                    
                }
            } else {
                // Click occurred inside the overlay panel but not on a slot:
                // Cancel the event immediately to prevent item dropping
                event.setCanceled(true);
                return;
            }
        }
        // If click is outside the overlay area, let the base GUI handle it normally
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Toggle.data) return;
        if (!isVisible) return;
        if (!(event.gui instanceof GuiContainer)) return;

        // Determine hovered slot (use current mouse position)
        int mouseX = Mouse.getX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        int leftPanelWidth = Math.min(sr.getScaledWidth() / 2 - 30, 375);
        if (mouseX > leftPanelWidth) return; // not over overlay

        HoverTarget target = findOverlaySlotAt(mouseX, mouseY);
        if (target == null || !isActiveContainerSlot(target)) return;

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE) return;

        int windowId = ((GuiContainer) event.gui).inventorySlots.windowId;

        // Hotbar swap keys 1..9 -> indices 0..8
        if (key >= Keyboard.KEY_1 && key <= Keyboard.KEY_9) {
            int hotbarIndex = key - Keyboard.KEY_1; // 0..8
            mc.playerController.windowClick(windowId, target.slotId, hotbarIndex, 2, mc.thePlayer);
            playClick();
            event.setCanceled(true);
            return;
        }

        // Drop (Q), Ctrl+Q to drop stack
        if (key == Keyboard.KEY_Q) {
            boolean ctrl = GuiScreen.isCtrlKeyDown();
            int mouseParam = ctrl ? 1 : 0; // 1=drop entire stack, 0=drop single
            mc.playerController.windowClick(windowId, target.slotId, mouseParam, 4, mc.thePlayer);
            playClick();
            event.setCanceled(true);
        }
    }

    private void playClick() {
        mc.getSoundHandler().playSound(
            net.minecraft.client.audio.PositionedSoundRecord.create(new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F)
        );
    }

//    private void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
//        // Only handle left click (button 0) and only if we have a storage to click
//        if (mouseButton == 0 && clickedStorage != null) {
//            // Play click sound when storage is clicked
//            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(
//                new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));
//
//            String command = clickedStorage.IsEnderChest
//                ? "/ec " + clickedStorage.StorageNum
//                : "/backpack " + clickedStorage.StorageNum;
//
//            mc.thePlayer.sendChatMessage(command);
//            return;
//        }
//
//    }

    private void handleScrolling(int wheel) {
        int scrollAmount = 20;
        if (wheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - scrollAmount);
        } else if (wheel < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + scrollAmount);
        }
    }

    private void renderStorageOverlay() {

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // Ensure scroll range reflects current storage list each frame
        calculateMaxScroll();

        hoveredItem = null;
//        clickedStorage = null;

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
            // Position scroll bar completely outside the right edge of the panel
            int scrollBarX = leftPanelWidth + 10; // fully outside the panel
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

//        // Check for storage click (store for mouseClicked event)
//        if (mouseX >= containerX && mouseX <= containerX + itemAreaWidth &&
//            mouseY >= containerY && mouseY <= containerY + itemAreaHeight) {
//            clickedStorage = storage;
//        }

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
        // Draw scrollbar background - ensure it's to the right of the panel
        drawRect(x, y, x + 5, y + height, 0x88000000);

        if (maxScroll > 0) {
            // Calculate handle size and position
            int handleHeight = Math.max(10, (height * height) / (height + maxScroll));
            int handleY = y + (int)((height - handleHeight) * ((float)scrollOffset / maxScroll));

            // Draw handle - smaller than the background
            drawRect(x + 1, handleY, x + 4, handleY + handleHeight, 0xFFAAAAAA);
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
        if (!Toggle.data) {
            return; // Exit early if storage system is disabled
        }

        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            // Show storage overlay for any container GUI that includes player inventory
            if (container.inventorySlots != null && !container.inventorySlots.inventorySlots.isEmpty()) {
                if ((container.inventorySlots.getSlot(9).inventory == mc.thePlayer.inventory && Inv.data) ||
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
