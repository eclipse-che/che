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
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;

/** Represents loading mode widget for editor */
public class EditorPlaceholderWidget extends Composite {

  interface EditorPlaceholderWidgetUiBinder extends UiBinder<Widget, EditorPlaceholderWidget> {}

  @UiField DivElement lineNumbers;

  @Inject
  public EditorPlaceholderWidget(EditorPlaceholderWidgetUiBinder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    for (int i = 1; i <= 20; i++) {
      Element lineNumber = DOM.createDiv();
      lineNumber.setInnerText("" + i);
      lineNumbers.appendChild(lineNumber);
    }
  }
}
