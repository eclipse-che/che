/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.python.ide.inject;

import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_EXT;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.python.ide.PythonResources;
import org.eclipse.che.plugin.python.ide.project.PythonProjectWizardRegistrar;

/** @author Valeriy Svydenko */
@ExtensionGinModule
public class PythonGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(PythonProjectWizardRegistrar.class);
  }

  @Provides
  @Singleton
  @Named("PythonFileType")
  protected FileType providePythonFile() {
    return new FileType(PythonResources.INSTANCE.pythonFile(), PYTHON_EXT);
  }
}
