/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.projecttree;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;
import org.eclipse.che.ide.ext.java.client.navigation.JavaNavigationService;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;

/**
 * Tree structure provider responsible for creating tree structure instances for project.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class AntProjectTreeStructureProvider implements TreeStructureProvider {
    public final static String ID = "ant";
    private AntNodeFactory         nodeFactory;
    private EventBus               eventBus;
    private AppContext             appContext;
    private IconRegistry           iconRegistry;
    private ProjectServiceClient   projectServiceClient;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private JavaNavigationService  service;

    /** Create instance of {@link AntProjectTreeStructureProvider}. */
    @Inject
    public AntProjectTreeStructureProvider(AntNodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                           IconRegistry iconRegistry, ProjectServiceClient projectServiceClient,
                                           DtoUnmarshallerFactory dtoUnmarshallerFactory, JavaNavigationService service) {
        this.nodeFactory = nodeFactory;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.iconRegistry = iconRegistry;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.service = service;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TreeStructure get() {
        return new AntProjectTreeStructure(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory,
                                           service);
    }
}
