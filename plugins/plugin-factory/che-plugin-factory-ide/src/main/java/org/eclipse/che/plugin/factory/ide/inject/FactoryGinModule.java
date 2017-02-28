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
package org.eclipse.che.plugin.factory.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.plugin.factory.ide.json.ImportFromConfigView;
import org.eclipse.che.plugin.factory.ide.json.ImportFromConfigViewImpl;
import org.eclipse.che.plugin.factory.ide.welcome.GreetingPartView;
import org.eclipse.che.plugin.factory.ide.welcome.GreetingPartViewImpl;
import org.eclipse.che.plugin.factory.ide.welcome.preferences.ShowWelcomePreferencePagePresenter;
import org.eclipse.che.plugin.factory.ide.welcome.preferences.ShowWelcomePreferencePageView;
import org.eclipse.che.plugin.factory.ide.welcome.preferences.ShowWelcomePreferencePageViewImpl;

import javax.inject.Singleton;

/**
 * @author Vladyslav Zhukovskii
 */
@ExtensionGinModule
public class FactoryGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(GreetingPartView.class).to(GreetingPartViewImpl.class).in(Singleton.class);
        bind(ImportFromConfigView.class).to(ImportFromConfigViewImpl.class).in(Singleton.class);
        bind(ShowWelcomePreferencePageView.class).to(ShowWelcomePreferencePageViewImpl.class).in(Singleton.class);

        final GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(ShowWelcomePreferencePagePresenter.class);
    }
}
