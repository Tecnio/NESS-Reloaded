package com.github.ness.check.required;

import java.time.Duration;

import org.bukkit.event.player.PlayerTeleportEvent;

import com.github.ness.NessPlayer;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;

public class TeleportEvent extends ListeningCheck<PlayerTeleportEvent> {

	public static final ListeningCheckInfo<PlayerTeleportEvent> checkInfo = CheckInfos
			.forEventWithAsyncPeriodic(PlayerTeleportEvent.class, Duration.ofMillis(1500));

	public TeleportEvent(ListeningCheckFactory<?, PlayerTeleportEvent> factory, NessPlayer player) {
		super(factory, player);
	}

	@Override
	protected void checkAsyncPeriodic() {
		player().setTeleported(false);
		player().setHasSetback(false);
	}

	protected void checkEvent(PlayerTeleportEvent e) {
		NessPlayer nessPlayer = this.player();
		if (!nessPlayer.isHasSetback()) {
			nessPlayer.setTeleported(true);
		}
		nessPlayer.setHasSetback(false);
	}

}
