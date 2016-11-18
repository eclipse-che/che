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
package org.eclipse.che.ide.part;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveView;
import org.eclipse.che.ide.api.parts.ProjectExplorerPart;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerView;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerViewImpl;
import org.eclipse.che.ide.workspace.PartStackPresenterFactory;
import org.eclipse.che.ide.workspace.PartStackViewFactory;
import org.eclipse.che.ide.workspace.perspectives.general.PerspectiveViewImpl;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * GIN module for configuring Part API components.
 *
 * @author Artem Zatsarynnyi
 */
public class PartApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(Resources.class).in(Singleton.class);
        bind(PartStackUIResources.class).to(Resources.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder()
                        .implement(PartStack.class, PartStackPresenter.class)
                        .build(PartStackPresenterFactory.class));

        install(new GinFactoryModuleBuilder()
                        .implement(PartStackView.class, PartStackViewImpl.class)
                        .build(PartStackViewFactory.class));

        bind(EditorMultiPartStack.class).to(EditorMultiPartStackPresenter.class).in(Singleton.class);

        // perspective related components
        bind(PerspectiveView.class).to(PerspectiveViewImpl.class);
        GinMapBinder.newMapBinder(binder(), String.class, Perspective.class)
                    .addBinding(PROJECT_PERSPECTIVE_ID)
                    .to(ProjectPerspective.class);

        // project explorer
        bind(ProjectExplorerView.class).to(ProjectExplorerViewImpl.class).in(Singleton.class);
        bind(ProjectExplorerPart.class).to(ProjectExplorerPresenter.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    protected PartStackPresenter.PartStackEventHandler providePartStackEventHandler(FocusManager partAgentPresenter) {
        return partAgentPresenter.getPartStackHandler();
    }

    @Provides
    @Singleton
    @Named("defaultPerspectiveId")
    protected String defaultPerspectiveId() {
        return PROJECT_PERSPECTIVE_ID;
    }
}
