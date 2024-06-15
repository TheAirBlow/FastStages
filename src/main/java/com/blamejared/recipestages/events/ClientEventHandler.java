package com.blamejared.recipestages.events;

import com.blamejared.recipestages.RecipeStages;
import net.darkhax.gamestages.event.StagesSyncedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.*;

public class ClientEventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGameStageSync(StagesSyncedEvent event) {
        RecipeStages.proxy.syncJEI(event.getEntityPlayer());
    }
}
