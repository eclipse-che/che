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
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistProcessor.insertStyle;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.resolveFQN;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.annotation.QueryAnnotationsEvent;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistInvocationContext;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.action.ProposalAction;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ProposalPresentation;
import org.eclipse.che.ide.ext.java.shared.dto.Proposals;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

/** {@link QuickAssistProcessor} for java files. */
public class JavaQuickAssistProcessor implements QuickAssistProcessor {

  private final JavaCodeAssistClient client;
  /** The resources used for java assistants. */
  private final JavaResources javaResources;

  private final Map<String, ProposalAction> proposalActions;
  private final DtoUnmarshallerFactory unmarshallerFactory;
  private final DtoFactory dtoFactory;
  private final RefactoringUpdater refactoringUpdater;
  private final EditorAgent editorAgent;

  @Inject
  public JavaQuickAssistProcessor(
      final JavaCodeAssistClient client,
      final JavaResources javaResources,
      Map<String, ProposalAction> proposalActions,
      DtoUnmarshallerFactory unmarshallerFactory,
      DtoFactory dtoFactory,
      RefactoringUpdater refactoringUpdater,
      EditorAgent editorAgent) {
    this.client = client;
    this.javaResources = javaResources;
    this.proposalActions = proposalActions;
    this.unmarshallerFactory = unmarshallerFactory;
    this.dtoFactory = dtoFactory;
    this.refactoringUpdater = refactoringUpdater;
    this.editorAgent = editorAgent;
  }

  @Override
  public void computeQuickAssistProposals(
      final QuickAssistInvocationContext quickAssistContext, final CodeAssistCallback callback) {
    final TextEditor textEditor = quickAssistContext.getTextEditor();
    final Document document = textEditor.getDocument();

    LinearRange tempRange;

    tempRange = textEditor.getSelectedLinearRange();

    final LinearRange range = tempRange;

    final boolean goToClosest = (range.getLength() == 0);

    final QueryAnnotationsEvent.AnnotationFilter filter =
        new QueryAnnotationsEvent.AnnotationFilter() {
          @Override
          public boolean accept(final Annotation annotation) {
            if (!(annotation instanceof JavaAnnotation)) {
              return false;
            } else {
              JavaAnnotation javaAnnotation = (JavaAnnotation) annotation;
              return (!javaAnnotation
                  .isMarkedDeleted()) /*&& JavaAnnotationUtil.hasCorrections(annotation)*/;
            }
          }
        };
    final QueryAnnotationsEvent.QueryCallback queryCallback =
        new QueryAnnotationsEvent.QueryCallback() {
          @Override
          public void respond(final Map<Annotation, Position> annotations) {
            List<Problem> problems = new ArrayList<>();
            /*final Map<Annotation, Position> problems =*/
            int offset =
                collectQuickFixableAnnotations(range, document, annotations, goToClosest, problems);
            if (offset != range.getStartOffset()) {
              TextEditor presenter = ((TextEditor) textEditor);
              presenter.getCursorModel().setCursorPosition(offset);
            }

            setupProposals(callback, textEditor, offset, problems);
          }
        };
    final QueryAnnotationsEvent event =
        new QueryAnnotationsEvent.Builder().withFilter(filter).withCallback(queryCallback).build();
    document.getDocumentHandle().getDocEventBus().fireEvent(event);
  }

  private void showProposals(
      final CodeAssistCallback callback, final Proposals responds, TextEditor editor) {
    List<ProposalPresentation> presentations = responds.getProposals();
    final List<CompletionProposal> proposals = new ArrayList<>(presentations.size());
    HasLinkedMode linkedEditor = editor instanceof HasLinkedMode ? (HasLinkedMode) editor : null;
    for (ProposalPresentation proposal : presentations) {
      CompletionProposal completionProposal;
      String actionId = proposal.getActionId();
      if (actionId != null) {
        ProposalAction action = proposalActions.get(actionId);
        completionProposal =
            new ActionCompletionProposal(
                insertStyle(javaResources, proposal.getDisplayString()),
                actionId,
                action,
                JavaCodeAssistProcessor.getIcon(proposal.getImage()));
      } else {
        completionProposal =
            new JavaCompletionProposal(
                proposal.getIndex(),
                insertStyle(javaResources, proposal.getDisplayString()),
                JavaCodeAssistProcessor.getIcon(proposal.getImage()),
                client,
                responds.getSessionId(),
                linkedEditor,
                refactoringUpdater,
                editorAgent);
      }
      proposals.add(completionProposal);
    }

    callback.proposalComputed(proposals);
  }

