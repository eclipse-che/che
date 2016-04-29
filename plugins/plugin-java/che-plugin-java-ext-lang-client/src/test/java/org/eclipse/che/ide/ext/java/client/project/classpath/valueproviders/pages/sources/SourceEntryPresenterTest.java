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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources;

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
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.SourceFolderNodeInterceptor;
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
public class SourceEntryPresenterTest {
    final private static String PLAIN_TYPE = "plainJava";
    final private static String SOURCE     = "lib";

    @Mock
    private SourceEntryView                           view;
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

    private Set<String> sources = new HashSet<>();

    @InjectMocks
    private SourceEntryPresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(projectConfig.getType()).thenReturn(PLAIN_TYPE);
        when(resources.removeNode()).thenReturn(icon);
        when(icon.getSvg()).thenReturn(svgElement);
        when(javaResources.sourceFolder()).thenReturn(icon);
        when(icon.getSvg()).thenReturn(svgElement);
        when(resources.getCss()).thenReturn(style);
        when(style.selectNode()).thenReturn("");

        presenter.setUpdateDelegate(delegate);

        sources.add(SOURCE);
        when(classpathResolver.getSources()).thenReturn(sources);
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

        verify(view).setAddSourceButtonState(true);
        verify(view).clear();
        verify(view).addNode(Matchers.<NodeWidget>anyObject());
        verify(delegate, times(2)).onDirtyChanged();
        assertFalse(presenter.isDirty());

        verify(container).setWidget(view);
    }

    @Test
    public void showWindowForSelectingJars() throws Exception {
        presenter.onAddSourceClicked();

        verify(selectNodePresenter).show(Matchers.<SourceEntryPresenter>anyObject(),
                                         Matchers.<SourceFolderNodeInterceptor>anyObject(),
                                         anyBoolean());
    }

    @Test
    public void selectedNodeShouldBeRemove() throws Exception {
        presenter.go(container);
        presenter.onRemoveClicked();

        assertTrue(presenter.isDirty());
        verify(delegate, times(3)).onDirtyChanged();
        verify(view).removeNode(Matchers.<NodeWidget>anyObject());
    }

    @Test
    public void allChangesShouldStore() throws Exception {
        presenter.storeChanges();

        verify(classpathResolver).getSources();
        assertTrue(sources.isEmpty());
        assertFalse(presenter.isDirty());
        verify(delegate).onDirtyChanged();
    }

    @Test
    public void changesShouldRevert() throws Exception {
        presenter.revertChanges();

        verify(view).clear();
        verify(view).addNode(Matchers.<NodeWidget>anyObject());
        verify(delegate, times(2)).onDirtyChanged();
        assertFalse(presenter.isDirty());
        verify(delegate, times(2)).onDirtyChanged();
    }
}
