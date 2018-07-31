/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.preferences.editorproperties.property;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.TextBox;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The class provides methods to create and control property's widget which contains name and
 * numerical value of property.
 *
 * @author Roman Nikitenko
 */
public class EditorNumberPropertyWidget extends EditorPropertyBaseWidget implements KeyUpHandler {
  TextBox propertyValueBox;

  public EditorNumberPropertyWidget(String name, int value) {
    propertyName.setText(name);

    propertyValueBox = new TextBox();
    propertyValueBox.setVisibleLength(3);
    propertyValueBox.setValue(String.valueOf(value));
    propertyValueBox.addKeyUpHandler(this);

    valuePanel.add(propertyValueBox);
  }

  @Nullable
  @Override
  public JSONValue getValue() {
    if (isPropertyValueCorrect()) {
      return new JSONNumber(new Double(propertyValueBox.getValue()));
    }
    return null;
  }

  @Override
  public void setValue(JSONValue value) {
    propertyValueBox.setValue(value.toString());
  }

  /** Returns {@code true} if the value is correct and {@code false} - otherwise. */
  private boolean isPropertyValueCorrect() {
    try {
      String value = propertyValueBox.getValue();
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  public void onKeyUp(KeyUpEvent event) {
    delegate.onPropertyChanged();
  }
}
