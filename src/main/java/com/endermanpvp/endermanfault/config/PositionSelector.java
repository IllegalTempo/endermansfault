package com.endermanpvp.endermanfault.config;

import com.endermanpvp.endermanfault.DataType.Dimension;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public class PositionSelector extends GuiScreen {
    private final GuiScreen parent;
    private final String title;
    private final Dimension dim;
    private final int defaultX;
    private final int defaultY;
    private final float defaultScale;
    private boolean isDragging = false;



    public PositionSelector(GuiScreen parent, String title, String mainKey, Integer defaultX, Integer defaultY, float defaultScale) {
        this.parent = parent;
        this.title = title;

        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.defaultScale = defaultScale;
        // Load current position and scale from config
        this.dim = AllConfig.INSTANCE.DimensionConfig.get(mainKey);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        // Add done button
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height - 30, 100, 20, "Done"));


        // Update position based on current screen size if needed
        if (dim.x <= 0) dim.x = this.defaultX > 0 ? this.defaultX : this.width / 2;
        if (dim.y <= 0) dim.y = this.defaultY > 0 ? this.defaultY : this.height / 2;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) { // Left click
            // Calculate scaled size for click detection
            int scaledSize = (int)(50 * dim.Scale);
            int halfSize = scaledSize / 2;
            // Check if clicking on position area (scaled area)
            if (mouseX >= dim.x - halfSize && mouseX <= dim.x + halfSize &&
                mouseY >= dim.y - halfSize && mouseY <= dim.y + halfSize) {
                this.isDragging = true;
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
            dim.x = mouseX;
            dim.y = mouseY;
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
            dim.Scale = Math.max(0.1f, Math.min(5.0f, dim.Scale + scaleChange));
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
        String coordText = "Position: X=" + dim.x + ", Y=" + dim.y + " | Scale: " + String.format("%.1f", dim.Scale);
        this.drawString(this.fontRendererObj, coordText, 10, this.height - 50, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
