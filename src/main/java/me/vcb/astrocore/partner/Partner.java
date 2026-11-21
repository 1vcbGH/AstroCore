package me.vcb.astrocore.partner;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Partner {

    private String nick;
    private String displayName;
    private boolean hidden;
    private int claimCount;
    private List<String> rewardCommands;

    public Partner() {
        this.rewardCommands = new ArrayList<>();
    }

    public Partner(String nick, String displayName) {
        this.nick = nick;
        this.displayName = displayName;
        this.hidden = false;
        this.claimCount = 0;
        this.rewardCommands = new ArrayList<>();
    }
}
