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
package org.eclipse.che.ide.editor.preferences.editorproperties.propertiessection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.editor.preferences.EditorPreferencesManager;
import org.eclipse.che.ide.editor.preferences.editorproperties.property.EditorPropertyWidget;
import org.eclipse.che.ide.editor.preferences.editorproperties.property.EditorPropertyWidgetFactory;

/**
 * The class provides special panel to store and control editor's properties.
 *
 * @author Roman Nikitenko
 */
public class EditorPropertiesSectionViewImpl extends Composite
    implements EditorPropertiesSectionView, EditorPropertyWidget.ActionDelegate {

  private static final EditorPropertiesSectionViewImplUiBinder UI_BINDER =
      GWT.create(EditorPropertiesSectionViewImplUiBinder.class);

  @UiField FlowPanel propertiesPanel;
  @UiField Label sectionTitle;

  private final EditorPropertyWidgetFactory editorPropertyWidgetFactory;
  private final EditorPreferencesManager editorPreferencesManager;
  private ActionDelegate delegate;
  private Map<String, EditorPropertyWidget> properties = new HashMap<>();

  @Inject
  public EditorPropertiesSectionViewImpl(
      EditorPropertyWidgetFactory editorPropertyWidgetFactory,
      EditorPreferencesManager editorPreferencesManager) {
    this.editorPropertyWidgetFactory = editorPropertyWidgetFactory;
    this.editorPreferencesManager = editorPreferencesManager;

    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void onPropertyChanged() {
    delegate.onPropertyChanged();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Nullable
  @Override
  public JSONValue getPropertyValueById(@NotNull String propertyId) {
    EditorPropertyWidget propertyWidget = properties.get(propertyId);
    if (propertyWidget != null) {
      return propertyWidget.getValue();
    }
    return null;
  }

  @Override
  public void setSectionTitle(String title) {
    sectionTitle.setText(title);
    propertiesPanel.ensureDebugId("editorPropertiesSection-" + title); // for selenium tests
  }

  @Override
  public void addProperty(@NotNull String propertyId, JSONValue value) {
    EditorPropertyWidget propertyWidget = properties.get(propertyId);
    if (propertyWidget != null) {
      propertyWidget.setValue(value);
      return;
    }

    String propertyName = editorPreferencesManager.getPropertyNameById(propertyId);
    if (propertyName == null) {
      return;
    }

    propertyWidget = editorPropertyWidgetFactory.create(propertyName, value);
    propertyWidget.setDelegate(this);

    propertiesPanel.add(propertyWidget);
    properties.put(propertyId, propertyWidget);
  }

  interface EditorPropertiesSectionViewImplUiBinder
      extends UiBinder<Widget, EditorPropertiesSectionViewImpl> {}
}
