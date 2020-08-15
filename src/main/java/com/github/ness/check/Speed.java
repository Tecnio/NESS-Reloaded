package com.github.ness.check;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import com.github.ness.CheckManager;
import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.utility.Utility;

public class Speed extends AbstractCheck<PlayerMoveEvent> {

	public Speed(CheckManager manager) {
		super(manager, CheckInfo.eventWithAsyncPeriodic(PlayerMoveEvent.class, 1, TimeUnit.SECONDS));
	}

	@Override
	void checkEvent(PlayerMoveEvent e) {
		Check(e);
		Check1(e);
		Check2(e);
		Check3(e);
	}

	@Override
	void checkAsyncPeriodic(NessPlayer player) {
	}

	private void punish(PlayerMoveEvent e, String module) {
		Player p = e.getPlayer();
		if (Utility.hasflybypass(p) || manager.getPlayer(e.getPlayer()).isTeleported()) {
			return;
		}
		manager.getPlayer(p).setViolation(new Violation("Speed", module));
		if (manager.getPlayer(e.getPlayer()).shouldCancel(e, this.getClass().getSimpleName())) {
			e.setCancelled(true);
		}
	}

	/**
	 * This is a really Bad Check I don't suggest to you to skid this
	 * 
	 * @param e
	 */
	public void Check(PlayerMoveEvent e) {
		Location from = e.getFrom().clone();
		Location to = e.getTo().clone();
		// Bukkit.getPlayer("herobrine99dan").sendMessage(
		// "Player: " + e.getPlayer().getName() + " YDist: " + Utility.around(to.getY()
		// - from.getY(), 6)
		// + " Dist: " + Utility.around(Utility.getMaxSpeed(from, to), 6));
		Player player = e.getPlayer();
		if (Utility.hasflybypass(player)) {
			return;
		}
		if (Utility.isStairs(Utility.getPlayerUnderBlock(player).getLocation()) || Utility.isStairs(to)) {
			return;
		}
		// player.sendMessage("Time: "+Utility.around(System.currentTimeMillis(), 12));
		if (Utility.specificBlockNear(player.getLocation(), Material.STATIONARY_LAVA)
				|| Utility.specificBlockNear(player.getLocation(), Material.WATER)
				|| Utility.specificBlockNear(player.getLocation(), Material.LAVA)
				|| Utility.specificBlockNear(player.getLocation(), Material.STATIONARY_WATER)
				|| Utility.hasflybypass(player) || Utility.specificBlockNear(player.getLocation(), Material.SNOW)) {
			return;
		}
		if (!player.getNearbyEntities(5, 5, 5).isEmpty()) {
			return;
		}
		if (to.add(0, -1, 0).getBlock().getType().name().contains("chest")
				|| from.add(0, -1, 0).getBlock().getType().name().contains("chest")) {
			return;
		}
		if (Utility.getMaterialName(to).toLowerCase().contains("ladder")
				|| Utility.getMaterialName(from).toLowerCase().contains("ladder")) {
			return;
		}
		if (Utility.getMaterialName(to.clone().add(0, 0.5, 0)).toLowerCase().contains("ladder")
				|| Utility.getMaterialName(from.clone().add(0, 0.5, 0)).toLowerCase().contains("ladder")) {
			return;
		}
		if (to.add(0, -1, 0).getBlock().getType().name().contains("detector")
				|| from.add(0, -1, 0).getBlock().getType().name().contains("detector")) {
			return;
		}
		if (!player.isInsideVehicle() && !player.isFlying() && !player.hasPotionEffect(PotionEffectType.JUMP)) {
			if (to.getY() > from.getY()) {
				double y = Utility.around(to.getY() - from.getY(), 6);

				ArrayList<Block> blocchivicini = Utility.getSurrounding(Utility.getPlayerUnderBlock(player), false);
				boolean bypass = false;
				for (Block s : blocchivicini) {
					if (s.getType().equals(Material.SLIME_BLOCK)) {
						bypass = true;
					}
				}
				if (y > 0.248 && y < 0.333 && !Utility.hasBlock(player, Material.SLIME_BLOCK)) {
					punish(e, "MiniJump2 " + y);
				}
			}
		}
	}

