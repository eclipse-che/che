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
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.text.Position;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedData;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedPositionGroup;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.link.HasLinkedMode;
import org.eclipse.che.ide.jseditor.client.link.LinkedMode;
import org.eclipse.che.ide.jseditor.client.link.LinkedModel;
import org.eclipse.che.ide.jseditor.client.link.LinkedModelData;
import org.eclipse.che.ide.jseditor.client.link.LinkedModelGroup;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
public class JavaRefactoringRename {
    private final RenamePresenter          renamePresenter;
    private final RefactoringUpdater       refactoringUpdater;
    private final JavaLocalizationConstant locale;
    private final RefactoringServiceClient refactoringServiceClient;
    private final DtoFactory               dtoFactory;
    private final DialogFactory            dialogFactory;
    private final NotificationManager      notificationManager;
    private final MessageLoader            loader;

    private boolean    isActiveLinkedEditor;
    private LinkedMode mode;

    @Inject
    public JavaRefactoringRename(RenamePresenter renamePresenter,
                                 RefactoringUpdater refactoringUpdater,
                                 JavaLocalizationConstant locale,
                                 RefactoringServiceClient refactoringServiceClient,
                                 DtoFactory dtoFactory,
                                 DialogFactory dialogFactory,
                                 NotificationManager notificationManager,
                                 LoaderFactory loaderFactory) {
        this.renamePresenter = renamePresenter;
        this.refactoringUpdater = refactoringUpdater;
        this.locale = locale;
        this.dialogFactory = dialogFactory;
        this.refactoringServiceClient = refactoringServiceClient;
        this.dtoFactory = dtoFactory;
        this.notificationManager = notificationManager;
        this.loader = loaderFactory.newLoader();

        isActiveLinkedEditor = false;
    }

    /**
     * Launch java rename refactoring process
     *
     * @param textEditorPresenter
     *         editor where user makes refactoring
     */
    public void refactor(final TextEditor textEditorPresenter) {
        final CreateRenameRefactoring createRenameRefactoring = createRenameRefactoringDto(textEditorPresenter);

        textEditorPresenter.setFocus();

        Promise<RenameRefactoringSession> createRenamePromise = refactoringServiceClient.createRenameRefactoring(createRenameRefactoring);
        createRenamePromise.then(new Operation<RenameRefactoringSession>() {
            @Override
            public void apply(RenameRefactoringSession session) throws OperationException {
                if (session.isMastShowWizard() || isActiveLinkedEditor) {
                    renamePresenter.show(session);
                    if (mode != null) {
                        mode.exitLinkedMode(false);
                    }
                } else if (session.getLinkedModeModel() != null && textEditorPresenter instanceof HasLinkedMode) {
                    isActiveLinkedEditor = true;
                    activateLinkedModeIntoEditor(session, ((HasLinkedMode)textEditorPresenter), textEditorPresenter.getDocument());
                } else {
                    notificationManager.notify(locale.failedToRename(), locale.renameErrorEditor(), FAIL, true,
                                               textEditorPresenter.getEditorInput().getFile().getProject().getProjectConfig());
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog(locale.renameRename(), locale.renameOperationUnavailable(), null).show();
                if (mode != null) {
                    mode.exitLinkedMode(false);
                }
            }
        });
    }

    private void activateLinkedModeIntoEditor(final RenameRefactoringSession session,
                                              final HasLinkedMode linkedEditor,
                                              final Document document) {
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
        if (linkedEditor instanceof EditorWithAutoSave) {
            ((EditorWithAutoSave)linkedEditor).disableAutoSave();
        }

        mode.enterLinkedMode(model);

        mode.addListener(new LinkedMode.LinkedModeListener() {
            @Override
            public void onLinkedModeExited(boolean successful, int start, int end) {
                boolean isSuccessful = false;
                try {
                    if (successful) {
                        isSuccessful = true;
                        loader.show(locale.renameLoader());
                        String newName = document.getContentRange(start, end - start);
                        performRename(newName, session, linkedEditor);
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

    private void performRename(final String newName, RenameRefactoringSession session, final HasLinkedMode linkedEditor) {
        final LinkedRenameRefactoringApply dto = createLinkedRenameRefactoringApplyDto(newName, session.getSessionId());
        Promise<RefactoringResult> applyModelPromise = refactoringServiceClient.applyLinkedModeRename(dto);
        applyModelPromise.then(new Operation<RefactoringResult>() {
            @Override
            public void apply(RefactoringResult result) throws OperationException {
                if (result.getSeverity() > WARNING) {
                    if (linkedEditor instanceof EmbeddedTextEditorPresenter) {
                        ((EmbeddedTextEditorPresenter)linkedEditor).getUndoRedo().undo();
                    }

                    loader.hide();
                    notificationManager.notify(locale.failedToRename(), result.getEntries().get(0).getMessage() , FAIL, true);
                } else {
                    onTargetRenamed(result, linkedEditor);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                if (linkedEditor instanceof EditorWithAutoSave) {
                    ((EditorWithAutoSave)linkedEditor).enableAutoSave();
                }

                if (linkedEditor instanceof EmbeddedTextEditorPresenter) {
                    ((EmbeddedTextEditorPresenter)linkedEditor).getUndoRedo().undo();
                }

                loader.hide();
                notificationManager.notify(locale.failedToRename(), arg.getMessage(), FAIL, true);
            }
        });
    }

    private void onTargetRenamed(RefactoringResult result, HasLinkedMode linkedEditor) {
        if (linkedEditor instanceof EditorWithAutoSave) {
            ((EditorWithAutoSave)linkedEditor).enableAutoSave();
        }
        switch (result.getSeverity()) {
            case OK:
                RefactorInfo refactorInfo = RefactorInfo.of(RefactoredItemType.JAVA_ELEMENT, null);
                refactoringUpdater.updateAfterRefactoring(refactorInfo, result.getChanges());
                loader.hide();
                break;
            case INFO:
            case WARNING:
            case ERROR:
            case FATAL:
                loader.hide();
            default:
                break;
        }
    }

    @NotNull
    private CreateRenameRefactoring createRenameRefactoringDto(TextEditor editor) {
        CreateRenameRefactoring dto = dtoFactory.createDto(CreateRenameRefactoring.class);

        dto.setOffset(editor.getCursorOffset());
        dto.setRefactorLightweight(!isActiveLinkedEditor);

        String fqn = JavaSourceFolderUtil.getFQNForFile(editor.getEditorInput().getFile());
        dto.setPath(fqn);

        String projectPath = editor.getDocument().getFile().getProject().getProjectConfig().getPath();
        dto.setProjectPath(projectPath);

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
