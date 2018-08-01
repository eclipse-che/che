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
package org.eclipse.che.ide.editor.preferences.editorproperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The class provides special panel to store editor's sections.
 *
 * @author Roman Nikitenko
 */
public class EditorPropertiesViewImpl extends Composite implements EditorPropertiesView {

  private static final EditorPropertiesSectionViewImplUiBinder UI_BINDER =
      GWT.create(EditorPropertiesSectionViewImplUiBinder.class);

  @UiField FlowPanel sectionsPanel;

  public EditorPropertiesViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public AcceptsOneWidget getEditorSectionsContainer() {
    SimplePanel container = new SimplePanel();
    sectionsPanel.add(container);
    return container;
  }

  interface EditorPropertiesSectionViewImplUiBinder
      extends UiBinder<Widget, EditorPropertiesViewImpl> {}
}
