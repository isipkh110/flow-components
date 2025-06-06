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
package com.vaadin.flow.component.textfield;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;

/**
 * Abstract base class for components based on {@code vaadin-number-field}
 * element and its subclasses.
 *
 * @author Vaadin Ltd.
 */
public abstract class AbstractNumberField<C extends AbstractNumberField<C, T>, T extends Number>
        extends TextFieldBase<C, T> {

    private AbstractNumberFieldI18n i18n;
    /*
     * Note: setters and getters for min/max/step needed to be duplicated in
     * NumberField and IntegerField, because they use primitive double and int
     * types, which can't be used as generic type parameters. Changing to Double
     * and Integer classes would be API-breaking change.
     */
    private double min;
    private double max;
    private double step;

    private boolean stepSetByUser;
    private boolean minSetByUser;

    private DomListenerRegistration inputListenerRegistration;

    private String unparsableValue;

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<T>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

    private Validator<T> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        if (unparsableValue != null) {
            return ValidationResult.error(getI18nErrorMessage(
                    AbstractNumberFieldI18n::getBadInputErrorMessage));
        }

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(getI18nErrorMessage(
                            AbstractNumberFieldI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        Double doubleValue = value != null ? value.doubleValue() : null;

        ValidationResult maxResult = ValidationUtil.validateMaxConstraint(
                getI18nErrorMessage(
                        AbstractNumberFieldI18n::getMaxErrorMessage),
                doubleValue, max);
        if (maxResult.isError()) {
            return maxResult;
        }

        ValidationResult minResult = ValidationUtil.validateMinConstraint(
                getI18nErrorMessage(
                        AbstractNumberFieldI18n::getMinErrorMessage),
                doubleValue, min);
        if (minResult.isError()) {
            return minResult;
        }

        if (!isValidByStep(value)) {
            return ValidationResult.error(getI18nErrorMessage(
                    AbstractNumberFieldI18n::getStepErrorMessage));
        }

        return ValidationResult.ok();
    };

    private ValidationController<AbstractNumberField<C, T>, T> validationController = new ValidationController<>(
            this);

    /**
     * Sets up the common logic for number fields.
     *
     * @param parser
     *            function to parse the client-side value string into
     *            server-side value
     * @param formatter
     *            function to format the server-side value into client-side
     *            value string
     * @param absoluteMin
     *            the smallest possible value of the number type of the field,
     *            will be used as the default min value at server-side
     * @param absoluteMax
     *            the largest possible value of the number type of the field,
     *            will be used as the default max value at server-side
     */
    public AbstractNumberField(SerializableFunction<String, T> parser,
            SerializableFunction<T, String> formatter, double absoluteMin,
            double absoluteMax) {
        super(null, null, String.class, parser, formatter, true);

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        // Not setting these defaults to the web component, so it will have
        // undefined as min and max
        this.min = absoluteMin;
        this.max = absoluteMax;
        this.step = 1.0;

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());

        getElement().addEventListener("unparsable-change", e -> {
            // The unparsable-change event is fired in the following situations:
            // 1. User modifies input but it remains unparsable
            // 2. User enters unparsable input in empty field
            // 3. User clears unparsable input
            //
            // In all these cases, ValueChangeEvent isn't fired, so
            // we call setModelValue manually to trigger validation.
            setModelValue(getEmptyValue(), true);
        }).synchronizeProperty("_inputElementValue");
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        if (inputListenerRegistration != null) {
            inputListenerRegistration.remove();
            inputListenerRegistration = null;
        }

        if (ValueChangeMode.EAGER.equals(valueChangeMode)
                || ValueChangeMode.LAZY.equals(valueChangeMode)
                || ValueChangeMode.TIMEOUT.equals(valueChangeMode)) {
            inputListenerRegistration = getElement().addEventListener("input",
                    event -> {
                        setModelValue(getValue(), true);
                    });
        }

        super.setValueChangeMode(valueChangeMode);

        getSynchronizationRegistration()
                .synchronizeProperty("_inputElementValue");
    }

    @Override
    void applyChangeTimeout() {
        super.applyChangeTimeout();
        ValueChangeMode.applyChangeTimeout(getValueChangeMode(),
                getValueChangeTimeout(), inputListenerRegistration);
    }

    /**
     * Sets the visibility of the buttons for increasing/decreasing the value
     * accordingly to the default or specified step.
     *
     * @see #setStep(double)
     *
     * @param stepButtonsVisible
     *            {@code true} if control buttons should be visible;
     *            {@code false} if those should be hidden
     */
    public void setStepButtonsVisible(boolean stepButtonsVisible) {
        getElement().setProperty("stepButtonsVisible", stepButtonsVisible);
    }

    /**
     * Gets whether the buttons for increasing/decreasing the value are visible.
     *
     * @see #setStep(double)
     *
     * @return {@code true} if buttons are visible, {@code false} otherwise
     */
    public boolean isStepButtonsVisible() {
        return getElement().getProperty("stepButtonsVisible", false);
    }

    /**
     * Returns the value that represents an empty value.
     */
    @Override
    public T getEmptyValue() {
        return null;
    }

    /**
     * Sets the value of this number field. If the new value is not equal to
     * {@code getValue()}, fires a value change event.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(T value) {
        T oldValue = getValue();
        if (oldValue == null && value == null && unparsableValue != null) {
            // When the value is programmatically cleared while the field
            // contains an unparsable input, ValueChangeEvent isn't fired,
            // so we need to call setModelValue manually to clear the bad
            // input and trigger validation.
            setModelValue(getEmptyValue(), false);
            return;
        }

        super.setValue(value);
    }

    @Override
    protected void setModelValue(T newModelValue, boolean fromClient) {
        T oldModelValue = getValue();
        String oldUnparsableValue = unparsableValue;

        if (fromClient && newModelValue == null
                && !getInputElementValue().isEmpty()) {
            unparsableValue = getInputElementValue();
        } else {
            unparsableValue = null;
        }

        boolean isModelValueRemainedEmpty = newModelValue == null
                && oldModelValue == null;

        // Cases:
        // - User modifies input but it remains unparsable
        // - User enters unparsable input in empty field
        // - User clears unparsable input
        // - User enters input that is parsable in JavaScript but unparsable in
        // Java (e.g., integer overflow)
        if (fromClient && isModelValueRemainedEmpty) {
            validate();
            fireValidationStatusChangeEvent();
            return;
        }

        // Case: setValue(null) is called on a field with unparsable input
        if (!fromClient && isModelValueRemainedEmpty
                && oldUnparsableValue != null) {
            setInputElementValue("");
            validate();
            fireValidationStatusChangeEvent();
            return;
        }

        super.setModelValue(newModelValue, fromClient);
    }

    /**
     * Returns the current value of the number field. By default, the empty
     * number field will return {@code null} .
     *
     * @return the current value.
     */
    @Override
    public T getValue() {
        return super.getValue();
    }

    /**
     * Sets the minimum value for this field.
     *
     * @param min
     *            the double value to set
     */
    protected void setMin(double min) {
        getElement().setProperty("min", min);
        this.min = min;
        minSetByUser = true;
    }

    /**
     * Gets the minimum value for this field.
     */
    protected double getMinDouble() {
        return min;
    }

    /**
     * Sets the maximum value for this field.
     *
     * @param max
     *            the double value to set
     */
    protected void setMax(double max) {
        getElement().setProperty("max", max);
        this.max = max;
    }

    /**
     * Gets the maximum value for this field.
     */
    protected double getMaxDouble() {
        return max;
    }

    /**
     * Sets the allowed number intervals for this field.
     *
     * @param step
     *            the double value to set
     */
    protected void setStep(double step) {
        getElement().setProperty("step", step);
        this.step = step;
        stepSetByUser = true;
    }

    /**
     * Gets the allowed number intervals for this field.
     */
    protected double getStepDouble() {
        return step;
    }

    private String getInputElementValue() {
        return getElement().getProperty("_inputElementValue", "");
    }

    private void setInputElementValue(String value) {
        getElement().setProperty("_inputElementValue", value);
    }

    @Override
    public Validator<T> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<T> listener) {
        return Registration.addAndRemove(validationStatusChangeListeners,
                listener);
    }

    /**
     * Notifies Binder that it needs to revalidate the component since the
     * component's validity state may have changed. Note, there is no need to
     * notify Binder separately in the case of a ValueChangeEvent, as Binder
     * already listens to this event and revalidates automatically.
     */
    private void fireValidationStatusChangeEvent() {
        ValidationStatusChangeEvent<T> event = new ValidationStatusChangeEvent<>(
                this, !isInvalid());
        validationStatusChangeListeners
                .forEach(listener -> listener.validationStatusChanged(event));
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property based on
     * the result. If a custom error message is provided with
     * {@link #setErrorMessage(String)}, it is used. Otherwise, the error
     * message defined in the i18n object is used.
     * <p>
     * The method does nothing if the manual validation mode is enabled.
     */
    protected void validate() {
        validationController.validate(getValue());
    }

    private boolean isValidByStep(T value) {

        if (!stepSetByUser// Don't use step in validation if it's not explicitly
                          // set by user. This follows the web component logic.
                || value == null || step == 0) {
            return true;
        }

        // When min is not defined by user, its value is the absoluteMin
        // provided in constructor. In this case, min should not be considered
        // in the step validation.
        double stepBasis = minSetByUser && !Double.isInfinite(getMinDouble())
                ? getMinDouble()
                : 0.0;

        // (value - stepBasis) % step == 0
        return new BigDecimal(String.valueOf(value))
                .subtract(BigDecimal.valueOf(stepBasis))
                .remainder(BigDecimal.valueOf(step))
                .compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(AbstractNumberFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    protected AbstractNumberFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    protected void setI18n(AbstractNumberFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<AbstractNumberFieldI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }
}
