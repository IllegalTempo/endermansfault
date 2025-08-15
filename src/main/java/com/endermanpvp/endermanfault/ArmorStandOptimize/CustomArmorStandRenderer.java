package com.endermanpvp.endermanfault.ArmorStandOptimize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelArmorStand;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ResourceLocation;

public class CustomArmorStandRenderer extends ArmorStandRenderer {

    RenderManager rm;
    public CustomArmorStandRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityArmorStand entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // If the armor stand has any armor equipped, render the original way
        boolean hasAnyArmor = false;
        for (int i = 1; i <= 4; i++) {
            if (entity.getEquipmentInSlot(i) != null) {
                hasAnyArmor = true;
                break;
            }
        }
        if (!entity.hasCustomName() || hasAnyArmor || !entity.isInvisible()) {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);

        } else {
            if (!this.renderManager.isRenderShadow() && !entity.isInRangeToRender3d(x, y, z)) {
                return;
            }
            renderFloatingText(entity.getCustomNameTag(), x, y, z, entity.ticksExisted + partialTicks,entity.height);
        }

    }

    @Override
    protected ResourceLocation getEntityTexture(EntityArmorStand entity) {
        return null;
    }
    private static void renderFloatingText(String text, double x, double y, double z,float ticks, float height) {
        Minecraft mc = Minecraft.getMinecraft();
        float scale = 0.025F;
        float alpha = 1.0F; // Default full opacity

        float baseOffset = 2.5F;
        if(text.contains("✧"))
        {
            float animationTime = ticks / 20.0F; // Convert ticks to seconds

            if (animationTime < 1.0F) { // Animation lasts 1 second
                alpha = -4 * (float)Math.pow(animationTime-0.5,2) + 1;
                scale = 0.025F *(float)(Math.pow(animationTime,0.35)); // Scale from 1.0 to 2.5 (was 2.0)
            }

        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + height + 0.5, z);
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
