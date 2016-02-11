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
package org.eclipse.che.ide.extension.maven.client.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.extension.maven.client.MavenResources;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.project.node.FileReferenceNode.DISPLAY_NAME_ATTR;

/**
 * @author Vlad Zhukovskiy
 */
@Singleton
public class PomNodeInterceptor implements NodeInterceptor {

    private MavenResources resources;

    @Inject
    public PomNodeInterceptor(MavenResources resources) {
        this.resources = resources;
    }

    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {
        for (Node child : children) {
            if (child instanceof FileReferenceNode && "pom.xml".equals(child.getName())) {
                FileReferenceNode pom = (FileReferenceNode)child;
                ProjectConfigDto project = ((FileReferenceNode)child).getProject().getProjectConfig();
                Map<String, List<String>> attributes = project.getAttributes();

                pom.getAttributes().put(DISPLAY_NAME_ATTR, singletonList(attributes.containsKey(ARTIFACT_ID)
                                                                         ? attributes.get(ARTIFACT_ID).get(0)
                                                                         : project.getName() + "/" + pom.getName()));
            }
        }

        return Promises.resolve(children);
    }

    @Override
    public int getPriority() {
        return MAX_PRIORITY;
    }
}
