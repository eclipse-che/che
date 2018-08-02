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
package org.eclipse.che.ide.api.editor.texteditor;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/** Resources interface for the editor. */
public interface EditorResources extends ClientBundle {

  /** CssResource for the editor. */
  interface EditorCss extends CssResource {

    /** Style added to warnings. */
    String lineWarning();

    /** Style added to errors. */
    String lineError();

    /** Style added to the current breakpoint line. */
    String debugLine();
  }

  @Source({"Editor.css", "org/eclipse/che/ide/api/ui/style.css"})
  EditorCss editorCss();
}
