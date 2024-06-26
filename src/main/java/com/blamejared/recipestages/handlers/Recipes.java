package com.blamejared.recipestages.handlers;
import com.blamejared.recipestages.RecipeStages;
import com.blamejared.recipestages.recipes.RecipeStage;
import crafttweaker.*;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.*;
import crafttweaker.api.recipes.*;
import crafttweaker.mc1120.item.MCItemStack;
import crafttweaker.mc1120.recipes.*;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.*;

import java.util.*;
import java.util.regex.*;

@ZenClass("mods.recipestages.Recipes")
@ZenRegister
public class Recipes {
    public static Map<String, List<IRecipe>> recipes = new LinkedHashMap<>();
    private static ActionSetOutputStages actionSetOutputStages;
    private static ActionSetRegexStages actionSetRegexStages;
    private static ActionSetNameStages actionSetNameStages;
    private static ActionSetModIdStages actionSetModIdStages;
    private static final TIntSet usedHashes = new TIntHashSet();
    private static final HashSet<String> usedRecipeNames = new HashSet<>();
    public static Map<String, String[]> containerStages = new HashMap<>();
    public static Map<String, String[]> packageStages = new HashMap<>();
    
    @ZenMethod
    public static void setContainerStage(String container, String[] stage) {
        CraftTweakerAPI.apply(new ActionSetContainerStages(container, stage));
    }
    
    @ZenMethod
    public static void setPackageStage(String pack, String[] stage) {
        CraftTweakerAPI.apply(new ActionSetPackageStages(pack, stage));
    }
    
    @ZenMethod
    public static void addShaped(String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        RecipeStages.LATE_ADDITIONS.add(new ActionAddShapedRecipe(stage, output, new MCRecipeShaped(ingredients, output, function, action, false, false)));
    }
    
    @ZenMethod
    public static void addShaped(String name, String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        RecipeStages.LATE_ADDITIONS.add(new ActionAddShapedRecipe(stage, name, output, new MCRecipeShaped(ingredients, output, function, action, false, false)));
    }
    
    @ZenMethod
    public static void addShapedMirrored(String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        RecipeStages.LATE_ADDITIONS.add(new ActionAddShapedRecipe(stage, output, new MCRecipeShaped(ingredients, output, function, action, true, false)));
    }
    
    @ZenMethod
    public static void addShapedMirrored(String name, String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        RecipeStages.LATE_ADDITIONS.add(new ActionAddShapedRecipe(stage, name, output, new MCRecipeShaped(ingredients, output, function, action, true, false)));
    }
    
    @ZenMethod
    public static void addShapeless(String stage, IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        if (output == null || Arrays.stream(ingredients).anyMatch(Objects::isNull)) {
            CraftTweakerAPI.logError(String.format("Shapeless recipe %s was not crated because either the output or the ingredients had null", output));
            return;
        }

        RecipeStages.LATE_ADDITIONS.add(new ActionAddShapelessRecipe(stage, output, ingredients, function, action));
    }
    
    @ZenMethod
    public static void addShapeless(String name, String stage, IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        if (output == null || Arrays.stream(ingredients).anyMatch(Objects::isNull)) {
            CraftTweakerAPI.logError(String.format("Shapeless recipe %s was not crated because either the output or the ingredients had null", output));
            return;
        }

        RecipeStages.LATE_ADDITIONS.add(new ActionAddShapelessRecipe(stage, name, output, ingredients, function, action, false));
    }
    
    @ZenMethod
    public static void setRecipeStage(String stage, IIngredient output) {
        if (actionSetOutputStages == null) {
            actionSetOutputStages = new ActionSetOutputStages();
            RecipeStages.LATE_REMOVALS.add(actionSetOutputStages);
        }

        actionSetOutputStages.addOutput(stage, output);
    }
    
    @ZenMethod
    public static void setRecipeStage(String stage, String recipeName) {
        if (actionSetNameStages == null) {
            actionSetNameStages = new ActionSetNameStages();
            RecipeStages.LATE_REMOVALS.add(actionSetNameStages);
        }

        actionSetNameStages.addName(stage, recipeName);
    }
    
