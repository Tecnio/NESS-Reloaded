package com.github.ness.check.movement;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
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
import com.github.ness.utility.PlayerManager;
import com.github.ness.utility.Utility;

public class NoFall extends AbstractCheck<PlayerMoveEvent> {
	public static final CheckInfo<PlayerMoveEvent> checkInfo = CheckInfo.eventWithAsyncPeriodic(PlayerMoveEvent.class,
			1, TimeUnit.SECONDS);

	int noGround; // Used in NoGround Check

	public NoFall(CheckFactory<?> factory, NessPlayer player) {
		super(factory, player);
		noGround = 0;
	}

	@Override
	protected void checkAsyncPeriodic() {
		noGround = 0;
	}

	private void punish(PlayerMoveEvent e, String cheat, String module) {
		NessPlayer nessPlayer = this.player();
		if (nessPlayer.isTeleported()) {
			return;
		}
		nessPlayer.setViolation(new Violation(cheat, module), e);
	}

	@Override
	protected void checkEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		Material below = player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)).getType();
		Material bottom = null;
		NessPlayer nessPlayer = this.player();
		final boolean debugMode = nessPlayer.isDebugMode();
		Location from = event.getFrom(), to = event.getTo();
		Double dist = from.distance(to);
		if (nessPlayer.nanoTimeDifference(PlayerAction.VELOCITY) < 1600) {
			dist -= Math.abs(nessPlayer.velocity.getX()) + Math.abs(nessPlayer.velocity.getY())
					+ Math.abs(nessPlayer.velocity.getZ());
		}
		Double hozDist = dist - (to.getY() - from.getY());
		Double fallDist = (double) player.getFallDistance();
		MovementValues movementValues = nessPlayer.getMovementValues();
		if (Utility.hasflybypass(player) || player.getAllowFlight() || Utility.hasVehicleNear(player, 4)
				|| nessPlayer.isTeleported()) {
			return;
		}
		if (to.getY() < from.getY())
			hozDist = dist - (from.getY() - to.getY());
		Double vertDist = Math.abs(dist - hozDist);
		double dTG = 0; // Distance to ground
		boolean groundAround = Utility.groundAround(player.getLocation()), waterAround = false;
		int radius = 2;
		boolean ice = false;

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				int y = 0;
				while (!player.getLocation().subtract(x, y, z).getBlock().getType().isSolid() && y < 20) {
					y++;
				}
				if (y < dTG || dTG == 0)
					dTG = y;
			}
		}
		dTG += player.getLocation().getY() % 1;
		bottom = player.getLocation().getWorld().getBlockAt(player.getLocation().subtract(0, dTG, 0)).getType();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				Material belowSel = player.getWorld().getBlockAt(player.getLocation().add(x, -1, z)).getType();
				if (belowSel.name().toLowerCase().contains("piston") || belowSel.name().toLowerCase().contains("ice")) {
					ice = true;
				}
				belowSel = player.getWorld().getBlockAt(player.getLocation().add(x, -.01, z)).getType();
				if (belowSel.isSolid()) {
					nessPlayer.updateLastWasOnGround();
				}
			}
		}
		if (ice) {
			nessPlayer.updateLastWasOnIce();
		}
		for (int x = -radius; x < radius; x++) {
			for (int y = -1; y < radius; y++) {
				for (int z = -radius; z < radius; z++) {
					Block b = to.getWorld().getBlockAt(player.getLocation().add(x, y, z));
					if (b.isLiquid())
						waterAround = true;
				}
			}
		}

		if (debugMode) {
			MSG.tell(player, "&7dist: &e" + dist);
			MSG.tell(player, "&7X: &e" + player.getLocation().getX() + " &7V: &e" + player.getVelocity().getX());
			MSG.tell(player, "&7Y: &e" + player.getLocation().getY() + " &7V: &e" + player.getVelocity().getY());
			MSG.tell(player, "&7Z: &e" + player.getLocation().getZ() + " &7V: &e" + player.getVelocity().getZ());
			MSG.tell(player, "&7hozDist: &e" + hozDist + " &7vertDist: &e" + vertDist + " &7fallDist: &e" + fallDist);
			MSG.tell(player,
					"&7below: &e" + Utility.getMaterialName(below) + " bottom: " + Utility.getMaterialName(bottom));
			MSG.tell(player, "&7dTG: " + dTG);
			MSG.tell(player,
					"&7groundAround: &e" + MSG.torF(groundAround) + " &7onGround: " + MSG.torF(player.isOnGround()));
			MSG.tell(player, "&7ice: " + MSG.torF(ice));
			MSG.tell(player, " &7waterAround: " + MSG.torF(waterAround));
		}
		if (to.getY() != from.getY()) {
			if (from.getY() - to.getY() > .3 && fallDist <= .4 && !below.name().toLowerCase().contains("water")
					&& !player.getLocation().getBlock().isLiquid()) {
				if (hozDist < .2 || !groundAround) {
					if (PlayerManager.timeSince("breakTime", player) >= 2000 && !nessPlayer.isTeleported()
							&& !below.name().toLowerCase().contains("piston")) {
						if ((!player.isInsideVehicle()
								|| (player.isInsideVehicle() && player.getVehicle().getType() != EntityType.HORSE))
								&& !player.isFlying() && to.getY() > 0) {
							if (!bottom.name().toLowerCase().contains("slime") && !Utility.hasBlock(player, "water")
									&& !Utility.isInWater(player) && !movementValues.AroundLiquids
									&& !Utility.specificBlockNear(event.getTo(), "fire")
									&& !Utility.getMaterialName(event.getTo()).contains("fire") && !Utility
											.getMaterialName(event.getTo().clone().add(0, 0.4, 0)).contains("fire")) {
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
									punish(event, "NoFall", "(OnMove)");
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
