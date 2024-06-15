package com.blamejared.recipestages.recipes;

import com.blamejared.recipestages.RecipeStages;
import com.blamejared.recipestages.handlers.Recipes;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistryEntry;
import java.util.*;

public class RecipeStage extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final String tier;
    private final IRecipe recipe;
    private final boolean shapeless;
    private int width, height;
    
    public RecipeStage(String tier, IRecipe recipe, boolean shapeless) {
        this.tier = tier;
        this.recipe = recipe;
        this.shapeless = shapeless;
    }
    
    public RecipeStage(String tier, IRecipe recipe, boolean shapeless, int width, int height) {
        this.tier = tier;
        this.recipe = recipe;
        this.shapeless = shapeless;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return recipe.matches(inv, worldIn);
    }
    
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return isGoodForCrafting(inv) ? recipe.getCraftingResult(inv) : ItemStack.EMPTY;
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return recipe.canFit(width, height);
    }
    
    public boolean isGoodForCrafting(InventoryCrafting inv) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            EntityPlayer player = RecipeStages.proxy.getClientPlayer();
            return player == null || player instanceof FakePlayer
                    || GameStageHelper.getPlayerData(player).hasStage(tier);
        }

        // I don't feel guilty for doing this
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            Container container = inv.eventHandler;
            if (container == null) return false;
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if (player.openContainer != container
                        || !container.canInteractWith(player)
                        || !container.getCanCraft(player)) continue;
                return GameStageHelper.getPlayerData(player).hasStage(tier);
            }
        }

        String[] stages = Recipes.containerStages.getOrDefault(inv.eventHandler.getClass().getName(), new String[0]);
        if (Arrays.stream(stages).anyMatch(x -> x.equalsIgnoreCase(tier))) return true;

        for (Map.Entry<String, String[]> entry : Recipes.packageStages.entrySet()) {
            String pack = entry.getKey().toLowerCase(); stages = entry.getValue();
            if (inv.eventHandler.getClass().getName().toLowerCase().startsWith(pack)
                && Arrays.stream(stages).anyMatch(x -> x.equalsIgnoreCase(tier)))
                return true;
        }

        return false;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return recipe.getRecipeOutput();
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return recipe.getRemainingItems(inv);
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @Override
    public String toString() {
        return String.format("RecipeStage{tier='%s',output='%s',ingredients='%s'}",
                tier, recipe.getRecipeOutput(), recipe.getIngredients());
    }
    
    public IRecipe getRecipe() {
        return recipe;
    }
    
    public String getTier() {
        return tier;
    }
    
    public boolean isShapeless() {
        return shapeless;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}