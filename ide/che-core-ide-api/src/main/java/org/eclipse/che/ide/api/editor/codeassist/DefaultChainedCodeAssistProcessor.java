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
package org.eclipse.che.ide.api.editor.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * Allows to chain code assist processor for the default given content type. It will delegate to sub
 * processors.
 *
 * @author Florent Benoit
 */
public abstract class DefaultChainedCodeAssistProcessor implements CodeAssistProcessor {

  /** Delegate code assist processors. */
  private Set<? extends CodeAssistProcessor> codeAssistProcessors;

  /**
   * Allow to set processors.
   *
   * @param codeAssistProcessors the expected processors
   */
  protected void setProcessors(final Set<? extends CodeAssistProcessor> codeAssistProcessors) {
    this.codeAssistProcessors = codeAssistProcessors;
  }

  @Override
  public void computeCompletionProposals(
      final TextEditor textEditor,
      final int offset,
      final boolean triggered,
      final CodeAssistCallback callback) {
    if (!this.codeAssistProcessors.isEmpty()) {
      final List<CompletionProposal> proposalList = new ArrayList<>();
      final List<CodeAssistProcessor> expected = new ArrayList<>();
      for (final CodeAssistProcessor processor : this.codeAssistProcessors) {
        expected.add(processor);
        processor.computeCompletionProposals(
            textEditor,
            offset,
            triggered,
            new CodeAssistCallback() {
              @Override
              public void proposalComputed(final List<CompletionProposal> processorProposals) {
                expected.remove(processor);
                if (processorProposals == null || processorProposals.isEmpty()) {
                  return;
                }
                proposalList.addAll(processorProposals);

                // all processors have computed their result
                if (expected.isEmpty()) {
                  callback.proposalComputed(proposalList);
                }
              }
            });
      }
    }
  }

  @Override
  public String getErrorMessage() {
    String errorMessage = null;
    if (!this.codeAssistProcessors.isEmpty()) {
      for (final CodeAssistProcessor processor : this.codeAssistProcessors) {
        final String processorErrorMessage = processor.getErrorMessage();
        if (processorErrorMessage != null) {
          if (errorMessage == null) {
            errorMessage = processorErrorMessage;
          } else {
            errorMessage = errorMessage.concat(processorErrorMessage);
          }
        }
      }
    }
    return errorMessage;
  }

  /**
   * Returns the injected processors.
   *
   * @return injected processors
   */
  public Set<? extends CodeAssistProcessor> getProcessors() {
    return this.codeAssistProcessors;
  }
}
