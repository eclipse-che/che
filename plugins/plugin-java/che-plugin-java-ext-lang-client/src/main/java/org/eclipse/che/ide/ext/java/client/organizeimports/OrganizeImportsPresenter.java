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
package org.eclipse.che.ide.ext.java.client.organizeimports;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistClient;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingResumeEvent;
import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingSuspendEvent;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * The class that manages conflicts with organize imports if if they occur.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OrganizeImportsPresenter implements OrganizeImportsView.ActionDelegate {
    private final OrganizeImportsView      view;
    private final JavaCodeAssistClient     javaCodeAssistClient;
    private final DtoFactory               dtoFactory;
    private final JavaLocalizationConstant locale;
    private final NotificationManager      notificationManager;
    private final EventBus                 eventBus;

    private int                     page;
    private List<ConflictImportDTO> choices;
    private Map<Integer, String>    selected;
    private VirtualFile             file;
    private Document                document;
    private EditorPartPresenter     editor;

    @Inject
    public OrganizeImportsPresenter(OrganizeImportsView view,
                                    JavaCodeAssistClient javaCodeAssistClient,
                                    DtoFactory dtoFactory,
                                    JavaLocalizationConstant locale,
                                    NotificationManager notificationManager,
                                    EventBus eventBus) {
        this.view = view;
        this.javaCodeAssistClient = javaCodeAssistClient;
        this.eventBus = eventBus;
        this.view.setDelegate(this);

        this.dtoFactory = dtoFactory;
        this.locale = locale;
        this.notificationManager = notificationManager;
    }

    /**
     * Make Organize imports operation. If the operation doesn't have conflicts all imports will be applied
     * otherwise a special window will be showed for resolving conflicts.
     *
     * @param editor
     *         current active editor
     */
    public void organizeImports(EditorPartPresenter editor) {
        this.editor = editor;
        this.document = ((TextEditor)editor).getDocument();
        this.file = editor.getEditorInput().getFile();

        if (file instanceof Resource) {
            final Optional<Project> project = ((Resource)file).getRelatedProject();

            final Optional<Resource> srcFolder = ((Resource)file).getParentWithMarker(SourceFolderMarker.ID);

            if (!srcFolder.isPresent()) {
                return;
            }

            final String fqn = JavaUtil.resolveFQN((Container)srcFolder.get(), (Resource)file);

            eventBus.fireEvent(newFileTrackingSuspendEvent());
            javaCodeAssistClient.organizeImports(project.get().getLocation().toString(), fqn)
                                .then(new Operation<List<ConflictImportDTO>>() {
                                    @Override
                                    public void apply(List<ConflictImportDTO> choices) throws OperationException {
                                        if (!choices.isEmpty()) {
                                            show(choices);
                                        } else {
                                            applyChanges(file);
                                        }
                                        eventBus.fireEvent(newFileTrackingResumeEvent());
                                    }
                                })
                                .catchError(new Operation<PromiseError>() {
                                    @Override
                                    public void apply(PromiseError arg) throws OperationException {
                                        String title = locale.failedToProcessOrganizeImports();
                                        String message = arg.getMessage();
                                        notificationManager.notify(title, message, FAIL, FLOAT_MODE);
                                        eventBus.fireEvent(newFileTrackingResumeEvent());
                                    }
                                });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onNextButtonClicked() {
        selected.put(page++, view.getSelectedImport());
        if (!selected.containsKey(page)) {
            String newSelection = choices.get(page).getTypeMatches().get(0);
            selected.put(page, newSelection);
        }
        view.setSelectedImport(selected.get(page));
        view.changePage(choices.get(page));
        updateButtonsState();
    }

    /** {@inheritDoc} */
    @Override
    public void onBackButtonClicked() {
        selected.put(page--, view.getSelectedImport());
        view.setSelectedImport(selected.get(page));
        view.changePage(choices.get(page));
        updateButtonsState();
    }

    /** {@inheritDoc} */
    @Override
    public void onFinishButtonClicked() {
        selected.put(page, view.getSelectedImport());

        ConflictImportDTO result = dtoFactory.createDto(ConflictImportDTO.class).withTypeMatches(new ArrayList<>(selected.values()));

        if (file instanceof Resource) {
            final Optional<Project> project = ((Resource)file).getRelatedProject();

            javaCodeAssistClient.applyChosenImports(project.get().getLocation().toString(), JavaUtil.resolveFQN(file), result)
                                .then(new Operation<Void>() {
                                    @Override
                                    public void apply(Void arg) throws OperationException {
                                        applyChanges(file);
                                        view.hide();
                                        ((TextEditor)editor).setFocus();
                                    }
                                })
                                .catchError(new Operation<PromiseError>() {
                                    @Override
                                    public void apply(PromiseError arg) throws OperationException {
                                        String title = locale.failedToProcessOrganizeImports();
                                        String message = arg.getMessage();
                                        notificationManager.notify(title, message, FAIL, FLOAT_MODE);
                                    }
                                });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelButtonClicked() {
        ((TextEditor)editor).setFocus();
    }

    /** Show Organize Imports panel with the special information. */
    private void show(List<ConflictImportDTO> choices) {
        if (choices == null || choices.isEmpty()) {
            return;
        }

        this.choices = choices;

        page = 0;
        selected = new HashMap<>(choices.size());

        String selection = choices.get(0).getTypeMatches().get(0);
        selected.put(page, selection);
        view.setSelectedImport(selection);

        updateButtonsState();

        view.show(choices.get(page));
    }

    /**
     * Update content of the file.
     *
     * @param file
     *         current file
     */
    private void applyChanges(VirtualFile file) {
        HandlesUndoRedo undoRedo = null;
        if (editor instanceof UndoableEditor) {
            undoRedo = ((UndoableEditor)editor).getUndoRedo();
        }
        try {
            if (undoRedo != null) {
                undoRedo.beginCompoundChange();
            }
            replaceContent(file, document);
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            if (undoRedo != null) {
                undoRedo.endCompoundChange();
            }
        }
    }

    private void replaceContent(VirtualFile file, final Document document) {
        if (file instanceof File) {
            file.getContent().then(new Operation<String>() {
                @Override
                public void apply(String content) throws OperationException {
                    document.replace(0, document.getContents().length(), content);
                }
            });
        }
    }

    private void updateButtonsState() {
        view.setEnableBackButton(!isFirstPage());
        view.setEnableNextButton(!isLastPage());
        view.setEnableFinishButton(selected.size() == choices.size());
    }

    private boolean isFirstPage() {
        return page == 0;
    }

    private boolean isLastPage() {
        return (choices.size() - 1) == page;
    }

}
