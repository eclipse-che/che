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
package org.eclipse.che.ide.editor.texteditor.infopanel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.text.TextPosition;

/**
 * The presenter for the editor info panel.<br>
 * Info panel shows the following things: cursor position, number of lines, tab settings and file
 * type.
 *
 * @author "Mickaël Leduque"
 * @author Vitaliy Guliy
 */
public class InfoPanel extends Composite {

  /** A set with file type descriptions. */
  private static Map<String, String> fileTypes = new HashMap<String, String>();

  static {
    fileTypes.put("application/xml", "XML");
    fileTypes.put("text/html", "HTML");
    fileTypes.put("application/x-jsp", "JSP");
    fileTypes.put("application/javascript", "JavaScript");
    fileTypes.put("text/css", "CSS");
    fileTypes.put("text/x-java-source", "Java");
    fileTypes.put("text/x-less", "Less");
  }

  /**
   * UI binder interface for this component.
   *
   * @author "Mickaël Leduque"
   */
  interface InfoPanelUiBinder extends UiBinder<FlowPanel, InfoPanel> {}

  /** The UI binder instance. */
  private static final InfoPanelUiBinder UIBINDER = GWT.create(InfoPanelUiBinder.class);

  @UiField HTMLPanel cursorPosition;

  @UiField HTMLPanel fileType;

  @UiField HTMLPanel encoding;

  @Inject
  public InfoPanel() {
    initWidget(UIBINDER.createAndBindUi(this));
  }

  /**
   * Creates an initial state, before actual data is available.
   *
   * @param fileContentDescription the file type
   * @param numberOfLines the file number of lines
   * @param tabSize the space-equivalent width of a tabulation character
   */
  public void createDefaultState(
      final String fileContentDescription, final int numberOfLines, final int tabSize) {
    setFileType(fileContentDescription);
  }

  /**
   * Update the line and char display to show a position in the text.
   *
   * @param position the position in the text
   */
  public void updateCursorPosition(final TextPosition position) {
    if (position != null) {
      cursorPosition
          .getElement()
          .setInnerText("" + (position.getLine() + 1) + ":" + (position.getCharacter() + 1));
    } else {
      cursorPosition.getElement().setInnerText("");
    }
  }

  /**
   * Changes the displayed value of the file type.
   *
   * @param type the new value
   */
  private void setFileType(final String type) {
    String displayName = fileTypes.get(type);
    if (displayName != null) {
      fileType.getElement().setInnerHTML(displayName);
    } else {
      fileType.getElement().setInnerHTML(type);
    }
  }
}
