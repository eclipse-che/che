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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The interface provides methods to control property's widget which contains name and value of
 * property.
 *
 * @author Roman Nikitenko
 */
public interface EditorPropertyWidget extends View<EditorPropertyWidget.ActionDelegate> {

  /**
   * Returns property value from the property widget Note: the method returns {@code null} when
   * value is incorrect
   */
  @Nullable
  JSONValue getValue();

  /** Sets the given value */
  void setValue(JSONValue value);

  interface ActionDelegate {
    /** Performs some action when user change value of property. */
    void onPropertyChanged();
  }
}