	public void Check1(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (Utility.hasflybypass(p)) {
			return;
		}
		NessPlayer np = this.manager.getPlayer(p);
		if (np.isTeleported() || Utility.hasVehicleNear(p, 3) || Utility.hasEntityNear(p, 3)) {
			return;
		}
		float dist = (float) np.getMovementValues().XZDiff;
		Location to = event.getTo().clone();
		Location from = event.getFrom().clone();
		if (p.getGameMode() == GameMode.SPECTATOR || p.isInsideVehicle()) {
			return;
		}
		int speedLevel = Utility.getPotionEffectLevel(p, PotionEffectType.SPEED);
		if (speedLevel > 2) {
			return;
		}
		float f = to.getYaw() * 0.017453292F;
		float resultX = Math.abs((float) (Math.sin(f) * p.getWalkSpeed())) + 0.02f;
		float resultZ = Math.abs((float) (Math.cos(f) * p.getWalkSpeed())) + 0.02f;
		float maxDist = resultX + resultZ + 0.04f;
		float xVelocity = (float) p.getVelocity().getX();
		float zVelocity = (float) p.getVelocity().getZ();
		maxDist += (float) (Math.abs(zVelocity) + Math.abs(xVelocity)) * 1.11;
		maxDist += (float) Math.abs(p.getVelocity().getY()) * 0.06;
		if (p.isSprinting() && Utility.isMathematicallyOnGround(to.getY())) {
			maxDist = 0.38f;
		} else if (p.isSprinting()) {
			maxDist *= 1.48f;
		}
		if (to.clone().add(0, -1, 0).getBlock().getType().name().toLowerCase().contains("ice")) {
			maxDist *= 1.24f;
		}
		if (from.clone().add(0, -1, 0).getBlock().getType().name().toLowerCase().contains("ice")) {
			maxDist *= 1.24f;
		}
		if (p.isSneaking()) {
			maxDist = 0.172f;
			maxDist += (float) (Math.abs(zVelocity) + Math.abs(xVelocity)) * 1.11;
			maxDist += (float) Math.abs(p.getVelocity().getY()) * 0.06;
		}
		if (speedLevel > 0) {
			dist -= (dist / 100.0) * (speedLevel * 20.0);
		}
		float result = dist - maxDist;
		// p.sendMessage("maxDist: " + maxDist + " Dist: " + dist);
		if (result > 0.1) {
			this.punish(event, "MaxDistance: " + dist + " Max: " + maxDist);
		}
	}

	public void Check2(PlayerMoveEvent e) {
		NessPlayer np = this.manager.getPlayer(e.getPlayer());
		Player p = e.getPlayer();
		double y = np.getMovementValues().yDiff;
		double yresult = y - p.getVelocity().getY();
		if (Utility.hasflybypass(p) || Utility.hasBlock(p, Material.SLIME_BLOCK) || Utility.hasWater(p)
				|| Utility.isInWater(p)) {
			return;
		}
		if (Math.abs(yresult) > 0.9 && !manager.getPlayer(e.getPlayer()).isTeleported()) {
			manager.getPlayer(p).setViolation(new Violation("Speed", "InvalidVelocity: " + yresult));
			if (manager.getPlayer(e.getPlayer()).shouldCancel(e, this.getClass().getSimpleName())) {
				e.setCancelled(true);
			}
		}
	}

	public void Check3(PlayerMoveEvent e) {
		Location to = e.getTo().clone();
		Location from = e.getFrom().clone();
		NessPlayer np = this.manager.getPlayer(e.getPlayer());
		double dist = this.manager.getPlayer(e.getPlayer()).getMovementValues().xzDiffMultiplier;
		double lastDist = np.lastSpeedPredictionDist;
		np.lastSpeedPredictionDist = dist;
		boolean lastOnGround = np.lastSpeedPredictionOnGround;
		np.lastSpeedPredictionOnGround = Utility.isMathematicallyOnGround(to.getY());
		float friction = 0.91F;
		if (Utility.getMaterialName(to).toLowerCase().contains("ladder")
				|| Utility.getMaterialName(from).toLowerCase().contains("ladder")
				|| Utility.hasVehicleNear(e.getPlayer(), 4) || e.getPlayer().getNearbyEntities(2, 2, 2).isEmpty()) {
			return;
		}
		if (Utility.getMaterialName(to.clone().add(0, 0.5, 0)).toLowerCase().contains("ladder")
				|| Utility.getMaterialName(from.clone().add(0, 0.5, 0)).toLowerCase().contains("ladder")) {
			return;
		}
		double shiftedLastDist = lastDist * friction;
		double equalness = dist - shiftedLastDist;
		float scaledEqualness = (float) (equalness * 138);
		if (!Utility.isMathematicallyOnGround(to.getY()) && !lastOnGround) {
			if (scaledEqualness > 1.1) {
				this.punish(e, "InvalidFriction: " + scaledEqualness);
			}
		}
	}

}
