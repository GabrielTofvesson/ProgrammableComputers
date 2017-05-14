package org.teamavion.pcomp.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityComputer extends TileEntity{
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5,getPos().getZ() + 0.5) < 64;
    }
}
