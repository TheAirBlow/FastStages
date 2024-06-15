package com.blamejared.recipestages;

import com.blamejared.recipestages.proxy.CommonProxy;
import crafttweaker.*;
import net.darkhax.bookshelf.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.*;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod(modid = "recipestages", name = "FastStages", version = "2.0.0",
     dependencies = "required-after:crafttweaker@[4.1.20,);after:gamestages@[2.0.90,)")
public class RecipeStages {
    public static final List<IAction> LATE_ADDITIONS = new LinkedList<>();
    public static final List<IAction> LATE_REMOVALS = new LinkedList<>();
    @SidedProxy(
            clientSide = "com.blamejared.recipestages.proxy.ClientProxy",
            serverSide = "com.blamejared.recipestages.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static Logger LOGGER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerEvents();
    }
    
    @EventHandler
    @SideOnly(Side.CLIENT)
    public void onFMLLoadComplete(FMLLoadCompleteEvent event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(listener -> {
            if(Loader.isModLoaded("jei") && GameUtils.isClient()) {
                proxy.syncJEI(Minecraft.getMinecraft().player);
            }
        });
    }
}
