package com.spectrasonic.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import lombok.experimental.UtilityClass;

@SuppressWarnings("all")
@UtilityClass
public final class SoundUtils {

    public static void playerSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player, sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void broadcastPlayerSound(Sound sound, float volume, float pitch) {
        Bukkit.getOnlinePlayers()
                .forEach(player -> player.playSound(player, sound, SoundCategory.MASTER, volume, pitch));
    }
}
