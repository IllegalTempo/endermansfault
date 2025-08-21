package com.endermanpvp.endermanfault.MiningShift;

import com.endermanpvp.endermanfault.PlayerStat;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class miningShift {
    public Dictionary<IBlockState,Integer> blockStrength = new Hashtable<>();
    public int ExtraTick = 0;

    //<editor-fold desc="Skyblock mineral Blocks">
    private IBlockState GreenMithril1 = Blocks.prismarine.getStateFromMeta(2); // Dark Prismarine
        private IBlockState GreenMithril2 = Blocks.prismarine.getStateFromMeta(1); // Prismarine Bricks
        private IBlockState GreenMithril3 = Blocks.prismarine.getStateFromMeta(0); // Regular Prismarine

        // 5x multiplier blocks
        private IBlockState BlueMithril = Blocks.wool.getStateFromMeta(3); // Light Blue

        // High strength blocks
        private IBlockState PolishedDiorite = Blocks.stone.getStateFromMeta(4); // Polished Diorite

        // Very high strength blocks (5600)
        private IBlockState Cobblestone = Blocks.cobblestone.getDefaultState();
        private IBlockState Clay = Blocks.clay.getDefaultState();
        private IBlockState HardenedClay = Blocks.hardened_clay.getDefaultState();
        private IBlockState BrownHardenedClay = Blocks.stained_hardened_clay.getStateFromMeta(12); // Brown
        private IBlockState RedSandstone = Blocks.red_sandstone.getDefaultState();

        // Packed Ice (6000)
        private IBlockState PackedIce = Blocks.packed_ice.getDefaultState();

        // Red Glass blocks (2300)
        private IBlockState RedGlass = Blocks.stained_glass.getStateFromMeta(14); // Red
        private IBlockState RedGlassPane = Blocks.stained_glass_pane.getStateFromMeta(14); // Red

        // Purple/Lime/Light Blue/Orange/White Glass blocks (3000)
        private IBlockState PurpleGlass = Blocks.stained_glass.getStateFromMeta(10); // Purple
        private IBlockState PurpleGlassPane = Blocks.stained_glass_pane.getStateFromMeta(10); // Purple
        private IBlockState LimeGlass = Blocks.stained_glass.getStateFromMeta(5); // Lime
        private IBlockState LimeGlassPane = Blocks.stained_glass_pane.getStateFromMeta(5); // Lime
        private IBlockState LightBlueGlass = Blocks.stained_glass.getStateFromMeta(3); // Light Blue
        private IBlockState LightBlueGlassPane = Blocks.stained_glass_pane.getStateFromMeta(3); // Light Blue
        private IBlockState OrangeGlass = Blocks.stained_glass.getStateFromMeta(1); // Orange
        private IBlockState OrangeGlassPane = Blocks.stained_glass_pane.getStateFromMeta(1); // Orange
        private IBlockState WhiteGlass = Blocks.stained_glass.getStateFromMeta(0); // White
        private IBlockState WhiteGlassPane = Blocks.stained_glass_pane.getStateFromMeta(0); // White

        // Yellow Glass blocks (3800)
        private IBlockState YellowGlass = Blocks.stained_glass.getStateFromMeta(4); // Yellow
        private IBlockState YellowGlassPane = Blocks.stained_glass_pane.getStateFromMeta(4); // Yellow

        // Pink Glass blocks (4800)
        private IBlockState PinkGlass = Blocks.stained_glass.getStateFromMeta(6); // Pink
        private IBlockState PinkGlassPane = Blocks.stained_glass_pane.getStateFromMeta(6); // Pink

        // Blue/Black/Brown/Green Glass blocks (5200)
        private IBlockState BlueGlass = Blocks.stained_glass.getStateFromMeta(11); // Blue
        private IBlockState BlueGlassPane = Blocks.stained_glass_pane.getStateFromMeta(11); // Blue
        private IBlockState BlackGlass = Blocks.stained_glass.getStateFromMeta(15); // Black
        private IBlockState BlackGlassPane = Blocks.stained_glass_pane.getStateFromMeta(15); // Black
        private IBlockState BrownGlass = Blocks.stained_glass.getStateFromMeta(12); // Brown
        private IBlockState BrownGlassPane = Blocks.stained_glass_pane.getStateFromMeta(12); // Brown
        private IBlockState GreenGlass = Blocks.stained_glass.getStateFromMeta(13); // Green
        private IBlockState GreenGlassPane = Blocks.stained_glass_pane.getStateFromMeta(13); // Green

        // Legacy block definitions (keeping for compatibility)
        private IBlockState GrayMithril1 = Blocks.stained_hardened_clay.getStateFromMeta(9);
        private IBlockState GrayMithril2 = Blocks.wool.getStateFromMeta(7);
    //</editor-fold>

    private BreakingBlock currentBreakingBlock = null;
    public miningShift()
    {
        // Legacy entries (keeping for compatibility)
        blockStrength.put(GrayMithril1, 500);
        blockStrength.put(GrayMithril2, 500);
        // Green Mithril blocks
        blockStrength.put(GreenMithril1, 800);
        blockStrength.put(GreenMithril2, 800);
        blockStrength.put(GreenMithril3, 800);
        // Blue Mithril block
        blockStrength.put(BlueMithril, 1500);
        // Polished Diorite block
        blockStrength.put(PolishedDiorite, 2000);

        // Red Glass blocks (2300)
        blockStrength.put(RedGlass, 2300);
        blockStrength.put(RedGlassPane, 2300);

        // Purple/Lime/Light Blue/Orange/White Glass blocks (3000)
        blockStrength.put(PurpleGlass, 3000);
        blockStrength.put(PurpleGlassPane, 3000);
        blockStrength.put(LimeGlass, 3000);
        blockStrength.put(LimeGlassPane, 3000);
        blockStrength.put(LightBlueGlass, 3000);
        blockStrength.put(LightBlueGlassPane, 3000);
        blockStrength.put(OrangeGlass, 3000);
        blockStrength.put(OrangeGlassPane, 3000);
        blockStrength.put(WhiteGlass, 3000);
        blockStrength.put(WhiteGlassPane, 3000);

        // Yellow Glass blocks (3800)
        blockStrength.put(YellowGlass, 3800);
        blockStrength.put(YellowGlassPane, 3800);

        // Pink Glass blocks (4800)
        blockStrength.put(PinkGlass, 4800);
        blockStrength.put(PinkGlassPane, 4800);

        // Blue/Black/Brown/Green Glass blocks (5200)
        blockStrength.put(BlueGlass, 5200);
        blockStrength.put(BlueGlassPane, 5200);
        blockStrength.put(BlackGlass, 5200);
        blockStrength.put(BlackGlassPane, 5200);
        blockStrength.put(BrownGlass, 5200);
        blockStrength.put(BrownGlassPane, 5200);
        blockStrength.put(GreenGlass, 5200);
        blockStrength.put(GreenGlassPane, 5200);

        // Very high strength blocks (5600)
        blockStrength.put(Cobblestone, 5600);
        blockStrength.put(Clay, 5600);
        blockStrength.put(HardenedClay, 5600);
        blockStrength.put(BrownHardenedClay, 5600);
        blockStrength.put(RedSandstone, 5600);

        // Packed Ice (6000)
        blockStrength.put(PackedIce, 6000);
    }


    //On Breaking Block Event
    @SubscribeEvent
    public void WhenBreaking(TickEvent.PlayerTickEvent event)
    {
        if(!event.player.equals(Minecraft.getMinecraft().thePlayer)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.gameSettings.keyBindAttack.isKeyDown()) {
            BlockPos currentPos = mc.objectMouseOver.getBlockPos();
            if(currentBreakingBlock == null||!currentPos.equals(currentBreakingBlock.pos))
            {

                BreakBlockSwitched(currentPos,mc.theWorld.getBlockState(currentPos));
            } else {

                currentBreakingBlock.BreakingBlock();
            }

        }

    }
    private void BreakBlockSwitched(BlockPos pos,IBlockState newblock)
    {
        currentBreakingBlock = new BreakingBlock(pos,newblock);
    }

    public int getTicksToBreak(IBlockState b) {

        Integer strength = blockStrength.get(b);
        if(strength == null) return 100;

        return Math.max((strength*30)/ PlayerStat.INSTANCE.MiningSpeed,4);

    }
    private class BreakingBlock
    {
        public BlockPos pos;
        public int ticksToBreak;
        public int tick = 0;
        public BreakingBlock(BlockPos p,IBlockState b) {
            this.pos = p;
            ticksToBreak = getTicksToBreak(b)*2;
        }
        public void BreakingBlock()
        {
            System.out.println("Breaking Block ttb:" + ticksToBreak + " tick: " + tick);

            tick++;
            if(tick > ticksToBreak + ExtraTick)
            {
                // Break the block on client side
                Minecraft mc = Minecraft.getMinecraft();
                World world = mc.theWorld;

                if (world != null && pos != null) {
                    // Get the block at the position
                    IBlockState blockState = world.getBlockState(pos);
                    Block block = blockState.getBlock();

                    // Only break if it's not air and the block still exists
                    if (block != Blocks.bedrock) {
                        // Break the block (set to bedrock instead of air)
                        world.setBlockState(pos, Blocks.bedrock.getDefaultState());
                        //world.setBlockToAir(pos);
                        // Play break sound effect
                        world.playAuxSFX(2001, pos, Block.getStateId(blockState));

                        // Spawn break particles
                        mc.effectRenderer.addBlockDestroyEffects(pos, blockState);

                        // Optional: Drop items if needed (commented out for client-side only)
                        // block.dropBlockAsItem(world, pos, blockState, 0);
                    }
                }

                currentBreakingBlock = null;
            }
        }
    }
}
