/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder;
import org.eclipse.che.plugin.maven.client.command.MavenCommandType;
import org.eclipse.che.plugin.maven.client.editor.PomEditorConfigurationFactory;
import org.eclipse.che.plugin.maven.client.editor.PomReconcilingStrategyFactory;
import org.eclipse.che.plugin.maven.client.project.ResolvingMavenProjectStateHolder;
import org.eclipse.che.plugin.maven.client.resource.MavenProjectInterceptor;
import org.eclipse.che.plugin.maven.client.resource.MavenSourceFolderInterceptor;
import org.eclipse.che.plugin.maven.client.resource.PomInterceptor;
import org.eclipse.che.plugin.maven.client.wizard.MavenProjectWizardRegistrar;

/**
 * GIN module for Maven extension.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class MavenGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(MavenProjectWizardRegistrar.class);

        GinMultibinder.newSetBinder(binder(), CommandType.class).addBinding().to(MavenCommandType.class);

        GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(MavenSourceFolderInterceptor.class);
        GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(PomInterceptor.class);
        GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(MavenProjectInterceptor.class);

        install(new GinFactoryModuleBuilder().build(PomReconcilingStrategyFactory.class));
        install(new GinFactoryModuleBuilder().build(PomEditorConfigurationFactory.class));

        GinMultibinder.newSetBinder(binder(), ResolvingProjectStateHolder.class).addBinding().to(ResolvingMavenProjectStateHolder.class);
    }
}
