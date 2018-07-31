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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** Event fired when java reconsiler finish work */
public class JavaReconsilerEvent extends GwtEvent<JavaReconsilerEvent.JavaReconsilerHandler> {
  public static final Type<JavaReconsilerHandler> TYPE = new Type<>();
  private final TextEditor editor;

  public JavaReconsilerEvent(TextEditor editor) {
    this.editor = editor;
  }

  public TextEditor getEditor() {
    return editor;
  }

  @Override
  public Type<JavaReconsilerHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(JavaReconsilerHandler handler) {
    handler.onJavaResonsile(this);
  }

  public interface JavaReconsilerHandler extends EventHandler {
    void onJavaResonsile(JavaReconsilerEvent event);
  }
}
