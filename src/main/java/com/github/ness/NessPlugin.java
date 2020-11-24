package com.github.ness;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.ness.packets.NessPacketListener;

import io.github.retrooper.packetevents.PacketEvents;

public class NessPlugin extends JavaPlugin {

    private NessAnticheat ness;
    
    @Override
    public synchronized void onLoad() {
        if (ness != null) {
            throw new IllegalStateException("Already enabled and running");
        }
        PacketEvents.load();
    }

    @Override
    public synchronized void onEnable() {
        if (ness != null) {
            throw new IllegalStateException("Already enabled and running");
        }
        NessAnticheat ness = new NessAnticheat(this, getDataFolder().toPath());
        ness.start();
        getCommand("ness").setExecutor(new NessCommands(ness));
        this.ness = ness;
        PacketEvents.getSettings().injectAsync(true);
        PacketEvents.getSettings().ejectAsync(true);
        PacketEvents.init(this);
        PacketEvents.getAPI().getEventManager().registerListener(new NessPacketListener(ness));
    }

    @Override
    public synchronized void onDisable() {
        if (ness == null) {
            getLogger().warning("No running instance of NESS. Did an error occur at startup?");
            return;
        }
        PacketEvents.stop();
        try {
            ness.close();
        } finally {
            ness = null;
        }
    }

}
