/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.inject.factories.PropertyWidgetFactory;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;
import org.eclipse.che.ide.ext.java.client.settings.service.SettingsServiceClient;
import org.eclipse.che.ide.settings.common.AbstractSettingsPagePresenter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPARING_IDENTICAL_VALUES;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPILER_UNUSED_IMPORT;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.COMPILER_UNUSED_LOCAL;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.DEAD_CODE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.FIELD_HIDES_ANOTHER_VARIABLE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.METHOD_WITH_CONSTRUCTOR_NAME;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.MISSING_DEFAULT_CASE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.MISSING_OVERRIDE_ANNOTATION;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.MISSING_SERIAL_VERSION_UID;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.NO_EFFECT_ASSIGNMENT;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.NULL_POINTER_ACCESS;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.POTENTIAL_NULL_POINTER_ACCESS;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.REDUNDANT_NULL_CHECK;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.TYPE_PARAMETER_HIDE_ANOTHER_TYPE;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.UNCHECKED_TYPE_OPERATION;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.UNNECESSARY_ELSE_STATEMENT;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.UNUSED_PRIVATE_MEMBER;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.USAGE_OF_RAW_TYPE;

/**
 * The class contains business logic which allow control changing of compiler's properties.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ErrorWarningsPresenter extends AbstractSettingsPagePresenter implements PropertyWidget.ActionDelegate {

    private final ErrorWarningsView     view;
    private final SettingsServiceClient service;
    private final PropertyWidgetFactory propertyFactory;

    private final Map<String, String>         changedProperties;
    private final Map<String, PropertyWidget> widgets;

    private Map<String, String> allProperties;

    @Inject
    public ErrorWarningsPresenter(JavaLocalizationConstant locale,
                                  ErrorWarningsView view,
                                  SettingsServiceClient service,
                                  PropertyWidgetFactory propertyFactory) {
        super(locale.compilerSetup());

        this.view = view;

        this.service = service;
        this.propertyFactory = propertyFactory;

        this.changedProperties = new HashMap<>();
        this.allProperties = new HashMap<>();
        this.widgets = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirty() {
        return !changedProperties.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void storeChanges() {
        service.applyCompileParameters(changedProperties);

        for (Map.Entry<String, String> entry : changedProperties.entrySet()) {
            String id = entry.getKey();
            String changedValue = entry.getValue();

            allProperties.put(id, changedValue);
        }

        changedProperties.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void revertChanges() {
        changedProperties.clear();

        for (Map.Entry<String, PropertyWidget> entry : widgets.entrySet()) {
            String propertyId = entry.getKey();
            PropertyWidget widget = entry.getValue();

            String previousValue = allProperties.get(propertyId);

            widget.selectPropertyValue(previousValue);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPropertyChanged(@NotNull String propertyId, @NotNull String value) {
        changedProperties.put(propertyId, value);

        delegate.onDirtyChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        addErrorWarningsPanel();

        container.setWidget(view);
    }

    private void addErrorWarningsPanel() {
        Promise<Map<String, String>> propertiesPromise = service.getCompileParameters();

        propertiesPromise.then(new Operation<Map<String, String>>() {
            @Override
            public void apply(Map<String, String> properties) throws OperationException {
                ErrorWarningsPresenter.this.allProperties = properties;

                createAndAddWidget(COMPILER_UNUSED_LOCAL);

                createAndAddWidget(COMPILER_UNUSED_IMPORT);

                createAndAddWidget(DEAD_CODE);

                createAndAddWidget(METHOD_WITH_CONSTRUCTOR_NAME);

                createAndAddWidget(UNNECESSARY_ELSE_STATEMENT);

                createAndAddWidget(COMPARING_IDENTICAL_VALUES);

                createAndAddWidget(NO_EFFECT_ASSIGNMENT);

                createAndAddWidget(MISSING_SERIAL_VERSION_UID);

                createAndAddWidget(TYPE_PARAMETER_HIDE_ANOTHER_TYPE);

                createAndAddWidget(FIELD_HIDES_ANOTHER_VARIABLE);

                createAndAddWidget(MISSING_DEFAULT_CASE);

                createAndAddWidget(UNUSED_PRIVATE_MEMBER);

                createAndAddWidget(UNCHECKED_TYPE_OPERATION);

                createAndAddWidget(USAGE_OF_RAW_TYPE);

                createAndAddWidget(MISSING_OVERRIDE_ANNOTATION);

                createAndAddWidget(NULL_POINTER_ACCESS);

                createAndAddWidget(POTENTIAL_NULL_POINTER_ACCESS);

                createAndAddWidget(REDUNDANT_NULL_CHECK);
            }
        });
    }

    private void createAndAddWidget(@NotNull ErrorWarningsOptions option) {
        String parameterId = option.toString();

        if (widgets.containsKey(parameterId)) {
            return;
        }

        PropertyWidget widget = propertyFactory.create(option);

        String value = allProperties.get(parameterId);

        widget.selectPropertyValue(value);

        widget.setDelegate(ErrorWarningsPresenter.this);

        widgets.put(parameterId, widget);

        view.addProperty(widget);
    }
}
