package io.github.johnfg10.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import io.github.johnfg10.ExpeditConst;
import io.github.johnfg10.utils.RequestBufferHelper;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.sql.SQLException;
import java.util.List;

import static io.github.johnfg10.utils.MessageUtil.hasPerm;

/**
 * Created by johnfg10 on 16/03/2017.
 */
public class GeneralCommandHandler implements CommandExecutor {

    @Command(aliases = {"help", "halp", "welp"}, description = "Help", async = true)
    public void onCommandHelp(IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withTitle("Help!");
        embedBuilder.withColor(38, 112, 4);
        List<CommandHandler.SimpleCommand> simpleCommands = ExpeditConst.commandHandler.getCommands();

        for (CommandHandler.SimpleCommand simpleCommand:simpleCommands) {
            if(simpleCommand.getCommandAnnotation().showInHelpPage() == true){
                String alias = null;
                for (String string:simpleCommand.getCommandAnnotation().aliases()) {
                    if(alias == null){
                        alias = string;
                    }else{
                        alias = alias + ", " + string;
                    }
                }
                String formatedString = String.format("Aliases: %1s \n" +
                        "Description: %2s \n" +
                        "Usage: %3s \n" +
                        "Required permission: %4s \n",
                        alias,
                        simpleCommand.getCommandAnnotation().description(),
                        simpleCommand.getCommandAnnotation().usage(),
                        simpleCommand.getCommandAnnotation().requiredPermissions()
                );
                embedBuilder.appendField(simpleCommand.getCommandAnnotation().aliases()[0], formatedString, false);

            }
        }
        EmbedObject returnObject = embedBuilder.build();
        user.getOrCreatePMChannel().sendMessage("", returnObject, false);
    }

    @Command(aliases = {"ban"}, description = "bans all tagged players", usage = "ban @playername")
    public String onCommandBan(IMessage message, IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        if (hasPerm(user, guild, "ExpeditMod")){
            List<IUser> users = message.getMentions();
            for (IUser user1:users) {
                guild.banUser(user1);
            }
            return "Users banned";
        }else{
            return "You do not have permission for that command";
        }
    }

    @Command(aliases = {"kick"}, description = "kicks all tagged players", usage = "kick @playername")
    public String onCommandKick(IMessage message, IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        String perm = null;
        try {
            perm = ExpeditConst.databaseUtils.getSetting("modrole", guild.getID());
        } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        if (perm == null)
            perm = "expeditMod";

        if (hasPerm(user, guild, perm)){
            List<IUser> users = message.getMentions();
            for (IUser user1:users) {
                guild.kickUser(user1);
            }
            return "Users kicked";
        }else{
            return "You do not have permission for that command";
        }
    }

    @Command(aliases = {"pardon"}, description = "kicks all tagged players", usage = "kick @playername")
    public void onCommandPardon(IMessage message, IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        String perm = null;
        try {
            perm = ExpeditConst.databaseUtils.getSetting("modrole", guild.getID());
        } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        if (perm == null)
            perm = "expeditMod";


        if (hasPerm(user, guild, perm)){
            for (IUser bannedUser:guild.getBannedUsers()) {
                for (String string:args) {
                    if(bannedUser.getName().equals(string)){
                        guild.pardonUser(bannedUser.getID());
                    }
                }
            }
            channel.sendMessage("Done!");
        }else{
            channel.sendMessage("You do not have permission for that command");
        }
    }

    @Command(aliases = {"bannedusers"}, description = "kicks all tagged players", usage = "kick @playername")
    public void onCommandBannedUsers(IMessage message, IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        String perm = null;
        try {
            perm = ExpeditConst.databaseUtils.getSetting("modrole", guild.getID());
        } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        if (perm == null)
            perm = "expeditMod";

        if (hasPerm(user, guild, perm)){
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.withTitle("Banned Users");
            embedBuilder.withColor(255, 0, 0);
            List<IUser> bannedUsers = guild.getBannedUsers();
            for (IUser user1:bannedUsers) {
                embedBuilder.appendField(user1.getName(), user1.getID(), true);
            }
            EmbedObject returnObject = embedBuilder.build();
            channel.sendMessage("", returnObject, false);
        }else{
            channel.sendMessage("You do not have permission for that command");
        }
    }

    @Command(aliases = {"clear", "purge"})
    public void onCommandPurge(IMessage message, IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        String perm = null;
        try {
            perm = ExpeditConst.databaseUtils.getSetting("modrole", guild.getID());
        } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        if (perm == null)
            perm = "expeditMod";

        if (hasPerm(user, guild, perm)){
            if (args[0] != null && args[0].matches("^(\\d)+$")){
                int amountToDelete = Integer.valueOf(args[0]) + 1;
                System.out.println(amountToDelete);
                RequestBufferHelper.RequestBufferDelete(channel, amountToDelete);
            }
            message.delete();
        }else{
            channel.sendMessage("You do not have permission for that command");
        }
    }

    @Command(aliases = {"nuke"})
    public void onCommandNuke(IMessage message, IUser user, IGuild guild, IChannel channel, String command, String[] args) throws RateLimitException, DiscordException, MissingPermissionsException {
        String perm = null;
        try {
            perm = ExpeditConst.databaseUtils.getSetting("modrole", guild.getID());
        } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        if (perm == null)
            perm = "expeditMod";

        if (hasPerm(user, guild, perm)){
            RequestBufferHelper.RequestBufferDelete(channel, true);
        }else{
            channel.sendMessage("You do not have permission for that command");
        }
    }

}
