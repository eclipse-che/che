/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.move.wizard.MovePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;

import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingMoveEvent;
import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingResumeEvent;
import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingSuspendEvent;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class PreviewPresenter implements PreviewView.ActionDelegate {

    private final PreviewView               view;
    private final Provider<RenamePresenter> renamePresenterProvider;
    private final DtoFactory                dtoFactory;
    private final EditorAgent               editorAgent;
    private final RefactoringUpdater        refactoringUpdater;
    private final RefactoringServiceClient  refactoringService;
    private final Provider<MovePresenter>   movePresenterProvider;
    private final EventBus eventBus;

    private RefactorInfo       refactorInfo;
    private RefactoringSession session;

    @Inject
    public PreviewPresenter(PreviewView view,
                            Provider<MovePresenter> movePresenterProvider,
                            Provider<RenamePresenter> renamePresenterProvider,
                            DtoFactory dtoFactory,
                            EditorAgent editorAgent,
                            RefactoringUpdater refactoringUpdater,
                            RefactoringServiceClient refactoringService,
                            EventBus eventBus) {
        this.view = view;
        this.renamePresenterProvider = renamePresenterProvider;
        this.dtoFactory = dtoFactory;
        this.editorAgent = editorAgent;
        this.refactoringUpdater = refactoringUpdater;
        this.refactoringService = refactoringService;
        this.eventBus = eventBus;
        this.view.setDelegate(this);

        this.movePresenterProvider = movePresenterProvider;
    }

    public void show(String refactoringSessionId, RefactorInfo refactorInfo) {
        this.refactorInfo = refactorInfo;

        session = dtoFactory.createDto(RefactoringSession.class);
        session.setSessionId(refactoringSessionId);

        refactoringService.getRefactoringPreview(session).then(new Operation<RefactoringPreview>() {
            @Override
            public void apply(RefactoringPreview changes) throws OperationException {
                view.setTreeOfChanges(changes);
            }
        });

        view.show();
    }

    /**
     * Set a title of the window.
     *
     * @param title
     *         the name of the preview window
     */
    public void setTitle(String title) {
        view.setTitle(title);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelButtonClicked() {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor instanceof TextEditor) {
            ((TextEditor)activeEditor).setFocus();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onAcceptButtonClicked() {
        eventBus.fireEvent(newFileTrackingSuspendEvent());

        refactoringService.applyRefactoring(session).then(new Operation<RefactoringResult>() {
            @Override
            public void apply(RefactoringResult result) throws OperationException {
                if (result.getSeverity() == OK) {
                    view.hide();
                    refactoringUpdater.updateAfterRefactoring(result.getChanges());
                } else {
                    view.showErrorMessage(result);
                }
                for (ChangeInfo change : result.getChanges()) {
                    final String path = change.getPath();
                    final String oldPath = change.getOldPath();

                    eventBus.fireEvent(newFileTrackingMoveEvent(path, oldPath));
                }
                eventBus.fireEvent(newFileTrackingResumeEvent());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onBackButtonClicked() {
        if (refactorInfo == null || refactorInfo.getMoveType() == null) {
            RenamePresenter renamePresenter = renamePresenterProvider.get();
            renamePresenter.show(refactorInfo);
        } else {
            MovePresenter movePresenter = movePresenterProvider.get();
            movePresenter.show(refactorInfo);
        }

        view.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onEnabledStateChanged(final RefactoringPreview change) {
        ChangeEnabledState changeEnableState = dtoFactory.createDto(ChangeEnabledState.class);
        changeEnableState.setChangeId(change.getId());
        changeEnableState.setSessionId(session.getSessionId());
        changeEnableState.setEnabled(change.isEnabled());

        refactoringService.changeChangeEnabledState(changeEnableState).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                onSelectionChanged(change);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onSelectionChanged(RefactoringPreview change) {
        RefactoringChange refactoringChanges = dtoFactory.createDto(RefactoringChange.class);
        refactoringChanges.setChangeId(change.getId());
        refactoringChanges.setSessionId(session.getSessionId());

        Promise<ChangePreview> changePreviewPromise = refactoringService.getChangePreview(refactoringChanges);
        changePreviewPromise.then(new Operation<ChangePreview>() {
            @Override
            public void apply(ChangePreview arg) throws OperationException {
                view.showDiff(arg);
            }
        });
    }

}