    @ZenMethod
    public static void setRecipeStageByRegex(String stage, String regex) {
        if (actionSetRegexStages == null) {
            actionSetRegexStages = new ActionSetRegexStages();
            RecipeStages.LATE_REMOVALS.add(actionSetRegexStages);
        }

        actionSetRegexStages.addRegex(stage, regex);
    }
    
    @ZenMethod
    public static void setRecipeStageByMod(String stage, String modId) {
        if (actionSetModIdStages == null) {
            actionSetModIdStages = new ActionSetModIdStages();
            RecipeStages.LATE_REMOVALS.add(actionSetModIdStages);
        }

        actionSetModIdStages.addModId(stage, modId);
    }

    private static void replaceRecipe(String stage, IRecipe iRecipe) {
        ResourceLocation registryName = iRecipe.getRegistryName();
        if (registryName == null) return;

        int width = 0, height = 0;
        if (iRecipe instanceof IShapedRecipe) {
            width = ((IShapedRecipe) iRecipe).getRecipeWidth();
            height = ((IShapedRecipe) iRecipe).getRecipeHeight();
        } else if (iRecipe instanceof RecipeStage && !((RecipeStage) iRecipe).isShapeless()) {
            width = ((RecipeStage) iRecipe).getWidth();
            height = ((RecipeStage) iRecipe).getHeight();
        }

        boolean shapeless = (width == 0 && height == 0);
        IRecipe recipe = new RecipeStage(stage, iRecipe, shapeless, width, height);
        setRecipeRegistryName(recipe, registryName);
        ForgeRegistries.RECIPES.register(recipe);

        List<IRecipe> list = recipes.getOrDefault(stage, new LinkedList<>());
        list.add(recipe); recipes.put(stage, list);

        if (iRecipe instanceof MCRecipeBase)
            MCRecipeManager.recipesToAdd.removeIf(baseAddRecipe -> baseAddRecipe.getRecipe() == iRecipe);

        CraftTweakerAPI.logInfo(String.format("Replacing recipe %s with %s", iRecipe.getRecipeOutput(), recipe));
    }

    private static void setRecipeRegistryName(IRecipe recipe, ResourceLocation registryName) {
        Loader loader = Loader.instance();
        ModContainer activeModContainer = loader.activeModContainer();
        ModContainer modContainer = loader.getIndexedModList().get(registryName.getNamespace());
        if (modContainer != null)
            loader.setActiveModContainer(modContainer);
        recipe.setRegistryName(registryName);
        loader.setActiveModContainer(activeModContainer);
    }

    public static String cleanRecipeName(String s) {
        if (s.contains(":"))
            CraftTweakerAPI.logWarning("Recipe names should not contain \":\"");
        return s.replace(":", "_");
    }
    
    private static class ActionSetContainerStages implements IAction {
        private final String container;
        private final String[] stages;
        
        public ActionSetContainerStages(String container, String[] stages) {
            this.container = container; this.stages = stages;
        }
        
        @Override
        public void apply() {
            containerStages.put(container, stages);
        }
        
        @Override
        public String describe() {
            return String.format("Set container %s stages to %s", container, String.join(", ", stages));
        }
    }
    
    private static class ActionSetPackageStages implements IAction {
        private final String container;
        private final String[] stages;
        
        public ActionSetPackageStages(String container, String[] stages) {
            this.container = container; this.stages = stages;
        }
        
        @Override
        public void apply() {
            packageStages.put(container, stages);
        }
        
        @Override
        public String describe() {
            return String.format("Set package %s stages to %s", container, String.join(", ", stages));
        }
    }
    
    private static class ActionSetStage implements IAction {
        private final List<IRecipe> recipes;
        private final String stage;
        
        public ActionSetStage(List<IRecipe> recipe, String stage) {
            this.recipes = recipe;
            this.stage = stage;
        }
        
        @Override
        public void apply() {
            recipes.forEach(i -> replaceRecipe(stage, i));
        }
        
