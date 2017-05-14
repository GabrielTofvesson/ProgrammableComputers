package org.teamavion.pcomp.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.teamavion.pcomp.tile.TileEntityComputer;

public class ContainerComputer extends Container{

    protected final TileEntityComputer computer;

    public ContainerComputer(TileEntityComputer computer){ this.computer = computer; }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return computer.getWorld().getTileEntity(computer.getPos()) == computer &&
                playerIn.getDistanceSq(computer.getPos().getX() + 0.5, computer.getPos().getY() + 0.5, computer.getPos().getZ() + 0.5) < 64;
    }
}
