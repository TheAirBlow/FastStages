package com.blamejared.recipestages.compat;

import com.blamejared.recipestages.recipes.RecipeStage;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.*;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {
    public static IRecipeRegistry recipeRegistry;
    public static IJeiHelpers jeiHelpers;
    public static IIngredientRegistry ingredientRegistry;
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
    }
    
    @Override
    public void register(IModRegistry registry) {
        ingredientRegistry = registry.getIngredientRegistry();
        registry.handleRecipes(RecipeStage.class, new StagedRecipeFactory(), VanillaRecipeCategoryUid.CRAFTING);
    }
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeRegistry = jeiRuntime.getRecipeRegistry();
    }
}