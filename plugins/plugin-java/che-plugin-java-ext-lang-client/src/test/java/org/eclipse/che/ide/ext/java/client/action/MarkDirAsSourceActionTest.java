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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.project.classpath.service.ClasspathServiceClient;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.client.project.node.SourceFolderNode;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link MarkDirAsSourceAction}
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class MarkDirAsSourceActionTest {
    final private static String PROJECT_PATH = "/projects/path";
    final private static String TEXT = "to be or not to be";

    @Mock
    private JavaResources            javaResources;
    @Mock
    private AppContext               appContext;
    @Mock
    private ClasspathServiceClient   classpathService;
    @Mock
    private ClasspathResolver        classpathResolver;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private ProjectExplorerPresenter projectExplorerPresenter;
    @Mock
    private JavaLocalizationConstant locale;

    @Mock
    private CurrentProject                   currentProject;
    @Mock
    private ProjectConfigDto                 projectConfigDto;
    @Mock
    private ActionEvent                      actionEvent;
    @Mock
    private Presentation                     presentation;
    @Mock
    private Selection                        selection;
    @Mock
    private FolderReferenceNode              folderReferenceNode;
    @Mock
    private Promise<List<ClasspathEntryDto>> classpathPromise;
    @Mock
    private PromiseError                     promiseError;

    @Captor
    private ArgumentCaptor<Operation<List<ClasspathEntryDto>>> classpathCapture;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>            classpathErrorCapture;

    @InjectMocks
    private MarkDirAsSourceAction action;

    @Before
    public void setUp() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfigDto);
        when(projectConfigDto.getPath()).thenReturn(PROJECT_PATH);
        when(projectExplorerPresenter.getSelection()).thenReturn(selection);
        when(selection.getHeadElement()).thenReturn(folderReferenceNode);
        when(actionEvent.getPresentation()).thenReturn(presentation);

        FolderReferenceNode folder = mock(FolderReferenceNode.class);
        when(selection.getHeadElement()).thenReturn(folder);

        when(classpathService.getClasspath(anyString())).thenReturn(classpathPromise);
        when(classpathPromise.then(Matchers.<Operation<List<ClasspathEntryDto>>>anyObject())).thenReturn(classpathPromise);
    }

    @Test
    public void titleAndDescriptionShouldBeSet() throws Exception {
        verify(locale).markDirectoryAsSourceAction();
        verify(locale).markDirectoryAsSourceDescription();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        List<ClasspathEntryDto> classpathEntries = new ArrayList<>();
        Set<String> sources = new HashSet<>();
        when(folderReferenceNode.getStorablePath()).thenReturn(TEXT);
        when(classpathResolver.getSources()).thenReturn(sources);

        action.actionPerformed(actionEvent);

        verify(classpathService.getClasspath(PROJECT_PATH)).then(classpathCapture.capture());
        classpathCapture.getValue().apply(classpathEntries);

        verify(classpathResolver).resolveClasspathEntries(classpathEntries);
        verify(classpathResolver).getSources();
        Assert.assertEquals(1, sources.size());
        verify(classpathResolver).updateClasspath();
    }

    @Test
    public void readingClasspathFailed() throws Exception {
        action.actionPerformed(actionEvent);
        when(promiseError.getMessage()).thenReturn(TEXT);

        verify(classpathService.getClasspath(PROJECT_PATH)).catchError(classpathErrorCapture.capture());
        classpathErrorCapture.getValue().apply(promiseError);

        verify(notificationManager).notify("Can't get classpath", TEXT, FAIL, EMERGE_MODE);
    }

    @Test
    public void actionShouldBeHideIfSelectedNodeIsSource() throws Exception {
        SourceFolderNode sourceFolder = mock(SourceFolderNode.class);
        when(selection.getHeadElement()).thenReturn(sourceFolder);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(false);
        verify(presentation).setEnabled(anyBoolean());
    }

    @Test
    public void actionShouldBeVisibleIfSelectedNodeIsNotSource() throws Exception {
        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(anyBoolean());
    }

    @Test
    public void actionShouldBeDisableIfSelectionIsMultiple() throws Exception {
        when(selection.isSingleSelection()).thenReturn(false);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(anyBoolean());
        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeDisableIfSelectedNodeIsPackage() throws Exception {
        PackageNode packageNode = mock(PackageNode.class);
        when(selection.getHeadElement()).thenReturn(packageNode);
        when(selection.isSingleSelection()).thenReturn(true);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(anyBoolean());
        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeDisableIfSelectedNodeIsNotFolder() throws Exception {
        Object someNode = mock(Object.class);
        when(selection.getHeadElement()).thenReturn(someNode);
        when(selection.isSingleSelection()).thenReturn(true);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(anyBoolean());
        verify(presentation).setEnabled(false);
    }
}
