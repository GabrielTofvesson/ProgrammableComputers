package org.teamavion.pcomp.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.teamavion.pcomp.PComp;
import org.teamavion.pcomp.tile.TileEntityComputer;

import javax.annotation.Nullable;

import static org.teamavion.pcomp.PComp.ID_COMPUTER;

public class Computer extends Block {
    public Computer(Material blockMaterialIn, MapColor blockMapColorIn) { super(blockMaterialIn, blockMapColorIn); }
    public Computer(Material materialIn) { super(materialIn); }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        playerIn.openGui(PComp.instance, ID_COMPUTER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityComputer();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}
