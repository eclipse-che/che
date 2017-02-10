/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.link.LinkedModelData;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEvent.FileEventHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedData;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedPositionGroup;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;

/**
 * Class for rename refactoring java classes
 *
 * @author Alexander Andrienko
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaRefactoringRename implements FileEventHandler {
    private final RenamePresenter          renamePresenter;
    private final RefactoringUpdater       refactoringUpdater;
    private final JavaLocalizationConstant locale;
    private final RefactoringServiceClient refactoringServiceClient;
    private final DtoFactory               dtoFactory;
    private final DialogFactory            dialogFactory;
    private final NotificationManager      notificationManager;

    private boolean       isActiveLinkedEditor;
    private TextEditor    textEditor;
    private LinkedMode    mode;
    private HasLinkedMode linkedEditor;
    private String        newName;

    @Inject
    public JavaRefactoringRename(RenamePresenter renamePresenter,
                                 RefactoringUpdater refactoringUpdater,
                                 JavaLocalizationConstant locale,
                                 RefactoringServiceClient refactoringServiceClient,
                                 DtoFactory dtoFactory,
                                 EventBus eventBus,
                                 DialogFactory dialogFactory,
                                 NotificationManager notificationManager) {
        this.renamePresenter = renamePresenter;
        this.refactoringUpdater = refactoringUpdater;
        this.locale = locale;
        this.dialogFactory = dialogFactory;
        this.refactoringServiceClient = refactoringServiceClient;
        this.dtoFactory = dtoFactory;
        this.notificationManager = notificationManager;

        isActiveLinkedEditor = false;

        eventBus.addHandler(FileEvent.TYPE, this);
    }

    /**
     * Launch java rename refactoring process
     *
     * @param textEditorPresenter
     *         editor where user makes refactoring
     */
    public void refactor(final TextEditor textEditorPresenter) {
        if (!(textEditorPresenter instanceof HasLinkedMode)) {
            return;
        }

        if (isActiveLinkedEditor) {
            createRenameSession();
        } else {
            textEditor = textEditorPresenter;

            createLinkedRenameSession();
        }

        isActiveLinkedEditor = !isActiveLinkedEditor;

        linkedEditor = (HasLinkedMode)textEditorPresenter;
        textEditorPresenter.setFocus();
    }

    private void createRenameSession() {
        final CreateRenameRefactoring refactoringSession = createRenameRefactoringDto(textEditor, false);

        Promise<RenameRefactoringSession> createRenamePromise = refactoringServiceClient.createRenameRefactoring(refactoringSession);
        createRenamePromise.then(new Operation<RenameRefactoringSession>() {
            @Override
            public void apply(RenameRefactoringSession session) throws OperationException {
                renamePresenter.show(session);
                if (mode != null) {
                    mode.exitLinkedMode(false);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                showError();
            }
        });
    }

    private void showError() {
        dialogFactory.createMessageDialog(locale.renameRename(), locale.renameOperationUnavailable(), null).show();
        if (mode != null) {
            mode.exitLinkedMode(false);
        }
    }

    private void createLinkedRenameSession() {
        final CreateRenameRefactoring refactoringSession = createRenameRefactoringDto(textEditor, true);

        Promise<RenameRefactoringSession> createRenamePromise = refactoringServiceClient.createRenameRefactoring(refactoringSession);
        createRenamePromise.then(new Operation<RenameRefactoringSession>() {
            @Override
            public void apply(RenameRefactoringSession session) throws OperationException {
                activateLinkedModeIntoEditor(session, textEditor.getDocument());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                isActiveLinkedEditor = false;
                showError();
            }
        });
    }

    @Override
    public void onFileOperation(FileEvent event) {
        if (event.getOperationType() == CLOSE && textEditor != null && textEditor.getDocument() != null &&
            textEditor.getDocument().getFile().getLocation().equals(event.getFile().getLocation())) {
            isActiveLinkedEditor = false;
        }
    }

    /** returns {@code true} if linked editor is activated. */
    public boolean isActiveLinkedEditor() {
        return isActiveLinkedEditor;
    }

    private void activateLinkedModeIntoEditor(final RenameRefactoringSession session, final Document document) {
        mode = linkedEditor.getLinkedMode();
        LinkedModel model = linkedEditor.createLinkedModel();
        LinkedModeModel linkedModeModel = session.getLinkedModeModel();
        List<LinkedModelGroup> groups = new ArrayList<>();
        for (LinkedPositionGroup positionGroup : linkedModeModel.getGroups()) {
            LinkedModelGroup group = linkedEditor.createLinkedGroup();
            LinkedData data = positionGroup.getData();
            if (data != null) {
                LinkedModelData modelData = linkedEditor.createLinkedModelData();
                modelData.setType("link");
                modelData.setValues(data.getValues());
                group.setData(modelData);
            }
            List<Position> positions = new ArrayList<>();
            for (Region region : positionGroup.getPositions()) {
                positions.add(new Position(region.getOffset(), region.getLength()));
            }
            group.setPositions(positions);
            groups.add(group);
        }
        model.setGroups(groups);
        disableAutoSave();

        mode.enterLinkedMode(model);

        mode.addListener(new LinkedMode.LinkedModeListener() {
            @Override
            public void onLinkedModeExited(boolean successful, int start, int end) {
                boolean isSuccessful = false;
                try {
                    if (successful) {
                        isSuccessful = true;
                        newName = document.getContentRange(start, end - start);
                        performRename(session);
                    }
                } finally {
                    mode.removeListener(this);
                    isActiveLinkedEditor = false;
                    if (!isSuccessful && linkedEditor instanceof EditorWithAutoSave) {
                        ((EditorWithAutoSave)linkedEditor).enableAutoSave();
                    }
                }
            }
        });
    }

    private void disableAutoSave() {
        if (linkedEditor instanceof EditorWithAutoSave) {
            ((EditorWithAutoSave)linkedEditor).disableAutoSave();
        }
    }

    private void performRename(RenameRefactoringSession session) {
        final LinkedRenameRefactoringApply dto = createLinkedRenameRefactoringApplyDto(newName, session.getSessionId());
        Promise<RefactoringResult> applyModelPromise = refactoringServiceClient.applyLinkedModeRename(dto);
        applyModelPromise.then(new Operation<RefactoringResult>() {
            @Override
            public void apply(RefactoringResult result) throws OperationException {
                switch (result.getSeverity()) {
                    case OK:
                    case INFO:
                        final VirtualFile file = textEditor.getDocument().getFile();

                        if (file instanceof Resource) {
                            final Optional<Project> project = ((Resource)file).getRelatedProject();

                            refactoringServiceClient.reindexProject(project.get().getLocation().toString());
                        }

                        refactoringUpdater.updateAfterRefactoring(result.getChanges());
                        break;
                    case WARNING:
                    case ERROR:
                        enableAutoSave();

                        undoChanges();

                        showWarningDialog();
                        break;
                    case FATAL:
                        undoChanges();

                        notificationManager.notify(locale.failedToRename(), result.getEntries().get(0).getMessage(), FAIL, FLOAT_MODE);
                        break;
                    default:
                        break;
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                enableAutoSave();

                undoChanges();

                notificationManager.notify(locale.failedToRename(), arg.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }

    private void enableAutoSave() {
        if (linkedEditor instanceof EditorWithAutoSave) {
            ((EditorWithAutoSave)linkedEditor).enableAutoSave();
        }
    }

    private void undoChanges() {
        if (linkedEditor instanceof UndoableEditor) {
            ((UndoableEditor)linkedEditor).getUndoRedo().undo();
        }
    }

    private void showWarningDialog() {
        dialogFactory.createConfirmDialog(locale.warningOperationTitle(),
                                          locale.renameWithWarnings(),
                                          locale.showRenameWizard(),
                                          locale.buttonCancel(),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  isActiveLinkedEditor = true;

                                                  refactor(textEditor);

                                                  isActiveLinkedEditor = false;
                                              }
                                          },
                                          new CancelCallback() {
                                              @Override
                                              public void cancelled() {
                                              }
                                          }).show();
    }

    @NotNull
    private CreateRenameRefactoring createRenameRefactoringDto(TextEditor editor, boolean isActiveLinkedMode) {
        CreateRenameRefactoring dto = dtoFactory.createDto(CreateRenameRefactoring.class);

        dto.setOffset(editor.getCursorOffset());
        dto.setRefactorLightweight(isActiveLinkedMode);

        final VirtualFile file = editor.getEditorInput().getFile();

        dto.setPath(JavaUtil.resolveFQN(file));

        if (file instanceof Resource) {
            final Optional<Project> project = ((Resource)file).getRelatedProject();

            dto.setProjectPath(project.get().getLocation().toString());
        }

        dto.setType(JAVA_ELEMENT);

        return dto;
    }

    @NotNull
    private LinkedRenameRefactoringApply createLinkedRenameRefactoringApplyDto(String newName, String sessionId) {
        LinkedRenameRefactoringApply dto = dtoFactory.createDto(LinkedRenameRefactoringApply.class);
        dto.setNewName(newName);
        dto.setSessionId(sessionId);
        return dto;
    }
}
