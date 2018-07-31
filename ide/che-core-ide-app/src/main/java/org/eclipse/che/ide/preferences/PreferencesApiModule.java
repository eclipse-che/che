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
package org.eclipse.che.ide.preferences;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.preferences.pages.extensions.ExtensionManagerPresenter;
import org.eclipse.che.ide.preferences.pages.extensions.ExtensionManagerView;
import org.eclipse.che.ide.preferences.pages.extensions.ExtensionManagerViewImpl;
import org.eclipse.che.ide.preferences.pages.general.IdeGeneralPreferencesPresenter;
import org.eclipse.che.ide.preferences.pages.general.IdeGeneralPreferencesView;
import org.eclipse.che.ide.preferences.pages.general.IdeGeneralPreferencesViewImpl;

/** GIN module for configuring Preferences API components. */
public class PreferencesApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(PreferencesManager.class).to(PreferencesManagerImpl.class).in(Singleton.class);
    GinMultibinder.newSetBinder(binder(), PreferencesManager.class)
        .addBinding()
        .to(PreferencesManagerImpl.class);

    bind(PreferencesView.class).to(PreferencesViewImpl.class).in(Singleton.class);

    GinMultibinder<PreferencePagePresenter> pagesBinder =
        GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
    pagesBinder.addBinding().to(IdeGeneralPreferencesPresenter.class);
    pagesBinder.addBinding().to(ExtensionManagerPresenter.class);

    bind(IdeGeneralPreferencesView.class)
        .to(IdeGeneralPreferencesViewImpl.class)
        .in(Singleton.class);
    bind(ExtensionManagerView.class).to(ExtensionManagerViewImpl.class).in(Singleton.class);
  }
}
