/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/view")
public class ViewPage extends Div {
    public ViewPage() {
        Map map = new Map();
        map.getView().setZoom(5);
        DebugMapEventDiv debugMapEventDiv = new DebugMapEventDiv(map);

        NativeButton setCenterButton = new NativeButton("Set Center", e -> {
            // here we set center
            map.getView().setCenter(new Coordinate(22.3, 60.45));
        });
        setCenterButton.setId("set-center-button");

        NativeButton setZoom = new NativeButton("Set Zoom", e -> {
            map.getView().setCenter(new Coordinate(22.3, 60.45));
            map.getView().setZoom(14);
        });
        setZoom.setId("set-zoom-button");

        NativeButton setRotation = new NativeButton("Set Rotation", e -> {
            // 45 degrees
            map.getView().setRotation(0.785398f);
        });
        setRotation.setId("set-rotation-button");

        NativeButton setZoomLimits = new NativeButton("Set Zoom Limits", e -> {
            map.getView().setMinZoom(3.5);
            map.getView().setMaxZoom(12.5);
            debugMapEventDiv.updateZoomLimits();
        });
        setZoomLimits.setId("set-zoom-limits-button");

        NativeButton resetZoomLimits = new NativeButton("Reset Zoom Limits",
                e -> {
                    map.getView().setMinZoom(null);
                    map.getView().setMaxZoom(null);
                    debugMapEventDiv.updateZoomLimits();
                });
        resetZoomLimits.setId("reset-zoom-limits-button");

        NativeButton setZoomBelowMinimum = new NativeButton(
                "Set Zoom Below Minimum", e -> {
                    map.getView().setZoom(2);
                    debugMapEventDiv.updateZoomLimits();
                });
        setZoomBelowMinimum.setId("set-zoom-below-minimum-button");

        NativeButton setZoomAboveMaximum = new NativeButton(
                "Set Zoom Above Maximum", e -> {
                    map.getView().setZoom(14);
                    debugMapEventDiv.updateZoomLimits();
                });
        setZoomAboveMaximum.setId("set-zoom-above-maximum-button");

        add(map, setCenterButton, setZoom, setRotation, setZoomLimits,
                resetZoomLimits, setZoomBelowMinimum, setZoomAboveMaximum,
                debugMapEventDiv);
    }

    private static class DebugMapEventDiv extends Div {

        private final Map map;
        private final Pre zoomLimitsInfo;

        public DebugMapEventDiv(Map map) {
            this.map = map;

            // HTML-basierte Log-Ausgaben
            Pre viewEventInfo = new Pre();
            viewEventInfo.getStyle().set("white-space", "pre-wrap");
            viewEventInfo.setWidthFull();

            zoomLimitsInfo = new Pre();
            zoomLimitsInfo.getStyle().set("white-space", "pre-wrap");
            zoomLimitsInfo.setWidthFull();

            add(new Div(new Div("Zoom Limits"), zoomLimitsInfo),
                    new Div(new Div("View Move End Event"), viewEventInfo));
            updateZoomLimits();

            // Event Listener
            map.addViewMoveEndListener(e -> {
                Coordinate center = e.getCenter();
                Extent extent = e.getExtent();

                String info = "";
                info += String.format("Zoom   = %s%n", e.getZoom());
                info += String.format("Center = { x: %s, y: %s }%n",
                        center.getX(), center.getY());
                info += String.format("Extent = { left: %s, top: %s,%n",
                        extent.getMinX(), extent.getMinY());
                info += String.format("           right: %s, bottom: %s }",
                        extent.getMaxX(), extent.getMaxY());

                viewEventInfo.setText(info);
                updateZoomLimits();
            });
        }

        private void updateZoomLimits() {
            zoomLimitsInfo.setText(String.format("Min Zoom = %s%nMax Zoom = %s",
                    map.getView().getMinZoom(), map.getView().getMaxZoom()));
        }
    }
}
