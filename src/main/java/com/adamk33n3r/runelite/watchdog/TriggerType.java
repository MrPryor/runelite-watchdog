package com.adamk33n3r.runelite.watchdog;

import com.adamk33n3r.runelite.watchdog.alerts.*;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TriggerType {
    GAME_MESSAGE("Game Message", "Game messages sent in chat", ChatAlert.class),
    PLAYER_CHAT_MESSAGE("Player Chat Message", "Player messages sent in chat", PlayerChatAlert.class),
    STAT_CHANGED("Stat Changed", "Stat changes like boosts or drains", StatChangedAlert.class),
    XP_DROP("XP Drop", "Get an xp drop", XPDropAlert.class),
    SPAWNED_OBJECT("Spawned Object", "When an object, player, or npc spawns or despawns", SpawnedAlert.class),
    INVENTORY("Inventory", "When your inventory is full, empty, or contains certain items", InventoryAlert.class),
    // Keep this last so that people maybe won't try to use it over the chat one
    NOTIFICATION_FIRED("Notification Fired", "When other plugins fire notifications", NotificationFiredAlert.class),
    ;

    private final String name;
    private final String tooltip;
    private final Class<? extends Alert> implClass;
}
