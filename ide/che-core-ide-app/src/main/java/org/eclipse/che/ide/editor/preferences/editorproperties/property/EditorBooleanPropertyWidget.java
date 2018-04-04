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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * The class provides methods to create and control property's widget which contains name and
 * boolean value of property.
 *
 * @author Roman Nikitenko
 */
public class EditorBooleanPropertyWidget extends EditorPropertyBaseWidget
    implements ValueChangeHandler<Boolean> {
  CheckBox propertyValueBox;

  public EditorBooleanPropertyWidget(String name, boolean value) {
    propertyName.setText(name);

    propertyValueBox = new CheckBox();
    propertyValueBox.setValue(value);
    propertyValueBox.addValueChangeHandler(this);
    valuePanel.add(propertyValueBox);
  }

  @Override
  public JSONValue getValue() {
    return JSONBoolean.getInstance(propertyValueBox.getValue());
  }

  @Override
  public void setValue(JSONValue value) {
    if (value != null && value.isBoolean() != null) {
      propertyValueBox.setValue(value.isBoolean().booleanValue());
    }
  }

  @Override
  public void onValueChange(ValueChangeEvent event) {
    delegate.onPropertyChanged();
  }
}
