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
package org.eclipse.che.ide.editor.preferences.editorproperties.property;

import com.google.gwt.json.client.JSONValue;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * The factory which creates instances of {@link EditorPropertyWidget}.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorPropertyWidgetFactory {

  /**
   * Creates one of implementations of {@link EditorPropertyWidget}.
   *
   * @return an instance of {@link EditorPropertyWidget}
   */
  public EditorPropertyWidget create(@NotNull String propertyName, @NotNull JSONValue value) {
    if (value.isBoolean() != null) {
      return new EditorBooleanPropertyWidget(propertyName, value.isBoolean().booleanValue());
    }

    if (value.isNumber() != null) {
      Double doubleValue = value.isNumber().doubleValue();
      return new EditorNumberPropertyWidget(propertyName, doubleValue.intValue());
    }
    return new EditorStringPropertyWidget(propertyName, value.toString());
  }
}