        @Override
        public String describe() {
            return String.format("Set output stack %s stages to %s", recipes.get(0).getRecipeOutput().getDisplayName(), stage);
        }
    }
    
    private static class ActionSetOutputStages implements IAction {
        private final Map<String, List<IItemStack>> outputs = new HashMap<>();
        
        public void addOutput(String stage, IIngredient output) {
            List<IItemStack> outputsForStage = this.outputs.computeIfAbsent(stage, k -> new ArrayList<>());
            outputsForStage.addAll(output.getItems());
            CraftTweakerAPI.logInfo("Adding: " + output + " to stage: \"" + stage + "\"");
        }
        
        @Override
        public void apply() {
            for (IRecipe recipe : new ArrayList<>(ForgeRegistries.RECIPES.getValuesCollection())) {
                IItemStack stack = MCItemStack.createNonCopy(recipe.getRecipeOutput());
                if (stack == null) continue;
                for (Map.Entry<String, List<IItemStack>> entry : outputs.entrySet())
                    for (IItemStack output : entry.getValue()) {
                        if (!output.matches(stack)) continue;
                        replaceRecipe(entry.getKey(), recipe);
                    }
            }
            actionSetOutputStages = null;
        }
        
        @Override
        public String describe() {
            return String.format("Set stages for output stack (%s stages total)", outputs.size());
        }
    }
    
    private static class ActionSetRegexStages implements IAction {
        private final Map<String, List<String>> outputs = new HashMap<>();
        
        public void addRegex(String stage, String regex) {
            List<String> outputsForStage = this.outputs.computeIfAbsent(stage, k -> new ArrayList<>());
            outputsForStage.add(regex);
        }
        
        @Override
        public void apply() {
            for (Map.Entry<ResourceLocation, IRecipe> ent : new HashSet<>(ForgeRegistries.RECIPES.getEntries()))
                for (Map.Entry<String, List<String>> entry : outputs.entrySet())
                    for (String regex : entry.getValue()) {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher m = pattern.matcher(ent.getKey().toString());
                        if (!m.matches()) continue;
                        IRecipe recipe = ent.getValue();
                        new ActionSetStage(Collections.singletonList(recipe), entry.getKey()).apply();
                    }
            actionSetRegexStages = null;
        }
        
        @Override
        public String describe() {
            return String.format("Set stages for output stack by regex (%s stages total)", outputs.size());
        }
    }
    
    private static class ActionSetNameStages implements IAction {
        private final Map<String, List<String>> outputs = new HashMap<>();
        
        public void addName(String stage, String name) {
            List<String> outputsForStage = this.outputs.computeIfAbsent(stage, k -> new ArrayList<>());
            outputsForStage.add(name);
        }
        
        @Override
        public void apply() {
            for (Map.Entry<ResourceLocation, IRecipe> ent : new HashSet<>(ForgeRegistries.RECIPES.getEntries()))
                for (Map.Entry<String, List<String>> entry : outputs.entrySet())
                    for (String s : entry.getValue()) {
                        if (!s.equalsIgnoreCase(ent.getKey().toString())) continue;
                        IRecipe recipe = ent.getValue();
                        new ActionSetStage(Collections.singletonList(recipe), entry.getKey()).apply();
                    }
            actionSetNameStages = null;
        }
        
        @Override
        public String describe() {
            return String.format("Set stages for output stack by name (%s stages total)", outputs.size());
        }
    }
    
    private static class ActionSetModIdStages implements IAction {
        private final Map<String, List<String>> outputs = new HashMap<>();
        
        public void addModId(String stage, String modId) {
            List<String> outputsForStage = this.outputs.computeIfAbsent(stage, k -> new ArrayList<>());
            outputsForStage.add(modId);
        }
        
