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
package org.eclipse.che.ide.workspace;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidget;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidgetImpl;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidget;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidgetImpl;

/**
 * GIN module for configuring Workspace API related components.
 *
 * @author Artem Zatsarynnyi
 */
public class WorkspaceApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(WorkspaceServiceClient.class).to(WorkspaceServiceClientImpl.class).in(Singleton.class);

        GinMapBinder.newMapBinder(binder(), String.class, Component.class)
                    .addBinding("WorkspaceComponentProvider")
                    .toProvider(WorkspaceComponentProvider.class);

        install(new GinFactoryModuleBuilder()
                        .implement(RecipeWidget.class, RecipeWidgetImpl.class)
                        .implement(WorkspaceWidget.class, WorkspaceWidgetImpl.class)
                        .build(WorkspaceWidgetFactory.class));
    }
}
