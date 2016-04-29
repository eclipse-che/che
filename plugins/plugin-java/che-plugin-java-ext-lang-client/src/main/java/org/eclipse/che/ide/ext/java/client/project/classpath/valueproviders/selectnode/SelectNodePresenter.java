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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.ClasspathNodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.JarNodeInterceptor;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Collections;

/**
 * Presenter for choosing directory for searching a node.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodePresenter implements SelectNodeView.ActionDelegate {
    private final static String WORKSPACE_PATH = "/projects";

    private final SelectNodeView           view;
    private final ProjectExplorerPresenter projectExplorerPresenter;
    private final AppContext               appContext;

    private ClasspathPagePresenter   classpathPagePresenter;
    private ClasspathNodeInterceptor interceptor;

    @Inject
    public SelectNodePresenter(SelectNodeView view, ProjectExplorerPresenter projectExplorerPresenter, AppContext appContext) {
        this.view = view;
        this.projectExplorerPresenter = projectExplorerPresenter;
        this.appContext = appContext;
        this.view.setDelegate(this);
    }

    /**
     * Show tree view with all needed nodes of the workspace.
     *
     * @param pagePresenter
     *         delegate from the property page
     * @param nodeInterceptor
     *         interceptor for showing nodes
     * @param forCurrent
     *         if is true - show only current project, otherwise - whole workspace
     */
    public void show(ClasspathPagePresenter pagePresenter, ClasspathNodeInterceptor nodeInterceptor, boolean forCurrent) {
        this.classpathPagePresenter = pagePresenter;
        this.interceptor = nodeInterceptor;
        if (forCurrent) {
            for (Node node : projectExplorerPresenter.getRootNodes()) {
                if (node.getName().equals(appContext.getCurrentProject().getRootProject().getName())) {
                    view.setStructure(Collections.singletonList(node), interceptor);
                    break;
                }
            }
        } else {
            view.setStructure(projectExplorerPresenter.getRootNodes(), interceptor);
        }

        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectedNode(String path, SVGResource icon) {
        if (interceptor instanceof JarNodeInterceptor) {
            path = WORKSPACE_PATH + path;
        }
        classpathPagePresenter.addNode(path, interceptor.getKind(), icon);
    }
}
