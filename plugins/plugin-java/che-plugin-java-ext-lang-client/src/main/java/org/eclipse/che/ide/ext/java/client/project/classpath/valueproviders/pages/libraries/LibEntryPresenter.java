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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node.NodeWidget;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.AbstractClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.SelectNodePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.ClassFolderNodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.JarNodeInterceptor;
import org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * The page for the information about libraries which are including into classpath.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class LibEntryPresenter extends AbstractClasspathPagePresenter implements LibEntryView.ActionDelegate,
                                                                                 NodeWidget.ActionDelegate {
    private final ClasspathResolver         classpathResolver;
    private final ProjectClasspathResources resources;
    private final JavaResources             javaResources;
    private final AppContext                appContext;
    private final NodesResources            nodesResources;
    private final SelectNodePresenter       selectNodePresenter;
    private final LibEntryView              view;

    private boolean                 dirty;
    private boolean                 isMaven;
    private Map<String, NodeWidget> pageNodes;
    private String                  selectedNode;

    @Inject
    public LibEntryPresenter(LibEntryView view,
                             ClasspathResolver classpathResolver,
                             JavaLocalizationConstant localization,
                             ProjectClasspathResources resources,
                             JavaResources javaResources,
                             AppContext appContext,
                             NodesResources nodesResources,
                             SelectNodePresenter selectNodePresenter) {
        super(localization.librariesPropertyName(), localization.javaBuildPathCategory(), null);
        this.view = view;
        this.classpathResolver = classpathResolver;
        this.resources = resources;
        this.javaResources = javaResources;
        this.appContext = appContext;
        this.nodesResources = nodesResources;
        this.selectNodePresenter = selectNodePresenter;

        pageNodes = new HashMap<>();
        dirty = false;

        view.setDelegate(this);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        view.clear();

        isMaven = "maven".equals(appContext.getCurrentProject().getProjectConfig().getType());

        setReadOnlyMod();

        boolean dirtyState = dirty;
        if (pageNodes.isEmpty()) {
            for (String con : classpathResolver.getContainers()) {
                addNode(con, ClasspathEntryKind.CONTAINER, javaResources.externalLibraries());
            }
            for (String lib : classpathResolver.getLibs()) {
                addNode(lib, ClasspathEntryKind.LIBRARY, lib.endsWith(".jar") ? javaResources.jarFileIcon() : nodesResources.simpleFolder());
            }
        } else {
            for (NodeWidget node : pageNodes.values()) {
                view.addNode(node);
            }
        }

        if (dirtyState != dirty) {
            dirty = dirtyState;
            delegate.onDirtyChanged();
        }

        container.setWidget(view);
    }

    @Override
    public void onAddJarClicked() {
        selectNodePresenter.show(this, new JarNodeInterceptor(), false);
    }

    @Override
    public void onAddClassFolderClicked() {
        selectNodePresenter.show(this, new ClassFolderNodeInterceptor(), false);
    }

    @Override
    public void onRemoveClicked() {
        removeSelectedNode();
    }

    @Override
    public void storeChanges() {
        classpathResolver.getLibs().clear();
        classpathResolver.getContainers().clear();
        for (NodeWidget node : pageNodes.values()) {
            if (ClasspathEntryKind.LIBRARY == node.getKind()) {
                classpathResolver.getLibs().add(node.getName());
            } else if (ClasspathEntryKind.CONTAINER == node.getKind()) {
                classpathResolver.getContainers().add(node.getName());
            }
        }
        dirty = false;
        delegate.onDirtyChanged();
    }

    @Override
    public void revertChanges() {
        view.clear();
        pageNodes.clear();
        selectedNode = null;

        for (String con : classpathResolver.getContainers()) {
            addNode(con, ClasspathEntryKind.CONTAINER, javaResources.externalLibraries());
        }
        for (String lib : classpathResolver.getLibs()) {
            addNode(lib, ClasspathEntryKind.LIBRARY, javaResources.jarFileIcon());
        }

        dirty = false;
        delegate.onDirtyChanged();
    }

    @Override
    public void clearData() {
        selectedNode = null;
        pageNodes.clear();
    }

    @Override
    public void addNode(String path, int kind, SVGResource icon) {
        if (pageNodes.keySet().contains(path)) {
            return;
        }
        if (selectedNode != null) {
            pageNodes.get(selectedNode).deselect();
        }
        selectedNode = path;
        NodeWidget addedNode = new NodeWidget(path, resources, kind, icon);

        if (isMaven) {
            addedNode.hideRemoveButton();
        }

        addedNode.setDelegate(this);
        addedNode.select();
        pageNodes.put(path, addedNode);
        dirty = true;
        view.addNode(addedNode);

        delegate.onDirtyChanged();
    }

    @Override
    public void removeSelectedNode() {
        dirty = true;
        delegate.onDirtyChanged();
        view.removeNode(pageNodes.remove(selectedNode));
        if (!pageNodes.isEmpty()) {
            selectedNode = pageNodes.keySet().iterator().next();
            pageNodes.get(selectedNode).select();
        } else {
            selectedNode = null;
        }
    }

    @Override
    public void onNodeClicked(@NotNull NodeWidget nodeWidget) {
        pageNodes.get(selectedNode).deselect();
        nodeWidget.select();
        selectedNode = nodeWidget.getName();
    }

    @Override
    public void onRemoveButtonClicked( NodeWidget nodeWidget) {
        onNodeClicked(nodeWidget);
        removeSelectedNode();
    }

    private void setReadOnlyMod() {
        view.setAddClassFolderJarButtonState(!isMaven);
        view.setAddJarButtonState(!isMaven);
    }

}
