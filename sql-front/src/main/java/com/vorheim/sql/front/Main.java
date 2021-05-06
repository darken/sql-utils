/*
 * Copyright (C) 2021 Gustavo Rodriguez
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vorheim.sql.front;

import com.vorheim.sql.generator.SelectGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gustavo Rodriguez
 */
public final class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // TODO create in app
        Path p = Path.of("./config/settings.properties");

        var settings = new Properties();
        if (Files.notExists(p)) {
            try {
                if (Files.notExists(p.getParent()))
                    Files.createDirectories(p.getParent());
                Files.createFile(p);

                settings.put("db.url", System.console().readLine("[%s]", "Database URL:"));
                settings.put("db.user", System.console().readLine("[%s]", "Database username:"));
                settings.put("db.pass", System.console().readLine("[%s]", "Database password:"));

                try (var os = Files.newOutputStream(p, StandardOpenOption.WRITE, StandardOpenOption.WRITE)) {
                    settings.store(os, "File created");
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "", e);
            }
        } else {
            try (var is = Files.newInputStream(p, StandardOpenOption.READ)) {
                settings.load(is);
            } catch (IOException e) {
                logger.log(Level.WARNING, "", e);
            }
        }

        var cmdLine = new CmdLineManager(args);
        if (cmdLine.getOption() == null) {
            return;
        }

        try (var connection = DriverManager.getConnection(settings.getProperty("db.url"),
                settings.getProperty("db.user"), settings.getProperty("db.pass"))) {

            switch (cmdLine.getOption()) {
                case SELECT:
                    var generator = new SelectGenerator(connection);
                    var sourceTable = cmdLine.getOptionArgs().get(0);
                    var targetTable = cmdLine.getOptionArgs().get(1);
                    var select = generator.generate(sourceTable, targetTable); //cfg_campo
                    logger.info("\n" + select);
                    break;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "SQL error", e);
        }
    }

}
