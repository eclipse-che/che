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
package org.eclipse.che.plugin.docker.ext.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.plugin.docker.ext.client.manage.CredentialsDialogFactory;
import org.eclipse.che.plugin.docker.ext.client.manage.CredentialsPreferencesPresenter;
import org.eclipse.che.plugin.docker.ext.client.manage.input.InputDialog;
import org.eclipse.che.plugin.docker.ext.client.manage.input.InputDialogPresenter;

/**
 * @author Sergii Leschenko
 */
@ExtensionGinModule
public class DockerGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        final GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(CredentialsPreferencesPresenter.class);

        install(new GinFactoryModuleBuilder().implement(InputDialog.class, InputDialogPresenter.class)
                                             .build(CredentialsDialogFactory.class));
    }
}
