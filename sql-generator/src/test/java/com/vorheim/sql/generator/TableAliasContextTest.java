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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gustavo Rodriguez
 */
public class TableAliasContextTest {

    private TableAliasContext aliasContext;

    @BeforeEach
    void init() {
        aliasContext = new TableAliasContext();
    }

    @Test
    void testGetAlias() {
        assertEquals("twd",
                aliasContext.getAlias("table_with_dash"));
        assertEquals("tw",
                aliasContext.getAlias("table_with"));
        assertEquals("tab",
                aliasContext.getAlias("table"));
        assertEquals("tb",
                aliasContext.getAlias("tb"));

        assertEquals("tab2",
                aliasContext.getAlias("tab"));
        assertEquals("tab3",
                aliasContext.getAlias("table_and_bits"));

        assertEquals("ottf",
                aliasContext.getAlias("one_two_three_four"));
        assertEquals("ac",
                aliasContext.getAlias("ab___c__"));
        assertEquals("___",
                aliasContext.getAlias("___"));
    }
}
