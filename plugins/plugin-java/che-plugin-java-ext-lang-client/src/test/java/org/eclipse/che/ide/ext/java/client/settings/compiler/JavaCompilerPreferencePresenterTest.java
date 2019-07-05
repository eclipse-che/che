/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter.DirtyStateListener;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.inject.factories.PropertyWidgetFactory;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Dmitry Shnurenko */
@RunWith(MockitoJUnitRunner.class)
public class JavaCompilerPreferencePresenterTest {

  private static final String ID_1 = "id1";
  private static final String ID_2 = "id2";

  private static final String VALUE_1 = "value1";
  private static final String VALUE_2 = "value2";

  // constructor mocks
  @Mock private ErrorWarningsView view;
  @Mock private PropertyWidgetFactory propertyFactory;
  @Mock private JavaLocalizationConstant locale;
  @Mock private PreferencesManager preferencesManager;
  @Mock private EventBus eventBus;
  @Mock private Provider<NotificationManager> notificationManagerProvider;

  @Mock private DirtyStateListener dirtyStateListener;
  @Mock private Promise<Map<String, String>> mapPromise;
  @Mock private AcceptsOneWidget container;
  @Mock private PropertyWidget widget;

  @Captor private ArgumentCaptor<Map<String, String>> mapCaptor;
  @Captor private ArgumentCaptor<Operation<Map<String, String>>> operationCaptor;
  @Captor private ArgumentCaptor<Operation<PromiseError>> errorOperationCaptor;

  @InjectMocks private JavaCompilerPreferencePresenter presenter;

  @Before
  public void setUp() {
    when(preferencesManager.loadPreferences()).thenReturn(mapPromise);
    when(mapPromise.then(org.mockito.ArgumentMatchers.<Operation<Map<String, String>>>anyObject()))
        .thenReturn(mapPromise);
    when(propertyFactory.create(org.mockito.ArgumentMatchers.<ErrorWarningsOptions>anyObject()))
        .thenReturn(widget);

    presenter.setUpdateDelegate(dirtyStateListener);
  }

  @Test
  public void constructorShouldBeVerified() {
    verify(locale).compilerSetup();
  }

  @Test
  public void pageShouldNotBeDirty() {
    boolean isDirty = presenter.isDirty();

    assertThat(isDirty, equalTo(false));
  }

  @Test
  public void changedValuesShouldBeSaved() throws OperationException {
    initWidgets();
    when(widget.getSelectedValue()).thenReturn(VALUE_2);
    when(preferencesManager.getValue(anyString())).thenReturn(VALUE_1);

    presenter.go(container);

    verify(mapPromise).then(operationCaptor.capture());
    operationCaptor.getValue().apply(getAllProperties());

    presenter.storeChanges();

    verify(preferencesManager, times(18)).setValue(anyString(), anyString());
    verify(preferencesManager, times(36)).getValue(anyString());

    when(preferencesManager.getValue(anyString())).thenReturn(VALUE_2);

    assertThat(presenter.isDirty(), equalTo(false));
  }

  @Test
  public void changesShouldBeReverted() throws Exception {
    initWidgets();

    when(widget.getSelectedValue()).thenReturn(VALUE_2);
    when(preferencesManager.getValue(anyString())).thenReturn(VALUE_1);

    presenter.go(container);

    verify(mapPromise).then(operationCaptor.capture());
    operationCaptor.getValue().apply(getAllProperties());

    presenter.onPropertyChanged();
    presenter.revertChanges();

    verify(preferencesManager, times(36)).getValue(anyString());
    verify(widget, times(36)).selectPropertyValue(anyString());
    verify(widget, times(18)).getSelectedValue();
  }

