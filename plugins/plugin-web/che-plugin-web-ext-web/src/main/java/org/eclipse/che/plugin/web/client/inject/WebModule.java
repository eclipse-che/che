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
package org.eclipse.che.plugin.web.client.inject;

import static com.google.gwt.inject.client.multibindings.GinMultibinder.newSetBinder;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.web.client.CamelLanguageDescriptionProvider;
import org.eclipse.che.plugin.web.client.JsonLanguageDescriptionProvider;
import org.eclipse.che.plugin.web.client.TypeScriptLanguageDescriptionProvider;
import org.eclipse.che.plugin.web.client.WebExtensionResource;
import org.eclipse.che.plugin.web.client.typescript.TSProjectWizardRegistrar;

/**
 * Adds custom binding for Editors.
 *
 * @author Florent Benoit
 */
@ExtensionGinModule
public class WebModule extends AbstractGinModule {

  @Override
  protected void configure() {
    newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(TSProjectWizardRegistrar.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(TypeScriptLanguageDescriptionProvider.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(JsonLanguageDescriptionProvider.class);
    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(CamelLanguageDescriptionProvider.class);
  }

  @Provides
  @Singleton
  @Named("CSSFileType")
  protected FileType provideCSSFile(WebExtensionResource res) {
    return new FileType(res.cssFile(), "css");
  }

  @Provides
  @Singleton
  @Named("LESSFileType")
  protected FileType provideLESSFile(WebExtensionResource res) {
    return new FileType(res.lessFile(), "less");
  }

  @Provides
  @Singleton
  @Named("JSFileType")
  protected FileType provideJSFile(WebExtensionResource res) {
    return new FileType(res.jsFile(), "js");
  }

  @Provides
  @Singleton
  @Named("ES6FileType")
  protected FileType provideES6File(WebExtensionResource res) {
    return new FileType(res.jsFile(), "es6");
  }

  @Provides
  @Singleton
  @Named("JSXFileType")
  protected FileType provideJSXFile(WebExtensionResource res) {
    return new FileType(res.jsFile(), "jsx");
  }

  @Provides
  @Singleton
  @Named("TypeScript")
  protected FileType provideTypeScriptFile(WebExtensionResource res) {
    return new FileType(res.jsFile(), "ts");
  }

  @Provides
  @Singleton
  @Named("HTMLFileType")
  protected FileType provideHTMLFile(WebExtensionResource res) {
    return new FileType(res.htmlFile(), "html");
  }

  @Provides
  @Singleton
  @Named("PHPFileType")
  protected FileType providePHPFile(WebExtensionResource res) {
    return new FileType(res.phpFile(), "php");
  }
}
