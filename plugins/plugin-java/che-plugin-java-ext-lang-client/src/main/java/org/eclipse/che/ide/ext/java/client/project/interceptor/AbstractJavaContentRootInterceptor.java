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

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.project.node.FolderReferenceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vlad Zhukovskiy
 */
public abstract class AbstractJavaContentRootInterceptor implements NodeInterceptor {

    private JavaNodeManager nodeManager;

    public AbstractJavaContentRootInterceptor(JavaNodeManager nodeManager) {
        this.nodeManager = nodeManager;
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

            JavaNodeSettings settings = (JavaNodeSettings)nodeManager.getJavaSettingsProvider().getSettings();

            nodes.add(nodeManager.getJavaNodeFactory().newSourceFolderNode(oldNode.getData(),
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
        final ProjectConfigDto projectConfig = folderNode.getProjectConfig();

        String srcFolder = _getSourceFolder(projectConfig, getSrcFolderAttribute());
        if (srcFolder == null) {
            return null;
        }

        if (folderNode.getStorablePath().endsWith(srcFolder)) {
            return ContentRoot.SOURCE;
        }

        String testSrcFolder = _getSourceFolder(projectConfig, getTestSrcFolderAttribute());

        if (testSrcFolder == null) {
            return null;
        }

        if (folderNode.getStorablePath().endsWith(testSrcFolder)) {
            return ContentRoot.TEST_SOURCE;
        }

        return null;
    }

    private String _getSourceFolder(ProjectConfigDto projectConfig, String srcAttribute) {
        Map<String, List<String>> attributes = projectConfig.getAttributes();
        if (!attributes.containsKey(srcAttribute)) {
            return null;
        }

        List<String> values = attributes.get(srcAttribute);

        if (values.isEmpty()) {
            return "";
        }

        String srcFolder = "";

        if ("maven.resource.folder".equals(srcAttribute)) {
            for (String srcFolderValue : values) {
                if (srcFolderValue.endsWith("/resources")) {
                    srcFolder = srcFolderValue;

                    break;
                }
            }
        } else {
            srcFolder = values.get(0);
        }

        return projectConfig.getPath() + (srcFolder.startsWith("/") ? srcFolder : "/" + srcFolder);
    }

    public abstract String getSrcFolderAttribute();

    public abstract String getTestSrcFolderAttribute();

    @Override
    public int getPriority() {
        return MAX_PRIORITY;
    }
}
