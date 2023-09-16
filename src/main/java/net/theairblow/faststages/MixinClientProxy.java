package net.theairblow.faststages;

import com.blamejared.recipestages.handlers.Recipes;
import com.blamejared.recipestages.proxy.ClientProxy;
import com.blamejared.recipestages.proxy.CommonProxy;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.time.StopWatch;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.blamejared.recipestages.compat.JEIPlugin.recipeRegistry;

@Mixin(ClientProxy.class)
public class MixinClientProxy extends CommonProxy {
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);
    private static Collection<String> stages = new ArrayList<>();

    @Override
    public void syncJEI(EntityPlayer player) {
        executor.execute(() -> {
            final Minecraft minecraft = Minecraft.getMinecraft();
            if (recipeRegistry == null || player == null ||
                    GameStageHelper.getPlayerData(player) == null) return;

            StopWatch watch = new StopWatch(); watch.start();
            Collection<String> current = GameStageSaveHandler.clientData.getStages();
            Collection<String> added = new ArrayList<>(current); added.removeAll(stages);
            Collection<String> removed = new ArrayList<>(stages); removed.removeAll(current);
            String guid = VanillaRecipeCategoryUid.CRAFTING;
            for (String stage : added)
                for (IRecipe recipe : Recipes.recipes.get(stage)) {
                    IRecipeWrapper wrapper = recipeRegistry.getRecipeWrapper(recipe, guid);
                    if (wrapper == null) continue;
                    recipeRegistry.unhideRecipe(wrapper, guid);
                }

            for (String stage : removed)
                for (IRecipe recipe : Recipes.recipes.get(stage)) {
                    IRecipeWrapper wrapper = recipeRegistry.getRecipeWrapper(recipe, guid);
                    if (wrapper == null) continue;
                    recipeRegistry.hideRecipe(wrapper, guid);
                }

            minecraft.addScheduledTask(() -> player.sendStatusMessage(new TextComponentString(
                    "FastStages: Added " + added.size() + ", removed " + removed.size() +
                    " in " + watch.getTime(TimeUnit.MILLISECONDS) + " ms."), true));
            stages = current;
        });
    }
}
