package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.cooldown.CooldownTimer;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand extends BaseCommand {

    public ReportCommand() {
        super("report", "astrocore.report", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.PREFIX + Language.REPORT_USAGE);
            return;
        }

        Player player = (Player) sender;

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        CooldownTimer timer = TimerManager.getInstance().getCooldownTimer();

        if(timer.isActive(player, "REPORT")) {
            player.sendMessage(Language.PREFIX + Language.REPORT_COOLDOWN
                .replace("<seconds>", timer.getTimeLeft(player, "REPORT")));
            return;
        }

        String reason = StringUtils.joinArray(args, " ", 2);

        // Add to GUI queue
        me.vcb.astrocore.report.ReportManager.getInstance()
            .addReport(player.getUniqueId(), player.getName(),
                       target.getUniqueId(), target.getName(), reason);

        Language.REPORT_FORMAT.forEach(line ->
            Messages.sendMessage(line
                .replace("<reporter>", player.getName())
                .replace("<suspect>", target.getName())
                .replace("<reason>", reason), "astrocore.report.receive")
        );

        player.sendMessage(Language.PREFIX + Language.REPORT_REPORTED
            .replace("<player>", target.getName()));

        timer.activate(player, "REPORT", Config.REPORT_COOLDOWN,
            Language.PREFIX + Language.REPORT_COOLDOWN_EXPIRED);
    }
}
