package com.adamk33n3r.runelite.watchdog.notifications;

import com.adamk33n3r.runelite.watchdog.Util;
import com.adamk33n3r.runelite.watchdog.WatchdogConfig;

import net.runelite.api.Client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Overhead extends MessageNotification {
    @Getter @Setter
    private int displayTime;

    @Inject
    private transient Client client;
    @Inject
    private transient ScheduledExecutorService executor;

    @Inject
    public Overhead(WatchdogConfig config) {
        this.displayTime = config.defaultOverHeadDisplayTime();
    }

    @Override
    protected void fireImpl(String[] triggerValues) {
        String message = Util.processTriggerValues(this.message, triggerValues);
        this.client.getLocalPlayer().setOverheadText(message);
        this.executor.schedule(() -> {
            if (this.client.getLocalPlayer().getOverheadText().equals(message)) {
                this.client.getLocalPlayer().setOverheadText("");
            }
        }, this.displayTime, TimeUnit.SECONDS);
    }
}
