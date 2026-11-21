package me.vcb.astrocore.utils;

import com.google.gson.reflect.TypeToken;
import me.vcb.astrocore.deathban.Deathban;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionPlayer;
import me.vcb.astrocore.factions.claim.Claim;
import me.vcb.astrocore.factions.type.ConquestFaction;
import me.vcb.astrocore.factions.type.DtcFaction;
import me.vcb.astrocore.factions.type.KothFaction;
import me.vcb.astrocore.factions.type.MountainFaction;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.factions.type.RoadFaction;
import me.vcb.astrocore.factions.type.SpawnFaction;
import me.vcb.astrocore.factions.type.SystemFaction;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.games.mountain.MountainData;
import me.vcb.astrocore.games.schedule.ScheduleData;
import me.vcb.astrocore.hologram.Hologram;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GsonUtils {

    public static final Type CLAIM_TYPE = new TypeToken<List<Claim>>(){}.getType();

    public static final Type DEATHBAN_TYPE = new TypeToken<Map<UUID, Deathban>>(){}.getType();

    public static final Type FACTION_TYPE = new TypeToken<Map<UUID, Faction>>(){}.getType();

    public static final Type KOTH_TYPE = new TypeToken<List<KothData>>(){}.getType();

    public static final Type LOOT_TYPE = new TypeToken<List<LootData>>(){}.getType();

    public static final Type SCHEDULE_TYPE = new TypeToken<List<ScheduleData>>(){}.getType();

    public static final Type MOUNTAIN_TYPE = new TypeToken<List<MountainData>>(){}.getType();

    public static final Type PLAYER_TYPE = new TypeToken<Map<UUID, FactionPlayer>>(){}.getType();

    public static final Type HOLOGRAMS_TYPE = new TypeToken<List<Hologram>>(){}.getType();

    public static Class<?> getFactionClass(String name) {
        switch(name) {
            case "ConquestFaction": return ConquestFaction.class;
            case "DtcFaction": return DtcFaction.class;
            case "KothFaction": return KothFaction.class;
            case "MountainFaction": return MountainFaction.class;
            case "PlayerFaction": return PlayerFaction.class;
            case "RoadFaction": return RoadFaction.class;
            case "SpawnFaction": return SpawnFaction.class;
            case "SystemFaction": return SystemFaction.class;
        }

        throw new RuntimeException("Invalid faction type");
    }

    public static String getFactionType(Class<?> clazz) {
        if(clazz == ConquestFaction.class) {
            return "ConquestFaction";
        } else if(clazz == DtcFaction.class) {
            return "DtcFaction";
        } else if(clazz == KothFaction.class) {
            return "KothFaction";
        } else if(clazz == MountainFaction.class) {
            return "MountainFaction";
        } else if(clazz == PlayerFaction.class) {
            return "PlayerFaction";
        } else if(clazz == RoadFaction.class) {
            return "RoadFaction";
        } else if(clazz == SpawnFaction.class) {
            return "SpawnFaction";
        } else if(clazz == SystemFaction.class) {
            return "SystemFaction";
        }

        throw new RuntimeException("Invalid faction class type");
    }
}
