package com.blamejared.recipestages.compat;

import com.blamejared.recipestages.config.Configuration;
import com.blamejared.recipestages.recipes.RecipeStage;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StagedRecipeWrapperShaped extends ShapelessRecipeWrapper<RecipeStage> implements IShapedCraftingRecipeWrapper {
    private final RecipeStage recipe;
    
    public StagedRecipeWrapperShaped(IJeiHelpers jeiHelpers, RecipeStage recipe) {
        super(jeiHelpers, recipe);
        this.recipe = recipe;
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (!Configuration.showStageName || mouseX < 60 || mouseX > 82 || mouseY < 18 || mouseY > 32)
            return Collections.emptyList();
        return Arrays.asList(
                I18n.format("gui.recipestages.tip.line1"),
                I18n.format("gui.recipestages.tip.line2", recipe.getTier())
        );
    }
    
    public RecipeStage getRecipe() {
        return recipe;
    }
    
    @Override
    public int getWidth() {
        return recipe.getWidth();
    }
    
    @Override
    public int getHeight() {
        return recipe.getHeight();
    }
}
