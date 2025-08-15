package com.endermanpvp.endermanfault.plush;

import com.endermanpvp.endermanfault.config.ModConfig;
import com.endermanpvp.endermanfault.main;
import com.google.common.base.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static com.endermanpvp.endermanfault.main.MODID;

public class PlushRenderer {
    private OBJModel fumoReimuModel;
    private IBakedModel bakedFumoReimuModel; // Use a direct instance variable
    private boolean isHoldingSqueeze = false;
    private double squeezeFactor = 0.0; // 0.0 = released, 1.0 = fully squeezed
    private long lastFrameTime = 0;
    private final double squeezeSpeed = 0.005; // units per millisecond

    public void startSqueezing() {
        this.isHoldingSqueeze = true;
    }

    public void stopSqueezing() {
        this.isHoldingSqueeze = false;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        // Step 2: Register our custom texture so it gets included in the master texture atlas.
        ResourceLocation fumoTexture = new ResourceLocation(MODID, "item/fumo_reimu");
        event.map.registerSprite(fumoTexture);
    }
    public void init()
    {
        // Load the OBJ model, but DO NOT bake it yet.
        try {
            // Step 1: Load the raw model data from the .obj file.
            OBJLoader.instance.addDomain(MODID);
            ResourceLocation objLocation = new ResourceLocation(MODID, "item/fumo_reimu.obj");
            this.fumoReimuModel = (OBJModel) ModelLoaderRegistry.getModel(objLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onModelBake(final ModelBakeEvent event) {
        if (this.fumoReimuModel == null) return;

        try {
            // Step 3: Bake the model and store it directly in our instance variable.
            this.bakedFumoReimuModel = this.fumoReimuModel.bake(this.fumoReimuModel.getDefaultState(), DefaultVertexFormats.ITEM, new Function<ResourceLocation, TextureAtlasSprite>() {
                public TextureAtlasSprite apply(ResourceLocation location) {
                    // Force the model to use the texture we registered in onTextureStitch.
                    return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("endermanfault:item/fumo_reimu");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if(!ModConfig.getInstance().getBoolean("toggle_fumo", false))return;

        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        // Step 4: Render the model directly from our instance variable.
        if (this.bakedFumoReimuModel == null) {
            return;
        }

        GlStateManager.pushMatrix();
        try {
            // Time-based animation logic
            long currentTime = System.currentTimeMillis();
            if (lastFrameTime == 0) {
                lastFrameTime = currentTime;
            }
            long deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            if (isHoldingSqueeze) {
                squeezeFactor = Math.min(1.0, squeezeFactor + squeezeSpeed * deltaTime);
            } else {
                squeezeFactor = Math.max(0.0, squeezeFactor - squeezeSpeed * deltaTime);
            }

            // Define a base size for the model. You can adjust this value.
            float baseScale = 100.0F;
            // Calculate a dynamic scale factor based on the screen's scaled height.
            // This makes the model resize along with the GUI.
            float dynamicScale = baseScale * (event.resolution.getScaledHeight() / 1920F) * ModConfig.getInstance().getFloat("plushScale", 1);


            // Calculate position based on config
            float posX = ModConfig.getInstance().getInt("plushXOffset", event.resolution.getScaledWidth() / 2);
            float posY = ModConfig.getInstance().getInt("plushYOffset", event.resolution.getScaledHeight() / 2);

            // Position the model based on config settings
            GlStateManager.translate(posX, posY, 100.0F);

            // Create a smooth "breathing" animation using a sine wave.
            // The animation speed is controlled by the divisor of currentTimeMillis.
            // The animation amplitude (how much it scales) is controlled by the multiplier of the sine result.
            double animationSpeed = 1000.0; // Smaller is faster, larger is slower.
            double animationAmplitude = 0.06; // The range of scaling (e.g., 0.1 means 10% scale up/down).
            double breathingScaleFactor = 1.0 + animationAmplitude * Math.sin(System.currentTimeMillis() / animationSpeed);

            double yScale = breathingScaleFactor;

            if (squeezeFactor > 0) {
                double maxSqueeze = 0.5; // Squeeze to 50% of original height
                yScale *= (1.0 - maxSqueeze * squeezeFactor); // Apply squeeze on top of breathing
            }

            GlStateManager.scale(dynamicScale, -dynamicScale * yScale, dynamicScale);
            float angle = (System.currentTimeMillis() % 8000L) / 8000.0F * 360.0F;
            GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);

            // Set up custom lighting instead of the standard GUI lighting.
            GlStateManager.enableLighting();
            GlStateManager.enableLight(0);

            // Define the light's properties.
            // The ambient light is a soft, non-directional light that hits all faces equally.
            // The diffuse light is a directional light that creates highlights and shadows.
            // The last value is alpha, which is usually 1.0.
            float[] lightAmbient = {0.8f, 0.8f, 0.8f, 1.0f};
            float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
            float[] lightPosition = {0.0f, 0.0f, 10.0f, 0.0f}; // Directional light from the front

            // Create FloatBuffers to pass to OpenGL.
            FloatBuffer lightAmbientBuffer = BufferUtils.createFloatBuffer(4).put(lightAmbient);
            FloatBuffer lightDiffuseBuffer = BufferUtils.createFloatBuffer(4).put(lightDiffuse);
            FloatBuffer lightPositionBuffer = BufferUtils.createFloatBuffer(4).put(lightPosition);
            lightAmbientBuffer.flip();
            lightDiffuseBuffer.flip();
            lightPositionBuffer.flip();

            // Set the light properties.
            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, lightAmbientBuffer);
            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDiffuseBuffer);
            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPositionBuffer);

            // Enable alpha blending to fix the white edges on the texture.
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            GlStateManager.enableTexture2D();
            GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

            // Render the model's vertices.
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

            java.util.List<BakedQuad> quads = this.bakedFumoReimuModel.getGeneralQuads();
            for (BakedQuad quad : quads) {
                worldRenderer.addVertexData(quad.getVertexData());
            }

            tessellator.draw();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always restore the graphics state.
            GlStateManager.disableBlend();
            GlStateManager.disableLight(0);
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();
        }
    }
}