  private Map<String, String> getAllProperties() {
    Map<String, String> allProperties = new HashMap<>();

    allProperties.put(COMPILER_UNUSED_LOCAL.toString(), VALUE_1);
    allProperties.put(COMPILER_UNUSED_IMPORT.toString(), VALUE_1);
    allProperties.put(DEAD_CODE.toString(), VALUE_1);
    allProperties.put(METHOD_WITH_CONSTRUCTOR_NAME.toString(), VALUE_1);
    allProperties.put(UNNECESSARY_ELSE_STATEMENT.toString(), VALUE_1);
    allProperties.put(COMPARING_IDENTICAL_VALUES.toString(), VALUE_1);
    allProperties.put(NO_EFFECT_ASSIGNMENT.toString(), VALUE_1);
    allProperties.put(MISSING_SERIAL_VERSION_UID.toString(), VALUE_1);
    allProperties.put(TYPE_PARAMETER_HIDE_ANOTHER_TYPE.toString(), VALUE_1);
    allProperties.put(FIELD_HIDES_ANOTHER_VARIABLE.toString(), VALUE_1);
    allProperties.put(MISSING_DEFAULT_CASE.toString(), VALUE_1);
    allProperties.put(UNUSED_PRIVATE_MEMBER.toString(), VALUE_1);
    allProperties.put(UNCHECKED_TYPE_OPERATION.toString(), VALUE_1);
    allProperties.put(USAGE_OF_RAW_TYPE.toString(), VALUE_1);
    allProperties.put(MISSING_OVERRIDE_ANNOTATION.toString(), VALUE_1);
    allProperties.put(NULL_POINTER_ACCESS.toString(), VALUE_1);
    allProperties.put(POTENTIAL_NULL_POINTER_ACCESS.toString(), VALUE_1);
    allProperties.put(REDUNDANT_NULL_CHECK.toString(), VALUE_1);

    return allProperties;
  }

  private void initWidgets() {
    when(widget.getOptionId())
        .thenReturn(
            COMPILER_UNUSED_LOCAL,
            COMPILER_UNUSED_IMPORT,
            DEAD_CODE,
            METHOD_WITH_CONSTRUCTOR_NAME,
            UNNECESSARY_ELSE_STATEMENT,
            COMPARING_IDENTICAL_VALUES,
            NO_EFFECT_ASSIGNMENT,
            MISSING_SERIAL_VERSION_UID,
            TYPE_PARAMETER_HIDE_ANOTHER_TYPE,
            FIELD_HIDES_ANOTHER_VARIABLE,
            MISSING_DEFAULT_CASE,
            UNUSED_PRIVATE_MEMBER,
            UNCHECKED_TYPE_OPERATION,
            USAGE_OF_RAW_TYPE,
            MISSING_OVERRIDE_ANNOTATION,
            NULL_POINTER_ACCESS,
            POTENTIAL_NULL_POINTER_ACCESS,
            REDUNDANT_NULL_CHECK);
  }

  @Test
  public void propertyShouldBeChanged() {
    presenter.onPropertyChanged();

    verify(dirtyStateListener).onDirtyChanged();
  }

  @Test
  public void propertiesShouldBeDisplayed() throws Exception {
    presenter.go(container);

    verify(mapPromise).then(operationCaptor.capture());
    operationCaptor.getValue().apply(getAllProperties());

    verify(propertyFactory, times(18))
        .create(org.mockito.ArgumentMatchers.<ErrorWarningsOptions>anyObject());
    verify(widget, times(18)).selectPropertyValue(nullable(String.class));
    verify(widget, times(18)).setDelegate(presenter);
    verify(view, times(18)).addProperty(widget);
  }

  @Test
  public void propertiesShouldBeDisplayedFailed() throws OperationException {
    PromiseError promiseError = mock(PromiseError.class);
    NotificationManager notificationManager = mock(NotificationManager.class);

    when(notificationManagerProvider.get()).thenReturn(notificationManager);

    presenter.go(container);

    verify(mapPromise).catchError(errorOperationCaptor.capture());

    errorOperationCaptor.getValue().apply(promiseError);

    verify(preferencesManager).loadPreferences();
    verify(notificationManager).notify(nullable(String.class), eq(FAIL), eq(FLOAT_MODE));
  }
}
