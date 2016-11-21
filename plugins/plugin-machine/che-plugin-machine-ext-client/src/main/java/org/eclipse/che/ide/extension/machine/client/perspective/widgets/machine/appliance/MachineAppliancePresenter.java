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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe.RecipeTabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server.ServerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo.MachineInfoPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.RecipePartPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container.RecipesContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.Tab;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerView.TabSelectHandler;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartsComparator;
import org.eclipse.che.ide.part.widgets.TabItemFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * The class is a container for tab panels which display additional information about machine and adds ability to control machine's
 * processes. The class is a wrapper of {@link TabContainerPresenter} to use logic  of {@link TabContainerPresenter} for control tabs
 * instead {@link PartStackPresenter}
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class MachineAppliancePresenter extends PartStackPresenter implements ActivePartChangedHandler {

    private final MachineApplianceView        view;
    private final TabContainerPresenter       tabContainer;
//    private final TerminalContainer           terminalContainer;
    private final MachineInfoPresenter        infoPresenter;
    private final ServerPresenter             serverPresenter;
    private final RecipeTabPresenter          recipeTabPresenter;
    private final RecipesContainerPresenter   recipesContainerPresenter;
    private final WidgetsFactory              widgetsFactory;
    private final EntityFactory               entityFactory;
    private final MachineLocalizationConstant locale;

    /**
     * Contains info about active tab for corresponding machine.
     * Machine ID is the key and active tab is the value
     */
    private final Map<String, String> activeTabs;

    private MachineEntity selectedMachine;

    @Inject
    public MachineAppliancePresenter(EventBus eventBus,
                                     PartsComparator partsComparator,
                                     PartStackEventHandler partStackEventHandler,
                                     MachineApplianceView view,
                                     final MachineLocalizationConstant locale,
                                     WidgetsFactory widgetsFactory,
                                     EntityFactory entityFactory,
                                     TabItemFactory tabItemFactory,
//                                     final TerminalContainer terminalContainer,
                                     MachineInfoPresenter infoPresenter,
                                     RecipesContainerPresenter recipesContainer,
                                     ServerPresenter serverPresenter,
                                     RecipeTabPresenter recipeTabPresenter,
                                     TabContainerPresenter tabContainer) {
        super(eventBus, partStackEventHandler, tabItemFactory, partsComparator, view, null);

        this.view = view;
        this.tabContainer = tabContainer;
//        this.terminalContainer = terminalContainer;
        this.recipesContainerPresenter = recipesContainer;
        this.infoPresenter = infoPresenter;
        this.recipeTabPresenter = recipeTabPresenter;
        this.serverPresenter = serverPresenter;
        this.widgetsFactory = widgetsFactory;
        this.entityFactory = entityFactory;
        this.locale = locale;
        this.activeTabs = new HashMap<>();

        final String terminalTabName = locale.tabTerminal();
        final String infoTabName = locale.tabInfo();
        final String serverTabName = locale.tabServer();

//        TabSelectHandler terminalHandler = new TabSelectHandler() {
//            @Override
//            public void onTabSelected() {
//                activeTabs.put(selectedMachine.getId(), terminalTabName);
//                terminalContainer.addOrShowTerminal(selectedMachine);
//            }
//        };
//        createAndAddTab(terminalTabName, terminalContainer, terminalHandler);

        TabSelectHandler infoHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                activeTabs.put(selectedMachine.getId(), infoTabName);
            }
        };
        createAndAddTab(infoTabName, infoPresenter, infoHandler);

        TabSelectHandler serverHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                activeTabs.put(selectedMachine.getId(), serverTabName);
            }
        };
        createAndAddTab(serverTabName, serverPresenter, serverHandler);

        this.view.addContainer(tabContainer.getView());
        this.view.addContainer(recipesContainer.getView());

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    private void createAndAddTab(@NotNull String tabName, @NotNull TabPresenter content, @Nullable TabSelectHandler handler) {
        TabHeader header = widgetsFactory.createTabHeader(tabName);
        Tab tab = entityFactory.createTab(header, content, handler);

        tabContainer.addTab(tab);
    }

    /**
     * Shows all information and processes about current machine.
     *
     * @param machine
     *         machine for which need show info
     */
    public void showAppliance(MachineEntity machine) {
        selectedMachine = machine;

        view.showContainer(tabContainer.getView());

        String activeTab = activeTabs.get(machine.getId());
        activeTab = activeTab != null ? activeTab : locale.tabInfo();
        tabContainer.showTab(activeTab);

//        terminalContainer.addOrShowTerminal(machine);
        infoPresenter.update(machine);
        recipeTabPresenter.updateInfo(machine);
        serverPresenter.updateInfo(machine);
    }

    /** {@inheritDoc} */
    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        if (event.getActivePart() instanceof RecipePartPresenter) {
            view.showContainer(recipesContainerPresenter.getView());
        } else if (event.getActivePart() instanceof MachinePanelPresenter) {
            if (selectedMachine == null) {
                view.showStub(locale.unavailableMachineInfo());

                return;
            }

            view.showContainer(tabContainer.getView());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Shows special stub panel when no machine exist.
     *
     * @param message
     *         message which will be shown on stub panel
     */
    public void showStub(String message) {
        view.showStub(message);
    }
}
