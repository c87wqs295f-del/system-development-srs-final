package com.fs.srs.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * this generates the single sql database connection creates schema.sql on the first run

 */
public final class Database {

    private final Connection connection; 

    public Database(String jdbcUrl) {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            applySchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database: " + jdbcUrl, e);
        }
    }

    /** Creates the datavase file on local device  */
    public static Database openDefault() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create ./data directory", e);
        }
        return new Database("jdbc:sqlite:data/srs.db");
    }

    public Connection getConnection() {
        return connection;
    }

    private void applySchema() {
        String ddl = readSchemaResource();
        try (Statement st = connection.createStatement()) {
            for (String stmt : ddl.split(";")) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    st.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to apply schema.sql", e);
        }
    }

    private String readSchemaResource() {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql not found on classpath");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema.sql", e);
        }
    }
}
