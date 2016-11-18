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
package org.eclipse.che.ide.projectimport;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.ProjectImportersServiceClient;
import org.eclipse.che.ide.api.project.ProjectImportersServiceClientImpl;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistry;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.projectimport.wizard.ImportWizardFactory;
import org.eclipse.che.ide.projectimport.wizard.ImportWizardRegistryImpl;
import org.eclipse.che.ide.projectimport.wizard.ProjectNotificationSubscriberImpl;
import org.eclipse.che.ide.projectimport.zip.ZipImportWizardRegistrar;

/**
 * GIN module for configuring components related to projects importing.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectImportModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ProjectImportersServiceClient.class).to(ProjectImportersServiceClientImpl.class).in(Singleton.class);

        GinMultibinder.newSetBinder(binder(), ImportWizardRegistrar.class).addBinding().to(ZipImportWizardRegistrar.class);

        bind(ImportWizardRegistry.class).to(ImportWizardRegistryImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(ImportWizardFactory.class));

        bind(ProjectNotificationSubscriber.class).to(ProjectNotificationSubscriberImpl.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder()
                        .implement(ProjectNotificationSubscriber.class, ProjectNotificationSubscriberImpl.class)
                        .build(ImportProjectNotificationSubscriberFactory.class));
    }
}
