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

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistClient;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class OrganizeImportsPresenterTest {
    private final static String PATH  = "/project/a/b/A.java";
    private final static String WS_ID = "wsId";

    @Mock
    private OrganizeImportsView      view;
    @Mock
    private AppContext               appContext;
    @Mock
    private ProjectServiceClient     projectService;
    @Mock
    private JavaCodeAssistClient     javaCodeAssistClient;
    @Mock
    private DtoFactory               dtoFactory;
    @Mock
    private JavaLocalizationConstant locale;
    @Mock
    private NotificationManager      notificationManager;

    private OrganizeImportsPresenter presenter;

    @Mock
    private VirtualFile      file;
    @Mock
    private EditorInput      editorInput;
    @Mock
    private HasProjectConfig hasProjectConfig;
    @Mock
    private ProjectConfigDto projectConfigDto;
    @Mock
    private TextEditor       editor;
    @Mock
    private Document         document;

    @Mock
    private Promise<List<ConflictImportDTO>> importsPromise;
    @Mock
    private Promise<Void>                    resolveConflictsPromise;

    @Captor
    private ArgumentCaptor<Operation<List<ConflictImportDTO>>> importsOperation;
    @Captor
    private ArgumentCaptor<Operation<Void>>                    resolveConflictsOperation;

    private ConflictImportDTO conflict1;
    private ConflictImportDTO conflict2;


    @Before
    public void setUp() throws Exception {
        when(editor.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(file);
        when(file.getProject()).thenReturn(hasProjectConfig);
        when(hasProjectConfig.getProjectConfig()).thenReturn(projectConfigDto);
        when(projectConfigDto.getPath()).thenReturn(PATH);
        when(file.getName()).thenReturn("A.java");
        when(file.getPath()).thenReturn(PATH);
        when(javaCodeAssistClient.organizeImports(anyString(), anyString())).thenReturn(importsPromise);
        when(importsPromise.then(Matchers.<Operation<List<ConflictImportDTO>>>anyObject())).thenReturn(importsPromise);

        presenter = new OrganizeImportsPresenter(view,
                                                 appContext,
                                                 projectService,
                                                 javaCodeAssistClient,
                                                 dtoFactory,
                                                 locale,
                                                 notificationManager);

        prepareConflicts();

    }

    @Test
    public void organizeImportsShouldBeDoneWithoutConflicts() throws Exception {
        presenter.organizeImports(editor);

        verify(javaCodeAssistClient).organizeImports(PATH, "A");
        verify(importsPromise).then(importsOperation.capture());
        importsOperation.getValue().apply(Collections.emptyList());
    }

    private void prepareConflicts() {
        conflict1 = Mockito.mock(ConflictImportDTO.class);
        conflict2 = Mockito.mock(ConflictImportDTO.class);

        List<String> imports1 = Arrays.asList("import1", "import2");
        List<String> imports2 = Arrays.asList("import3", "import4");

        when(conflict1.getTypeMatches()).thenReturn(imports1);
        when(conflict2.getTypeMatches()).thenReturn(imports2);

    }

    @Test
    public void openWindowForResolvingConflicts() throws Exception {
        showOrganizeImportsWindow();

        verify(view).setSelectedImport("import1");

        verify(view).setEnableBackButton(false);
        verify(view).setEnableFinishButton(false);
        verify(view).setEnableNextButton(true);

        verify(view).show(conflict1);
    }

    private void showOrganizeImportsWindow() throws Exception {
        presenter.organizeImports(editor);

        List<ConflictImportDTO> result = Arrays.asList(conflict1, conflict2);

        verify(javaCodeAssistClient).organizeImports(PATH, "A");
        verify(importsPromise).then(importsOperation.capture());
        importsOperation.getValue().apply(result);
    }

    @Test
    public void showNextConflictPage() throws Exception {
        when(view.getSelectedImport()).thenReturn("import1");

        showOrganizeImportsWindow();
        presenter.onNextButtonClicked();

        verify(view).getSelectedImport();
        verify(view).setSelectedImport("import3");
        verify(view).changePage(conflict2);

        verify(view).setEnableBackButton(true);
        verify(view).setEnableFinishButton(true);
        verify(view).setEnableNextButton(false);
    }

    @Test
    public void showPreviousConflictPage() throws Exception {
        when(view.getSelectedImport()).thenReturn("import4");

        showOrganizeImportsWindow();
        presenter.onNextButtonClicked();
        reset(view);
        presenter.onBackButtonClicked();

        verify(view).getSelectedImport();
        verify(view).setSelectedImport("import4");
        verify(view).changePage(conflict1);

        verify(view).setEnableBackButton(false);
        verify(view).setEnableFinishButton(true);
        verify(view).setEnableNextButton(true);
    }

    @Test
    public void focusShouldBeSetAfterClosingWindow() throws Exception {
        showOrganizeImportsWindow();
        presenter.onCancelButtonClicked();

        verify(editor).setFocus();
    }

    @Test
    public void focusShouldBeSetAfterApplyingConflicts() throws Exception {
        when(view.getSelectedImport()).thenReturn("import1");
        when(dtoFactory.createDto(ConflictImportDTO.class)).thenReturn(conflict1);
        when(conflict1.withTypeMatches(Matchers.<List<String>>anyObject())).thenReturn(conflict1);
        when(javaCodeAssistClient.applyChosenImports(anyString(), anyString(), any())).thenReturn(resolveConflictsPromise);
        when(resolveConflictsPromise.then(Matchers.<Operation<Void>>anyObject())).thenReturn(resolveConflictsPromise);

        showOrganizeImportsWindow();
        presenter.onFinishButtonClicked();

        verify(resolveConflictsPromise).then(resolveConflictsOperation.capture());
        resolveConflictsOperation.getValue().apply(null);

        verify(editor).setFocus();
        verify(view).hide();
    }

}
