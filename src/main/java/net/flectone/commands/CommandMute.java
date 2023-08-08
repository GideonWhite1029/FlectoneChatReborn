package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.voicechats.plasmovoice.FlectonePlasmoVoice;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandMute extends FTabCompleter {

    public CommandMute() {
        super.commandName = "mute";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return true;

        String stringTime = strings[1];

        if (!fCommand.isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1))) {
            fCommand.sendUsageMessage();
            return true;
        }

        String playerName = strings[0];
        FPlayer mutedFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (mutedFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        int time = fCommand.getTimeFromString(stringTime);

        if (time < -1) {
            fCommand.sendMeMessage("command.long-number");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        String reason = strings.length > 2
                ? ObjectUtil.toString(strings, 2)
                : Main.locale.getString("command.mute.default-reason");

        String formatString = Main.locale.getString("command.mute.global-message")
                .replace("<player>", mutedFPlayer.getRealName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        boolean announceModeration = Main.config.getBoolean("command.mute.announce");

        if (announceModeration) FDiscordSRV.sendModerationMessage(formatString);

        Set<Player> receivers = announceModeration
                ? new HashSet<>(Bukkit.getOnlinePlayers())
                : Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> player.hasPermission("flectonechat.mute") || player.equals(mutedFPlayer.getPlayer()))
                .collect(Collectors.toSet());

        fCommand.sendGlobalMessage(receivers, formatString, false);

        if (Main.isHavePlasmoVoice) {
            FlectonePlasmoVoice.mute(mutedFPlayer.isMuted(), mutedFPlayer.getRealName(), strings[1], reason);
        }

        mutedFPlayer.mute(time, reason);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length){
            case 1 -> isOfflinePlayer(strings[0]);
            case 2 -> isFormatString(strings[1]);
            case 3 -> isStartsWith(strings[2], "(reason)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
