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


import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses the command line options and arguments.
 *
 * @author Gustavo Rodriguez
 */
public final class CmdLineManager {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private CmdLineOption option;

    private List<String> optionArgs;

    private boolean useGUI;

    public CmdLineManager(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        var options = new Options();

        for (var opt : CmdLineOption.values()) {
            options.addOption(opt.getOption());
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
            option = findOption(cmdLine);
        } catch (ParseException e) {
        }

        if (option == null || cmdLine == null) {
            new HelpFormatter().printHelp("sql-front -[OPTION] <args>", options);
            return;
        }

        switch (option) {
            case SELECT:
                var tables = cmdLine.getOptionValues("s");
                logger.log(Level.INFO, "-{0} {1}", new Object[]{option, Arrays.toString(tables)});
                optionArgs = List.of(tables);
                break;
        }
    }

    private CmdLineOption findOption(CommandLine cmdLine) throws ParseException {
        for (var opt : CmdLineOption.values()) {
            if (cmdLine.hasOption(opt.getOpt())) {
                return opt;
            }
        }
        return null;
    }

    // TODO use multiple options
    public CmdLineOption getOption() {
        return option;
    }

    public List<String> getOptionArgs() {
        return optionArgs;
    }

    public boolean isUseGUI() {
        return useGUI;
    }
}
