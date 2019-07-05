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
package org.eclipse.che.plugin.ceylon.ide.inject;

import static com.google.gwt.inject.client.multibindings.GinMultibinder.newSetBinder;
import static org.eclipse.che.plugin.ceylon.ide.CeylonResources.INSTANCE;
import static org.eclipse.che.plugin.ceylon.shared.Constants.CEYLON_EXT;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.ceylon.ide.CeylonLanguageDescriptionProvider;
import org.eclipse.che.plugin.ceylon.ide.project.CeylonProjectWizardRegistrar;

/** @author David Festal */
@ExtensionGinModule
public class CeylonGinModule extends AbstractGinModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(CeylonProjectWizardRegistrar.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(CeylonLanguageDescriptionProvider.class);
  }

  @Provides
  @Singleton
  @Named("CeylonFileType")
  protected FileType provideCeylonFile(FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(INSTANCE.ceylonFile(), CEYLON_EXT);
  }
}
