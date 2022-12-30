package net.theairblow.geolosyshax;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.theairblow.capatible.Capatible;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@Mod(modid = GeolosysHax.MOD_ID, name = GeolosysHax.MOD_NAME, version = GeolosysHax.VERSION)
public class GeolosysHax {
    public static final String MOD_ID = "geolosyshax";
    public static final String MOD_NAME = "GeolosysHax";
    public static final String VERSION = "1.1.1";
    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Capatible.handleMod(MOD_ID);
        LOGGER = event.getModLog();
    }
}
