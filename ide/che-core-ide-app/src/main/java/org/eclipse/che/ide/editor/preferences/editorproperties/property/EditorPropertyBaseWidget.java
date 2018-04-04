/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.preferences.editorproperties.property;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import javax.validation.constraints.NotNull;

/**
 * The base widget to create property's widget which contains label for name of property and panel
 * for value box.
 *
 * @author Roman Nikitenko
 */
abstract class EditorPropertyBaseWidget extends Composite implements EditorPropertyWidget {
  private static final PropertyWidgetImplUiBinder UI_BINDER =
      GWT.create(PropertyWidgetImplUiBinder.class);

  @UiField Label propertyName;
  @UiField FlowPanel valuePanel;

  ActionDelegate delegate;

  public EditorPropertyBaseWidget() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public JSONValue getValue() {
    return null;
  }

  @Override
  public void setValue(JSONValue value) {
    // do nothing
  }

  interface PropertyWidgetImplUiBinder extends UiBinder<Widget, EditorPropertyBaseWidget> {}
}
