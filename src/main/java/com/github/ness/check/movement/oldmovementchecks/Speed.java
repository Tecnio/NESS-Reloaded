package com.github.ness.check.movement.oldmovementchecks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import com.github.ness.NessPlayer;
import com.github.ness.check.Check;
import com.github.ness.check.CheckManager;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.Utility;

public class Speed extends Check {

    public Speed(NessPlayer nessPlayer, CheckManager manager) {
        super(Speed.class, nessPlayer, manager);
    }

    @Override
    public void checkEvent(Event ev) {
        if (!(ev instanceof PlayerMoveEvent)) {
            return;
        }
        PlayerMoveEvent e = (PlayerMoveEvent) ev;
        Location to = e.getTo();
        Location from = e.getFrom();
        Player player = e.getPlayer();
        NessPlayer nessPlayer = this.player();
        MovementValues movementValues = this.player().getMovementValues();
        final double dist = from.distance(to);
        double hozDist = dist - (to.getY() - from.getY());
        if (to.getY() < from.getY())
            hozDist = dist - (from.getY() - to.getY());
        double maxSpd = 0.43;
        if (player().milliSecondTimeDifference(PlayerAction.VELOCITY) < 1800) {
            hozDist -= Math.abs(player().getLastVelocity().getX());
            hozDist -= Math.abs(player().getLastVelocity().getZ());
        }
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            final int level = Utility.getPotionEffectLevel(player, PotionEffectType.SPEED);
            hozDist = (float) (hozDist - hozDist / 100.0D * level * 20.0D);
        }
        Material mat = null;
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                mat = from.getWorld()
                        .getBlockAt(from.getBlockX() + x, player.getEyeLocation().getBlockY() + 1, from.getBlockZ() + z)
                        .getType();
                if (mat.isSolid()) {
                    maxSpd = 0.50602;
                    break;
                }
            }
        }
        if (movementValues.isAroundSlabs() || movementValues.isAroundStairs()) {
            maxSpd += 0.1;
        }
        if (movementValues.isAroundIce() || nessPlayer.getTimeSinceLastWasOnIce() < 1000) {
            maxSpd += 0.2;
        }
        if (player.isInsideVehicle() && player.getVehicle().getType().name().contains("BOAT"))
            maxSpd = 2.787;
        if (hozDist > maxSpd && !player.isFlying() && !player.getAllowFlight()
                && nessPlayer.milliSecondTimeDifference(PlayerAction.DAMAGE) >= 2000 && !nessPlayer.isTeleported()) {
            if (nessPlayer.getMovementValues().isGroundAround()) {
                if (nessPlayer.getTimeSinceLastWasOnIce() >= 1000) {
                    if (!player.isInsideVehicle()
                            || (player.isInsideVehicle() && !player.getVehicle().getType().name().contains("HORSE"))) {
                        Material small = player.getWorld().getBlockAt(player.getLocation().subtract(0, .1, 0))
                                .getType();
                        if (!player.getWorld().getBlockAt(from).getType().isSolid()
                                && !player.getWorld().getBlockAt(to).getType().isSolid()
                                && nessPlayer.milliSecondTimeDifference(PlayerAction.BLOCKPLACED) > 1000) {
                            if (!small.name().contains("TRAPDOOR")) {
                                this.flag();
                            }
                        }
                    }
                }
            }
        }
    }
}
