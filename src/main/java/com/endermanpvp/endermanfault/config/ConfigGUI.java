package com.endermanpvp.endermanfault.config;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

public class ConfigGUI extends GuiScreen {
    public static ConfigGUI INSTANCE = new ConfigGUI();
    public GuiButton typingTo = null;
    int guiWidth = 554;
    int guiHeight = 202;

    // Store category header positions for rendering
    private int performanceHeaderY;
    private int qolHeaderY;
    private int cuteHeaderY;
    private int leftMargin = 20;
    public int SubSettingIndex = 999;
    // Animation variables
    private long guiOpenTime;
    private int animationDuration = 500; // 500ms animation
    private int originalButtonPositions[];
    private int originalHeaderPositions[];
    private List<List<GuiButton>> SubSettings = new ArrayList<List<GuiButton>>();

    // Category selection variables
    private int selectedCategory = 0; // 0=Performance, 1=QOL, 2=Cute
    private String[] categoryNames = {"Performance", "Quality of Life", "Cute"};
    private int categoryTabWidth = 140;
    private int categoryTabHeight = 25;
    private int categoryStartY = 50;
    int buttonStartX = 180; // Start buttons to the right of category tabs
    int buttonWidth = 256;
    int buttonHeight = 32;
    int buttonMargin = 5;

    private List<GuiButton> SubSetting_PingShift = new ArrayList<GuiButton>();

    public ConfigGUI() {
        super();
        this.guiOpenTime = System.currentTimeMillis();
        System.out.println("[ConfigGUI.constructor] Created ConfigGUI instance: " + this);
    }


    @Override
    public void initGui() {
        super.initGui();
        System.out.println("[ConfigGUI.initGui] mc.thePlayer: " + mc.thePlayer);
        System.out.println("[ConfigGUI.initGui] mc.currentScreen: " + mc.currentScreen);

        this.buttonList.clear();

        // Add buttons for the currently selected category only
        addButtonsForSelectedCategory();

        // Store original positions for animation
        this.originalButtonPositions = new int[this.buttonList.size()];
        for (int i = 0; i < this.buttonList.size(); i++) {
            this.originalButtonPositions[i] = this.buttonList.get(i).xPosition;
        }

        this.originalHeaderPositions = new int[]{leftMargin, leftMargin, leftMargin};

        AddSubSetting_PingShift();
        SubSettings.add(SubSetting_PingShift);

    }
    private void AddSubSetting_PingShift()
    {
        SubSetting_PingShift.clear();
        int currentY = categoryStartY;
        int x = buttonStartX+ 290;
        // Add TextInputField example for PingShift block name
        this.SubSetting_PingShift.add(new TextInputField(9,x , currentY, "PingShift Block", "inp_pingshift_block", "minecraft:wool"));
        currentY += buttonHeight + buttonMargin + 20;
        // Add IntInputField example for PingShift extra ping
        this.SubSetting_PingShift.add(new IntInputField(10, x, currentY, "Extra Ping (ms)", "inp_pingshift_extraping", 0, 500));
        currentY += buttonHeight + buttonMargin + 20;
        // Add IntInputField example for PingShift block metadata
        this.SubSetting_PingShift.add(new IntInputField(11, x, currentY, "Block Metadata", "inp_pingshift_blockmeta", 0, 15));
    }

