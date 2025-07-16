package com.endermanpvp.endermanfault;

import com.google.common.base.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

@Mod(modid = main.MODID, version = main.VERSION)
public class main
{
    public static final String MODID = "endermanfault";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new HighlightSameEnchantClickListener());

        PlushRenderer plushRenderer = new PlushRenderer();
        plushRenderer.init();
        MinecraftForge.EVENT_BUS.register(plushRenderer);
        MinecraftForge.EVENT_BUS.register(new MouseInputHandler(plushRenderer));
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiContainer && !HighlightSameEnchantClickListener.highlightedSlots.isEmpty()) {
            GuiContainer container = (GuiContainer) event.gui;

            // Check if we're still in an anvil
            if (container.inventorySlots.getSlot(0).inventory.getName().equals("Anvil")) {
                Highlightrenderer.renderHighlightedSlots(container);
            }
        }
    }
}
