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
package org.eclipse.che.ide.editor.orion.client.incremental.find;

import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import elemental.dom.Text;
import elemental.html.DivElement;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.editor.orion.client.OrionResource;
import org.eclipse.che.ide.status.message.StatusMessage;
import org.eclipse.che.ide.status.message.StatusMessageObserver;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * IncrementalFindReportStatusObserver listens editor status messages and filter messages which
 * contains information about incremental find state. It creates simple UI for user notification
 * about incremental find operation progress. Note: incremental find can be straight or reverse.
 *
 * @author Alexander Andrienko
 */
public class IncrementalFindReportStatusObserver implements StatusMessageObserver {

  private final OrionResource orionResource;

  private EditorWidget editorWidget;
  private DivElement findDiv;

  @Inject
  public IncrementalFindReportStatusObserver(OrionResource orionResource) {
    this.orionResource = orionResource;
  }

  /**
   * Sets editor widget which contains text source to incremental search.
   *
   * @param editorWidget editor widget with content to search.
   */
  public void setEditorWidget(OrionEditorWidget editorWidget) {
    this.editorWidget = editorWidget;
  }

  /**
   * Checks if {@code statusMessage} is incremental find message. In case if this is true than
   * create or update simple UI to display message content, otherwise skip this message.
   *
   * @param statusMessage editor status message.
   */
  @Override
  public void update(StatusMessage statusMessage) {
    String message = statusMessage.getMessage();
    boolean isIncrementalFindMessage =
        message.startsWith("Incremental find:") | message.startsWith("Reverse Incremental find:");
    if (!message.isEmpty() && !isIncrementalFindMessage) {
      return;
    }

    Element editorElem = editorWidget.asWidget().getElement();

    Element findDiv = createFindDiv(message);
    setStyle(message, findDiv);
    editorElem.appendChild(findDiv);

    if (isNullOrEmpty(message) && findDiv != null) {
      editorElem.removeChild(findDiv);
      this.findDiv = null;
    }
  }

  private Element createFindDiv(String message) {
    if (findDiv == null) {
      findDiv = Elements.createDivElement();
      Text messageNode = Elements.createTextNode(message);
      findDiv.appendChild(messageNode);
    }

    findDiv.getFirstChild().setTextContent(message);
    return (Element) findDiv;
  }

  private void setStyle(String message, Element element) {
    if (message.endsWith("(not found)")) {
      element.addClassName(orionResource.getIncrementalFindStyle().incrementalFindContainer());
      element.addClassName(orionResource.getIncrementalFindStyle().incrementalFindError());
    } else {
      element.setClassName(orionResource.getIncrementalFindStyle().incrementalFindContainer());
    }
  }
}
