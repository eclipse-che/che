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
package org.eclipse.che.ide.part;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveView;
import org.eclipse.che.ide.part.editor.EditorPartStackPresenter;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.editor.multipart.SplitEditorPartView;
import org.eclipse.che.ide.part.editor.multipart.SplitEditorPartViewFactory;
import org.eclipse.che.ide.part.editor.multipart.SplitEditorPartViewImpl;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerView;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerViewImpl;
import org.eclipse.che.ide.part.perspectives.general.PerspectiveViewImpl;
import org.eclipse.che.ide.part.perspectives.project.ProjectPerspective;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.widgets.editortab.EditorTabWidget;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton;
import org.eclipse.che.ide.part.widgets.partbutton.PartButtonWidget;

/** GIN module for configuring Part API components. */
public class PartApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(Resources.class).in(Singleton.class);
    bind(PartStackUIResources.class).to(Resources.class).in(Singleton.class);

    install(
        new GinFactoryModuleBuilder()
            .implement(PartStack.class, PartStackPresenter.class)
            .build(PartStackPresenterFactory.class));

    install(
        new GinFactoryModuleBuilder()
            .implement(PartStackView.class, PartStackViewImpl.class)
            .build(PartStackViewFactory.class));

    install(
        new GinFactoryModuleBuilder()
            .implement(PartButton.class, PartButtonWidget.class)
            .implement(EditorTab.class, EditorTabWidget.class)
            .build(TabItemFactory.class));

    bind(EditorPartStack.class).to(EditorPartStackPresenter.class);
    bind(EditorMultiPartStack.class).to(EditorMultiPartStackPresenter.class).in(Singleton.class);
    install(
        new GinFactoryModuleBuilder()
            .implement(SplitEditorPartView.class, SplitEditorPartViewImpl.class)
            .build(SplitEditorPartViewFactory.class));

    // perspective related components
    bind(PerspectiveView.class).to(PerspectiveViewImpl.class);
    GinMapBinder.newMapBinder(binder(), String.class, Perspective.class)
        .addBinding(PROJECT_PERSPECTIVE_ID)
        .to(ProjectPerspective.class);

    // project explorer
    bind(ProjectExplorerView.class).to(ProjectExplorerViewImpl.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  protected PartStackPresenter.PartStackEventHandler providePartStackEventHandler(
      FocusManager partAgentPresenter) {
    return partAgentPresenter.getPartStackHandler();
  }

  @Provides
  @Singleton
  @Named("defaultPerspectiveId")
  protected String defaultPerspectiveId() {
    return PROJECT_PERSPECTIVE_ID;
  }
}
