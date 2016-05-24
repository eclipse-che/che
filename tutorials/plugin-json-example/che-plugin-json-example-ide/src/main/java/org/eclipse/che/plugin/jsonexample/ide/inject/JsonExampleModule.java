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
package org.eclipse.che.plugin.jsonexample.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.jsonexample.ide.editor.JsonExampleCodeAssistProcessorFactory;
import org.eclipse.che.plugin.jsonexample.ide.editor.JsonExampleEditorConfigurationFactory;
import org.eclipse.che.plugin.jsonexample.ide.project.JsonExampleProjectWizardRegistrar;

/**
 * JSON Example Gin Module for binding the project wizard and helper factories.
 */
@ExtensionGinModule
public class JsonExampleModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder
                .newSetBinder(binder(), ProjectWizardRegistrar.class)
                .addBinding()
                .to(JsonExampleProjectWizardRegistrar.class);

        // TODO: remove if not required, currently unclear
        install(new GinFactoryModuleBuilder().build(JsonExampleCodeAssistProcessorFactory.class));
        install(new GinFactoryModuleBuilder().build(JsonExampleEditorConfigurationFactory.class));
    }
}
