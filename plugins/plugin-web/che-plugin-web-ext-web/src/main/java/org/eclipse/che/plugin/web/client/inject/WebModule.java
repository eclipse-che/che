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
package org.eclipse.che.plugin.web.client.inject;

import static com.google.gwt.inject.client.multibindings.GinMultibinder.newSetBinder;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.web.client.JsonLanguageDescriptionProvider;
import org.eclipse.che.plugin.web.client.TypeScriptLanguageDescriptionProvider;
import org.eclipse.che.plugin.web.client.VueLanguageDescriptionProvider;
import org.eclipse.che.plugin.web.client.WebExtensionResource;
import org.eclipse.che.plugin.web.client.typescript.TSProjectWizardRegistrar;
import org.eclipse.che.plugin.web.client.vue.VueProjectWizardRegistrar;

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

    newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(VueProjectWizardRegistrar.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(TypeScriptLanguageDescriptionProvider.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(VueLanguageDescriptionProvider.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(JsonLanguageDescriptionProvider.class);
  }

  @Provides
  @Singleton
  @Named("CSSFileType")
  protected FileType provideCSSFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.cssFile(), "css");
  }

  @Provides
  @Singleton
  @Named("LESSFileType")
  protected FileType provideLESSFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.lessFile(), "less");
  }

  @Provides
  @Singleton
  @Named("JSFileType")
  protected FileType provideJSFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.jsFile(), "js");
  }

  @Provides
  @Singleton
  @Named("ES6FileType")
  protected FileType provideES6File(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.es6File(), "es6");
  }

  @Provides
  @Singleton
  @Named("JSXFileType")
  protected FileType provideJSXFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.jsxFile(), "jsx");
  }

  @Provides
  @Singleton
  @Named("TypeScript")
  protected FileType provideTypeScriptFile(
      WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.tsFile(), "ts");
  }

  @Provides
  @Singleton
  @Named("HTMLFileType")
  protected FileType provideHTMLFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.htmlFile(), "html");
  }

  @Provides
  @Singleton
  @Named("VueFileType")
  protected FileType provideVueFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.vueFile(), "vue");
  }

  @Provides
  @Singleton
  @Named("PHPFileType")
  protected FileType providePHPFile(WebExtensionResource res, FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(res.phpFile(), "php");
  }
}
