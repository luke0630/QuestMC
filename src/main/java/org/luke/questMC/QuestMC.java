package org.luke.questMC;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.luke.questMC.Command.CommandManager;
import org.luke.questMC.Event.EventManager;
import org.luke.questMC.GUI.*;
import org.luke.questMC.GUI.Confirm.GUI_Confirm;
import org.luke.questMC.Quest.Normal.*;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.SQL.SQLData;
import org.luke.questMC.SQL.SQLManager;
import org.luke.yakisobaGUILib.YakisobaGUILib;

import java.util.*;

import static org.luke.questMC.SQL.SQLManager.*;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public final class QuestMC extends JavaPlugin implements Listener {
    @Getter
    private static QuestMC instance;
    @Getter
    private static YakisobaGUILib<GUITypes.GUIEnum, GUITypes.ListGUIEnum> manager;
    @Getter
    private static DataClass guiManager;
    @Getter
    private static FileConfiguration settingConfig;

    @Override
    public void onEnable() {
        instance = this;
        manager = new YakisobaGUILib<>(this, List.of(
                new List_Quests(),
                new List_QuestDetails(),
                new List_CompletedQuests(),

                new QuestHome(),
                new GUI_Confirm()
        ));

        guiManager = new DataClass();

        saveDefaultConfig();
        settingConfig = getConfig();
        LoadConfig();

        ConnectionToDatabase(() -> CreateDatabase(() -> {}));

        var command = getCommand("quest");
        Objects.requireNonNull(command).setExecutor(new CommandManager());

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new EventManager(), this);

        QuestManager.registerQuest(
            List.of(
                    new Quest_HelloWorld(),
                    new Quest_Master_of_Crafting_Table(),
                    new Quest_The_Executor_Of_Land_Leveling(),
                    new Quest_Manager_of_the_bed(),
                    new Quest_Fisher_of_the_Sea(),
            )
        );

        LoadProgressData(this::LoadAllProgressData);
    }

    @Override
    public void onDisable() {
        for(Map.Entry<Enum<QuestEnum.Quest_Normal>, QuestBase> entry :  QuestManager.getQuests().entrySet()) {
            SQLManager.SaveProgressData((QuestEnum.Quest_Normal) entry.getKey(), entry.getValue().SaveJson());
        }
    }

    private void LoadConfig() {
        String url = settingConfig.getString("mysql-url");
        String username = settingConfig.getString("mysql-username");
        String password = settingConfig.getString("mysql-password");
        String databaseName = settingConfig.getString("mysql-database-name");
        SQLData.Initialization(url, username, password, databaseName);
    }

    private void LoadAllProgressData() {
        for(QuestBase quest : QuestManager.getQuests().values()) {
            quest.LoadJson(
                    LoadProgressData(quest.getType())
            );
            for(UUID uuid : QuestManager.getProgressInfo().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                SendFirstMessage(quest.getType(), player);
            }
        }
    }

    public static void SendFirstMessage(QuestEnum.Quest_Normal quest_type, Player player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(QuestMC.getInstance(), () -> {
            if(player == null) return;

            UUID uuid = player.getUniqueId();
            QuestBase quest = QuestManager.getQuest(quest_type);

            if(!QuestManager.isProgressPlayer(uuid)) return;
            if(QuestManager.getProgressInfo().get(uuid).getType() != quest.getType()) return;
            if(quest.getProgressInfo(player) == null) return;

            List<String> progressInfo = quest.getProgressInfo(player);
            QuestManager.UpdateProgressInfo(
                    player,
                    progressInfo
            );

            List<String> messageList = new ArrayList<>(List.of(
                    "&e===================================",
                    "&b現在クエストが進行中です。 クエスト: " + quest.getType().getTitle(),
                    "&c&l-----進行状況-----"
            ));
            messageList.addAll(progressInfo);
            messageList.add("&e===================================");

            for(String message : messageList) {
                player.sendMessage(toColor("&a"+message));
            }
        }, 2*20L);
    }
}
