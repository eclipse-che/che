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
package org.eclipse.che.plugin.php.ide.inject;

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
import org.eclipse.che.plugin.php.ide.PhpLanguageDescriptionProvider;
import org.eclipse.che.plugin.php.ide.PhpResources;
import org.eclipse.che.plugin.php.ide.project.PhpProjectWizardRegistrar;
import org.eclipse.che.plugin.php.shared.Constants;

/** @author Kaloyan Raev */
@ExtensionGinModule
public class PhpGinModule extends AbstractGinModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(PhpProjectWizardRegistrar.class);

    newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toProvider(PhpLanguageDescriptionProvider.class);
  }

  @Provides
  @Singleton
  @Named("PhpFileType")
  protected FileType provideCppFile(FileTypeProvider fileTypeProvider) {
    return fileTypeProvider.getByExtension(PhpResources.INSTANCE.phpFile(), Constants.PHP_EXT);
  }
}
