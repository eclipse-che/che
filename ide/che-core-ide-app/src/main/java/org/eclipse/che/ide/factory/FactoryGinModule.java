/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.factory;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.factory.json.ImportFromConfigView;
import org.eclipse.che.ide.factory.json.ImportFromConfigViewImpl;
import org.eclipse.che.ide.factory.welcome.GreetingPartView;
import org.eclipse.che.ide.factory.welcome.GreetingPartViewImpl;
import org.eclipse.che.ide.factory.welcome.preferences.ShowWelcomePreferencePagePresenter;
import org.eclipse.che.ide.factory.welcome.preferences.ShowWelcomePreferencePageView;
import org.eclipse.che.ide.factory.welcome.preferences.ShowWelcomePreferencePageViewImpl;

/** @author Vladyslav Zhukovskii */
public class FactoryGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(JsIntervalSetter.class).asEagerSingleton();

    bind(GreetingPartView.class).to(GreetingPartViewImpl.class).in(Singleton.class);
    bind(ImportFromConfigView.class).to(ImportFromConfigViewImpl.class).in(Singleton.class);
    bind(ShowWelcomePreferencePageView.class)
        .to(ShowWelcomePreferencePageViewImpl.class)
        .in(Singleton.class);
    bind(FactoryServiceClient.class).to(FactoryServiceClientImpl.class).in(Singleton.class);

    final GinMultibinder<PreferencePagePresenter> prefBinder =
        GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
    prefBinder.addBinding().to(ShowWelcomePreferencePagePresenter.class);
  }
}
