package me.vcb.astrocore.userdata.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.userdata.Userdata;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum UserdataValueType {

    KILLS(Userdata::getKills),
    DEATHS(Userdata::getDeaths),
    BALANCE(Userdata::getBalance),
    HIGHEST_KILLSTREAK(Userdata::getHighestKillstreak),
    ELO(Userdata::getElo),
    KOTHS_CAPPED(Userdata::getKothsCapped),
    BOUNTIES_COLLECTED(Userdata::getBountiesCollected),
    KD(ud -> ud.getKills() * 100 / Math.max(1, ud.getDeaths()));

    private final Function<Userdata, Number> consumer;

    public Number getNewValue(Userdata userdata) {
        return this.consumer.apply(userdata);
    }
}
