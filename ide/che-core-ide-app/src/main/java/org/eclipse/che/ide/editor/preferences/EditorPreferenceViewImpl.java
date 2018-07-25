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
package org.eclipse.che.ide.editor.preferences;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

/** Implementation of the {@link EditorPreferenceView}. */
public class EditorPreferenceViewImpl extends Composite implements EditorPreferenceView {

  /** The UI binder instance. */
  private static final EditorPreferenceViewImplUiBinder UIBINDER =
      GWT.create(EditorPreferenceViewImplUiBinder.class);

  @UiField FlowPanel editorPreferencesContainer;

  @Inject
  public EditorPreferenceViewImpl() {
    initWidget(UIBINDER.createAndBindUi(this));
  }

  @Override
  public FlowPanel getEditorPreferencesContainer() {
    return editorPreferencesContainer;
  }

  /** UI binder interface for the {@link EditorPreferenceViewImpl} component. */
  interface EditorPreferenceViewImplUiBinder
      extends UiBinder<ScrollPanel, EditorPreferenceViewImpl> {}
}
