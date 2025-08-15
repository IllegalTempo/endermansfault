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
        int buttonWidth = 256;
        int buttonHeight = 32;
        int buttonMargin = 2;
        int categorySpacing = 19;
        int currentY = 50; // Start position

        // Performance Category
        this.performanceHeaderY = currentY - categorySpacing+4;
        this.buttonList.add(new SettingToggle(0, leftMargin, currentY, "Enable Armor Stand Optimization", "toggle_armorstandoptimization"));
        currentY += buttonHeight + categorySpacing;

        // QOL Category
        this.qolHeaderY = currentY - categorySpacing+4;
        this.buttonList.add(new SettingToggle(3, leftMargin, currentY, "Enable Superpair Support", "toggle_superpair"));
        currentY += buttonHeight + buttonMargin;
        this.buttonList.add(new SettingToggle(4, leftMargin, currentY, "Enable Enchantbook Crafter", "toggle_enchantbook"));
        currentY += buttonHeight + categorySpacing;

        // Cute Category
        this.cuteHeaderY = currentY - categorySpacing+4;
        this.buttonList.add(new SettingToggle(1, leftMargin, currentY, "Enable Fumo", "toggle_fumo"));
        currentY += buttonHeight + buttonMargin;
        this.buttonList.add(new CustomButton(2, leftMargin, currentY, buttonWidth, 32, "Set Fumo Position",
                new CustomButton.IButtonAction() {
                    public void execute() {
                        mc.displayGuiScreen(new PositionSelector(ConfigGUI.this, "Select Fumo Position", "plushXOffset", "plushYOffset", "plushScale", width / 2, height / 2, 1.0f));
                    }
                }));

        // Store original positions for animation
        this.originalButtonPositions = new int[this.buttonList.size()];
        for (int i = 0; i < this.buttonList.size(); i++) {
            this.originalButtonPositions[i] = this.buttonList.get(i).xPosition;
        }

        this.originalHeaderPositions = new int[]{leftMargin, leftMargin, leftMargin};
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
        int backgroundWidth = 320; // Width of the background rectangle
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

        // Draw category headers with animation
        this.drawString(this.fontRendererObj, "Performance", animatedHeaderX, performanceHeaderY, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "Quality of Life", animatedHeaderX, qolHeaderY, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "Cute", animatedHeaderX, cuteHeaderY, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
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
