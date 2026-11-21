package me.vcb.astrocore.factions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.config.Config;

@AllArgsConstructor
public enum Relation {

    MEMBER(Config.TEAMMATE_COLOR),
    ALLY(Config.ALLY_COLOR),
    ENEMY(Config.ENEMY_COLOR);

    @Getter private final String color;
}
