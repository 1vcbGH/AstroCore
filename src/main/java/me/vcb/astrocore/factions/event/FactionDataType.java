package me.vcb.astrocore.factions.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.factions.type.PlayerFaction;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum FactionDataType {

    KILLS(PlayerFaction::getKills),
    POINTS(PlayerFaction::getPoints),
    BALANCE(PlayerFaction::getBalance),
    KOTHS_CAPPED(PlayerFaction::getKothsCapped);

    private final Function<PlayerFaction, Number> consumer;

    public Number getNewValue(PlayerFaction faction) {
        return this.consumer.apply(faction);
    }
}
