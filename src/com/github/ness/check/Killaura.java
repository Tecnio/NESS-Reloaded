package com.github.ness.check;

import java.util.HashMap;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.mswsplex.MSWS.NESS.MSG;
import org.mswsplex.MSWS.NESS.NESS;
import org.mswsplex.MSWS.NESS.NESSPlayer;
import org.mswsplex.MSWS.NESS.PlayerManager;
import org.mswsplex.MSWS.NESS.Protocols;

import com.github.ness.CheckManager;
import com.github.ness.Utility;
import com.github.ness.Violation;

public class Killaura  extends AbstractCheck<EntityDamageByEntityEvent>{
	public  HashMap<Player, Entity> lastEntityHit = new HashMap<Player, Entity>();

	public Killaura(CheckManager manager) {
		super(manager, CheckInfo.eventOnly(EntityDamageByEntityEvent.class));
		// TODO Auto-generated constructor stub
	}
	
	@Override
	void checkEvent(EntityDamageByEntityEvent e) {
       Check(e);
       Check1(e);
       Check2(e);
	}
	
	public  void Check(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() == EntityType.PLAYER) {
			Player player = (Player) event.getDamager();
			double maxDist = 5.3D;
			if (player.getGameMode() == GameMode.CREATIVE) {
				maxDist = 5.563D;
			}

			if (event.getEntity().getLocation().distance(player.getLocation()) > maxDist) {
				punish(player, 15, "Reach", 6);
				event.setCancelled(true);
				if (NESS.main.devMode) {
					MSG.tell(player, "&9Dev> &7Reach Distance: "
							+ event.getEntity().getLocation().distance(player.getLocation()));
				}
			}

			if (NESS.main.lastHitLoc.containsKey(player) && ((Location) NESS.main.lastHitLoc.get(player))
					.distance(event.getEntity().getLocation()) >= 5.0D) {
				punish(player, 16, "Reach", 6);
			}

			if (lastEntityHit.containsKey(player)
					&& ((Entity) lastEntityHit.get(player)).getWorld().equals(event.getEntity().getWorld())
					&& NESS.main.lastHitLoc.containsKey(player)
					&& ((Entity) lastEntityHit.get(player)).equals(event.getEntity())) {
				Double dist = event.getEntity().getLocation().distance((Location) NESS.main.lastHitLoc.get(player));
				if (PlayerManager.timeSince("lastHit", player) <= 100.0D && dist > 0.23D) {
					punish((Player) event.getEntity(), 5, "Timer", 5);
					if (NESS.main.devMode) {
						MSG.tell(player, "&9Dev> &7Quick hit: " + PlayerManager.timeSince("lastHit", player)
								+ " Velocity: " + dist);
					}
				}
			}

			NESS.main.vl.set(player.getUniqueId() + ".accuracy.hits",
					NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.hits") + 1);
			int hits = NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.hits");
			int miss = NESS.main.vl.getInt(player.getUniqueId() + ".accuracy.misses");
			double acc = (double) hits / (double) Math.max(miss + hits, 1);
			if (hits + miss >= 10) {
				if (acc > 0.8D && NESS.main.lastHitLoc.containsKey(player)
						&& ((Entity) lastEntityHit.get(player)).equals(event.getEntity())) {
					Double dist = event.getEntity().getLocation().distance((Location) NESS.main.lastHitLoc.get(player));
					if (dist > 0.129D) {
						punish(player, 17, "PerfectAura", 4);
					}
				}

				NESS.main.vl.set(player.getUniqueId() + ".accuracy", (Object) null);
			}

			NESS.main.lastHitLoc.put(player, event.getEntity().getLocation());
			lastEntityHit.put(player, event.getEntity());
			Block target = player.getTargetBlock((Set<Material>) null, 5);
			if (target.getType().isSolid()) {
				PlayerManager.addAction("clicks", player);
			}
			if (Protocols.angles == null) {
				return;
			}
			if (Protocols.angles.containsKey(player.getUniqueId())) {
				Location real = (Location) Protocols.angles.get(player.getUniqueId());
				double difference = (double) (real.getYaw() - player.getLocation().getYaw());
				if (difference > 1.5D && player.getFallDistance() < 1.0F) {
					if (NESS.main.devMode) {
						MSG.tell(player, "&9Dev> &7Diff: " + difference);
					}

					punish(player, 18, "Angles", 5);
				}

				PlayerManager.setAction("oldDist", player, difference);
			}

			PlayerManager.setAction("lastHit", player, (double) System.currentTimeMillis());
		}

	}

