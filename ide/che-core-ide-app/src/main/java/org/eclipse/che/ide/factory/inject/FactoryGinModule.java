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
package org.eclipse.che.ide.factory.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.factory.FactoryServiceClientImpl;
import org.eclipse.che.ide.factory.json.ImportFromConfigView;
import org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl;
import org.eclipse.che.ide.factory.welcome.GreetingPartView;
import org.eclipse.che.ide.factory.welcome.GreetingPartViewImpl;
import org.eclipse.che.ide.factory.welcome.preferences.ShowWelcomePreferencePagePresenter;
import org.eclipse.che.ide.factory.welcome.preferences.ShowWelcomePreferencePageView;
import org.eclipse.che.ide.factory.welcome.preferences.ShowWelcomePreferencePageViewImpl;

import javax.inject.Singleton;

/**
 * @author Vladyslav Zhukovskii
 */
public class FactoryGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(GreetingPartView.class).to(GreetingPartViewImpl.class).in(Singleton.class);
        bind(ImportFromConfigView.class).to(ImportFromConfigViewImpl.class).in(Singleton.class);
        bind(ShowWelcomePreferencePageView.class).to(ShowWelcomePreferencePageViewImpl.class).in(Singleton.class);
        bind(FactoryServiceClient.class).to(FactoryServiceClientImpl.class).in(Singleton.class);

        final GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(ShowWelcomePreferencePagePresenter.class);
    }
}
