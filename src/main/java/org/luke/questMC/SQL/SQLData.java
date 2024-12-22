package org.luke.questMC.SQL;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class SQLData {
    @Getter
    private String URL;
    @Getter
    private String USERNAME;
    @Getter
    private String PASSWORD;
    @Getter
    private String DATABASE_NAME;

    public static void Initialization(String url, String username, String password, String databaseName) {
        URL = url;
        USERNAME = username;
        PASSWORD = password;
        DATABASE_NAME = databaseName;
    }
}