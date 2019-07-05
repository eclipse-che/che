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
package org.eclipse.che.plugin.cpp.ide.inject;

import static com.google.gwt.inject.client.multibindings.GinMultibinder.newSetBinder;
import static org.eclipse.che.plugin.cpp.ide.CppResources.INSTANCE;
import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_EXT;
import static org.eclipse.che.plugin.cpp.shared.Constants.C_EXT;
import static org.eclipse.che.plugin.cpp.shared.Constants.H_EXT;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.cpp.ide.CppLanguageDescriptionProvider;
import org.eclipse.che.plugin.cpp.ide.project.CProjectWizardRegistrar;
import org.eclipse.che.plugin.cpp.ide.project.CppProjectWizardRegistrar;

/** @author Vitalii Parfonov */
@ExtensionGinModule
public class CppGinModule extends AbstractGinModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(CppProjectWizardRegistrar.class);
    newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(CProjectWizardRegistrar.class);
    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(CppLanguageDescriptionProvider.class);
  }

  @Provides
  @Singleton
  @Named("CFileType")
  protected FileType provideCFile(FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(INSTANCE.cFile(), C_EXT);
  }

  @Provides
  @Singleton
  @Named("CppFileType")
  protected FileType provideCppFile(FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(INSTANCE.cppFile(), CPP_EXT);
  }

  @Provides
  @Singleton
  @Named("HFileType")
  protected FileType provideHeaderFile(FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(INSTANCE.cHeaderFile(), H_EXT);
  }
}
