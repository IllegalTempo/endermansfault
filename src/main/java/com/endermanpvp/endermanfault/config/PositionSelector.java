package com.endermanpvp.endermanfault.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public class PositionSelector extends GuiScreen {
    private final GuiScreen parent;
    private final String title;
    private final String xConfigKey;
    private final String yConfigKey;
    private final String scaleConfigKey;
    private final int defaultX;
    private final int defaultY;
    private final float defaultScale;
    private boolean isDragging = false;
    private int posX;
    private int posY;
    private float scale;

    public PositionSelector(GuiScreen parent, String title, String xConfigKey, String yConfigKey, int defaultX, int defaultY) {
        this(parent, title, xConfigKey, yConfigKey, "plushScale", defaultX, defaultY, 1.0f);
    }

    public PositionSelector(GuiScreen parent, String title, String xConfigKey, String yConfigKey, String scaleConfigKey, int defaultX, int defaultY, float defaultScale) {
        this.parent = parent;
        this.title = title;
        this.xConfigKey = xConfigKey;
        this.yConfigKey = yConfigKey;
        this.scaleConfigKey = scaleConfigKey;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.defaultScale = defaultScale;
        // Load current position and scale from config
        this.posX = ModConfig.getInstance().getInt(xConfigKey, defaultX);
        this.posY = ModConfig.getInstance().getInt(yConfigKey, defaultY);
        this.scale = ModConfig.getInstance().getFloat(scaleConfigKey, defaultScale);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        // Add done button
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height - 30, 100, 20, "Done"));


        // Update position based on current screen size if needed
        if (this.posX <= 0) this.posX = this.defaultX > 0 ? this.defaultX : this.width / 2;
        if (this.posY <= 0) this.posY = this.defaultY > 0 ? this.defaultY : this.height / 2;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            // Save position and return to config
            ModConfig.getInstance().setInt(this.xConfigKey, this.posX);
            ModConfig.getInstance().setInt(this.yConfigKey, this.posY);
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) { // Left click
            // Calculate scaled size for click detection
            int scaledSize = (int)(50 * this.scale);
            int halfSize = scaledSize / 2;
            // Check if clicking on position area (scaled area)
            if (mouseX >= this.posX - halfSize && mouseX <= this.posX + halfSize &&
                mouseY >= this.posY - halfSize && mouseY <= this.posY + halfSize) {
                this.isDragging = true;
            } else {
                // Click anywhere else to set new position
                this.posX = mouseX;
                this.posY = mouseY;

            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.isDragging = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (this.isDragging && clickedMouseButton == 0) {
            // Update position while dragging
            this.posX = mouseX;
            this.posY = mouseY;
            ModConfig.getInstance().setInt(this.xConfigKey, this.posX);
            ModConfig.getInstance().setInt(this.yConfigKey, this.posY);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mouseX = org.lwjgl.input.Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - org.lwjgl.input.Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int wheelDelta = org.lwjgl.input.Mouse.getEventDWheel();

        if (wheelDelta != 0) {
            float scaleChange = wheelDelta > 0 ? 0.1f : -0.1f;
            this.scale = Math.max(0.1f, Math.min(5.0f, this.scale + scaleChange));
            ModConfig.getInstance().setFloat(this.scaleConfigKey, this.scale);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background
        this.drawDefaultBackground();

        // Draw title
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 20, 0xFFFFFF);

        // Draw instructions
        this.drawCenteredString(this.fontRendererObj, "Drag to Move", this.width / 2, 40, 0xCCCCCC);
        this.drawCenteredString(this.fontRendererObj, "Use mouse scroll to change scale", this.width / 2, 55, 0xCCCCCC);

        // Draw current coordinates and scale
        String coordText = "Position: X=" + this.posX + ", Y=" + this.posY + " | Scale: " + String.format("%.1f", this.scale);
        this.drawString(this.fontRendererObj, coordText, 10, this.height - 50, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
