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
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.languageserver.shared.dto.DtoClientImpls.CodeActionParamsDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.editor.annotation.QueryAnnotationsEvent;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistInvocationContext;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.languageserver.ide.editor.DiagnosticAnnotation;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * A {@link QuickAssistProcessor} that implements LSP code actions as quick assists.
 *
 * @author Thomas Mäder
 */
public class LanguageServerQuickAssistProcessor implements QuickAssistProcessor {

  private TextDocumentServiceClient textDocumentService;
  private ActionManager actionManager;

  @Inject
  public LanguageServerQuickAssistProcessor(
      TextDocumentServiceClient textDocumentService, ActionManager actionManager) {
    this.textDocumentService = textDocumentService;
    this.actionManager = actionManager;
  }

  @Override
  public void computeQuickAssistProposals(
      QuickAssistInvocationContext invocationContext, CodeAssistCallback callback) {
    LinearRange range = invocationContext.getTextEditor().getSelectedLinearRange();
    Document document = invocationContext.getTextEditor().getDocument();
    TextEditor textEditor = invocationContext.getTextEditor();
    boolean goToClosest = (range.getLength() == 0);
    QueryAnnotationsEvent.QueryCallback annotationCallback =
        new QueryAnnotationsEvent.QueryCallback() {

          @SuppressWarnings("ReturnValueIgnored")
          @Override
          public void respond(
              Map<Annotation, org.eclipse.che.ide.api.editor.text.Position> annotations) {
            // iteration with range never returns anything; need to filter ourselves.
            // https://github.com/eclipse/che/issues/4338
            List<Diagnostic> diagnostics = new LinkedList<>();
            int offset =
                collectQuickFixableAnnotations(
                    range, document, annotations, goToClosest, diagnostics);
            if (offset != range.getStartOffset()) {
              TextEditor presenter = ((TextEditor) textEditor);
              presenter.getCursorModel().setCursorPosition(offset);
            }

            CodeActionContext context = new CodeActionContext(diagnostics);

            TextPosition start =
                document.getPositionFromIndex(goToClosest ? offset : range.getStartOffset());
            TextPosition end =
                document.getPositionFromIndex(
                    goToClosest ? offset : range.getStartOffset() + range.getLength());
            Position rangeStart = new Position(start.getLine(), start.getCharacter());
            Position rangeEnd = new Position(end.getLine(), end.getCharacter());
            Range rangeParam = new Range(rangeStart, rangeEnd);

            TextDocumentIdentifier textDocumentIdentifier =
                new TextDocumentIdentifier(document.getFile().getLocation().toString());
            CodeActionParams params =
                new CodeActionParams(textDocumentIdentifier, rangeParam, context);

            Promise<List<Command>> codeAction =
                textDocumentService.codeAction(new CodeActionParamsDto(params));
            List<CompletionProposal> proposals = new LinkedList<>();
            codeAction.then(
                (commands) -> {
                  for (Command command : commands) {
                    Action action = actionManager.getAction(command.getCommand());
                    if (action != null) {
                      proposals.add(new ActionCompletionProposal(command, action));
                    }
                  }
                  ;
                  callback.proposalComputed(proposals);
                });
          }
        };
    QueryAnnotationsEvent event =
        new QueryAnnotationsEvent.Builder()
            .withFilter(a -> a instanceof DiagnosticAnnotation)
            .withCallback(annotationCallback)
            .build();
    document.getDocumentHandle().getDocEventBus().fireEvent(event);
  }

