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
package org.eclipse.che.ide.ui;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.TextArea;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;

/**
 * Text Area widget that supports shifting to new line by pressing Enter key.
 *
 * @author Igor Vinokur
 */
public class ShiftableTextArea extends TextArea {
  public ShiftableTextArea() {
    super();
    initializeEnterKeyHandler();
  }

  private void initializeEnterKeyHandler() {
    new KeyboardNavigationHandler(this) {
      @Override
      public void onEnter(NativeEvent evt) {
        super.onEnter(evt);

        int cursorPos = getCursorPos();
        setText(new StringBuilder(getText()).insert(cursorPos, '\n').toString());
        setCursorPos(cursorPos + 1);
      }
    };
  }
}
