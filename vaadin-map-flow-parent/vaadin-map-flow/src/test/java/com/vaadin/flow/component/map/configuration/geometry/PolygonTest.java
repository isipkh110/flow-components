/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.Coordinate;

public class PolygonTest {

    private PropertyChangeListener propertyChangeListenerMock;

    @Before
    public void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    public void defaults() {
        final Polygon polygon = new Polygon(List.of(new Coordinate(1, 1),
                new Coordinate(2, 2), new Coordinate(1, 1)));

        final Coordinate[][] coordinates = polygon.getCoordinates();
        Assert.assertNotNull(coordinates);
        Assert.assertEquals(1, coordinates.length);

        final Coordinate[] linearRing = coordinates[0];
        Assert.assertEquals(3, linearRing.length);

        Assert.assertEquals(1, linearRing[0].getX(), 0);
        Assert.assertEquals(1, linearRing[0].getY(), 0);

        Assert.assertEquals(2, linearRing[1].getX(), 0);
        Assert.assertEquals(2, linearRing[1].getY(), 0);

        Assert.assertEquals(1, linearRing[2].getX(), 0);
        Assert.assertEquals(1, linearRing[2].getY(), 0);
    }

    @Test
    public void failsWithNullOrEmptyValues() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon((List<Coordinate>) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon(List.of()));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon((Coordinate[][]) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon(new Coordinate[0][]));
    }

    @Test
    public void setCoordinates() {
        final TestPolygon polygon = new TestPolygon(
                List.of(new Coordinate(1, 1), new Coordinate(2, 2),
                        new Coordinate(1, 1)));

        polygon.addPropertyChangeListener(propertyChangeListenerMock);
        polygon.setCoordinates(List.of(new Coordinate(10, 10),
                new Coordinate(20, 20), new Coordinate(10, 10)));

        final Coordinate[][] coordinates = polygon.getCoordinates();
        Assert.assertNotNull(coordinates);
        Assert.assertEquals(1, coordinates.length);

        final Coordinate[] linearRing = coordinates[0];
        Assert.assertEquals(3, linearRing.length);

        Assert.assertEquals(10, linearRing[0].getX(), 0);
        Assert.assertEquals(10, linearRing[0].getY(), 0);

        Assert.assertEquals(20, linearRing[1].getX(), 0);
        Assert.assertEquals(20, linearRing[1].getY(), 0);

        Assert.assertEquals(10, linearRing[2].getX(), 0);
        Assert.assertEquals(10, linearRing[2].getY(), 0);

        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setCoordinates_failsWithNullOrEmptyValues() {
        final Polygon polygon = new Polygon(List.of(new Coordinate()));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates((List<Coordinate>) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates(List.of()));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates((Coordinate[][]) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates(new Coordinate[0][]));
    }

    @Test
    public void translate() {
        final Polygon polygon = new Polygon(List.of(new Coordinate(1, 1),
                new Coordinate(2, 2), new Coordinate(1, 1)));

        polygon.translate(10, 10);

        final Coordinate[][] coordinates = polygon.getCoordinates();
        Assert.assertNotNull(coordinates);
        Assert.assertEquals(1, coordinates.length);

        final Coordinate[] linearRing = coordinates[0];
        Assert.assertEquals(3, linearRing.length);

        Assert.assertEquals(11, linearRing[0].getX(), 0);
        Assert.assertEquals(11, linearRing[0].getY(), 0);

        Assert.assertEquals(12, linearRing[1].getX(), 0);
        Assert.assertEquals(12, linearRing[1].getY(), 0);

        Assert.assertEquals(11, linearRing[2].getX(), 0);
        Assert.assertEquals(11, linearRing[2].getY(), 0);
    }

    private static class TestPolygon extends Polygon {
        public TestPolygon(List<Coordinate> coordinates) {
            super(coordinates);
        }

        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}
