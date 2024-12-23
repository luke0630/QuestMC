package org.luke.questMC.SQL;

import org.json.JSONArray;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLManager {
    public interface MyCallback {
        void onComplete(); // コールバックが完了したときに呼ばれるメソッド
    }

    static Connection connection = null;
    static final String tableName = "playerQuestData";
    final static String column_uuid = "minecraft_uuid";
    static final String column_quests_cleared = "quests_cleared";
    static final String column_quest_current = "quest_current";

    static final String progress_tableName = "questProgressData";
    final static String progress_column_type = "type";
    final static String progress_column_data = "data";

    public static void ConnectionToDatabase(MyCallback result) {
        String url = "jdbc:mysql://" + SQLData.getURL();
        try {
            // MySQLドライバのロード
            Class.forName("com.mysql.cj.jdbc.Driver");
            // データベースへの接続
            System.out.println(SQLData.getUSERNAME() + "   " + SQLData.getPASSWORD());
            connection = DriverManager.getConnection(url, SQLData.getUSERNAME(), SQLData.getPASSWORD());

            result.onComplete();
        } catch (Exception ignored) {

        }
    }

    public static void CreateDatabase(MyCallback callback) {
        final String dbName = SQLData.getDATABASE_NAME();
        List<String> executes = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();

            executes.add("CREATE DATABASE IF NOT EXISTS " + dbName);

            executes.add("USE " + dbName);

            String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    column_uuid + " VARCHAR(50) NOT NULL, " +
                    column_quests_cleared + " JSON NULL," +
                    column_quest_current + " VARCHAR(64) NULL," +
                    " PRIMARY KEY ( " + column_uuid + " )" +
                    " );";
            executes.add(createTable);

            String createTable_progress = "CREATE TABLE IF NOT EXISTS " + progress_tableName + " ( " +
                    progress_column_type + " VARCHAR(50) NOT NULL, " +
                    progress_column_data + " JSON NULL," +
                    " PRIMARY KEY ( " + progress_column_type + " )" +
                    " );";
            executes.add(createTable_progress);

            for (String execute : executes) {
                statement.executeUpdate(execute);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            callback.onComplete();
        }
    }
    public static void SaveProgressData(QuestEnum.Quest_Normal type, String json) {
        try {
            if(isExistsKey(progress_tableName, progress_column_type, type.name())) {
                String query = "UPDATE "+ progress_tableName +" SET "+ progress_column_data +" = ? WHERE "+ progress_column_type +" = ?;";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, String.valueOf(json));
                ps.setString(2, type.name());
                ps.executeUpdate();
            } else {
                String query = "INSERT INTO " + progress_tableName + " (" + progress_column_type + ", " + progress_column_data + ") VALUES (?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, type.name());
                ps.setString(2, String.valueOf(json));
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String LoadProgressData(QuestEnum.Quest_Normal type) {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("USE " + SQLData.getDATABASE_NAME());
            String query = "SELECT " + progress_column_data + " FROM " + progress_tableName + " WHERE " + progress_column_type + " = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, type.name());

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(column_quests_cleared);
            }

            resultSet.close();
            ps.close();
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        return null;
    }


    public static void addAndUpdateQuestData(String uuid, List<QuestEnum.Quest_Normal> quests) {
        try {
            List<String> string_quests = new ArrayList<>();
            for(QuestEnum.Quest_Normal quest : quests) {
                string_quests.add(quest.name());
            }
            JSONArray obj = new JSONArray(string_quests);

            if(isExistsKey(tableName, column_uuid, uuid)) {
                String query = "UPDATE "+ tableName +" SET "+ column_quests_cleared +" = ? WHERE "+ column_uuid +" = ?;";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, String.valueOf(obj));
                ps.setString(2, uuid);
                ps.executeUpdate();
            } else {
                String query = "INSERT INTO " + tableName + " (" + column_uuid + ", " + column_quests_cleared + ") VALUES (?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, uuid);
                ps.setString(2, String.valueOf(obj));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        } finally {

        }
    }

    public static void addClearedEnum(String uuid, QuestEnum.Quest_Normal quest) {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("USE " + SQLData.getDATABASE_NAME());
            String query = "SELECT " + column_quests_cleared + " FROM " + tableName + " WHERE " + column_uuid + " = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, uuid);

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString(column_quests_cleared);
                JSONArray jsonObj = new JSONArray(result);

                // contains の代替としてループでチェック
                boolean contains = false;
                for (int i = 0; i < jsonObj.length(); i++) {
                    if (jsonObj.getString(i).equals(quest.name())) {
                        contains = true;
                        break;
                    }
                }

                if(!contains) {
                    jsonObj.put(quest.name());

                    String updateQuery = "UPDATE "+ tableName +" SET "+ column_quests_cleared +" = ? WHERE "+ column_uuid +" = ?;";
                    PreparedStatement updatePs = connection.prepareStatement(updateQuery);
                    updatePs.setString(1, String.valueOf(jsonObj));
                    updatePs.setString(2, uuid);
                    updatePs.executeUpdate();
                } else {
                    System.out.println("すでに含まれています");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    public static List<QuestEnum.Quest_Normal> getClearedEnumList(String uuid) {
        var logger = QuestMC.getInstance().getLogger();
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("USE " + SQLData.getDATABASE_NAME());
            String query = "SELECT " + column_quests_cleared + " FROM " + tableName + " WHERE " + column_uuid + " = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, uuid);

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString(column_quests_cleared);
                logger.info(result);

                JSONArray jsonObj = new JSONArray(result);
                List<QuestEnum.Quest_Normal> types = new ArrayList<>();
                for (int i = 0; i < jsonObj.length(); i++) {
                    String string_type = jsonObj.getString(i);

                    try {
                        QuestEnum.Quest_Normal type = QuestEnum.Quest_Normal.valueOf(string_type);
                        types.add(type);
                    } catch (IllegalArgumentException e) {
                        System.out.println(string_type + "をEnumに変換できませんでした。");
                    }
                }

                return types;
            } else {
                logger.info("No data found for UUID: " + uuid);
            }

            resultSet.close();
            ps.close();
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        return null;
    }

    //Utility class
    public static boolean isExistsKey(String table, String key_column, String key) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM "+ table +" WHERE "+ table +"."+ key_column +" = ?) AS exists_flag;");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) { // カーソルを結果セットの最初の行に移動
                return rs.getBoolean("exists_flag");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
