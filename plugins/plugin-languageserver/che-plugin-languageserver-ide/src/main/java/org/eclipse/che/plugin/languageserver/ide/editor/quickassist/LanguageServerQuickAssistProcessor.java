/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
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
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.plugin.languageserver.ide.editor.DiagnosticAnnotation;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A {@link QuickAssistProcessor} that implements LSP code actions as quick
 * assists.
 * 
 * @author Thomas Mäder
 *
 */
public class LanguageServerQuickAssistProcessor implements QuickAssistProcessor {

    private TextDocumentServiceClient textDocumentService;
    private ActionManager             actionManager;
    private PerspectiveManager        perspectiveManager;

    private final class ActionCompletionProposal implements CompletionProposal {
        private final Command command;
        private final Action  action;

        private ActionCompletionProposal(Command command, Action action) {
            this.command = command;
            this.action = action;
        }

        @Override
        public void getAdditionalProposalInfo(AsyncCallback<Widget> callback) {
        }

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
            callback.onCompletion(new Completion() {

                @Override
                public LinearRange getSelection(Document document) {
                    return null;
                }

                @Override
                public void apply(Document document) {
                    QuickassistActionEvent evt = new QuickassistActionEvent(new Presentation(), actionManager, perspectiveManager,
                                    command.getArguments());
                    action.actionPerformed(evt);
                }
            });
        }
    }

    @Inject
    public LanguageServerQuickAssistProcessor(TextDocumentServiceClient textDocumentService, ActionManager actionManager,
                                              PerspectiveManager perspectiveManager) {
        this.textDocumentService = textDocumentService;
        this.actionManager = actionManager;
        this.perspectiveManager = perspectiveManager;
    }

    @Override
    public void computeQuickAssistProposals(QuickAssistInvocationContext invocationContext, CodeAssistCallback callback) {
        LinearRange range = invocationContext.getTextEditor().getSelectedLinearRange();
        Document document = invocationContext.getTextEditor().getDocument();
        QueryAnnotationsEvent.QueryCallback annotationCallback = new QueryAnnotationsEvent.QueryCallback() {

            @Override
            public void respond(Map<Annotation, org.eclipse.che.ide.api.editor.text.Position> annotations) {
                List<Diagnostic> diagnostics = new ArrayList<>();
                // iteration with range never returns anything; need to filter ourselves.
                // https://github.com/eclipse/che/issues/4338
                annotations.entrySet().stream().filter((e) -> e.getValue().overlapsWith(range.getStartOffset(), range.getLength()))
                                .map(Entry::getKey).map(a -> (DiagnosticAnnotation) a).map(DiagnosticAnnotation::getDiagnostic)
                                .collect(Collectors.toList());

                CodeActionContext context = new CodeActionContext(diagnostics);

                TextPosition start = document.getPositionFromIndex(range.getStartOffset());
                TextPosition end = document.getPositionFromIndex(range.getStartOffset() + range.getLength());
                Position rangeStart = new Position(start.getLine(), start.getCharacter());
                Position rangeEnd = new Position(end.getLine(), end.getCharacter());
                Range rangeParam = new Range(rangeStart, rangeEnd);
                rangeParam.setEnd(rangeEnd);

                TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier(document.getFile().getLocation().toString());
                CodeActionParams params = new CodeActionParams(textDocumentIdentifier, rangeParam, context);

                Promise<List<Command>> codeAction = textDocumentService.codeAction(new CodeActionParamsDto(params));
                List<CompletionProposal> proposals = new ArrayList<>();
                codeAction.then((commands) -> {
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
        QueryAnnotationsEvent event = new QueryAnnotationsEvent.Builder().withFilter(a -> a instanceof DiagnosticAnnotation)
                        .withCallback(annotationCallback).build();
        document.getDocumentHandle().getDocEventBus().fireEvent(event);
    }

}
