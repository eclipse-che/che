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
package org.eclipse.che.ide.project;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.ProjectServiceClientImpl;
import org.eclipse.che.ide.api.project.ProjectTemplateServiceClient;
import org.eclipse.che.ide.api.project.ProjectTemplateServiceClientImpl;
import org.eclipse.che.ide.api.project.ProjectTypeServiceClient;
import org.eclipse.che.ide.api.project.ProjectTypeServiceClientImpl;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistry;
import org.eclipse.che.ide.projecttype.BlankProjectWizardRegistrar;
import org.eclipse.che.ide.projecttype.ProjectTemplateRegistryImpl;
import org.eclipse.che.ide.projecttype.ProjectTemplatesComponent;
import org.eclipse.che.ide.projecttype.ProjectTypeComponent;
import org.eclipse.che.ide.projecttype.ProjectTypeRegistryImpl;
import org.eclipse.che.ide.projecttype.wizard.PreSelectedProjectTypeManagerImpl;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardFactory;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardRegistryImpl;

/**
 * GIN module for configuring Project API and project wizard related components.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMapBinder.newMapBinder(binder(), String.class, Component.class)
                    .addBinding("Project templates")
                    .to(ProjectTemplatesComponent.class);
        GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class)
                    .addBinding("Project types")
                    .to(ProjectTypeComponent.class);

        // clients for the REST services
        bind(ProjectTypeServiceClient.class).to(ProjectTypeServiceClientImpl.class).in(Singleton.class);
        bind(ProjectTemplateServiceClient.class).to(ProjectTemplateServiceClientImpl.class).in(Singleton.class);
        bind(ProjectServiceClient.class).to(ProjectServiceClientImpl.class).in(Singleton.class);

        // registries
        bind(ProjectTypeRegistry.class).to(ProjectTypeRegistryImpl.class).in(Singleton.class);
        bind(ProjectTemplateRegistry.class).to(ProjectTemplateRegistryImpl.class).in(Singleton.class);

        // project wizard
        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(BlankProjectWizardRegistrar.class);

        bind(ProjectWizardRegistry.class).to(ProjectWizardRegistryImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(ProjectWizardFactory.class));

        bind(PreSelectedProjectTypeManager.class).to(PreSelectedProjectTypeManagerImpl.class).in(Singleton.class);
    }
}
