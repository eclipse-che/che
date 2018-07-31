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
package org.eclipse.che.ide.command.editor.page.text;

import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View for {@link AbstractPageWithTextEditor}.
 *
 * @author Artem Zatsarynnyi
 */
public interface PageWithTextEditorView extends View<PageWithTextEditorView.ActionDelegate> {

  /** Returns the container where the editor should be placed. */
  SimpleLayoutPanel getEditorContainer();

  /** Sets height of the view. */
  void setHeight(int height);

  /** Sets title for the editor. */
  void setEditorTitle(String title);

  /** The action delegate for this view. */
  interface ActionDelegate {

    /** Called when exploring macros is requested. */
    void onExploreMacros();
  }
}
