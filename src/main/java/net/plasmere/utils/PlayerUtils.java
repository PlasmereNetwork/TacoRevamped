package net.plasmere.utils;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerUtils {
    public static String getPlayerDimension(PlayerEntity player) {
        return player.getEntityWorld().getRegistryKey().getValue().toString();
    }
}
