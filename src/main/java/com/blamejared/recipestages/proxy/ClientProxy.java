package com.blamejared.recipestages.proxy;

import com.blamejared.recipestages.config.Configuration;
import com.blamejared.recipestages.RecipeStages;
import com.blamejared.recipestages.events.ClientEventHandler;
import com.blamejared.recipestages.handlers.Recipes;
import mezz.jei.api.recipe.*;
import mezz.jei.collect.SetMultiMap;
import mezz.jei.recipes.RecipeRegistry;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.time.StopWatch;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.blamejared.recipestages.compat.JEIPlugin.recipeRegistry;

public class ClientProxy extends CommonProxy {
    private static Collection<String> stages;

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }
    
    @Override
    public void registerEvents() {
        super.registerEvents();
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }
    
    @Override
    public void syncJEI(EntityPlayer player) {
        super.syncJEI(player);
        try {
            if (Configuration.showRecipe || recipeRegistry == null || player == null
                    || GameStageHelper.getPlayerData(player) == null) return;
            StopWatch watch = new StopWatch(); watch.start();
            String guid = VanillaRecipeCategoryUid.CRAFTING;
            SetMultiMap<String, IRecipeWrapper> hiddenRecipes = ObfuscationReflectionHelper.getPrivateValue(
                    RecipeRegistry.class, (RecipeRegistry) recipeRegistry, "hiddenRecipes");
            Collection<String> current = GameStageSaveHandler.clientData.getStages();
            if (stages == null) {
                int removed = 0;
                for (String key : Recipes.recipes.keySet()) {
                    if (current.contains(key)) continue;
                    for (IRecipe recipe : Recipes.recipes.get(key)) {
                        IRecipeWrapper wrapper = recipeRegistry.getRecipeWrapper(recipe, guid);
                        if (wrapper == null) continue;
                        hiddenRecipes.put(guid, wrapper);
                    }
                    removed++;
                }

                RecipeStages.LOGGER.info("First sync, removed {} in {} ms.",
                        removed, watch.getTime(TimeUnit.MILLISECONDS));
                stages = current;
                return;
            }

            Collection<String> added = new ArrayList<>(current); added.removeAll(stages);
            Collection<String> removed = new ArrayList<>(stages); removed.removeAll(current);

            for (String stage : added)
                for (IRecipe recipe : Recipes.recipes.getOrDefault(stage, new ArrayList<>())) {
                    IRecipeWrapper wrapper = recipeRegistry.getRecipeWrapper(recipe, guid);
                    if (wrapper == null) continue;
                    hiddenRecipes.remove(guid, wrapper);
                }

            for (String stage : removed)
                for (IRecipe recipe : Recipes.recipes.getOrDefault(stage, new ArrayList<>())) {
                    IRecipeWrapper wrapper = recipeRegistry.getRecipeWrapper(recipe, guid);
                    if (wrapper == null) continue;
                    hiddenRecipes.put(guid, wrapper);
                }

            RecipeStages.LOGGER.info("Added {}, removed {} in {} ms.",
                    added.size(), removed.size(), watch.getTime(TimeUnit.MILLISECONDS));
            stages = current;
        } catch (Exception e) {
            RecipeStages.LOGGER.error("JEI syncing failed!", e);
        }
    }
}
