package org.teamavion.pcomp.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.teamavion.pcomp.net.DataListener;
import org.teamavion.util.support.Reflection;
import org.teamavion.util.support.Result;

import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class TileEntityComputer extends TileEntity {

    protected final HashMap<Integer, String> lines = new HashMap<>();
    protected final ArrayList<DataListener<HashMap<Integer, String>>> dataListeners = new ArrayList<>();

    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5,getPos().getZ() + 0.5) < 64;
    }

    public String readLine(int line){ return lines.keySet().contains(line)?lines.get(line):""; }
    public void writeLine(int line, String data){ lines.put(line, data); }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound t = new NBTTagCompound();
        t = writeToNBT(t);
        return new SPacketUpdateTileEntity(pos, 0, t);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }

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
    public NBTTagCompound serializeNBT() {
        NBTTagCompound n = new NBTTagCompound();
        writeToNBT(n);
        return n;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        readFromNBT(nbt);
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

    public void exec(){
        try{
            StringBuilder sb = new StringBuilder();
            File f = File.createTempFile("exec", ".java");
            ArrayList<Integer> skip = new ArrayList<>();
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for(Integer key : lines.keySet()){
                if(min>key) min = key;
                if(max<key) max = key;
            }
            String s;
            for(int i = min; i<max+1; ++i) if(lines.containsKey(i) && (s=lines.get(i)).startsWith("import ") && s.endsWith(";")) { skip.add(i); sb.append(s); }
            sb.append("public class ").append(f.getName().substring(0, f.getName().length() - 5)).append("{public static void main(String[] args){");
            for(int i = min; i<max+1; ++i) if(!skip.contains(i) && (s=lines.get(i))!=null) sb.append(s);
            sb.append("}}");
            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            try (OutputStream out = new FileOutputStream(f)) { out.write(sb.toString().getBytes()); }
            int result = jc.run(null, null, null, f.getAbsolutePath());
            if(result==0){
                //noinspection ResultOfMethodCallIgnored
                f.delete();
                f = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-5)+".class");
                byte[] b;
                try{
                    InputStream i = new FileInputStream(f);
                    ArrayList<Byte> a = new ArrayList<>();
                    byte[] b1 = new byte[4096];
                    int i1;
                    while(i.available()>0){
                        i1 = i.read(b1);
                        for(int j = 0; j<i1; ++j) a.add(b1[j]);
                    }
                    i.close();
                    b = new byte[a.size()];
                    for(int j = 0; j<a.size(); ++j) b[j] = a.get(j);
                }catch(Exception e){ return; }
                finally {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
                Result<Class<?>> compiled = (Result<Class<?>>) Reflection.invokeMethod(
                        Reflection.getMethod(ClassLoader.class, "defineClass", byte[].class, int.class, int.class),
                        TileEntityComputer.class.getClassLoader(),
                        b,
                        0,
                        b.length);
                if(compiled.success){
                    for(Method m : compiled.value.getDeclaredMethods())
                        if(m.getName().equals("main") && Modifier.isStatic(m.getModifiers()) && Arrays.equals(m.getParameterTypes(), new Class<?>[]{String[].class}))
                        {
                            m.setAccessible(true);
                            try {
                                m.invoke(null, new Object[]{new String[]{}});
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                }
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    public void registerDataListener(DataListener<HashMap<Integer, String>> listener){ dataListeners.add(listener); listener.getData(lines); }
    public void unregisterDataListener(DataListener<HashMap<Integer, String>> listener){ dataListeners.remove(listener); }

}
