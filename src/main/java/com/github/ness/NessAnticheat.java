package com.github.ness;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.ness.check.Check;
import com.github.ness.check.CheckManager;
import com.github.ness.check.CoreListener;
import com.github.ness.packets.PacketListener;
import com.github.ness.violation.ViolationHandler;

import lombok.Getter;

public class NessAnticheat {

    private static final Logger logger = NessLogger.getLogger(NessAnticheat.class);

    private final JavaPlugin plugin;
    private final static int minecraftVersion;
    private final ScheduledExecutorService executor;

    private final CheckManager checkManager;
    @Getter
    private final ViolationHandler violationHandler;

    static {
        minecraftVersion = getVersion();
    }

    NessAnticheat(JavaPlugin plugin, Path folder) {
        this.plugin = plugin;
        executor = Executors.newSingleThreadScheduledExecutor();
        this.violationHandler = new ViolationHandler(this);
        checkManager = new CheckManager(this);
    }

    private static int getVersion() {
        String first = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(MC: "));
        return Integer.valueOf(first.replace("(MC: ", "").replace(")", "").replace(" ", "").replace(".", ""));
    }

    void start() {
        // Detect version
        if (minecraftVersion > 1152 && minecraftVersion < 1162) {
            logger.warning("Please use 1.16.2 Spigot Version since 1.16/1.16.1 has a lot of false flags");
        }
        Check.updateCheckManager(this.getCheckManager());
        Bukkit.getServer().getPluginManager().registerEvents(new CoreListener(this.getCheckManager()), plugin);
        // Start configuration
        logger.fine("Configuration loaded. Initiating checks...");

        // Start checks
        logger.fine("Starting CheckManager");
        if (!Bukkit.getName().toLowerCase().contains("glowstone")) {
            plugin.getServer().getPluginManager().registerEvents(new PacketListener(this), plugin);
        }
    }

    /**
     * Gets the {@code JavaPlugin} NESS is using
     * 
     * @return the java plugin
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the detected minecraft version number, e.g. '1162'
     * 
     * @return the version number
     */
    public static int getMinecraftVersion() {
        return minecraftVersion;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    // public ViolationManager getViolationManager() {
    // return violationManager;
    // }

    void close() {
        // checkManager.close();
        try {
            executor.shutdown();
            executor.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.log(Level.WARNING, "Failed to complete thread pool termination", ex);
        }
    }

}