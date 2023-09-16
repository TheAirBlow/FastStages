package net.theairblow.faststages;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

@Mod(modid = FastStages.MOD_ID, name = FastStages.MOD_NAME, version = FastStages.VERSION)
public class FastStages implements ILateMixinLoader {
    public static final String MOD_ID = "faststages";
    public static final String MOD_NAME = "FastStages";
    public static final String VERSION = "1.0.0";
    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
    }

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("mixins.json");
    }
}
