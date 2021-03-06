package com.github.ness.check.movement.fly;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.github.ness.NessPlayer;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.Utility;

public class FlyInvalidJumpMotion extends ListeningCheck<PlayerMoveEvent> {

	public static final ListeningCheckInfo<PlayerMoveEvent> checkInfo = CheckInfos.forEvent(PlayerMoveEvent.class);

	public FlyInvalidJumpMotion(ListeningCheckFactory<?, PlayerMoveEvent> factory, NessPlayer player) {
		super(factory, player);
	}

	@Override
	protected void checkEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		double yDiff = event.getTo().getY() - event.getFrom().getY();
		NessPlayer nessPlayer = this.player();
		MovementValues movementValues = nessPlayer.getMovementValues();
		if (movementValues.isAroundSlabs()
				|| event.getTo().getBlock().isLiquid() || movementValues.isAroundLiquids() || movementValues.isAroundSnow()
				|| movementValues.hasBlockNearHead()
				|| movementValues.isAroundLadders()
				|| movementValues.isAroundNonOccludingBlocks() || movementValues.isAroundStairs()
				|| movementValues.isAroundChorus()
				|| movementValues.isAroundIce() || movementValues.isAroundSlime() || movementValues.getHelper().hasflybypass(nessPlayer)) {
			return;
		}
		// !player.getNearbyEntities(4, 4, 4).isEmpty()
		if (yDiff > 0 && !Utility.hasVehicleNear(player)) {
			if (player.getVelocity().getY() == 0.42f && !movementValues.getHelper().isMathematicallyOnGround(event.getTo().getY())
					&& movementValues.getHelper().isMathematicallyOnGround(event.getFrom().getY())) {
				double yResult = Math.abs(yDiff - player.getVelocity().getY());
				if (yResult != 0.0 && nessPlayer.milliSecondTimeDifference(PlayerAction.DAMAGE) > 1700
						&& nessPlayer.milliSecondTimeDifference(PlayerAction.VELOCITY) > 1700) {
					flagEvent(event, " yResult: " + yResult + "  yDiff: " + yDiff);
	            	//if(player().setViolation(new Violation("Fly", "InvalidJumpMotion yResult: " + yResult + "  yDiff: " + yDiff))) event.setCancelled(true);
				}
			}
		}
	}

}
