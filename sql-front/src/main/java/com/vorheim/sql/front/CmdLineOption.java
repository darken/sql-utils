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

import org.apache.commons.cli.Option;

/**
 * Command line options.
 *
 * @author Gustavo Rodriguez
 */
public enum CmdLineOption {
    SELECT("s", 2, "Generates a SELECT statement traversing the foreign keys from the source table to the target.");

    private String opt;
    private int argsSize;
    private Option option;

    private CmdLineOption(String opt, int argsSize, String description) {
        this.opt = opt;
        this.argsSize = argsSize;

        option = Option.builder(opt)
                .longOpt(this.name().toLowerCase())
                .numberOfArgs(argsSize)
                .argName("source target")
                .desc(description)
                .build();
    }

    public String getOpt() {
        return opt;
    }

    public int getArgsSize() {
        return argsSize;
    }

    public Option getOption() {
        return option;
    }
}