  private void setupProposals(
      final CodeAssistCallback callback,
      final TextEditor textEditor,
      final int offset,
      final List<Problem> annotations) {
    final VirtualFile file = textEditor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();

      Unmarshallable<Proposals> unmarshaller = unmarshallerFactory.newUnmarshaller(Proposals.class);
      client.computeAssistProposals(
          project.get().getLocation().toString(),
          resolveFQN(file),
          offset,
          annotations,
          new AsyncRequestCallback<Proposals>(unmarshaller) {
            @Override
            protected void onSuccess(Proposals proposals) {
              showProposals(callback, proposals, textEditor);
            }

            @Override
            protected void onFailure(Throwable throwable) {
              Log.error(JavaCodeAssistProcessor.class, throwable);
            }
          });
    }
  }

  private int collectQuickFixableAnnotations(
      final LinearRange lineRange,
      Document document,
      final Map<Annotation, Position> annotations,
      final boolean goToClosest,
      List<Problem> resultingProblems) {
    int invocationLocation = lineRange.getStartOffset();
    if (goToClosest) {

      LinearRange line =
          document.getLinearRangeForLine(
              document.getPositionFromIndex(lineRange.getStartOffset()).getLine());
      int rangeStart = line.getStartOffset();
      int rangeEnd = rangeStart + line.getLength();

      ArrayList<Position> allPositions = new ArrayList<>();
      List<JavaAnnotation> allAnnotations = new ArrayList<>();
      int bestOffset = Integer.MAX_VALUE;
      for (Annotation problem : annotations.keySet()) {
        if (problem instanceof JavaAnnotation) {
          JavaAnnotation ann = ((JavaAnnotation) problem);

          Position pos = annotations.get(problem);
          if (pos != null && isInside(pos.offset, rangeStart, rangeEnd)) { // inside our range?

            allAnnotations.add(ann);
            allPositions.add(pos);
            bestOffset = processAnnotation(problem, pos, invocationLocation, bestOffset);
          }
        }
      }
      if (bestOffset == Integer.MAX_VALUE) {
        return invocationLocation;
      }
      for (int i = 0; i < allPositions.size(); i++) {
        Position pos = allPositions.get(i);
        if (isInside(bestOffset, pos.offset, pos.offset + pos.length)) {
          resultingProblems.add(createProblem(allAnnotations.get(i), pos));
        }
      }
      return bestOffset;
    } else {
      for (Annotation problem : annotations.keySet()) {
        Position pos = annotations.get(problem);
        if (pos != null && isInside(invocationLocation, pos.offset, pos.offset + pos.length)) {
          resultingProblems.add(createProblem((JavaAnnotation) problem, pos));
        }
      }
      return invocationLocation;
    }
  }

  private Problem createProblem(JavaAnnotation javaAnnotation, Position pos) {
    Problem problem = dtoFactory.createDto(Problem.class);
    // server use only this fields
    problem.setID(javaAnnotation.getId());
    problem.setError(javaAnnotation.isError());
    problem.setArguments(Arrays.asList(javaAnnotation.getArguments()));
    problem.setSourceStart(pos.getOffset());
    // TODO I don't know why but in that place source end is bugger on 1 char
    problem.setSourceEnd(pos.getOffset() + pos.getLength() - 1);

    return problem;
  }

  private static int processAnnotation(
      Annotation annot, Position pos, int invocationLocation, int bestOffset) {
    final int posBegin = pos.offset;
    final int posEnd = posBegin + pos.length;
    if (isInside(invocationLocation, posBegin, posEnd)) { // covers invocation location?
      return invocationLocation;
    } else if (bestOffset != invocationLocation) {
      final int newClosestPosition = computeBestOffset(posBegin, invocationLocation, bestOffset);
      if (newClosestPosition != -1) {
        if (newClosestPosition != bestOffset) { // new best
          if (JavaAnnotationUtil.hasCorrections(annot)) { // only jump to it if there are proposals
            return newClosestPosition;
          }
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
   * @param newOffset the offset to llok at
   * @param invocationLocation the invocation location
   * @param bestOffset the current best offset
   * @return -1 is returned if the given offset is not closer or the new best offset
   */
  private static int computeBestOffset(int newOffset, int invocationLocation, int bestOffset) {
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

  /**
   * Tells is the offset is inside the (inclusive) range defined by start-end.
   *
   * @param offset the offset
   * @param start the start of the range
   * @param end the end of the range
   * @return true iff offset is inside
   */
  private static boolean isInside(int offset, int start, int end) {
    return offset == start
        || offset == end
        || (offset > start && offset < end); // make sure to handle 0-length ranges
  }
}
