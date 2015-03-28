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
package org.eclipse.che.ide.ext.tutorials.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.ext.tutorials.client.update.UpdateServiceClient;
import org.eclipse.che.ide.ext.tutorials.client.update.UpdateServiceClientImpl;
import org.eclipse.che.ide.ext.tutorials.client.wizard.ExtensionProjectWizardRegistrar;
import org.eclipse.che.ide.ext.tutorials.client.wizard.TutorialProjectWizardRegistrar;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

/**
 * @author Vitaly Parfonov
 */
@ExtensionGinModule
public class ExtensionsGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(UpdateServiceClient.class).to(UpdateServiceClientImpl.class).in(Singleton.class);

        GinMultibinder<ProjectWizardRegistrar> projectWizardBinder = GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class);
        projectWizardBinder.addBinding().to(ExtensionProjectWizardRegistrar.class);
        projectWizardBinder.addBinding().to(TutorialProjectWizardRegistrar.class);
    }
}