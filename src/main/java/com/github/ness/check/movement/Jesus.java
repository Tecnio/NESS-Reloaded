package com.github.ness.check.movement;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import com.github.ness.NessAnticheat;
import com.github.ness.NessPlayer;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.Utility;

import space.arim.dazzleconf.annote.ConfDefault.DefaultDouble;

public class Jesus extends ListeningCheck<PlayerMoveEvent> {
    
	public static final ListeningCheckInfo<PlayerMoveEvent> checkInfo = CheckInfos
			.forEvent(PlayerMoveEvent.class);
	double distmultiplier = 0.75;
	public Jesus(ListeningCheckFactory<?, PlayerMoveEvent> factory, NessPlayer player) {
		super(factory, player);
		this.distmultiplier = this.ness().getMainConfig().getCheckSection().jesus().distmultiplier();
	}
	
	public interface Config {
		@DefaultDouble(0.7)
		double distmultiplier();
	}

    @Override
    protected void checkEvent(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        NessPlayer nessPlayer = this.player();
        double xDist = nessPlayer.getMovementValues().getxDiff();
        double zDist = nessPlayer.getMovementValues().getzDiff();
        double walkSpeed = p.getWalkSpeed() * distmultiplier;
		if (NessAnticheat.getMinecraftVersion() > 1122) {
            walkSpeed = p.getWalkSpeed() * 1.3;
        }
        xDist -= (xDist / 100.0) * (Utility.getPotionEffectLevel(p, PotionEffectType.SPEED) * 20.0);
        zDist -= (zDist / 100.0) * (Utility.getPotionEffectLevel(p, PotionEffectType.SPEED) * 20.0);
        if (nessPlayer.milliSecondTimeDifference(PlayerAction.VELOCITY) < 1500) {
        	xDist -= Math.abs(nessPlayer.getLastVelocity().getX());
        	zDist -= Math.abs(nessPlayer.getLastVelocity().getZ());
        }
        final double yVelocity = Math.abs(p.getVelocity().getY()) * 0.32;
        walkSpeed += yVelocity;
        if ((xDist > walkSpeed || zDist > walkSpeed) && event.getTo().getBlock().isLiquid()
                && event.getTo().clone().add(0, 0.01, 0).getBlock().isLiquid()
                && event.getTo().clone().add(0, -0.01, 0).getBlock().isLiquid() && event.getFrom().getBlock().isLiquid()
                && !Utility.hasflybypass(p) && !Utility.hasVehicleNear(p, 3)) {
        	flagEvent(event);
        }
    }

}
