package org.teamavion.pcomp;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.teamavion.pcomp.blocks.Computer;
import org.teamavion.pcomp.gui.GUIHandler;
import org.teamavion.pcomp.tile.TileEntityComputer;
import org.teamavion.util.automation.BlockRegister;
import org.teamavion.util.automation.SetupHelper;
import org.teamavion.util.support.NetworkChannel;
import org.teamavion.util.support.Reflection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = PComp.MODID, version = PComp.VERSION)
public class PComp
{
    public static final String MODID = "pcomp";
    public static final String VERSION = "1.0";

    public static final int ID_COMPUTER = 0x10101010; // Eksdee
    public static @BlockRegister(name="Computer", material = "IRON") Computer computerBlock;
    public static @Mod.Instance PComp instance;

    public NetworkChannel channel;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        SetupHelper.setup(PComp.class);
        SetupHelper.registerRenders(PComp.class);
        channel = new NetworkChannel(MODID);
        channel.registerWorldHandler((side, worldEvent) -> {
            World w = (World) Reflection.getValue("world", DimensionManager.getProvider((Integer) Reflection.getValue("id", worldEvent, worldEvent.getClass())), WorldProvider.class); // Get World
            TileEntity t = w.getTileEntity(worldEvent.getPos());
            try {
                if (t != null) {
                    Matcher m = Pattern.compile("(\\d+):(\\d+):(\\d+);(\\d+);(.+)").matcher(worldEvent.getData());
                    if(!m.matches()) throw new RuntimeException();
                    String s1;
                    NBTTagCompound n = JsonToNBT.getTagFromJson((s1=m.group(5)).substring(8, s1.length()-1)
                            .replace("&rbr;", "}")
                            .replace("&lbr;", "{")
                            .replace("&quot;", "\"")
                            .replace("&amp;", "&"));
                    t.readFromNBT(n);
                }
            } catch (NBTException e) {
                e.printStackTrace();
            }
            return null;
        });
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GUIHandler());
        GameRegistry.registerTileEntity(TileEntityComputer.class, "computer");
    }

}