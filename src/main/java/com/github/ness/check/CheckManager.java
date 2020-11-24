package com.github.ness.check;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.github.ness.NessAnticheat;
import com.github.ness.NessLogger;
import com.github.ness.NessPlayer;
import com.github.ness.check.packet.Timer;
import com.github.ness.packets.ReceivedPacketEvent;

import lombok.Getter;

public class CheckManager implements Listener {

    private Map<UUID, NessPlayer> nessPlayers = Collections.synchronizedMap(new HashMap<UUID, NessPlayer>());
    private final boolean devMode = true;
    @Getter
    private final Set<CheckFactory> checkList = new HashSet<CheckFactory>();

    @Getter
    private final NessAnticheat ness;
    private static final Logger logger = NessLogger.getLogger(CheckManager.class);

    public CheckManager(NessAnticheat nessAnticheat) {
        this.ness = nessAnticheat;
        // this.listenToAllEvents();
        // this.checkList.add(new FlyHighDistance(null, this));
        // this.checkList.add(new NoFall(null, this));
        this.checkList.add(new CheckFactory(Timer.class));
        // this.checkList.add(new Speed(null, this));
        // this.checkList.add(new ScaffoldAngle(null, this));
        // this.checkList.add(new ScaffoldFalseTarget(null, this));
    }

    public Object onEvent(ReceivedPacketEvent event) {
        for (NessPlayer np : nessPlayers.values()) {
            for (Check c : np.getChecks()) {
                c.checkEvent(event);
            }
        }
        return event;
    }

    public void makeNessPlayer(Player player) {
        NessPlayer nessPlayer = new NessPlayer(player, devMode);
        for (CheckFactory c : this.checkList) {
            try {
                Check check = c.makeEqualCheck(nessPlayer);
                nessPlayer.addCheck(check);
                logger.finer(
                        "Adding Check: " + check.getCheckName() + " to: " + nessPlayer.getBukkitPlayer().getName());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException e) {
                logger.log(Level.SEVERE, "There was an exception while enabling the check: " + c.getClazz().getName(),
                        e);
            }
        }
        nessPlayers.put(player.getUniqueId(), nessPlayer);
    }

    public void removeNessPlayer(NessPlayer np) {
        nessPlayers.remove(np.getBukkitPlayer().getUniqueId());
    }

    public void removeNessPlayer(Player p) {
        nessPlayers.remove(p.getUniqueId());
    }

    public NessPlayer getNessPlayer(UUID uuid) {
        return nessPlayers.get(uuid);
    }

    public Object reload() {
        // TODO Auto-generated method stub
        return null;
    }
}
