/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.resources;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.data.tree.settings.impl.DummySettingsProvider;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.part.explorer.project.DefaultNodeInterceptor;
import org.eclipse.che.ide.part.explorer.project.TreeResourceRevealer;
import org.eclipse.che.ide.project.node.icon.DockerfileIconProvider;
import org.eclipse.che.ide.project.node.icon.FileIconProvider;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.resources.impl.ClipboardManagerImpl;
import org.eclipse.che.ide.resources.impl.ResourceManager;
import org.eclipse.che.ide.resources.tree.ResourceNode;

/**
 * GIN module for configuring Resource API components.
 *
 * @author Artem Zatsarynnyi
 */
public class ResourceApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(ResourceManager.ResourceFactory.class));
        install(new GinFactoryModuleBuilder().build(ResourceManager.ResourceManagerFactory.class));
        install(new GinFactoryModuleBuilder().build(ResourceNode.NodeFactory.class));

        GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(ResourceInterceptor.NoOpInterceptor.class);

        GinMultibinder<NodeInterceptor> nodeInterceptors = GinMultibinder.newSetBinder(binder(), NodeInterceptor.class);
        nodeInterceptors.addBinding().to(DefaultNodeInterceptor.class);

        bind(SettingsProvider.class).to(DummySettingsProvider.class).in(Singleton.class);

        GinMultibinder<NodeIconProvider> themeBinder = GinMultibinder.newSetBinder(binder(), NodeIconProvider.class);
        themeBinder.addBinding().to(FileIconProvider.class);
        themeBinder.addBinding().to(DockerfileIconProvider.class);

        bind(TreeResourceRevealer.class);

        bind(ClipboardManager.class).to(ClipboardManagerImpl.class);
    }
}
