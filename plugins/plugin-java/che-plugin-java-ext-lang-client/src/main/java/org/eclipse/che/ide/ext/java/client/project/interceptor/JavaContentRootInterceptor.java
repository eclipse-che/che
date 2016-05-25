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
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Interceptor for showing only java source folder.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaContentRootInterceptor implements NodeInterceptor {

    private final JavaNodeManager javaResourceNodeManager;

    @Inject
    public JavaContentRootInterceptor(JavaNodeManager javaResourceNodeManager) {
        this.javaResourceNodeManager = javaResourceNodeManager;
    }

    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {
        List<Node> nodes = new ArrayList<>();

        for (Node child : children) {
            ContentRoot contentRoot = getSourceType(child);

            if (contentRoot == null) {
                nodes.add(child);
                continue;
            }

            FolderReferenceNode oldNode = (FolderReferenceNode)child;

            JavaNodeSettings settings = (JavaNodeSettings)javaResourceNodeManager.getJavaSettingsProvider().getSettings();

            nodes.add(javaResourceNodeManager.getJavaNodeFactory().newSourceFolderNode(oldNode.getData(),
                                                                                       oldNode.getProjectConfig(),
                                                                                       settings,
                                                                                       contentRoot));
        }

        return Promises.resolve(nodes);
    }

    @Nullable
    private ContentRoot getSourceType(Node node) {
        if (!JavaNodeManager.isJavaProject(node)) {
            return null;
        }

        if (!(node instanceof FolderReferenceNode)) {
            return null;
        }

        final FolderReferenceNode folderNode = (FolderReferenceNode)node;

        Node parent = folderNode.getParent();
        while (!(parent instanceof ProjectNode)) {
            parent = parent.getParent();
        }

        HasProjectConfig project = (HasProjectConfig)parent;

        final ProjectConfigDto projectConfig = project.getProjectConfig();

        List<String> srcFolder = _getSourceFolder(projectConfig, "java.source.folder");
        if (srcFolder == null || srcFolder.isEmpty()) {
            return null;
        }

        for (String s : srcFolder) {
            if (folderNode.getStorablePath().endsWith(s)) {
                return ContentRoot.SOURCE;
            }
        }

        return null;
    }

    private List<String> _getSourceFolder(ProjectConfigDto projectConfig, String srcAttribute) {
        Map<String, List<String>> attributes = projectConfig.getAttributes();
        if (!attributes.containsKey(srcAttribute)) {
            return null;
        }

        List<String> values = attributes.get(srcAttribute);

        if (values.isEmpty()) {
            return emptyList();
        }

        List<String> srcFolders = new ArrayList<>();


        for (String src : values) {
            srcFolders.add(projectConfig.getPath() + (src.startsWith("/") ? src : "/" + src));
        }

        return srcFolders;
    }

    @Override
    public int getPriority() {
        return MAX_PRIORITY;
    }
}
