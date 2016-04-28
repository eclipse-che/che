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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.texteditor.UndoableEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistClient;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final AppContext               appContext;
    private final DtoFactory               dtoFactory;
    private final JavaLocalizationConstant locale;
    private final NotificationManager      notificationManager;

    private int                     page;
    private List<ConflictImportDTO> choices;
    private Map<Integer, String>    selected;
    private VirtualFile             file;
    private ProjectServiceClient    projectService;
    private Document                document;
    private EditorPartPresenter     editor;

    @Inject
    public OrganizeImportsPresenter(OrganizeImportsView view,
                                    AppContext appContext,
                                    ProjectServiceClient projectService,
                                    JavaCodeAssistClient javaCodeAssistClient,
                                    DtoFactory dtoFactory,
                                    JavaLocalizationConstant locale,
                                    NotificationManager notificationManager) {
        this.view = view;
        this.projectService = projectService;
        this.javaCodeAssistClient = javaCodeAssistClient;
        this.view.setDelegate(this);

        this.appContext = appContext;
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


        final String projectPath = file.getProject().getProjectConfig().getPath();
        final String fqn = JavaSourceFolderUtil.getFQNForFile(file);

        javaCodeAssistClient.organizeImports(projectPath, fqn)
                            .then(new Operation<List<ConflictImportDTO>>() {
                                @Override
                                public void apply(List<ConflictImportDTO> choices) throws OperationException {
                                    if (!choices.isEmpty()) {
                                        show(choices);
                                    } else {
                                        applyChanges(file);
                                    }
                                }
                            })
                            .catchError(new Operation<PromiseError>() {
                                @Override
                                public void apply(PromiseError arg) throws OperationException {
                                    notificationManager.notify(locale.failedToProcessOrganizeImports(), arg.getMessage(), FAIL, FLOAT_MODE);
                                }
                            });
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

        String projectPath = file.getProject().getProjectConfig().getPath();
        String fqn = JavaSourceFolderUtil.getFQNForFile(file);

        javaCodeAssistClient.applyChosenImports(projectPath, fqn, result)
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
                                    notificationManager.notify(locale.failedToProcessOrganizeImports(), arg.getMessage(), FAIL, FLOAT_MODE);
                                }
                            });
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
        projectService.getFileContent(appContext.getDevMachine(),
                                      file.getPath(),
                                      new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                          @Override
                                          protected void onSuccess(String result) {
                                              document.replace(0, document.getContents().length(), result);
                                          }

                                          @Override
                                          protected void onFailure(Throwable exception) {
                                              Log.error(getClass(), exception);
                                          }
                                      });
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
