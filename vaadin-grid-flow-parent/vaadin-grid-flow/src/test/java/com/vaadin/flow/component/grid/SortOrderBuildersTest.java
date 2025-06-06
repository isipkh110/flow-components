/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;

public class SortOrderBuildersTest {

    @Test
    public void gridSortOrderBuilder() {
        Grid<String> grid = new Grid<>();
        Column<String> col1 = grid.addColumn(string -> string);
        Column<String> col2 = grid.addColumn(string -> string);
        Column<String> col3 = grid.addColumn(string -> string);

        // construct with asc
        verifySortOrders(
                Arrays.asList(
                        new GridSortOrder<>(col1, SortDirection.ASCENDING),
                        new GridSortOrder<>(col2, SortDirection.DESCENDING),
                        new GridSortOrder<>(col3, SortDirection.ASCENDING)),
                GridSortOrder.asc(col1).thenDesc(col2).thenAsc(col3).build());
        // construct with desc
        verifySortOrders(
                Arrays.asList(
                        new GridSortOrder<>(col1, SortDirection.DESCENDING),
                        new GridSortOrder<>(col2, SortDirection.DESCENDING),
                        new GridSortOrder<>(col3, SortDirection.ASCENDING)),
                GridSortOrder.desc(col1).thenDesc(col2).thenAsc(col3).build());
    }

    @Test
    public void querySortOrderBuilder() {
        verifySortOrders(
                Arrays.asList(new QuerySortOrder("a", SortDirection.ASCENDING),
                        new QuerySortOrder("b", SortDirection.DESCENDING),
                        new QuerySortOrder("c", SortDirection.ASCENDING)),
                QuerySortOrder.asc("a").thenDesc("b").thenAsc("c").build());
        verifySortOrders(
                Arrays.asList(new QuerySortOrder("a", SortDirection.DESCENDING),
                        new QuerySortOrder("b", SortDirection.DESCENDING),
                        new QuerySortOrder("c", SortDirection.ASCENDING)),
                QuerySortOrder.desc("a").thenDesc("b").thenAsc("c").build());
        verifySortOrders(
                Arrays.asList(new QuerySortOrder("b", SortDirection.DESCENDING),
                        new QuerySortOrder("a", SortDirection.DESCENDING),
                        new QuerySortOrder("c", SortDirection.ASCENDING)),
                QuerySortOrder.desc("b").thenDesc("a").thenAsc("c").build());
    }

    private <T extends SortOrder<?>> void verifySortOrders(List<T> order1,
            List<T> order2) {
        Assert.assertEquals(order1.size(), order2.size());
        for (int i = 0; i < order1.size(); i++) {
            Assert.assertEquals(order1.get(i).getDirection(),
                    order2.get(i).getDirection());
            Assert.assertEquals(order1.get(i).getSorted(),
                    order1.get(i).getSorted());
        }
    }
}
