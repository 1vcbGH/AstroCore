package me.vcb.astrocore.tab.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import lombok.Getter;
import me.vcb.astrocore.scoreboard.base.ScoreboardBase_1_21;
import me.vcb.astrocore.tab.PlayerTab;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * 1.8-style tab list using 80 fake player entries (4 columns × 20 rows).
 * Fake profiles are named "!Ax" .. "!Dt" so they sort before real player
 * names (all start with [A-Za-z0-9_]) in the tab list.
 */
public class PlayerTab_1_21 extends ScoreboardBase_1_21 implements PlayerTab {

    private static final int COLS = 4;
    private static final int ROWS = 20;
    private static final int SLOTS = COLS * ROWS; // 80

    @Getter private final int clientVersion;

    private final ProtocolManager protocolManager;
    private final UUID[] uuids = new UUID[SLOTS];
    private final String[] content = new String[SLOTS];
    private final boolean[] dirty = new boolean[SLOTS];

    public PlayerTab_1_21(Player player) {
        super(player, NmsUtils.getInstance().getPlayerScoreboard(player));
        this.clientVersion = NmsUtils.getInstance().getClientVersion(player);
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        player.setScoreboard(this.scoreboard);

        for (int i = 0; i < SLOTS; i++) {
            this.uuids[i] = UUID.randomUUID();
            this.content[i] = "";
        }

        this.sendAddAllPacket();
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void unregister() {
        if (!this.player.isOnline()) return;
        try {
            PacketContainer packet = this.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
            List<UUID> uuidList = new ArrayList<>(Arrays.asList(this.uuids));
            packet.getUUIDLists().write(0, uuidList);
            this.protocolManager.sendServerPacket(this.player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void set(int index, String line) {
        int slot = index - 1; // TabUpdater uses 1-based indices
        if (slot < 0 || slot >= SLOTS) return;

        line = Color.translate(line);
        if (line.equals(this.content[slot])) return;
        this.content[slot] = line;
        this.dirty[slot] = true;
    }

    @Override
    public void flush() {
        List<PlayerInfoData> toUpdate = new ArrayList<>();

        for (int i = 0; i < SLOTS; i++) {
            if (!this.dirty[i]) continue;
            this.dirty[i] = false;

            String text = this.content[i].isEmpty() ? " " : this.content[i];
            toUpdate.add(new PlayerInfoData(
                this.uuids[i], 0, true,
                EnumWrappers.NativeGameMode.SURVIVAL,
                new WrappedGameProfile(this.uuids[i], this.slotName(i)),
                WrappedChatComponent.fromLegacyText(text)
            ));
        }

        if (!toUpdate.isEmpty()) {
            this.sendPacket(EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME), toUpdate);
        }
    }

    // -----------------------------------------------------------------------

    private void sendAddAllPacket() {
        List<PlayerInfoData> entries = new ArrayList<>();

        for (int i = 0; i < SLOTS; i++) {
            WrappedGameProfile profile = new WrappedGameProfile(this.uuids[i], this.slotName(i));

            entries.add(new PlayerInfoData(
                this.uuids[i], 0, true,
                EnumWrappers.NativeGameMode.SURVIVAL,
                profile,
                WrappedChatComponent.fromLegacyText(" ")
            ));
        }

        this.sendPacket(EnumSet.of(
            EnumWrappers.PlayerInfoAction.ADD_PLAYER,
            EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
            EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
            EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
            EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE
        ), entries);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sendPacket(EnumSet<EnumWrappers.PlayerInfoAction> actions, List<PlayerInfoData> entries) {
        if (!this.player.isOnline()) return;
        try {
            PacketContainer packet = this.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);

            // Build a real EnumSet of the NMS Action type — getSets() produces HashSet which
            // cannot be assigned to the 'private final EnumSet' field in the NMS packet.
            Class nmsActionClass = EnumWrappers.getPlayerInfoActionClass();
            Object[] nmsConstants = nmsActionClass.getEnumConstants();
            EnumSet nmsActions = EnumSet.noneOf(nmsActionClass);
            for (EnumWrappers.PlayerInfoAction action : actions) {
                nmsActions.add(nmsConstants[action.ordinal()]);
            }
            packet.getModifier().write(0, nmsActions);

            packet.getLists(PlayerInfoData.getConverter()).write(0, entries);
            this.protocolManager.sendServerPacket(this.player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Unique 3-char profile name for each slot, starting with '!' (ASCII 33)
     * so fake entries sort before real player names in the tab list.
     * Layout: col encoded as A-D, row as a-t → "!Aa".."!Dt"
     */
    private String slotName(int slot) {
        int col = slot / ROWS;
        int row = slot % ROWS;
        return "!" + (char)('A' + col) + (char)('a' + row);
    }
}