        @Override
        public void apply() {
            for(Map.Entry<ResourceLocation, IRecipe> ent : new HashSet<>(ForgeRegistries.RECIPES.getEntries()))
                for (Map.Entry<String, List<String>> entry : outputs.entrySet())
                    for (String s : entry.getValue()) {
                        if (!s.equalsIgnoreCase(ent.getKey().getNamespace())) continue;
                        IRecipe recipe = ent.getValue();
                        new ActionSetStage(Collections.singletonList(recipe), entry.getKey()).apply();
                    }
            actionSetNameStages = null;
        }
        
        @Override
        public String describe() {
            return String.format("Set stages for output stack by mod (%s stages total)", outputs.size());
        }
    }
    
    private static class ActionAddShapedRecipe extends ActionBaseAddRecipe {
        public ActionAddShapedRecipe(String stage, IItemStack output, MCRecipeShaped recipe) {
            this(stage, null, output, recipe);
        }
        
        public ActionAddShapedRecipe(String stage, String name, IItemStack output, MCRecipeShaped recipe) {
            super(new RecipeStage(stage, recipe, false, recipe.getRecipeWidth(), recipe.getRecipeHeight()), output, true, recipe.hasTransformers());
            setName(name);
        }
    }
    
    private static class ActionAddShapelessRecipe extends ActionBaseAddRecipe {
        public ActionAddShapelessRecipe(String stage, IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
            this(stage, null, output, ingredients, function, action, false);
        }
        
        public ActionAddShapelessRecipe(String stage, String name, IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action, boolean hidden) {
            super(new RecipeStage(stage, new MCRecipeShapeless(ingredients, output, function, action, hidden), true), output, false, new MCRecipeShapeless(ingredients, output, function, action, hidden).hasTransformers());
            setName(name);
        }
    }
    
    public static class ActionBaseAddRecipe implements IAction {
        protected RecipeStage recipe;
        protected IItemStack output;
        protected boolean isShaped;
        protected String name;
        
        @Deprecated
        public ActionBaseAddRecipe() { }
        
        private ActionBaseAddRecipe(RecipeStage recipe, IItemStack output, boolean isShaped, boolean hasTransformers) {
            this.recipe = recipe;
            this.output = output;
            this.isShaped = isShaped;
            IRecipe mcRecipe = recipe.getRecipe();
            if (mcRecipe instanceof MCRecipeBase) {
                MCRecipeBase ctRecipe = (MCRecipeBase) mcRecipe;
                if(hasTransformers)
                    MCRecipeManager.transformerRecipes.add(ctRecipe);
                if (ctRecipe.hasRecipeAction()) {
                    MCRecipeManager.actionRecipes.add(ctRecipe);
                }
            }
        }
        
        protected void setName(String name) {
            if (name != null) {
                String proposedName = cleanRecipeName(name);
                if (usedRecipeNames.contains(proposedName)) {
                    this.name = calculateName();
                    CraftTweakerAPI.logWarning("Recipe name [" + name + "] has duplicate uses, defaulting to calculated hash!");
                } else {
                    this.name = proposedName;
                }
            } else {
                this.name = calculateName();
            }
            usedRecipeNames.add(this.name); //TODO: FINISH THIS and replace stuff in constructor call.
        }
        
        public String calculateName() {
            int hash = ((MCRecipeBase) recipe.getRecipe()).toCommandString().hashCode();
            while(usedHashes.contains(hash))
                ++hash;
            usedHashes.add(hash);
            
            return (isShaped ? "ct_shaped" : "ct_shapeless") + hash;
        }
        
        @Override
        public void apply() {
            ForgeRegistries.RECIPES.register(recipe.setRegistryName(new ResourceLocation("crafttweaker", name)));
            List<IRecipe> list = recipes.getOrDefault(recipe.getTier(), new LinkedList<>());
            list.add(recipe);
            recipes.put(recipe.getTier(), list);
        }
        
        @Override
        public String describe() {
            if(output != null) {
                return "Adding " + (isShaped ? "shaped" : "shapeless") + " recipe for " + output.getDisplayName() + " with name " + name;
            } else {
                return "Trying to add " + (isShaped ? "shaped" : "shapeless") + "recipe without correct output";
            }
        }
    }
}
