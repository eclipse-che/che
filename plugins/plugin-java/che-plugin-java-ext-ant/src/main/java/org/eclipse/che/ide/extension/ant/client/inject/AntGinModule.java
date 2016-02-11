/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.extension.ant.client.projecttree.AntProjectTreeStructureProvider;
import org.eclipse.che.ide.extension.ant.client.wizard.AntPageView;
import org.eclipse.che.ide.extension.ant.client.wizard.AntPageViewImpl;
import org.eclipse.che.ide.extension.ant.client.projecttree.AntNodeFactory;
import org.eclipse.che.ide.extension.ant.client.wizard.AntProjectWizardRegistrar;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

/** @author Vladyslav Zhukovskii */
@ExtensionGinModule
public class AntGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(AntPageView.class).to(AntPageViewImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(AntNodeFactory.class));
        GinMultibinder.newSetBinder(binder(), TreeStructureProvider.class).addBinding().to(AntProjectTreeStructureProvider.class);

        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(AntProjectWizardRegistrar.class);
    }
}
