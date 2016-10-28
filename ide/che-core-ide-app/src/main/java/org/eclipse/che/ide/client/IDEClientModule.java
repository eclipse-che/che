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
package org.eclipse.che.ide.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.part.editor.EditorPartStackPresenter;
import org.eclipse.che.ide.part.editor.multipart.SplitEditorPartView;
import org.eclipse.che.ide.part.editor.multipart.SplitEditorPartViewFactory;
import org.eclipse.che.ide.part.editor.multipart.SplitEditorPartViewImpl;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.widgets.editortab.EditorTabWidget;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton;
import org.eclipse.che.ide.part.widgets.partbutton.PartButtonWidget;
import org.eclipse.che.ide.project.ResolvingProjectStateHolderRegistry;
import org.eclipse.che.ide.project.ResolvingProjectStateHolderRegistryImpl;

/**
 * GIN Client module for ide-client subproject. Used to maintain relations of
 * ide-client specific components.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@ExtensionGinModule
public class IDEClientModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(BootstrapController.class).in(Singleton.class);
        bind(StyleInjector.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().implement(PartButton.class, PartButtonWidget.class)
                                             .implement(EditorTab.class, EditorTabWidget.class)
                                             .build(TabItemFactory.class));

        bind(ResolvingProjectStateHolderRegistry.class).to(ResolvingProjectStateHolderRegistryImpl.class);

        bind(EditorPartStack.class).to(EditorPartStackPresenter.class);

        install(new GinFactoryModuleBuilder().implement(SplitEditorPartView.class, SplitEditorPartViewImpl.class)
                                             .build(SplitEditorPartViewFactory.class));
    }
}