    private void addButtonsForSelectedCategory() {

        int currentY = categoryStartY;

        switch (selectedCategory) {
            case 0: // Performance
                this.buttonList.add(new SettingToggle(0, buttonStartX, currentY, "Enable Armor Stand Optimization", "toggle_armorstandoptimization"));
                break;

            case 1: // Quality of Life
                this.buttonList.add(new SettingToggle(3, buttonStartX, currentY, "Enable Superpair Support", "toggle_superpair"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new SettingToggle(4, buttonStartX, currentY, "Enable Enchantbook Crafter", "toggle_enchantbook"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new SettingToggle(5, buttonStartX, currentY, "Enable Storage Display", "toggle_storage"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new SettingToggle(6, buttonStartX, currentY, "Show Storage In Inventory", "toggle_storageInInventory"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new SettingToggle(7, buttonStartX, currentY, "PingShift (HighPing Mining)", "toggle_pingshift"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new SubConfigButton(0,8, buttonStartX, currentY,"PingShift Settings"));

                break;

            case 2: // Cute
                this.buttonList.add(new SettingToggle(1, buttonStartX, currentY, "Enable Fumo", "toggle_fumo"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new PositionSelectorButton(2, buttonStartX, currentY, buttonWidth, 32, "Set Fumo Position","dim_fumo"));

                break;
        }
    }

    private float getAnimationProgress() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - this.guiOpenTime;
        if (elapsed >= this.animationDuration) {
            return 1.0f;
        }
        return (float) elapsed / this.animationDuration;
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3.0f);
    }

    private void updateAnimatedPositions() {
        float progress = getAnimationProgress();
        float easedProgress = easeOutCubic(progress);

        // Animate buttons sliding in from left (-300px start position)
        int startOffset = -300;
        for (int i = 0; i < this.buttonList.size(); i++) {
            GuiButton button = this.buttonList.get(i);
            int targetX = this.originalButtonPositions[i];
            int currentX = (int) (startOffset + (targetX - startOffset) * easedProgress);
            button.xPosition = currentX;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof SettingToggle) {
            // Handle toggle button click
            ((SettingToggle) button).toggleConfig();
            return;
        }
        if (button instanceof CustomButton) {
            // Handle custom button click by executing its function
            ((CustomButton) button).executeAction();
            return;
        }

        // Legacy switch case for any remaining old buttons
        switch (button.id)
        {
            case 2:
                this.mc.displayGuiScreen(new PositionSelector(this, "Select Fumo Position", "dim_fumo", this.width / 2, this.height / 2,1));
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Handle TextInputField and IntInputField clicks for main buttons
        for (GuiButton button : this.buttonList) {
            if (button instanceof TextInputField) {
                ((TextInputField) button).mouseClicked(mouseX, mouseY, mouseButton);
            } else if (button instanceof IntInputField) {
                ((IntInputField) button).mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        // Handle TextInputField and IntInputField clicks for subsetting buttons
        if (SubSettingIndex != 999 && SubSettingIndex < SubSettings.size()) {
            List<GuiButton> subSettingui = SubSettings.get(SubSettingIndex);
            for (GuiButton button : subSettingui) {
                if (button instanceof TextInputField) {
                    ((TextInputField) button).mouseClicked(mouseX, mouseY, mouseButton);
                } else if (button instanceof IntInputField) {
                    ((IntInputField) button).mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }

        // Check if click was on a category tab
        float progress = getAnimationProgress();
        float easedProgress = easeOutCubic(progress);
        int startOffset = -300;
        int animatedHeaderX = (int) (startOffset + (leftMargin - startOffset) * easedProgress);

        for (int i = 0; i < categoryNames.length; i++) {
            int tabX = animatedHeaderX;
            int tabY = categoryStartY + (i * (categoryTabHeight + 5));

            if (mouseX >= tabX && mouseX <= tabX + categoryTabWidth &&
                mouseY >= tabY && mouseY <= tabY + categoryTabHeight) {

                // Category tab clicked - switch category
                if (selectedCategory != i) {
                    selectedCategory = i;
                    // Reinitialize GUI to show new category's buttons
                    initGui();
                }
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        super.keyTyped(typedChar, keyCode);

        // Handle text input for focused input fields
        if (this.typingTo instanceof TextInputField) {
            ((TextInputField) this.typingTo).keyTyped(typedChar, keyCode);
        } else if (this.typingTo instanceof IntInputField) {
            ((IntInputField) this.typingTo).keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Update cursor counter for focused text fields in main buttons
        for (GuiButton button : this.buttonList) {
            if (button instanceof TextInputField) {
                ((TextInputField) button).updateCursorCounter();
            } else if (button instanceof IntInputField) {
                ((IntInputField) button).updateCursorCounter();
            }
        }

        // Update cursor counter for focused text fields in subsetting buttons
        if (SubSettingIndex != 999 && SubSettingIndex < SubSettings.size()) {
            List<GuiButton> subSettingui = SubSettings.get(SubSettingIndex);
            for (GuiButton button : subSettingui) {
                if (button instanceof TextInputField) {
                    ((TextInputField) button).updateCursorCounter();
                } else if (button instanceof IntInputField) {
                    ((IntInputField) button).updateCursorCounter();
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Update animated positions before drawing
        updateAnimatedPositions();

        // Remove background drawing for transparent effect
        // drawBackground(0); // Commented out for transparent background

        // Calculate animated positions for background
        float progress = getAnimationProgress();
        float easedProgress = easeOutCubic(progress);
        int startOffset = -300;
        int animatedHeaderX = (int) (startOffset + (leftMargin - startOffset) * easedProgress);

        // Draw transparent black background rectangle on the left
        int backgroundWidth = 450; // Increased width to accommodate both tabs and buttons
        int backgroundHeight = this.height;
        int backgroundX = animatedHeaderX - 20; // Slightly behind the elements

        // Enable blending for transparency
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        net.minecraft.client.renderer.GlStateManager.color(0.0F, 0.0F, 0.0F, 0.3F); // 30% opacity black

        // Draw the background rectangle
        drawRect(backgroundX, 0, backgroundX + backgroundWidth, backgroundHeight, 0x4D000000);

        // Restore rendering state
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw main title at the top - properly centered in the black rectangle
        int titleX = backgroundX + (backgroundWidth / 2);
        this.drawCenteredString(this.fontRendererObj, "Enderman's Fault Config", titleX, 15, 0xFFFFFF);

        // Draw category tabs
        drawCategoryTabs(animatedHeaderX, mouseX, mouseY);
        if(SubSettingIndex != 999)
        {
            List<GuiButton> subSettingui = SubSettings.get(SubSettingIndex);

            // Calculate the actual bounds of the subsetting buttons
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;

            for(GuiButton button : subSettingui) {
                minY = Math.min(minY, button.yPosition);
                maxY = Math.max(maxY, button.yPosition + button.height);
            }

            // Add padding and ensure we have a minimum height
            int padding = 10;
            int subSettingHeight = subSettingui.isEmpty() ? 50 : (maxY - minY + 2 * padding);
            int subSettingY = subSettingui.isEmpty() ? categoryStartY : (minY - padding);

            // Draw the subsetting background that properly fits the content
            drawRect(backgroundX + backgroundWidth, subSettingY, backgroundX + backgroundWidth + 400, subSettingY + subSettingHeight, 0x4D000000);

            for(GuiButton button : subSettingui) {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCategoryTabs(int animatedX, int mouseX, int mouseY) {
        for (int i = 0; i < categoryNames.length; i++) {
            int tabX = animatedX;
            int tabY = categoryStartY + (i * (categoryTabHeight + 5));

            // Check if mouse is hovering over this tab
            boolean isHovered = mouseX >= tabX && mouseX <= tabX + categoryTabWidth &&
                              mouseY >= tabY && mouseY <= tabY + categoryTabHeight;

            // Draw tab background
            net.minecraft.client.renderer.GlStateManager.enableBlend();
            net.minecraft.client.renderer.GlStateManager.disableTexture2D();

            if (i == selectedCategory) {
                // Selected tab - brighter background
                net.minecraft.client.renderer.GlStateManager.color(0.2F, 0.4F, 0.8F, 0.8F);
                drawRect(tabX, tabY, tabX + categoryTabWidth, tabY + categoryTabHeight, 0xCC3366CC);
            } else if (isHovered) {
                // Hovered tab - medium background
                net.minecraft.client.renderer.GlStateManager.color(0.4F, 0.4F, 0.4F, 0.6F);
                drawRect(tabX, tabY, tabX + categoryTabWidth, tabY + categoryTabHeight, 0x99666666);
            } else {
                // Normal tab - subtle background
                net.minecraft.client.renderer.GlStateManager.color(0.2F, 0.2F, 0.2F, 0.4F);
                drawRect(tabX, tabY, tabX + categoryTabWidth, tabY + categoryTabHeight, 0x66333333);
            }

            // Restore rendering state
            net.minecraft.client.renderer.GlStateManager.enableTexture2D();
            net.minecraft.client.renderer.GlStateManager.disableBlend();
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw tab text
            int textColor = (i == selectedCategory) ? 0xFFFFFF : 0xCCCCCC;
            this.drawCenteredString(this.fontRendererObj, categoryNames[i],
                tabX + categoryTabWidth / 2, tabY + (categoryTabHeight - 8) / 2, textColor);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        System.out.println("[ConfigGUI.onGuiClosed] GUI closed: " + this);

        // Save configuration when GUI is closed
        ModConfig.instance.saveConfig();
        System.out.println("[ConfigGUI.onGuiClosed] Configuration saved");
    }
}
