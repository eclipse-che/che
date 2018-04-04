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