  private int collectQuickFixableAnnotations(
      final LinearRange range,
      Document document,
      final Map<Annotation, org.eclipse.che.ide.api.editor.text.Position> annotations,
      final boolean goToClosest,
      List<Diagnostic> resultingDiagnostics) {
    int invocationLocation = range.getStartOffset();
    if (goToClosest) {
      LinearRange line =
          document.getLinearRangeForLine(
              document.getPositionFromIndex(range.getStartOffset()).getLine());
      int rangeStart = line.getStartOffset();
      int rangeEnd = rangeStart + line.getLength();

      List<org.eclipse.che.ide.api.editor.text.Position> allPositions = new LinkedList<>();
      List<DiagnosticAnnotation> allAnnotations = new LinkedList<>();
      int bestOffset = Integer.MAX_VALUE;
      for (Annotation a : annotations.keySet()) {
        org.eclipse.che.ide.api.editor.text.Position pos = annotations.get(a);
        if (pos != null && isInside(pos.offset, rangeStart, rangeEnd)) { // inside our range?
          allAnnotations.add((DiagnosticAnnotation) a);
          allPositions.add(pos);
          bestOffset = processAnnotation(a, pos, invocationLocation, bestOffset);
        }
      }
      if (bestOffset == Integer.MAX_VALUE) {
        return invocationLocation;
      }
      for (int i = 0; i < allPositions.size(); i++) {
        org.eclipse.che.ide.api.editor.text.Position pos = allPositions.get(i);
        if (isInside(bestOffset, pos.offset, pos.offset + pos.length)) {
          resultingDiagnostics.add(allAnnotations.get(i).getDiagnostic());
        }
      }

      return bestOffset;
    } else {
      // iteration with range never returns anything; need to filter ourselves.
      // https://github.com/eclipse/che/issues/4338
      resultingDiagnostics.addAll(
          annotations
              .entrySet()
              .stream()
              .filter((e) -> e.getValue().overlapsWith(range.getStartOffset(), range.getLength()))
              .map(Entry::getKey)
              .map(a -> (DiagnosticAnnotation) a)
              .map(DiagnosticAnnotation::getDiagnostic)
              .collect(Collectors.toList()));
    }
    return invocationLocation;
  }

  /**
   * Tells is the offset is inside the (inclusive) range defined by start-end.
   *
   * @param offset the offset
   * @param start the start of the range
   * @param end the end of the range
   * @return true if offset is inside
   */
  private boolean isInside(int offset, int start, int end) {
    return offset >= start && offset <= end; // make sure to handle 0-length ranges
  }

  private int processAnnotation(
      Annotation annot,
      org.eclipse.che.ide.api.editor.text.Position pos,
      int invocationLocation,
      int bestOffset) {
    final int posBegin = pos.offset;
    final int posEnd = posBegin + pos.length;
    if (isInside(invocationLocation, posBegin, posEnd)) { // covers invocation location?
      return invocationLocation;
    } else if (bestOffset != invocationLocation) {
      final int newClosestPosition = computeBestOffset(posBegin, invocationLocation, bestOffset);
      if (newClosestPosition != -1) {
        if (newClosestPosition != bestOffset) { // new best
          // Can't use JavaAnnotationUtil.hasCorrections() to see if there are
          // corrections really available for the given annotation,	so, just
          // using its position as `the closest` one
          return newClosestPosition;
        }
      }
    }
    return bestOffset;
  }

  /**
   * Computes and returns the invocation offset given a new position, the initial offset and the
   * best invocation offset found so far.
   *
   * <p>The closest offset to the left of the initial offset is the best. If there is no offset on
   * the left, the closest on the right is the best.
   *
   * @param newOffset the offset to look at
   * @param invocationLocation the invocation location
   * @param bestOffset the current best offset
   * @return -1 is returned if the given offset is not closer or the new best offset
   */
  private int computeBestOffset(int newOffset, int invocationLocation, int bestOffset) {
    if (newOffset <= invocationLocation) {
      if (bestOffset > invocationLocation) {
        return newOffset; // closest was on the right, prefer on the left
      } else if (bestOffset <= newOffset) {
        return newOffset; // we are closer or equal
      }
      return -1; // further away
    }

    if (newOffset <= bestOffset) {
      return newOffset; // we are closer or equal
    }

    return -1; // further away
  }

  private final class ActionCompletionProposal implements CompletionProposal {
    private final Command command;
    private final Action action;

    private ActionCompletionProposal(Command command, Action action) {
      this.command = command;
      this.action = action;
    }

    @Override
    public void getAdditionalProposalInfo(AsyncCallback<Widget> callback) {}

    @Override
    public String getDisplayString() {
      return command.getTitle();
    }

    @Override
    public Icon getIcon() {
      return null;
    }

    @Override
    public void getCompletion(CompletionCallback callback) {
      callback.onCompletion(
          new Completion() {

            @Override
            public LinearRange getSelection(Document document) {
              return null;
            }

            @Override
            public void apply(Document document) {
              QuickassistActionEvent evt =
                  new QuickassistActionEvent(
                      new Presentation(), actionManager, command.getArguments());
              action.actionPerformed(evt);
            }
          });
    }
  }
}
