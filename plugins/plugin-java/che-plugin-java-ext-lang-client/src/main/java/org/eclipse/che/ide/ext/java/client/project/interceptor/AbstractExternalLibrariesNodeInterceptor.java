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
package org.eclipse.che.ide.ext.java.client.project.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.project.node.AbstractProjectBasedNode;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.project.node.NodeManager.isProjectOrModuleNode;

/**
 * @author Vlad Zhukovskiy
 */
@Singleton
public abstract class AbstractExternalLibrariesNodeInterceptor implements NodeInterceptor {

    private JavaNodeManager javaResourceNodeManager;

    @Inject
    public AbstractExternalLibrariesNodeInterceptor(JavaNodeManager javaResourceNodeManager) {
        this.javaResourceNodeManager = javaResourceNodeManager;
    }

    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {

        if (!isProjectOrModuleNode(parent)) {
            return Promises.resolve(children);
        }

        if (getSettings() == null || !getSettings().isShowExternalLibrariesNode() || !show((HasProjectConfig)parent)) {
            return Promises.resolve(children);
        }

        insertExternalLibrariesNode(children, createExternalLibrariesNode(parent));

        return Promises.resolve(children);
    }

    public abstract boolean show(HasProjectConfig node);

    private void insertExternalLibrariesNode(@NotNull List<Node> children, @Nullable Node externalLibrariesNode) {
        if (externalLibrariesNode != null) {
            children.add(externalLibrariesNode);
        }
    }

    @Nullable
    private Node createExternalLibrariesNode(Node parent) {
        if (!(parent instanceof AbstractProjectBasedNode<?>)) {
            return null;
        }

        if (getSettings() == null) {
            return null;
        }

        ProjectConfigDto projectConfig = ((AbstractProjectBasedNode)parent).getProjectConfig();
        return javaResourceNodeManager.getJavaNodeFactory().newExternalLibrariesNode(projectConfig, getSettings());
    }

    @Nullable
    private JavaNodeSettings getSettings() {
        NodeSettings settings = javaResourceNodeManager.getJavaSettingsProvider().getSettings();

        return settings instanceof JavaNodeSettings ? (JavaNodeSettings)settings : null;
    }

    @Override
    public int getPriority() {
        return NORM_PRIORITY;
    }
}
