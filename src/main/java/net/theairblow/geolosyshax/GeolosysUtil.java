package net.theairblow.geolosyshax;

import com.oitsjustjose.geolosys.common.api.GeolosysAPI;
import com.oitsjustjose.geolosys.common.api.world.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeolosysUtil {
    /** Fetches deposit based on a list of blocks. */
    public static Optional<IOre> getDeposit(List<IBlockState> blocks) {
        return GeolosysAPI.oreBlocks.stream().filter(x ->
                blocks.stream().anyMatch(x::oreMatches)).findFirst();
    }

    /** List all biome names from an IOre. */
    public static Optional<List<String>> listBiomes(IOre ore) {
        if (ore instanceof DepositBiomeRestricted) {
            final DepositBiomeRestricted deposit =
                    (DepositBiomeRestricted) ore;
            return Optional.of(deposit.getBiomeList().stream()
                    .map(Biome::getBiomeName).collect(Collectors.toList()));
        } else if (ore instanceof DepositMultiOreBiomeRestricted) {
            final DepositMultiOreBiomeRestricted deposit =
                    (DepositMultiOreBiomeRestricted) ore;
            return Optional.of(deposit.getBiomeList().stream()
                    .map(Biome::getBiomeName).collect(Collectors.toList()));
        } else return Optional.empty();
    }

    /** Finds where an ore vein starts, null if not found. */
    public static BlockPos findVeinStart(Chunk chunk, IOre ore) {
        for (int y = 256; y >= 0; y--)
        for (int x = 0; x < 16; x++)
        for (int z = 0; z < 16; z++) {
            final IBlockState state = chunk.getBlockState(new BlockPos(x, y, z));
            if (ore instanceof DepositMultiOre) {
                final DepositMultiOre deposit = (DepositMultiOre) ore;
                if (deposit.getOres().contains(state)) {
                    x = chunk.x * 16 + x; z = chunk.z * 16 + z;
                    return new BlockPos(x, y, z);
                }
            } else if (ore.getOre().equals(state)) {
                x = chunk.x * 16 + x; z = chunk.z * 16 + z;
                return new BlockPos(x, y, z);
            }
        }

        return null;
    }

    /** Sends message in chat with GeolosysHax prefix. */
    public static void sendToChat(String str) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(() -> minecraft.ingameGUI.getChatGUI().printChatMessage(
                new TextComponentString("§6[§cGeolosysHax§6] " + str)));
    }

    /** Sends message in chat with or without GeolosysHax prefix. */
    public static void sendToChat(String str, boolean noPrefix) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(() -> minecraft.ingameGUI.getChatGUI().printChatMessage(
                new TextComponentString((noPrefix ? "" : "§6[§cGeolosysHax§6] ") + str)));
    }
}
