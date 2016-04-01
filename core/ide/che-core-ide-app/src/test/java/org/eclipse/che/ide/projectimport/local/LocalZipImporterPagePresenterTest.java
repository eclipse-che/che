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
package org.eclipse.che.ide.projectimport.local;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.OpenProjectEvent;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link LocalZipImporterPagePresenter} functionality.
 *
 * @author Roman Nikitenko
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalZipImporterPagePresenterTest {
    private static final String PROJECT_NAME    = "test";
    private static final String FILE_NAME       = "test.zip";
    private static final String PARSED_RESPONSE = "{\"name\": \"test-project-name\",\n" +
                                                  "        \"path\": \"/path/to/project\",\n" +
                                                  "        \"description\": \"This is test project\",\n" +
                                                  "        \"type\": \"maven\",\n" +
                                                  "        \"mixins\": [],\n" +
                                                  "        \"attributes\": {\n" +
                                                  "          \"project.attribute2\": [],\n" +
                                                  "          \"project.attribute1\": []\n" +
                                                  "        },\n" +
                                                  "        \"modules\": [],\n" +
                                                  "        \"storage\": {}\n" +
                                                  "        }}";

    private static final String RESPONSE = "<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">" + PARSED_RESPONSE + "</pre>";

    @Mock
    private ProjectServiceClient          projectServiceClient;
    @Mock
    private DtoFactory                    dtoFactory;
    @Mock
    private AppContext                    appContext;
    @Mock
    private EventBus                      eventBus;
    @Mock
    private CoreLocalizationConstant      locale;
    @Mock
    private ProjectNotificationSubscriber projectNotificationSubscriber;
    @Mock
    private LocalZipImporterPageView      view;
    @Mock
    private WorkspaceDto                  workspace;

    private LocalZipImporterPagePresenter presenter;

    @Before
    public void setUp() {
        when(appContext.getWorkspace()).thenReturn(workspace);

        presenter = new LocalZipImporterPagePresenter(view,
                                                      dtoFactory,
                                                      locale,
                                                      appContext,
                                                      "extPath",
                                                      eventBus,
                                                      projectServiceClient,
                                                      projectNotificationSubscriber);
    }

    @Test
    public void showDialogTest() {
        presenter.show();

        verify(view).setProjectName(eq(""));
        verify(view).setProjectDescription(eq(""));
        verify(view).setSkipFirstLevel(eq(true));
        verify(view).showDialog();
    }

    @Test
    public void shouldCloseDialogTest() {
        presenter.onCancelClicked();

        verify(view).closeDialog();
    }

    @Test
    public void correctProjectNameEnteredWhenZipForUploadChoosedTest() {
        when(view.getProjectName()).thenReturn(PROJECT_NAME);
        when(view.getFileName()).thenReturn(FILE_NAME);

        presenter.projectNameChanged();

        verify(view).setEnabledImportButton(eq(true));
        verify(view).hideNameError();
        verify(view, never()).showNameError();
    }

    @Test
    public void incorrectFileForUploadChoosedTest() {
        String incorrectFileName = "test.txt"; //not zip
        when(view.getProjectName()).thenReturn(PROJECT_NAME);
        when(view.getFileName()).thenReturn(incorrectFileName);

        presenter.projectNameChanged();

        verify(view).setEnabledImportButton(eq(false));
        verify(view, never()).showNameError();
    }

    @Test
    public void emptyProjectNameEnteredTest() {
        String emptyName = "";
        when(view.getFileName()).thenReturn(FILE_NAME);
        when(view.getProjectName()).thenReturn(emptyName);

        presenter.projectNameChanged();

        verify(view).setEnabledImportButton(eq(false));
        verify(view).showNameError();
    }

    @Test
    public void incorrectProjectNameEnteredTest() {
        String incorrectName = "angularjs+";
        when(view.getFileName()).thenReturn(FILE_NAME);
        when(view.getProjectName()).thenReturn(incorrectName);

        presenter.projectNameChanged();

        verify(view).setEnabledImportButton(eq(false));
        verify(view).showNameError();
    }

    @Test
    public void fileNameChangedWhenCorrectFileForUploadChoosedTest() {
        when(view.getFileName()).thenReturn("fakepath\\test.zip");
        when(view.getProjectName()).thenReturn(PROJECT_NAME);

        presenter.fileNameChanged();

        verify(view).setProjectName(eq(PROJECT_NAME));
        verify(view).setEnabledImportButton(eq(true));
        verify(view).hideNameError();
        verify(view, never()).showNameError();
    }

    @Test
    public void fileNameChangedWhenIncorrectFileForUploadChoosedTest() {
        String parsedProjectName = "";
        String incorrectFileName = "fakepath\\angularjs.txt"; //not zip
        when(view.getFileName()).thenReturn(incorrectFileName);
        when(view.getProjectName()).thenReturn(parsedProjectName);

        presenter.fileNameChanged();

        verify(view, never()).setProjectName(eq(parsedProjectName));
        verify(view, never()).setEnabledImportButton(anyBoolean());
    }

    @Test
    public void submitCompleteWhenImportIsSuccessTest() {
        reset(view);
        ProjectConfigDto projectConfigDto = mock(ProjectConfigDto.class);
        when(dtoFactory.createDtoFromJson(anyString(), Matchers.<Class<ProjectConfigDto>>anyObject())).thenReturn(projectConfigDto);

        presenter.onSubmitComplete(RESPONSE);

        verify(view).setLoaderVisibility(eq(false));
        verify(view).setInputsEnableState(eq(true));
        verify(dtoFactory).createDtoFromJson(PARSED_RESPONSE, ProjectConfigDto.class);
        verify(view).closeDialog();
        verify(projectNotificationSubscriber).onSuccess();
        verify(eventBus).fireEvent(Matchers.<Event<OpenProjectEvent>>anyObject());
        verify(projectNotificationSubscriber, never()).onFailure(anyString());
    }

    @Test
    public void onImportClickedWhenShouldImportAndOpenProjectTest() {
        when(view.getProjectName()).thenReturn(PROJECT_NAME);
        MessageDialog dialog = mock(MessageDialog.class);

        presenter.onImportClicked();

        verify(dialog, never()).show();
        verify(projectNotificationSubscriber).subscribe(eq(PROJECT_NAME));
        verify(view).setEncoding(eq(FormPanel.ENCODING_MULTIPART));
        verify(view).setAction(anyString());
        verify(view).submit();
        verify(view).setLoaderVisibility(eq(true));
        verify(view).setInputsEnableState(eq(false));
    }
}
