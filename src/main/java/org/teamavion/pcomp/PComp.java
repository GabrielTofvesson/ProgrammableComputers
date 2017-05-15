package org.teamavion.pcomp;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.teamavion.pcomp.blocks.Computer;
import org.teamavion.pcomp.gui.GUIHandler;
import org.teamavion.pcomp.tile.TileEntityComputer;
import org.teamavion.util.automation.BlockRegister;
import org.teamavion.util.automation.SetupHelper;
import org.teamavion.util.support.NetworkChannel;
import org.teamavion.util.support.Reflection;

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
            World w = (World) (side==Side.SERVER?
                    Reflection.getValue("world", DimensionManager.getProvider((Integer) Reflection.getValue("id", worldEvent, worldEvent.getClass())), WorldProvider.class):
                    Reflection.getValue("world", Minecraft.getMinecraft(), Minecraft.class)); // Get World
            TileEntity t = w.getTileEntity(worldEvent.getPos());
            if (t == null){
                System.err.println("A referenced TileEntity ("+worldEvent.getPos().toString()+") was null!");
                return null;
            }
            t.readFromNBT(worldEvent.getEvent());
            t.markDirty();
            if(worldEvent.getEvent().hasKey("update")) return new NetworkChannel.WorldEvent(t.getPos(), t.getWorld().provider.getDimension(), t.serializeNBT());
            if(worldEvent.getEvent().hasKey("exec")) if(t instanceof TileEntityComputer) ((TileEntityComputer) t).exec();
            return null;
        });
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GUIHandler());
        GameRegistry.registerTileEntity(TileEntityComputer.class, "computer");
    }

}