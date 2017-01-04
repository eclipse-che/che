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
package org.eclipse.che.plugin.csharp.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.plugin.csharp.shared.Constants;
import org.eclipse.che.plugin.csharp.ide.CSharpResources;
import org.eclipse.che.plugin.csharp.ide.project.CSharpProjectWizardRegistrar;


/**
 * @author Vitalii Parfonov
 */
@ExtensionGinModule
public class CSharpGinModule extends AbstractGinModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(CSharpProjectWizardRegistrar.class);
    }

    @Provides
    @Singleton
    @Named("CSharpFileType")
    protected FileType provideCppFile() {
        return new FileType(CSharpResources.INSTANCE.csharpFile(), Constants.CSHARP_EXT);
    }
}
