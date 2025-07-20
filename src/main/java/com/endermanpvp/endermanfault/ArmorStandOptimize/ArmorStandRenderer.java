package com.endermanpvp.endermanfault.ArmorStandOptimize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ResourceLocation;

public class ArmorStandRenderer extends Render<EntityArmorStand> {
    public ArmorStandRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityArmorStand entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if(!entity.hasCustomName() || entity.getEquipmentInSlot(4) != null || !entity.isInvisible()) {

            super.doRender(entity,x,y,z,entityYaw,partialTicks);
        } else {
            if (!this.renderManager.isRenderShadow() && !entity.isInRangeToRender3d(x, y, z)) {
                return;
            }
            renderFloatingText(entity.getCustomNameTag(),x,y,z,entity.ticksExisted + partialTicks);

        }

    }

    @Override
    protected ResourceLocation getEntityTexture(EntityArmorStand entity) {
        return null;
    }
    private static void renderFloatingText(String text, double x, double y, double z,float ticks) {
        Minecraft mc = Minecraft.getMinecraft();
        float scale = 0.025F;
        float alpha = 1.0F; // Default full opacity

        float baseOffset = 2.5F;
        if (text.contains("❤")) {
            baseOffset = 0.5F; // Small armor stands are roughly half the height
        } else
        if(text.contains("✧"))
        {
            float animationTime = ticks / 20.0F; // Convert ticks to seconds

            if (animationTime < 1.0F) { // Animation lasts 1 second
                alpha = -4 * (float)Math.pow(animationTime-0.5,2) + 1;
                scale = 0.025F *(float)(Math.pow(animationTime,0.35)); // Scale from 1.0 to 2.5 (was 2.0)
            }

        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + baseOffset, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int textWidth = mc.fontRendererObj.getStringWidth(text);
        int color = 0xFFFFFF | ((int)(alpha * 255) << 24); // White with alpha

        mc.fontRendererObj.drawString(text, -textWidth / 2, 0, color);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
