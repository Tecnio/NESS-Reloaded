package com.github.ness.check.combat;

import java.util.ArrayList;
import java.util.List;

import com.github.ness.NessPlayer;
import com.github.ness.check.Check;
import com.github.ness.check.CheckManager;
import com.github.ness.packets.event.FlyingEvent;
import com.github.ness.packets.event.ReceivedPacketEvent;
import com.github.ness.utility.MathUtils;
import com.github.ness.utility.Utility;

public class Aimbot extends Check {
    private float lastYaw;
    private List<Double> pitchDiff;
    private double lastGCD = 0;

    public Aimbot(NessPlayer nessPlayer, CheckManager manager) {
        super(Aimbot.class, nessPlayer, manager);
        lastYaw = 0;
        this.pitchDiff = new ArrayList<Double>();
    }
    
    @Override
    public void onFlying(FlyingEvent e) {
        if (e.getNessPlayer().isTeleported()) {
            return;
        }
        if(!e.getPacket().isRotation()) {
            return;
        }
        makeSensitivity(e);
        Check2(e);
    }

    private void makeSensitivity(ReceivedPacketEvent event) {
        // float yaw = to.getYaw() - from.getYaw();
        NessPlayer player = event.getNessPlayer();
        double pitch = Math.abs(player.getMovementValues().pitchDiff);
        if (Math.abs(pitch) >= 10 || Math.abs(pitch) < 0.05 || pitch == 0.0 || !player.isTeleported()
                || Math.abs(player.getMovementValues().getTo().getPitch()) == 90) {
            return;
        }
        pitchDiff.add(pitch);
        if (pitchDiff.size() >= 15) {
            final double gcd = MathUtils.gcdRational(pitchDiff);
            if (lastGCD == 0.0) {
                lastGCD = gcd;
            }
            double result = Math.abs(gcd - lastGCD);
            if (result < 0.007) {
                final double sensitivity = MathUtils.getSensitivity(gcd);
                if (player.isDevMode()) {
                    player.sendDevMessage("GCD: " + Utility.round(gcd, 100) + "Sensitivity: "
                            + Math.round(MathUtils.getSensitivity(gcd) * 200));
                }
                player.setSensitivity(sensitivity);
            }
            if (result > 0.001D || gcd < 1.0E-4D) {
                // flagEvent(event);
            }
            pitchDiff.clear();
            lastGCD = gcd;
        }
    }

    @SuppressWarnings("unused")
    private void Check1(ReceivedPacketEvent e) {
        NessPlayer np = e.getNessPlayer();
        if (np.getSensitivity() == 0) {
            return;
        }
        if (np.getMovementValues().yawDiff < 1) {
            return;
        }
        double firstvar = np.getSensitivity() * 0.6F + 0.2F;
        float secondvar = (float) (Math.pow(firstvar, 3f));
        double yawResult = np.getMovementValues().yawDiff - lastYaw;
        float thirdvar = (float) yawResult / (secondvar * 1.2f);
        float x = (float) (thirdvar - Math.floor(thirdvar));
        // TODO Fixing Smooth Camera
        if (x > 0.1 && x < 0.95) {
            flag("Diff: " + x);
            // if(player().setViolation(new Violation("Aimbot", "ImpossibleRotation: " +
            // x))) e.setCancelled(true);
        }
        lastYaw = (float) np.getMovementValues().yawDiff;
    }

    /**
     * Check for some Aimbot Pattern
     */
    private void Check2(ReceivedPacketEvent e) {
        NessPlayer player = e.getNessPlayer();
        float yawChange = (float) Math.abs(player.getMovementValues().yawDiff);
        float pitchChange = (float) Math.abs(player.getMovementValues().pitchDiff);
        if (yawChange >= 1.0f && yawChange % 0.1f == 0.0f) {
            flag(" PerfectAura");
        } else if (pitchChange >= 1.0f && pitchChange % 0.1f == 0.0f) {
            flag(" PerfectAura1");
        }
    }
}
