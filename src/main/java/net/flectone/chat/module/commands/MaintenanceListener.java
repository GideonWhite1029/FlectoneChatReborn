package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FileManager;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.commands;
import static net.flectone.chat.manager.FileManager.locale;

public class MaintenanceListener extends FListener {
    public MaintenanceListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void serverLickPingEvent(@NotNull ServerListPingEvent event) {
        if (!commands.getBoolean("maintenance.turned-on")) return;

        String motd = locale.getVaultString(null, "commands.maintenance.motd.message");
        event.setMotd(MessageUtil.formatAll(null, motd));

        try {
            CachedServerIcon serverIcon = Bukkit.loadServerIcon(FileManager.getIcon("maintenance"));
            event.setServerIcon(serverIcon);
        } catch (Exception e) {
            FlectoneChat.warning("Unable to load maintenance icon");
            e.printStackTrace();
        }

        event.setMaxPlayers(-1);
    }

    @EventHandler
    public void playerLoginEvent(@NotNull PlayerLoginEvent event) {
        if (!commands.getBoolean("maintenance.turned-on")) return;

        Player player = event.getPlayer();

        String kickedMessage = locale.getVaultString(player, "commands.maintenance.kicked-message");
        kickedMessage = MessageUtil.formatAll(player, kickedMessage);

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickedMessage);
    }
}