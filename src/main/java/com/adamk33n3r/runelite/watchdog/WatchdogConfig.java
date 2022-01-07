package com.adamk33n3r.runelite.watchdog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("watchdog")
public interface WatchdogConfig extends Config {
    @ConfigItem(
        keyName = "alerts",
        name = "Alerts",
        description = "Serialized Alerts as a JSON string"
    )
    default String alerts() { return "[]"; };
}