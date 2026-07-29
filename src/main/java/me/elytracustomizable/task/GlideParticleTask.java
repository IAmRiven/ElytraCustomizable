package me.elytracustomizable.task;

import me.elytracustomizable.ElytraCustomizablePlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class GlideParticleTask extends BukkitRunnable {

    private final ElytraCustomizablePlugin plugin;

    public GlideParticleTask(ElytraCustomizablePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("flame-particles.enabled", true)) {
            return;
        }

        Particle particle = Particle.valueOf(plugin.getConfig().getString("flame-particles.type", "FLAME"));
        int count = Math.max(1, plugin.getConfig().getInt("flame-particles.count", 6));
        double spread = Math.max(0.0D, plugin.getConfig().getDouble("flame-particles.spread", 0.25D));
        double speed = Math.max(0.0D, plugin.getConfig().getDouble("flame-particles.speed", 0.01D));
        double backwardOffset = Math.max(0.0D, plugin.getConfig().getDouble("flame-particles.backward-offset", 0.9D));
        double verticalOffset = plugin.getConfig().getDouble("flame-particles.vertical-offset", 0.35D);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline() || player.isDead() || !player.isGliding()) {
                continue;
            }

            spawnTrail(player, particle, count, spread, speed, backwardOffset, verticalOffset);
        }
    }

    private void spawnTrail(Player player, Particle particle, int count, double spread, double speed,
                            double backwardOffset, double verticalOffset) {
        Location origin = player.getLocation();
        World world = origin.getWorld();
        if (world == null) {
            return;
        }

        Vector direction = origin.getDirection().normalize();
        Vector backward = direction.multiply(-backwardOffset);

        for (int i = 0; i < count; i++) {
            double randomX = randomBetween(-spread, spread);
            double randomY = randomBetween(-spread, spread);
            double randomZ = randomBetween(-spread, spread);

            Location particleLocation = origin.clone()
                    .add(backward)
                    .add(randomX, verticalOffset + randomY, randomZ);

            world.spawnParticle(particle, particleLocation, 1, 0.0D, 0.0D, 0.0D, speed);
        }
    }

    private double randomBetween(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}