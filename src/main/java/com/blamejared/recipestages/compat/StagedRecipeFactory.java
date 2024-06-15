package com.blamejared.recipestages.compat;

import com.blamejared.recipestages.recipes.RecipeStage;
import mezz.jei.api.recipe.*;

import javax.annotation.ParametersAreNonnullByDefault;

public class StagedRecipeFactory implements IRecipeWrapperFactory<RecipeStage> {
    @Override @ParametersAreNonnullByDefault
    public IRecipeWrapper getRecipeWrapper(RecipeStage recipe) {
        return recipe.isShapeless()
                ? new StagedRecipeWrapperShapeless(JEIPlugin.jeiHelpers, recipe)
                : new StagedRecipeWrapperShaped(JEIPlugin.jeiHelpers, recipe);
    }
}
