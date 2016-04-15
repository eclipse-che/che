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
package org.eclipse.che.ide.ext.debugger.client.actions;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.client.configuration.DebugConfigurationsManager;
import org.eclipse.che.ide.ext.debugger.client.configuration.EditDebugConfigurationsPresenter;
import org.eclipse.che.ide.ui.dropdown.DropDownListFactory;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.eclipse.che.ide.ext.debugger.client.DebuggerExtension.GROUP_DEBUG_CONFIGURATIONS_LIST;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action that allows user to select debug configuration from the list of all existing configurations.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class SelectDebugConfigurationComboBoxAction extends AbstractPerspectiveAction implements CustomComponentAction,
                                                                                                 EditDebugConfigurationsPresenter.ConfigurationChangedListener,
                                                                                                 WsAgentStateHandler {

    private static final String                         GROUP_DEBUG_CONFIGURATIONS = "DebugConfigurationsGroup";
    private static final Comparator<DebugConfiguration> CONFIGURATIONS_COMPARATOR  = new ConfigurationsComparator();

    private final DropDownWidget             dropDownHeaderWidget;
    private final DebugConfigurationsManager debugConfigurationsManager;
    private final ActionManager              actionManager;
    private final DebuggerResources          resources;

    private List<DebugConfiguration> configurations;
    private DefaultActionGroup       configurationActions;

    @Inject
    public SelectDebugConfigurationComboBoxAction(DebuggerLocalizationConstant locale,
                                                  DebuggerResources resources,
                                                  ActionManager actionManager,
                                                  EventBus eventBus,
                                                  DropDownListFactory dropDownListFactory,
                                                  EditDebugConfigurationsPresenter editConfigurationsPresenter,
                                                  DebugConfigurationsManager debugConfigurationsManager) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.selectConfigurationActionText(),
              locale.selectConfigurationActionDescription(),
              null, null);
        this.resources = resources;
        this.actionManager = actionManager;

        this.debugConfigurationsManager = debugConfigurationsManager;
        this.dropDownHeaderWidget = dropDownListFactory.createDropDown(GROUP_DEBUG_CONFIGURATIONS);

        configurations = new LinkedList<>();

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        editConfigurationsPresenter.addConfigurationsChangedListener(this);

        configurationActions = new DefaultActionGroup(GROUP_DEBUG_CONFIGURATIONS, false, actionManager);
        actionManager.registerAction(GROUP_DEBUG_CONFIGURATIONS, configurationActions);
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        FlowPanel customComponentHeader = new FlowPanel();
        FlowPanel debugIconPanel = new FlowPanel();

        customComponentHeader.setStyleName(resources.getCss().selectConfigurationBox());
        debugIconPanel.setStyleName(resources.getCss().selectConfigurationsBoxIconPanel());
        debugIconPanel.add(new SVGImage(resources.debugIcon()));
        customComponentHeader.add(debugIconPanel);
        customComponentHeader.add((Widget)dropDownHeaderWidget);

        return customComponentHeader;
    }

    /** Returns the selected debug configuration. */
    @Nullable
    public DebugConfiguration getSelectedConfiguration() {
        if (configurations.isEmpty()) {
            return null;
        }

        final String selectedConfigurationName = dropDownHeaderWidget.getSelectedName();

        for (DebugConfiguration configuration : configurations) {
            if (configuration.getName().equals(selectedConfigurationName)) {
                return configuration;
            }
        }
        return null;
    }

    public void setSelectedConfiguration(DebugConfiguration configuration) {
        dropDownHeaderWidget.selectElement(configuration.getName(), configuration.getName());
    }

    /**
     * Load all saved debug configurations.
     *
     * @param configurationToSelect
     *         debug configuration that should be selected after loading all configurations
     */
    private void loadConfigurations(@Nullable final DebugConfiguration configurationToSelect) {
        List<DebugConfiguration> debugConfigurations = debugConfigurationsManager.readConfigurations();
        setDebugConfigurations(debugConfigurations, configurationToSelect);
    }

    /**
     * Sets debug configurations to the drop-down list.
     *
     * @param debugConfigurations
     *         collection of debug configurations to set
     * @param configurationToSelect
     *         configuration that should be selected or {@code null} if none
     */
    private void setDebugConfigurations(List<DebugConfiguration> debugConfigurations,
                                        @Nullable DebugConfiguration configurationToSelect) {
        final DefaultActionGroup configurationsList = (DefaultActionGroup)actionManager.getAction(GROUP_DEBUG_CONFIGURATIONS_LIST);

        configurations.clear();

        configurationActions.removeAll();
        if (configurationsList != null) {
            configurationActions.addAll(configurationsList);
        }
        Collections.sort(debugConfigurations, CONFIGURATIONS_COMPARATOR);
        DebugConfiguration prevConf = null;
        for (DebugConfiguration configuration : debugConfigurations) {
            if (prevConf == null || !configuration.getType().getId().equals(prevConf.getType().getId())) {
                configurationActions.addSeparator(configuration.getType().getDisplayName());
            }
            configurationActions
                    .add(dropDownHeaderWidget.createAction(configuration.getName(), configuration.getName()));
            prevConf = configuration;
        }
        configurations.addAll(debugConfigurations);

        if (configurationToSelect != null) {
            setSelectedConfiguration(configurationToSelect);
        } else {
            selectLastUsedConfiguration();
        }
    }

    /** Selects the last used configuration. */
    private void selectLastUsedConfiguration() {
        final String configName = configurations.isEmpty() ? null : configurations.get(0).getName();
        dropDownHeaderWidget.selectElement(configName, configName);
        // TODO: consider to saving last used configuration name somewhere
        // for now, we always select the first configuration
    }

    @Override
    public void onConfigurationAdded(DebugConfiguration configuration) {
        loadConfigurations(null);
    }

    @Override
    public void onConfigurationRemoved(DebugConfiguration configuration) {
        loadConfigurations(null);
    }

    @Override
    public void onConfigurationsUpdated(DebugConfiguration configuration) {
        loadConfigurations(configuration);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        loadConfigurations(null);
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    private static class ConfigurationsComparator implements Comparator<DebugConfiguration> {
        @Override
        public int compare(DebugConfiguration o1, DebugConfiguration o2) {
            return o1.getType().getId().compareTo(o2.getType().getId());
        }
    }
}
