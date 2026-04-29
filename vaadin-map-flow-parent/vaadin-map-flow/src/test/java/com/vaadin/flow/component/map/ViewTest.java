/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.View;

class ViewTest {

    @Test
    void setCenter_doesNotAllowNullValue() {
        View view = new View();

        Assertions.assertThrows(NullPointerException.class,
                () -> view.setCenter(null));
    }

    @Test
    void viewProjectionDefaultIsSet() {
        View view = new View();

        Assertions.assertEquals("EPSG:3857", view.getProjection());
    }

    @Test
    void viewProjectionDefaultCanBeChanged() {
        View view = new View("EPSG:4326");

        Assertions.assertEquals("EPSG:4326", view.getProjection());
    }

    @Test
    void defaultView_getMinZoomAndMaxZoom_returnsNull() {
        View view = new View();

        Assertions.assertNull(view.getMinZoom());
        Assertions.assertNull(view.getMaxZoom());
    }

    @Test
    void view_setMinZoomAndMaxZoom_updatesValues() {
        View view = new View();

        view.setMinZoom(2.5);
        view.setMaxZoom(17.5);

        Assertions.assertEquals(2.5, view.getMinZoom());
        Assertions.assertEquals(17.5, view.getMaxZoom());
    }

    @Test
    void view_setMinZoomAndMaxZoomToNull_returnsNull() {
        View view = new View();
        view.setMinZoom(2.5);
        view.setMaxZoom(17.5);

        view.setMinZoom(null);
        view.setMaxZoom(null);

        Assertions.assertNull(view.getMinZoom());
        Assertions.assertNull(view.getMaxZoom());
    }
}
