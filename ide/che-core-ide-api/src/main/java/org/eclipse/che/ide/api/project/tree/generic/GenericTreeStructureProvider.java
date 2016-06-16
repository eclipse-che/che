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
package org.eclipse.che.ide.api.project.tree.generic;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import javax.validation.constraints.NotNull;

/** @author Artem Zatsarynnyi */
final public class GenericTreeStructureProvider implements TreeStructureProvider {
    public final static String ID = "_codenvy_generic_tree_";
    private final NodeFactory            nodeFactory;
    private final EventBus               eventBus;
    private final AppContext             appContext;
    private final ProjectServiceClient   projectServiceClient;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public GenericTreeStructureProvider(NodeFactory nodeFactory,
                                        EventBus eventBus,
                                        AppContext appContext,
                                        ProjectServiceClient projectServiceClient,
                                        DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.nodeFactory = nodeFactory;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public GenericTreeStructure get() {
        return new GenericTreeStructure(nodeFactory, eventBus, appContext, projectServiceClient, dtoUnmarshallerFactory);
    }
}
