package org.teamavion.pcomp.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.teamavion.pcomp.net.DataListener;

import java.util.ArrayList;
import java.util.HashMap;

public class TileEntityComputer extends TileEntity{

    protected final HashMap<Integer, String> lines = new HashMap<>();
    protected final ArrayList<DataListener<HashMap<Integer, String>>> dataListeners = new ArrayList<>();

    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5,getPos().getZ() + 0.5) < 64;
    }

    public String readLine(int line){ return lines.keySet().contains(line)?lines.get(line):""; }
    public void writeLine(int line, String data){ lines.put(line, data); }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        int j = 0, k = 0;
        String s;
        for(Integer i : lines.keySet()) if(i>j) j = i;
        for(Integer i : lines.keySet()) if(i<k) k = i;
        for(Integer i : lines.keySet()) if((s=lines.get(i)).length()!=0) compound.setString("line_"+i, s);
        compound.setInteger("min", k);
        compound.setInteger("max", j);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(!compound.hasKey("max") || !compound.hasKey("min")) return; // Fail
        lines.clear();
        int min = compound.getInteger("min"), max = compound.getInteger("max");
        for(int i = min; (i-1<max) || (i == Integer.MIN_VALUE && max==Integer.MAX_VALUE); ++i) if(compound.hasKey("line_"+i)) lines.put(i, compound.getString("line_"+i));
        for(DataListener<HashMap<Integer, String>> d : dataListeners) d.getData(lines);
    }

    public void registerDataListener(DataListener<HashMap<Integer, String>> listener){ dataListeners.add(listener); listener.getData(lines); }
    public void unregisterDataListener(DataListener<HashMap<Integer, String>> listener){ dataListeners.remove(listener); }

}
