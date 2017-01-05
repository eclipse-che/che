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
package org.eclipse.che.plugin.sample.wizard.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.sample.wizard.ide.SampleWizardResources;
import org.eclipse.che.plugin.sample.wizard.ide.file.NewXFileView;
import org.eclipse.che.plugin.sample.wizard.ide.file.NewXFileViewImpl;
import org.eclipse.che.plugin.sample.wizard.ide.wizard.SampleWizardRegistrar;

import static org.eclipse.che.plugin.sample.wizard.shared.Constants.C_EXT;


/**
 * @author Vitalii Parfonov
 */
@ExtensionGinModule
public class SampleWizardGinModule extends AbstractGinModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(SampleWizardRegistrar.class);
        bind(NewXFileView.class).to(NewXFileViewImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("XFileType")
    protected FileType provideXFile() {
        return new FileType(SampleWizardResources.INSTANCE.xFile(), C_EXT);
    }
}
