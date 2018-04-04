/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.project;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.projectimport.InitialProjectImporter;
import org.eclipse.che.ide.projectimport.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.projectimport.wizard.ImportWizardFactory;
import org.eclipse.che.ide.projectimport.wizard.ProjectImportOutputJsonRpcNotifier;
import org.eclipse.che.ide.projectimport.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.projectimport.zip.ZipImportWizardRegistrar;
import org.eclipse.che.ide.projecttype.BlankProjectWizardRegistrar;
import org.eclipse.che.ide.projecttype.ProjectTemplateRegistryImpl;
import org.eclipse.che.ide.projecttype.ProjectTypeRegistryImpl;
import org.eclipse.che.ide.projecttype.wizard.PreSelectedProjectTypeManagerImpl;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardFactory;
import org.eclipse.che.ide.resources.ProjectTreeStateNotificationOperation;

/** GIN module for configuring Project API and project wizard related components. */
public class ProjectApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ProjectTreeNotificationsSubscriber.class).asEagerSingleton();
    bind(ProjectTreeStateNotificationOperation.class).asEagerSingleton();

    bind(ProjectTypeRegistry.class).to(ProjectTypeRegistryImpl.class).in(Singleton.class);
    bind(ProjectTypeRegistryImpl.class).asEagerSingleton();

    bind(ProjectTemplateRegistry.class).to(ProjectTemplateRegistryImpl.class).in(Singleton.class);
    bind(ProjectTemplateRegistryImpl.class).asEagerSingleton();

    // project wizard
    GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(BlankProjectWizardRegistrar.class);

    install(new GinFactoryModuleBuilder().build(ProjectWizardFactory.class));

    bind(PreSelectedProjectTypeManager.class)
        .to(PreSelectedProjectTypeManagerImpl.class)
        .in(Singleton.class);

    bind(ResolvingProjectStateHolderRegistry.class)
        .to(ResolvingProjectStateHolderRegistryImpl.class);

    // project import
    bind(InitialProjectImporter.class).asEagerSingleton();
    GinMultibinder.newSetBinder(binder(), ImportWizardRegistrar.class)
        .addBinding()
        .to(ZipImportWizardRegistrar.class);

    install(new GinFactoryModuleBuilder().build(ImportWizardFactory.class));

    install(
        new GinFactoryModuleBuilder()
            .implement(
                ProjectNotificationSubscriber.class, ProjectImportOutputJsonRpcNotifier.class)
            .build(ImportProjectNotificationSubscriberFactory.class));

    bind(WorkspaceProjectsSyncer.class).asEagerSingleton();
  }
}
