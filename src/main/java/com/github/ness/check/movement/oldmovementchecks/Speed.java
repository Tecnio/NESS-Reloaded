package com.github.ness.check.movement.oldmovementchecks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.AbstractCheck;
import com.github.ness.check.CheckFactory;
import com.github.ness.check.CheckInfo;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.MSG;
import com.github.ness.utility.Utility;

public class Speed extends AbstractCheck<PlayerMoveEvent> {

	public static final CheckInfo<PlayerMoveEvent> checkInfo = CheckInfo.eventOnly(PlayerMoveEvent.class);

	public Speed(CheckFactory<?> factory, NessPlayer player) {
		super(factory, player);
	}

	@Override
	protected void checkEvent(PlayerMoveEvent e) {
		Location to = e.getTo();
		Location from = e.getFrom();
		Player player = e.getPlayer();
		double maxSpd = player.getWalkSpeed() + 0.1;
		MovementValues movementValues = this.player().getMovementValues();
		double xDist = movementValues.xDiff;
		double zDist = movementValues.zDiff;
		if (Utility.hasflybypass(player) || player.getAllowFlight() || Utility.hasVehicleNear(player, 4)
				|| player().isTeleported()) {
			return;
		}
		// Handling Velocity From other Plugins
		if (player().nanoTimeDifference(PlayerAction.VELOCITY) < 1600) {
			xDist -= +Math.abs(player().velocity.getX());
			zDist -= Math.abs(player().velocity.getZ());
		}
		// Handling Stairs
		if (movementValues.AroundStairs) {
			maxSpd *= 2.5;
		}
		// Handling Ice Speed
		if (player().getTimeSinceLastWasOnIce() < 1300) {
			maxSpd += 0.12;
			if (Utility.groundAround(to.clone().add(0, 2, 0))) {
				maxSpd += 0.26;
			}
		}
		maxSpd += Math.abs(player.getVelocity().getY()) * 0.55f;
		// Handling Speed Potion Effect
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			final int level = Utility.getPotionEffectLevel(player, PotionEffectType.SPEED);
			xDist = (float) (xDist - xDist / 100.0D * level * 20.0D);
			zDist = (float) (zDist - zDist / 100.0D * level * 20.0D);
		}
		MSG.tell(player, "&9Dev> &7Speed Dist: " + maxSpd);
		if ((xDist > maxSpd || zDist > maxSpd) && player().nanoTimeDifference(PlayerAction.DAMAGE) >= 2000) {
			if (Utility.groundAround(player.getLocation()) && !player.isInsideVehicle()) {
				Material small = player.getWorld().getBlockAt(player.getLocation().subtract(0, .1, 0)).getType();
				if (!player.getWorld().getBlockAt(from).getType().isSolid()
						&& !player.getWorld().getBlockAt(to).getType().isSolid()
						&& !small.name().toLowerCase().contains("trap")) {
					if (player().isDevMode())
						MSG.tell(player, "&9Dev> &7Speed amo: " + movementValues.XZDiff);
					player().setViolation(new Violation("Speed",
							"MaxDistance(OnMove)" + " MaxDist: " + maxSpd + " Dist: " + (float) xDist), e);
				}
			}
		}
	}

}
