package me.vcb.astrocore.userdata.event;

import lombok.Getter;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class UserdataValueChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Userdata userdata;
    private final UserdataValueType type;
    private final Number newValue;

    public UserdataValueChangeEvent(Userdata userdata, UserdataValueType type) {
        super(false);
        this.userdata = userdata;
        this.type = type;
        this.newValue = type.getNewValue(userdata);

        Bukkit.getPluginManager().callEvent(this);
    }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
