package org.teamavion.pcomp;

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
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GUIHandler());
        GameRegistry.registerTileEntity(TileEntityComputer.class, "computer");
    }

}