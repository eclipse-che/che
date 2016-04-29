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

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node.NodeWidget;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.SelectNodePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.ClassFolderNodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.JarNodeInterceptor;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class LibEntryPresenterTest {
    final private static String PLAIN_TYPE = "plainJava";
    final private static String LIB        = "lib";
    final private static String CONTAINER  = "container";

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
    private NodesResources                            nodesResources;
    @Mock
    private AcceptsOneWidget                          container;
    @Mock
    private SelectNodePresenter                       selectNodePresenter;
    @Mock
    private ClasspathPagePresenter.DirtyStateListener delegate;

    @Mock
    private CurrentProject                              currentProject;
    @Mock
    private ProjectConfigDto                            projectConfig;
    @Mock
    private SVGResource                                 icon;
    @Mock
    private OMSVGSVGElement                             svgElement;
    @Mock
    private ProjectClasspathResources.EditCommandStyles style;

    private Set<String> libs       = new HashSet<>();
    private Set<String> containers = new HashSet<>();

    @InjectMocks
    private LibEntryPresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(projectConfig.getType()).thenReturn(PLAIN_TYPE);
        when(javaResources.externalLibraries()).thenReturn(icon);
        when(javaResources.jarFileIcon()).thenReturn(icon);
        when(nodesResources.simpleFolder()).thenReturn(icon);
        when(icon.getSvg()).thenReturn(svgElement);
        when(resources.removeNode()).thenReturn(icon);
        when(icon.getSvg()).thenReturn(svgElement);
        when(resources.getCss()).thenReturn(style);
        when(style.selectNode()).thenReturn("");

        presenter.setUpdateDelegate(delegate);

        libs.add(LIB);
        containers.add(CONTAINER);
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
    public void widgetShouldBEStart() throws Exception {
        presenter.go(container);

        verify(view).setAddClassFolderJarButtonState(true);
        verify(view).setAddJarButtonState(true);
        verify(view).clear();
        verify(view, times(2)).addNode(Matchers.<NodeWidget>anyObject());
        verify(delegate, times(3)).onDirtyChanged();
        assertFalse(presenter.isDirty());

        verify(container).setWidget(view);
    }

    @Test
    public void showWindowForSelectingJars() throws Exception {
        presenter.onAddJarClicked();

        verify(selectNodePresenter).show(Matchers.<LibEntryPresenter>anyObject(), Matchers.<JarNodeInterceptor>anyObject(), anyBoolean());
    }

    @Test
    public void showWindowForSelectingClassFolder() throws Exception {
        presenter.onAddClassFolderClicked();

        verify(selectNodePresenter).show(Matchers.<LibEntryPresenter>anyObject(),
                                         Matchers.<ClassFolderNodeInterceptor>anyObject(),
                                         anyBoolean());
    }

    @Test
    public void selectedNodeShouldBeRemove() throws Exception {
        presenter.go(container);
        presenter.onRemoveClicked();

        assertTrue(presenter.isDirty());
        verify(delegate, times(4)).onDirtyChanged();
        verify(view).removeNode(Matchers.<NodeWidget>anyObject());
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

        verify(view).clear();
        verify(view, times(2)).addNode(Matchers.<NodeWidget>anyObject());
        verify(delegate, times(3)).onDirtyChanged();
        assertFalse(presenter.isDirty());
        verify(delegate, times(3)).onDirtyChanged();
    }
}
