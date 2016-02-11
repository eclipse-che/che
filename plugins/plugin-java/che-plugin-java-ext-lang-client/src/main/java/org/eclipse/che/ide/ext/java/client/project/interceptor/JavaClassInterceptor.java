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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.node.JavaFileNode;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import java.util.List;

/**
 * @author Vlad Zhukovskiy
 */
@Singleton
public class JavaClassInterceptor implements NodeInterceptor {

    private final JavaNodeManager nodeManager;

    @Inject
    public JavaClassInterceptor(JavaNodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    public Promise<List<Node>> intercept(Node parent, List<Node> children) {
        Iterable<Node> nodes = Iterables.transform(children, intercept(parent));
        List<Node> nodeList = Lists.newArrayList(nodes);
        return Promises.resolve(nodeList);
    }

    @Override
    public int getPriority() {
        return NORM_PRIORITY;
    }

    private Function<Node, Node> intercept(final Node parent) {
        return new Function<Node, Node>() {
            @Nullable
            @Override
            public Node apply(@Nullable Node child) {
                if (!(child instanceof FileReferenceNode)) {
                    return child;
                }

                ItemReference data = ((FileReferenceNode)child).getData();

                if (!nodeManager.isJavaItemReference(data)) {
                    return child;
                }

                JavaFileNode node = nodeManager.getJavaNodeFactory()
                                               .newJavaFileNode(data,
                                                                ((HasProjectConfig)child).getProjectConfig(),
                                                                (JavaNodeSettings)nodeManager.getJavaSettingsProvider()
                                                                                             .getSettings());

                //fix parent
                node.setParent(parent);

                return node;
            }
        };
    }
}
