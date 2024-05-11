package lightwolf.lwrestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LwRestart extends JavaPlugin {
    private int countdownTaskId;
    private int countdownTime;
    private String restartTitle;
    private String cancelTitle;
    private int cancelDuration;

    @Override
    public void onEnable() {
        getLogger().info("LWRestartPlugin has been enabled!");

        // Загрузка конфига при включении плагина
        saveDefaultConfig();
        loadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("LWRestartPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("lwRestart")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("stop")) {
                    stopCountdown();
                    return true;
                } else {
                    try {
                        int time = Integer.parseInt(args[0]);
                        if (time > 0 && time <= 30) {
                            startCountdown(time, restartTitle, countdownTaskId);
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "Введите число от 1 до 30!");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Введите корректное число или 'stop' для отмены перезагрузки!");
                        return false;
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Использование: /lwRestart <время> или /lwRestart stop");
                return false;
            }
        }
        return false;
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().warning("Config not found, creating default config...");
            saveDefaultConfig();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        restartTitle = ChatColor.translateAlternateColorCodes('&', config.getString("restartTitle"));
        cancelTitle = ChatColor.translateAlternateColorCodes('&', config.getString("cancelTitle", "&aПерезагрузка отменена"));
        cancelDuration = config.getInt("cancelDuration", 3); // устанавливаем значение по умолчанию в 3 секунды
    }

    private class CountdownTask implements Runnable {
        private int remainingTime;

        public CountdownTask(int time) {
            this.remainingTime = time;
        }

        @Override
        public void run() {
            if (remainingTime > 0) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(restartTitle, "через " + remainingTime + " секунд", 10, 20, 10);
                }
                remainingTime--;
            } else {
                Bukkit.getServer().shutdown();
                Bukkit.getScheduler().cancelTask(countdownTaskId);
            }
        }
    }

    private void startCountdown(int time, String title, int taskId) {
        countdownTime = time;
        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new CountdownTask(time), 0L, 20L);
    }

    private void stopCountdown() {
        if (countdownTaskId != 0) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
            countdownTaskId = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle(cancelTitle, "", 0, cancelDuration * 20, 20); // Преобразуем секунды в тики
            }
        }
    }
}
