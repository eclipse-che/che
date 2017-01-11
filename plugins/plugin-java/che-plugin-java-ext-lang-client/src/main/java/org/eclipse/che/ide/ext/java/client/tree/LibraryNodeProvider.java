/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.tree;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resources.tree.ResourceNode;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

/**
 * @author Vlad Zhukovskiy
 */
@Beta
public class LibraryNodeProvider implements NodeInterceptor {

    private final JavaNodeFactory  nodeFactory;
    private final PromiseProvider  promises;
    private final SettingsProvider settingsProvider;

    @Inject
    public LibraryNodeProvider(JavaNodeFactory nodeFactory,
                               PromiseProvider promises,
                               SettingsProvider settingsProvider) {
        this.nodeFactory = nodeFactory;
        this.promises = promises;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {
        if (parent instanceof ResourceNode) {

            final Resource resource = ((ResourceNode)parent).getData();

            if (resource.getResourceType() != PROJECT) {
                return promises.resolve(children);
            }

            final Project project = (Project)resource;

            if (isJavaProject(project) && isDisplayLibraries(project)) {
                final List<Node> intercepted = newArrayList(children);

                intercepted.add(nodeFactory.newLibrariesNode(project.getLocation(), settingsProvider.getSettings()));

                return promises.resolve(intercepted);
            }
        }

        return promises.resolve(children);
    }

    @Override
    public int getPriority() {
        return NORM_PRIORITY;
    }

    public boolean isDisplayLibraries(Project project) {
        return true;
    }
}
