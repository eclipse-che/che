/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.codeassist;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.core.client.GWT;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;

/** Implementation of CodeAssistant. */
public class CodeAssistantImpl implements CodeAssistant {

  private final Map<String, CodeAssistProcessor> processors;
  private final TextEditor textEditor;
  private final DocumentPartitioner partitioner;
  private final NotificationManager notificationManager;
  private String lastErrorMessage;

  public static final AutoCompleteResources res = GWT.create(AutoCompleteResources.class);

  @AssistedInject
  public CodeAssistantImpl(
      @Assisted final DocumentPartitioner partitioner,
      @Assisted TextEditor textEditor,
      NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
    processors = new HashMap<>();
    res.defaultSimpleListCss().ensureInjected();
    res.autocompleteComponentCss().ensureInjected();
    res.popupCss().ensureInjected();
    this.partitioner = partitioner;
    this.textEditor = textEditor;
  }

  @Override
  public void computeCompletionProposals(
      final int offset, final boolean triggered, final CodeAssistCallback callback) {
    this.lastErrorMessage = "processing";

    final CodeAssistProcessor processor = getProcessor(offset);
    if (processor != null) {
      processor.computeCompletionProposals(textEditor, offset, triggered, callback);
      this.lastErrorMessage = processor.getErrorMessage();
      if (this.lastErrorMessage != null) {
        notificationManager.notify("", lastErrorMessage, FAIL, EMERGE_MODE);
        this.textEditor.showMessage(this.lastErrorMessage);
      }
    } else {
      final CodeAssistProcessor fallbackProcessor = getFallbackProcessor();
      if (fallbackProcessor != null) {
        fallbackProcessor.computeCompletionProposals(textEditor, offset, triggered, callback);
        this.lastErrorMessage = fallbackProcessor.getErrorMessage();
        if (this.lastErrorMessage != null) {
          this.textEditor.showMessage(this.lastErrorMessage);
        }
      }
    }
  }

  @Override
  public CodeAssistProcessor getProcessor(final int offset) {
    final String contentType = this.textEditor.getContentType();
    if (contentType == null) {
      return null;
    }

    final String type = this.partitioner.getContentType(offset);
    return getCodeAssistProcessor(type);
  }

  private CodeAssistProcessor getFallbackProcessor() {
    final CodeAssistProcessor emptyTypeProcessor = getCodeAssistProcessor("");
    if (emptyTypeProcessor != null) {
      return emptyTypeProcessor;
    }
    return getCodeAssistProcessor(null);
  }

  @Override
  public CodeAssistProcessor getCodeAssistProcessor(final String contentType) {
    return processors.get(contentType);
  }

  @Override
  public void setCodeAssistantProcessor(
      final String contentType, final CodeAssistProcessor processor) {
    processors.put(contentType, processor);
  }
}