	public  void Check1(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			final Location loc = p.getLocation();
			Bukkit.getScheduler().runTaskLater(NESS.main, new Runnable() {
				public void run() {
					Location loc1 = p.getLocation();
					float grade = loc.getYaw() - loc1.getYaw();
					float grade1 = Math.abs(loc.getPitch() - loc1.getPitch());
					if (Math.round(grade) > 170.0 || Math.round(grade1) > 45) {
						punish(p, 19, "Heuristic", 6);
						if (NESS.main.devMode) {
							p.sendMessage("Heuristic: " + grade);
						}
					}
				}
			}, 3L);
		}
	}

	public  void Check2(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
			Player player = (Player) event.getDamager();
			LivingEntity damaged = (LivingEntity) event.getEntity();
			double offset = 0.0D;

			Location entityLoc = damaged.getLocation().add(0.0D, damaged.getEyeHeight(), 0.0D);
			Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(), 0.0D);

			Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
			Vector expectedRotation = Utility.getRotation(playerLoc, entityLoc);

			double deltaYaw = Utility.clamp180(playerRotation.getX() - expectedRotation.getX());
			double deltaPitch = Utility.clamp180(playerRotation.getY() - expectedRotation.getY());

			double horizontalDistance = Utility.getHorizontalDistance(playerLoc, entityLoc);
			double distance = Utility.getDistance3D(playerLoc, entityLoc);

			double offsetX = deltaYaw * horizontalDistance * distance;
			double offsetY = deltaPitch * Math.abs(entityLoc.getY() - playerLoc.getY()) * distance;

			offset += Math.abs(offsetX);
			offset += Math.abs(offsetY);
			if (offset > 290.0D) {
				punish(player, 20, "Angles/Hitbox", 6);
			}
		}
	}

	public  void Check3(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if (player.getLocation().getPitch() == Math.round(player.getLocation().getPitch())) {
				punish(player, 21, "PerfectAngle", 5);
			}
		}
	}

	public  void Check4(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player damager = (Player) e.getDamager();
			if (isLookingAt(damager, e.getEntity().getLocation()) < 0.2D) {
				punish(damager, 23, "Angles/Hitbox", 4);
				if (NESS.main.devMode) {
					damager.sendMessage(
							"isLookingAt: " + Utility.around(isLookingAt(damager, e.getEntity().getLocation()), 6));
				}
			}
		}
	}

	public  void Check5(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			Entity damaged = e.getEntity();
			Location to = p.getLocation();
			Location from = damaged.getLocation();
			double x = Math.abs(from.getX() - to.getX());
			double z = Math.abs(from.getX() - to.getX());
			if (!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
				return;
			}
			if (x == 0.0D || z == 0.0D) {
				return;
			}
			if (Math.abs(from.getY() - to.getY()) >= 0.6D) {
				return;
			}
			Location l = null;
			if (x <= 0.5D && z >= 1.0D) {
				if (e.getDamager().getLocation().getZ() > e.getEntity().getLocation().getZ()) {
					l = e.getDamager().getLocation().clone().add(0.0D, 0.0D, -1.0D);
				} else {

					l = e.getDamager().getLocation().clone().add(0.0D, 0.0D, 1.0D);
				}

			} else if (z <= 0.5D && x >= 1.0D) {
				if (e.getDamager().getLocation().getX() > e.getEntity().getLocation().getX()) {
					l = e.getDamager().getLocation().clone().add(-1.0D, 0.0D, 0.0D);
				} else {

					l = e.getDamager().getLocation().clone().add(-1.0D, 0.0D, 0.0D);
				}
			}
			boolean failed = false;
			if (l != null) {
				failed = (l.getBlock().getType().isSolid()
						&& l.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid());
			}
			if (failed) {
				punish((Player) e.getEntity(), 5, "ThrougWalls", 5);
			}
		}
	}
	
	public  void Check6(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
           if(e.getEntity().getEntityId() == e.getDamager().getEntityId()) {
        	   punish((Player) e.getEntity(), 5, "SelfHit", 5);
           }
		}
	}

	private  double isLookingAt(Player player, Location target) {
		Location eye = player.getEyeLocation();
		Vector toEntity = target.toVector().subtract(eye.toVector());
		double dot = toEntity.normalize().dot(eye.getDirection());

		return dot;// dot > 0.99D
	}

	private  void punish(Player p, int i, String module, int vl) {
		manager.getPlayer(p).setViolation(new Violation("Killaura"));
	}

}