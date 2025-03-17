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
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.MAX_LENGTH_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.MIN_LENGTH_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.PATTERN_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBasicValidationPage.REQUIRED_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-email-field/validation/basic")
public class EmailFieldBasicValidationIT
        extends AbstractValidationIT<EmailFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("john@vaadin.com");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void minLength_changeValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("13", Keys.ENTER);

        testField.setValue("a@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_LENGTH_ERROR_MESSAGE);

        testField.setValue("aa@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.setValue("aaa@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void maxLength_changeValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("13", Keys.ENTER);

        testField.setValue("aaa@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_LENGTH_ERROR_MESSAGE);

        testField.setValue("aa@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.setValue("a@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void defaultPattern_changeValue_assertValidity() {
        testField.setValue("arbitrary string");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(PATTERN_ERROR_MESSAGE);

        testField.setValue("john@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void pattern_changeValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^[^\\d]+@vaadin.com$",
                Keys.ENTER);

        testField.setValue("2222@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(PATTERN_ERROR_MESSAGE);

        testField.setValue("john@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("john@vaadin.com");
        testField.setValue("");

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attachAndInvalidate_preservesInvalidState() {
        detachField();
        attachAndInvalidateField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_hide_attach_showAndInvalidate_preservesInvalidState() {
        detachField();
        hideField();
        attachField();
        showAndInvalidateField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("john@vaadin.com");
        testField.setValue("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected EmailFieldElement getTestField() {
        return $(EmailFieldElement.class).first();
    }
}
