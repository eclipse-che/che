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
package org.eclipse.che.plugin.svn.ide.common.filteredtree;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;

/**
 * Provides an instances of {@link org.eclipse.che.ide.api.project.tree.TreeStructure}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class FilteredTreeStructureProvider implements TreeStructureProvider {
    private final static String ID = "svn-copy-to";
    private FilteredNodeFactory    nodeFactory;
    private EventBus               eventBus;
    private AppContext             appContext;
    private ProjectServiceClient   projectServiceClient;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public FilteredTreeStructureProvider(FilteredNodeFactory nodeFactory,
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

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    /** {@inheritDoc} */
    @Override
    public TreeStructure get() {
        return new FilteredTreeStructure(nodeFactory, eventBus, appContext, projectServiceClient, dtoUnmarshallerFactory);
    }
}
