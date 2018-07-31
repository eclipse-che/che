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
package org.eclipse.che.ide.editor.preferences.editorproperties.propertiessection;

import com.google.gwt.json.client.JSONValue;
import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The class provides special panel to store and control editor's properties section.
 *
 * @author Roman Nikitenko
 */
@ImplementedBy(EditorPropertiesSectionViewImpl.class)
public interface EditorPropertiesSectionView
    extends View<EditorPropertiesSectionView.ActionDelegate> {

  /** Sets title of editor's properties section */
  void setSectionTitle(String title);

  /** Adds special property widget on special panel on view. */
  void addProperty(@NotNull String propertyId, JSONValue value);

  /**
   * Returns property value from the property widget Note: the method returns {@code null} when
   * property widget is not found or value is incorrect
   */
  @Nullable
  JSONValue getPropertyValueById(@NotNull String propertyId);

  interface ActionDelegate {
    /** Performs some action when user change value of property. */
    void onPropertyChanged();
  }
}
