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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Generates SQL select statements based on table relationships.
 *
 * @author Gustavo Rodriguez
 */
public final class SelectGenerator implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(SelectGenerator.class.getName());

    private final Connection connection;

    private final Set<String> processedTables;

    private final Deque<ImportedKey> frontier;

    public SelectGenerator(Connection connection) {
        Objects.requireNonNull(connection);
        this.connection = connection;
        processedTables = new HashSet<>();
        frontier = new ArrayDeque<>();
    }

    public String generate(final String sourceTable, final String targetTable) throws SQLException {
        if (StringUtils.isBlank(sourceTable) || StringUtils.isBlank(targetTable))
            throw new IllegalArgumentException("Tables can't be null");

        var metaData = connection.getMetaData();

        addImportedKeysToFrontier(metaData, sourceTable, null);

        while (!frontier.isEmpty()) {
            final var key = frontier.pollFirst();
            final var pkTable = key.getPkTable();

            if (targetTable.equals(pkTable)) {
                return formatKeysToSelect(key);
            }
            addImportedKeysToFrontier(metaData, pkTable, key);
        }
        return MessageFormat.format("Relationship path not found from {0} to {1} ", sourceTable, targetTable);
    }

    private void addImportedKeysToFrontier(DatabaseMetaData metaData, final String table, final ImportedKey parentKey) throws SQLException {
        var keys = new ArrayList<ImportedKey>();
        try (var resultSet = metaData.getImportedKeys(null, null, table)) {
            while (resultSet.next()) {
                // TODO composite fks
                // https://dba.stackexchange.com/questions/147374/composite-foreign-key-that-is-optional
                // TODO test with databases other than postgresql
                var importedKey = new ImportedKey(
                        resultSet.getString("pktable_name"),  // 3
                        resultSet.getString("pkcolumn_name"), // 4
                        resultSet.getString("fktable_name"),  // 7
                        resultSet.getString("fkcolumn_name"), // 8
                        parentKey
                );

                if (processedTables.add(importedKey.getPkTable()))
                    keys.add(importedKey);
            }
        }
        frontier.addAll(keys);
    }

    private String formatKeysToSelect(ImportedKey key) {
        final TableAliasContext aliasContext = new TableAliasContext();
        final StringBuilder sb = new StringBuilder();

        ImportedKey firstKey = key;
        ImportedKey lastKey = null;
        ImportedKey currentKey = key;
        while (currentKey != null) {
            var fkTable = currentKey.getFkTable();
            var fkAlias = aliasContext.getAlias(fkTable);
            var pkAlias = aliasContext.getAlias(currentKey.getPkTable());

            sb.append(MessageFormat.format("  JOIN {0} {1} ON {1}.{2} = {3}.{4}\n",
                    fkTable, fkAlias, key.getFkColumn(), pkAlias, key.getPkColumn()));

            lastKey = currentKey;
            currentKey = currentKey.getParent();
        }

        sb.insert(0, MessageFormat.format("  FROM {0} {1}\n",
                firstKey.getPkTable(), aliasContext.getAlias(firstKey.getPkTable())));
        sb.insert(0, MessageFormat.format("SELECT {0}.*\n",
                aliasContext.getAlias(lastKey.getFkTable())));
        sb.append(MessageFormat.format(" WHERE {0}.{1} = :id",
                aliasContext.getAlias(firstKey.getPkTable()), firstKey.getPkColumn()));

        return sb.toString();
    }

    private static class ImportedKey {
        private final String pkTable;
        private final String pkColumn;

        private final String fkTable;
        private final String fkColumn;

        private final ImportedKey parent;

        public ImportedKey(String pkTable, String pkColumn, String fkTable, String fkColumn, ImportedKey parent) {
            this.pkTable = pkTable;
            this.pkColumn = pkColumn;
            this.fkTable = fkTable;
            this.fkColumn = fkColumn;
            this.parent = parent;
        }

        public String getPkTable() {
            return pkTable;
        }

        public String getPkColumn() {
            return pkColumn;
        }

        public String getFkTable() {
            return fkTable;
        }

        public String getFkColumn() {
            return fkColumn;
        }

        public ImportedKey getParent() {
            return parent;
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
