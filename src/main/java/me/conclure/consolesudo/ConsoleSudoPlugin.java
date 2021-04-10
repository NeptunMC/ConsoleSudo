package me.conclure.consolesudo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

public final class ConsoleSudoPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    final PluginCommand consolesudo = super.getCommand("consolesudo");
    final Server server = ConsoleSudoPlugin.this.getServer();
    final CommandSender consoleSender = server.getConsoleSender();
    final PluginManager pluginManager = server.getPluginManager();
    SimpleCommandMap commandMap;

    try {
      final Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
      commandMapField.setAccessible(true);
      commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);
    } catch (ReflectiveOperationException | ClassCastException e) {
      throw new LinkageError("Unable to retrieve SimpleCommandMap instance", e);
    }

    if (commandMap == null) {
      throw new LinkageError("Unable to retrieve SimpleCommandMap instance");
    }

    final SimpleCommandMap simpleCommandMap = commandMap;

    consolesudo.setExecutor((sender, cmd, label, args) -> {
      if (!(sender instanceof Player)) {
        return true;
      }

      if (!sender.hasPermission("consolesudo.use")) {
        return true;
      }


      server.dispatchCommand(consoleSender, StringUtils.join(args, ' '));
      return true;
    });
    consolesudo.setTabCompleter(
        (sender, cmd, label, args) -> {
          if (!(sender instanceof Player)) {
            return Collections.emptyList();
          }

          if (!sender.hasPermission("consolesudo.use")) {
            return Collections.emptyList();
          }

          if (args.length == 0) {
            return Collections.emptyList();
          }

          if (args.length == 1) {
            List<String> commands = new ArrayList<>();

            for (Command command : simpleCommandMap.getCommands()) {
              commands.add(command.getName());
            }

            return StringUtil.copyPartialMatches(args[0],commands,new ArrayList<>());
          }

          final Command command = simpleCommandMap.getCommand(args[0]);

          if (command == null) {
            return Collections.emptyList();
          }

          return command.tabComplete(sender, label, Arrays.copyOfRange(args, 1, args.length));
        });
  }
}
