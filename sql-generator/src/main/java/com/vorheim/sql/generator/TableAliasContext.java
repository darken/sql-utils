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
package com.vorheim.sql.generator;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and keeps record of table aliases.
 *
 * @author Gustavo Rodriguez
 */
public class TableAliasContext {

    private static final int ALIAS_LENGTH = 3;

    private Map<String, String> tableAliasMap;
    private Map<String, Integer> aliasCount;

    public TableAliasContext() {
        tableAliasMap = new HashMap<>();
        aliasCount = new HashMap<>();
    }

    /**
     * <p>Obtains an alias based on the table name.</p>
     *
     * <p>If the name is composed by words separated by has underscores (for_example) the resulting alias will be the
     * first letters of each word.</p>
     *
     * <p>If the name length is equal or less than {@value ALIAS_LENGTH} the method return the name without any
     * modifications.</p>
     *
     * <p>Keeps the alias of a table for reuse. If there's collision of aliases for multiple different table names the
     * an incremental number is appended at the end of the new aliases e.g. tab, tab2, tab3, etc.</p>
     *
     * @param tableName Name of the table
     * @return Alias
     */
    public String getAlias(final String tableName) {
        var alias = tableAliasMap.get(tableName);
        if (alias != null)
            return alias;

        alias = makeAlias(tableName);


        var count = aliasCount.get(alias);
        if (count == null) {
            aliasCount.put(alias, 1);
        } else {
            aliasCount.put(alias, ++count);
            alias += count;
        }

        tableAliasMap.put(tableName, alias);
        return alias;
    }

    private String makeAlias(final String tableName) {
        if (tableName.length() <= ALIAS_LENGTH)
            return tableName;

        if (tableName.contains("_")) {
            var parts = tableName.split("_");
            var alias = Arrays.stream(parts).reduce("",
                    (acc, val) -> acc + (StringUtils.isBlank(val) ? "" : val.charAt(0)));

            if (alias.length() > 1) {
                return alias;
            }
        }
        return tableName.substring(0, ALIAS_LENGTH);
    }
}
