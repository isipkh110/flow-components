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
package com.vaadin.flow.component.masterdetaillayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.masterdetaillayout.testbench.MasterDetailLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-master-detail-layout")
public class MasterDetailLayoutIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void basics() {
        MasterDetailLayoutElement layout = $(MasterDetailLayoutElement.class)
                .waitForFirst();

        boolean hasShadowRoot = (Boolean) executeScript(
                "return arguments[0].shadowRoot !== null", layout);
        String componentName = (String) executeScript(
                "return Object.getPrototypeOf(arguments[0]).constructor.is",
                layout);

        Assert.assertTrue(hasShadowRoot);
        Assert.assertEquals("vaadin-master-detail-layout", componentName);

        TestBenchElement master = layout.getMaster();
        TestBenchElement detail = layout.getDetail();

        Assert.assertNotNull(master);
        Assert.assertEquals("Master content", master.getText());
        Assert.assertNotNull(detail);
        Assert.assertEquals("Detail content", detail.getText());
    }
}
