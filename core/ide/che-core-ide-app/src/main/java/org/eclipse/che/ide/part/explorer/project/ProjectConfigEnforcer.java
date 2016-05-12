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
package org.eclipse.che.ide.part.explorer.project;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.api.project.node.settings.SettingsProvider;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.factory.NodeFactory;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.api.promises.client.js.Promises.resolve;

/**
 * Interceptor monitors folders. If folder has item reference type equals to {@code project}, then this is unusual situation
 * and project configuration should be retrieved from the server side to fix this situation. Broken folder node replaces with
 * project node.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ProjectConfigEnforcer implements NodeInterceptor {

    private final ProjectServiceClient projectClient;
    private final AppContext           appContext;
    private final NodeFactory          nodeFactory;
    private final SettingsProvider     nodeSettingsProvider;

    private final Predicate<Node> PROJECT_FOLDER = new Predicate<Node>() {
        @Override
        public boolean apply(@Nullable Node input) {
            return input instanceof FolderReferenceNode && ((FolderReferenceNode)input).getData().getType().equals("project");
        }
    };

    @Inject
    public ProjectConfigEnforcer(ProjectServiceClient projectClient,
                                 AppContext appContext,
                                 NodeFactory nodeFactory,
                                 SettingsProvider nodeSettingsProvider) {
        this.projectClient = projectClient;
        this.appContext = appContext;
        this.nodeFactory = nodeFactory;
        this.nodeSettingsProvider = nodeSettingsProvider;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<Node>> intercept(Node parent, final List<Node> children) {
        final List<Node> toReplace = newArrayList(filter(children, PROJECT_FOLDER));

        if (toReplace.isEmpty()) {
            return resolve(children);
        }

        return projectClient.getProjects(appContext.getDevMachine())
                            .thenPromise(new Function<List<ProjectConfigDto>, Promise<List<Node>>>() {
                                @Override
                                public Promise<List<Node>> apply(final List<ProjectConfigDto> projects) throws FunctionException {
                                    appContext.getWorkspace().getConfig().withProjects(projects);

                                    List<Node> toAdd =
                                            newArrayList(transform(toReplace, new com.google.common.base.Function<Node, Node>() {
                                                @Nullable
                                                @Override
                                                public Node apply(@Nullable Node input) {
                                                    String path = null;

                                                    if (input instanceof HasStorablePath) {
                                                        path = ((HasStorablePath)input).getStorablePath();
                                                    }

                                                    if (isNullOrEmpty(path)) {
                                                        return input;
                                                    }

                                                    ProjectConfigDto config = null;

                                                    for (ProjectConfigDto project : projects) {
                                                        if (project.getPath().equals(((HasStorablePath)input).getStorablePath())) {
                                                            config = project;
                                                            break;
                                                        }
                                                    }

                                                    if (config == null) {
                                                        return input;
                                                    }

                                                    return nodeFactory.newProjectNode(config, nodeSettingsProvider.getSettings());
                                                }
                                            }));

                                    for (Node folder : toReplace) {
                                        children.remove(folder);
                                    }

                                    for (Node project : toAdd) {
                                        children.add(project);
                                    }

                                    return resolve(children);
                                }
                            })
                            .catchErrorPromise(new Function<PromiseError, Promise<List<Node>>>() {
                                @Override
                                public Promise<List<Node>> apply(PromiseError arg) throws FunctionException {
                                    return resolve(children);
                                }
                            });
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return MAX_PRIORITY;
    }
}
