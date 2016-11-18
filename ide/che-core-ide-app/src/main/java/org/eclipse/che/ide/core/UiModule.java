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
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.ChoiceDialog;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.icon.DefaultIconsComponent;
import org.eclipse.che.ide.icon.IconRegistryImpl;
import org.eclipse.che.ide.menu.MainMenuView;
import org.eclipse.che.ide.menu.MainMenuViewImpl;
import org.eclipse.che.ide.menu.StatusPanelGroupView;
import org.eclipse.che.ide.menu.StatusPanelGroupViewImpl;
import org.eclipse.che.ide.ui.button.ConsoleButton;
import org.eclipse.che.ide.ui.button.ConsoleButtonFactory;
import org.eclipse.che.ide.ui.button.ConsoleButtonImpl;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogFooter;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogView;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogViewImpl;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogFooter;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogView;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogViewImpl;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogFooter;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogView;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogViewImpl;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogFooter;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogView;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogViewImpl;
import org.eclipse.che.ide.ui.dropdown.DropDownListFactory;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.eclipse.che.ide.ui.dropdown.DropDownWidgetImpl;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelPresenter;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelView;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelViewFactory;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelViewImpl;
import org.eclipse.che.ide.ui.multisplitpanel.tab.Tab;
import org.eclipse.che.ide.ui.multisplitpanel.tab.TabItemFactory;
import org.eclipse.che.ide.ui.multisplitpanel.tab.TabWidget;
import org.eclipse.che.ide.ui.toolbar.MainToolbar;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.ui.toolbar.ToolbarView;
import org.eclipse.che.ide.ui.toolbar.ToolbarViewImpl;
import org.eclipse.che.ide.workspace.WorkBenchControllerFactory;
import org.eclipse.che.ide.workspace.WorkBenchPartController;
import org.eclipse.che.ide.workspace.WorkBenchPartControllerImpl;
import org.eclipse.che.ide.workspace.WorkspaceView;
import org.eclipse.che.ide.workspace.WorkspaceViewImpl;

/**
 * GIN module for configuring UI components.
 *
 * @author Artem Zatsarynnyi
 */
public class UiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(IconRegistry.class).to(IconRegistryImpl.class).in(Singleton.class);

        GinMapBinder<String, Component> componentsBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentsBinder.addBinding("DefaultIconsComponent").to(DefaultIconsComponent.class);
        componentsBinder.addBinding("FontAwesomeInjector").to(FontAwesomeInjector.class);

        // core UI components
        install(new GinFactoryModuleBuilder()
                        .implement(WorkBenchPartController.class, WorkBenchPartControllerImpl.class)
                        .build(WorkBenchControllerFactory.class));

        bind(WorkspaceView.class).to(WorkspaceViewImpl.class).in(Singleton.class);
        bind(MainMenuView.class).to(MainMenuViewImpl.class).in(Singleton.class);
        bind(ToolbarView.class).to(ToolbarViewImpl.class);
        bind(ToolbarPresenter.class).annotatedWith(MainToolbar.class).to(ToolbarPresenter.class).in(Singleton.class);

        // dialog factory
        bind(MessageDialogFooter.class);
        bind(MessageDialogView.class).to(MessageDialogViewImpl.class);
        bind(ConfirmDialogFooter.class);
        bind(ConfirmDialogView.class).to(ConfirmDialogViewImpl.class);
        bind(ChoiceDialogFooter.class);
        bind(ChoiceDialogView.class).to(ChoiceDialogViewImpl.class);
        bind(InputDialogFooter.class);
        bind(InputDialogView.class).to(InputDialogViewImpl.class);

        install(new GinFactoryModuleBuilder()
                        .implement(MessageDialog.class, MessageDialogPresenter.class)
                        .implement(ConfirmDialog.class, ConfirmDialogPresenter.class)
                        .implement(ChoiceDialog.class, ChoiceDialogPresenter.class)
                        .implement(InputDialog.class, InputDialogPresenter.class)
                        .build(DialogFactory.class));

        // drop down list widget
        install(new GinFactoryModuleBuilder()
                        .implement(DropDownWidget.class, DropDownWidgetImpl.class)
                        .build(DropDownListFactory.class));

        // multi-split panel
        install(new GinFactoryModuleBuilder()
                        .implement(SubPanel.class, SubPanelPresenter.class)
                        .build(SubPanelFactory.class));
        install(new GinFactoryModuleBuilder()
                        .implement(SubPanelView.class, SubPanelViewImpl.class)
                        .build(SubPanelViewFactory.class));
        install(new GinFactoryModuleBuilder()
                        .implement(Tab.class, TabWidget.class)
                        .build(TabItemFactory.class));

        // miscellaneous UI components
        install(new GinFactoryModuleBuilder()
                        .implement(ConsoleButton.class, ConsoleButtonImpl.class)
                        .build(ConsoleButtonFactory.class));

        bind(StatusPanelGroupView.class).to(StatusPanelGroupViewImpl.class).in(Singleton.class);
    }
}
