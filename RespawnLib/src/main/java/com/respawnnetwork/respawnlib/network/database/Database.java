package com.respawnnetwork.respawnlib.network.database;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.*;

public class Database {
    private final Logger log;
    private final String host, port, database, username, password;
    private Connection conn = null;


    public DSLContext getContext() {
        if(connected()) {
            return using(conn, SQLDialect.MARIADB);
        }

        return null;
    }

    public Database(Logger log, String host, String port, String database, String username, String password) {
        this.log = log;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public boolean connected() {
        try {
            return conn != null && !conn.isClosed();

        } catch (SQLException e) {
            return false;
        }
    }

    public Connection open() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);

        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to connect to database", e);
        }

        return conn;
    }

    public boolean close() {
        if(connected()) {
            try {
                conn.close();

            } catch (SQLException e) {
                log.log(Level.WARNING, "Failed to close connection", e);
                return false;
            }
        }

        return true;
    }

}
