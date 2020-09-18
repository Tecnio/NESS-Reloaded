package com.github.ness.check.impl.movement.fly;

import com.github.ness.check.CheckManager;
import com.github.ness.NESSPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.AbstractCheck;
import com.github.ness.check.CheckInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class FlyGhostMode extends AbstractCheck<PlayerMoveEvent> {


    public FlyGhostMode(CheckManager manager) {
        super(manager, CheckInfo.eventOnly(PlayerMoveEvent.class));
    }

    @Override
    protected void checkEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isDead()) {
            NESSPlayer np = this.manager.getPlayer(player);
            if ((np.getMovementValues().XZDiff > 0.3 || np.getMovementValues().yDiff > 0.16) && !np.isTeleported()) {
                this.getNessPlayer(player).setViolation(new Violation("Fly", "GhostMode"), event);
            }
        }
    }

}