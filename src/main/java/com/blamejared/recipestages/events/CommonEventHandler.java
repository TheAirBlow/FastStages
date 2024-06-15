package com.blamejared.recipestages.events;

import com.blamejared.recipestages.RecipeStages;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.events.ActionApplyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonEventHandler {
    @SubscribeEvent
    public void onActionApply(ActionApplyEvent.Post event) {
        try {
            RecipeStages.LATE_REMOVALS.forEach(CraftTweakerAPI::apply);
            RecipeStages.LATE_ADDITIONS.forEach(CraftTweakerAPI::apply);
        } catch(Exception e) {
            CraftTweakerAPI.logError("Failed to load late removals or additions:", e);
        }
    }
}
