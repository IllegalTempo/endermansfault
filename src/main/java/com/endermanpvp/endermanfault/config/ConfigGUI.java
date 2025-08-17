package com.endermanpvp.endermanfault.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class ConfigGUI extends GuiScreen {

    int guiWidth = 554;
    int guiHeight = 202;

    // Store category header positions for rendering
    private int performanceHeaderY;
    private int qolHeaderY;
    private int cuteHeaderY;
    private int leftMargin = 20;

    // Animation variables
    private long guiOpenTime;
    private int animationDuration = 500; // 500ms animation
    private int originalButtonPositions[];
    private int originalHeaderPositions[];

    // Category selection variables
    private int selectedCategory = 0; // 0=Performance, 1=QOL, 2=Cute
    private String[] categoryNames = {"Performance", "Quality of Life", "Cute"};
    private int categoryTabWidth = 140;
    private int categoryTabHeight = 25;
    private int categoryStartY = 50;

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
    }

    private void addButtonsForSelectedCategory() {
        int buttonStartX = 180; // Start buttons to the right of category tabs
        int buttonWidth = 256;
        int buttonHeight = 32;
        int buttonMargin = 5;
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
                break;

            case 2: // Cute
                this.buttonList.add(new SettingToggle(1, buttonStartX, currentY, "Enable Fumo", "toggle_fumo"));
                currentY += buttonHeight + buttonMargin;
                this.buttonList.add(new CustomButton(2, buttonStartX, currentY, buttonWidth, 32, "Set Fumo Position",
                    new CustomButton.IButtonAction() {
                        public void execute() {
                            mc.displayGuiScreen(new PositionSelector(ConfigGUI.this, "Select Fumo Position", "plushXOffset", "plushYOffset", "plushScale", width / 2, height / 2, 1.0f));
                        }
                    }));
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
            case 0:
                this.mc.thePlayer.closeScreen();
                break;
            case 2:
                this.mc.displayGuiScreen(new PositionSelector(this, "Select Fumo Position", "plushXOffset", "plushYOffset", this.width / 2, this.height / 2));
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) { // Left click
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
    }
}
