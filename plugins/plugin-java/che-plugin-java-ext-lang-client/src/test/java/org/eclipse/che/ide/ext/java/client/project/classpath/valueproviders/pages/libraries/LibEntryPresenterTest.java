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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.libraries;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.command.ClasspathContainer;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.SelectNodePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.JarNodeInterceptor;
import org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class LibEntryPresenterTest {
    final private static String LIB   = "lib";

    @Mock
    private LibEntryView                              view;
    @Mock
    private ClasspathResolver                         classpathResolver;
    @Mock
    private JavaLocalizationConstant                  localization;
    @Mock
    private ProjectClasspathResources                 resources;
    @Mock
    private JavaResources                             javaResources;
    @Mock
    private AppContext                                appContext;
    @Mock
    private ClasspathEntryDto                         classpathEntryDto;
    @Mock
    private NodesResources                            nodesResources;
    @Mock
    private ClasspathContainer                        classpathContainer;
    @Mock
    private AcceptsOneWidget                          container;
    @Mock
    private SelectNodePresenter                       selectNodePresenter;
    @Mock
    private ClasspathPagePresenter.DirtyStateListener delegate;

    @Mock
    private CurrentProject                            currentProject;
    @Mock
    private ProjectConfigDto                          projectConfig;
    @Mock
    private SVGResource                               icon;
    @Mock
    private OMSVGSVGElement                           svgElement;
    @Mock
    private ProjectClasspathResources.ClasspathStyles style;

    @Mock
    private Promise<List<ClasspathEntryDto>> entriesPromise;

    @Captor
    private ArgumentCaptor<Operation<List<ClasspathEntryDto>>> acceptCaptor;

    private Set<String>             libs       = new HashSet<>();
    private Set<ClasspathEntryDto>  containers = new HashSet<>();
    private List<ClasspathEntryDto> entries    = new LinkedList<>();

    @InjectMocks
    private LibEntryPresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(projectConfig.getType()).thenReturn(JAVAC);
        when(projectConfig.getPath()).thenReturn("path");
        when(javaResources.externalLibraries()).thenReturn(icon);
        when(javaResources.jarFileIcon()).thenReturn(icon);
        when(nodesResources.simpleFolder()).thenReturn(icon);
        when(icon.getSvg()).thenReturn(svgElement);
        when(resources.removeNode()).thenReturn(icon);
        when(icon.getSvg()).thenReturn(svgElement);
        when(resources.getCss()).thenReturn(style);
        when(style.selectNode()).thenReturn("");
        when(classpathContainer.getClasspathEntries(anyString())).thenReturn(entriesPromise);
        when(classpathEntryDto.getEntryKind()).thenReturn(ClasspathEntryKind.CONTAINER);
        when(classpathEntryDto.getPath()).thenReturn("path");

        presenter.setUpdateDelegate(delegate);

        libs.add(LIB);
        containers.add(classpathEntryDto);
        entries.add(classpathEntryDto);
        when(classpathResolver.getContainers()).thenReturn(containers);
        when(classpathResolver.getLibs()).thenReturn(libs);
    }

    @Test
    public void delegateShouldBeSet() throws Exception {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void dirtyStateShouldBeReturned() throws Exception {
        assertFalse(presenter.isDirty());
    }

    @Test
    public void widgetShouldBeStart() throws Exception {
        presenter.go(container);

        verify(classpathContainer).getClasspathEntries("path");
        verify(entriesPromise).then(acceptCaptor.capture());
        acceptCaptor.getValue().apply(entries);

        verify(view).setData(anyMap());
        verify(view).renderLibraries();
    }

    @Test
    public void showWindowForSelectingJars() throws Exception {
        presenter.onAddJarClicked();

        verify(selectNodePresenter).show(Matchers.<LibEntryPresenter>anyObject(), Matchers.<JarNodeInterceptor>anyObject(), anyBoolean());
    }

    @Test
    public void allChangesShouldStore() throws Exception {
        presenter.storeChanges();

        verify(classpathResolver).getLibs();
        verify(classpathResolver).getContainers();
        assertTrue(libs.isEmpty());
        assertTrue(containers.isEmpty());
        assertFalse(presenter.isDirty());
        verify(delegate).onDirtyChanged();
    }

    @Test
    public void changesShouldRevert() throws Exception {
        presenter.revertChanges();

        verify(delegate).onDirtyChanged();
        assertFalse(presenter.isDirty());
        verify(delegate).onDirtyChanged();
    }
}
