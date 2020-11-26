package com.github.ness.check.movement.oldmovementchecks;

import java.time.Duration;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import com.github.ness.NessPlayer;
import com.github.ness.check.Check;
import com.github.ness.data.ImmutableLoc;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.packets.ReceivedPacketEvent;
import com.github.ness.packets.event.FlyingEvent;
import com.github.ness.packets.event.UseEntityEvent;
import com.github.ness.utility.Utility;

public class Speed extends Check {

    private int preVL;

    public Speed(NessPlayer player) {
        super(Speed.class, player, true, Duration.ofSeconds(1).toMillis());
    }

    @Override
    public void checkAsyncPeriodic() {
        preVL = 0;
    }

    @Override
    public void onFlying(FlyingEvent e) {
        if (!e.isPosition()) {
            return;
        }
        MovementValues values = player().getMovementValues();
        ImmutableLoc to = values.getTo();
        ImmutableLoc from = values.getFrom();
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
        if(movementValues.isAroundSlabs() || movementValues.isAroundStairs()) {
            maxSpd += 0.1;
        }
        if(movementValues.isAroundIce() || nessPlayer.getTimeSinceLastWasOnIce() < 1000) {
            maxSpd += 0.2;
        }
        if (values.isInsideVehicle() && values.getVehicle().contains("BOAT"))
            maxSpd = 2.787;
        if (hozDist > maxSpd && !values.isAbleFly() && !values.isFlyBypass()
                && nessPlayer.milliSecondTimeDifference(PlayerAction.DAMAGE) >= 2000 && !nessPlayer.isTeleported()) {
            if (nessPlayer.getMovementValues().isGroundAround()) {
                if (nessPlayer.getTimeSinceLastWasOnIce() >= 1000) {
                    if (!values.isInsideVehicle()
                            || (values.isInsideVehicle() && !values.getVehicle().contains("HORSE"))) {
                        Material small = player.getWorld().getBlockAt(player.getLocation().subtract(0, .1, 0))
                                .getType();
                        if (!player.getWorld().getBlockAt(from).getType().isSolid()
                                && !player.getWorld().getBlockAt(to).getType().isSolid() && nessPlayer.milliSecondTimeDifference(PlayerAction.BLOCKPLACED) > 1000) {
                            if (!small.name().contains("TRAPDOOR")) {
                                this.flagEvent(e, maxSpd + " Dist: " + hozDist);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUseEntity(UseEntityEvent e) {
    }

    @Override
    public void onEveryPacket(ReceivedPacketEvent e) {
    }


}
