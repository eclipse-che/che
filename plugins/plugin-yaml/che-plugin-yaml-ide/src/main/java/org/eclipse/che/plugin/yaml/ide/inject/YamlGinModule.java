/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.yaml.ide.inject;

import static com.google.gwt.inject.client.multibindings.GinMultibinder.newSetBinder;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.plugin.yaml.ide.YamlDescriptionProvider;
import org.eclipse.che.plugin.yaml.ide.YamlServiceClient;
import org.eclipse.che.plugin.yaml.ide.YamlServiceClientImpl;
import org.eclipse.che.plugin.yaml.ide.preferences.YamlExtensionManagerPresenter;
import org.eclipse.che.plugin.yaml.ide.preferences.YamlExtensionManagerView;
import org.eclipse.che.plugin.yaml.ide.preferences.YamlExtensionManagerViewImpl;

/**
 * Gin module for Yaml support.
 *
 * @author Joshua Pinkney
 */
@ExtensionGinModule
public class YamlGinModule extends AbstractGinModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(YamlServiceClient.class).to(YamlServiceClientImpl.class).in(Singleton.class);
    bind(YamlExtensionManagerView.class).to(YamlExtensionManagerViewImpl.class).in(Singleton.class);

    newSetBinder(binder(), PreferencePagePresenter.class)
        .addBinding()
        .to(YamlExtensionManagerPresenter.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(YamlDescriptionProvider.class);
  }
}
