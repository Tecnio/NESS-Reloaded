package com.github.ness.check.movement.oldmovementchecks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import com.github.ness.NessPlayer;
import com.github.ness.check.Check;
import com.github.ness.check.CheckManager;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.PlayerManager;
import com.github.ness.utility.Utility;

public class NoFall extends Check {
    public NoFall(NessPlayer nessPlayer, CheckManager manager) {
        super(NoFall.class, nessPlayer, manager);
    }

    @Override
    public void checkEvent(Event ev) {
        if (!(ev instanceof PlayerMoveEvent)) {
            return;
        }
        PlayerMoveEvent event = (PlayerMoveEvent) ev;
		Player player = event.getPlayer();
		Material below = player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)).getType();
		NessPlayer nessPlayer = this.player();
		final boolean debugMode = nessPlayer.isDebugMode();
		Location from = event.getFrom(), to = event.getTo();
		MovementValues values = nessPlayer.getMovementValues();
		Double hozDist = values.getXZDiff();
		Double fallDist = (double) player.getFallDistance();
		MovementValues movementValues = nessPlayer.getMovementValues();
		if (Utility.hasflybypass(player) || player.getAllowFlight() || Utility.hasVehicleNear(player, 4)
				|| nessPlayer.isTeleported()) {
			return;
		}
		Double vertDist = values.yDiff;
		if (nessPlayer.milliSecondTimeDifference(PlayerAction.VELOCITY) < 1500) {
			hozDist -= Math.abs(nessPlayer.getLastVelocity().getX()) + Math.abs(nessPlayer.getLastVelocity().getZ());
			vertDist -= Math.abs(nessPlayer.getLastVelocity().getY());
		}
		boolean groundAround = Utility.groundAround(player.getLocation());

		if (debugMode) {
			nessPlayer
					.sendDevMessage("&7X: &e" + player.getLocation().getX() + " &7V: &e" + player.getVelocity().getX());
			nessPlayer
					.sendDevMessage("&7Y: &e" + player.getLocation().getY() + " &7V: &e" + player.getVelocity().getY());
			nessPlayer
					.sendDevMessage("&7Z: &e" + player.getLocation().getZ() + " &7V: &e" + player.getVelocity().getZ());
			nessPlayer.sendDevMessage(
					"&7hozDist: &e" + hozDist + " &7vertDist: &e" + vertDist + " &7fallDist: &e" + fallDist);
			nessPlayer.sendDevMessage("&7below: &e" + below.name());
			nessPlayer.sendDevMessage("&7groundAround: &e" + groundAround
					+ " &7onGround: " + player.isOnGround());
		}
		if (to.getY() != from.getY()) {
			if (from.getY() - to.getY() > .3 && fallDist <= .4 && !below.name().contains("WATER")
					&& !player.getLocation().getBlock().isLiquid()) {
				if (hozDist < .2 || !groundAround) {
					if (PlayerManager.timeSince("breakTime", player) >= 2000 && !nessPlayer.isTeleported()
							&& !below.name().contains("PISTON")) {
						if ((!player.isInsideVehicle()
								|| (player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE))
								&& !player.isFlying() && to.getY() > 0) {
							if (!movementValues.isAroundSlime() && !Utility.hasBlock(player, "water")
									&& !Utility.isInWater(player) && !movementValues.isAroundLiquids()
									&& !Utility.specificBlockNear(event.getTo(), "fire")
									&& !Utility.getMaterialName(event.getTo()).contains("FIRE") && !Utility
											.getMaterialName(event.getTo().clone().add(0, 0.4, 0)).contains("FIRE")) {
								boolean gotFire = false;
								if (player.getLastDamageCause() != null) {
									if (player.getLastDamageCause().getCause() != null) {
										if (player.getLastDamageCause().getCause().name().toLowerCase()
												.contains("fire")) {
											gotFire = true;
										}
									}
								}
								if (!gotFire) {
									this.flag("OnMove");
									player.damage(
											Math.abs(Utility.calcDamage((3.5 * player.getVelocity().getY()) / -0.71)));
								}
							}
						}
					}
				}
			}
		}
	}
}
