package org.teamavion.pcomp.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.teamavion.pcomp.PComp;
import org.teamavion.pcomp.container.ContainerComputer;
import org.teamavion.pcomp.tile.TileEntityComputer;
import org.teamavion.util.support.NetworkChannel;

import javax.annotation.Nullable;

import static org.teamavion.pcomp.PComp.ID_COMPUTER;

public class GUIHandler implements IGuiHandler{
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID){
            case ID_COMPUTER:
                TileEntity t = world.getTileEntity(new BlockPos(x, y, z));
                if(!(t instanceof TileEntityComputer)) return null; // Something went wrong. ID-clash maybe?
                NBTTagCompound n = new NBTTagCompound();
                t.writeToNBT(n);
                PComp.instance.channel.sendTo(new NetworkChannel.WorldEvent(new BlockPos(x, y, z), world.provider.getDimension(), n), (EntityPlayerMP) player);
                return new ContainerComputer((TileEntityComputer) t);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID){
            case ID_COMPUTER:
                TileEntity t = world.getTileEntity(new BlockPos(x, y, z));
                if(!(t instanceof TileEntityComputer)) return null; // Something went wrong. ID-clash maybe?
                return new GUIComputer((TileEntityComputer) t);
        }
        return null;
    }
}
