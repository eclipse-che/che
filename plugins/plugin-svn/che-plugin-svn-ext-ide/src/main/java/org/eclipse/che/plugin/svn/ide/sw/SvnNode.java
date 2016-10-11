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
package org.eclipse.che.plugin.svn.ide.sw;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * A tree {@link Node} to be selected as a switch location.
 *
 * @author Anatoliy Bazko
 */
public class SvnNode extends AbstractTreeNode implements HasPresentation {

    private final String                  location;
    private final String                  name;
    private final Path                    projectPath;
    private final SubversionClientService service;
    private final NotificationManager     notificationManager;
    private final NodesResources          resources;

    public SvnNode(Path projectPath,
                   String location,
                   SubversionClientService service,
                   NotificationManager notificationManager,
                   NodesResources resources) {
        this.service = service;
        this.projectPath = projectPath;
        this.location = location;
        this.notificationManager = notificationManager;
        this.resources = resources;

        String[] entries = location.split("/");
        this.name = entries.length == 0 ? location : entries[entries.length - 1];
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return service.list(projectPath, location).then(new Function<CLIOutputResponse, List<Node>>() {
            @Override
            public List<Node> apply(CLIOutputResponse response) throws FunctionException {
                List<Node> nodes = new ArrayList<>();

                List<String> output = response.getOutput();
                for (String item : output) {
                    if (item.endsWith("/")) {
                        String nodeLocation = location + "/" + item.substring(0, item.length() - 1);
                        nodes.add(new SvnNode(projectPath, nodeLocation, service, notificationManager, resources));
                    }
                }

                return nodes;
            }
        }).catchError(new Function<PromiseError, List<Node>>() {
            @Override
            public List<Node> apply(PromiseError error) throws FunctionException {
                notificationManager.notify("Error retrieving children nodes. " + error.getMessage(), FAIL, EMERGE_MODE);
                return Collections.emptyList();
            }
        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) { }

    @Override
    public NodePresentation getPresentation(boolean update) {
        return new NodePresentation(name, null, null, resources.simpleFolder());
    }
}
