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
package org.eclipse.che.ide.extension.machine.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The resource interface for the Machine extension.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public interface MachineResources extends ClientBundle {

    /** Returns the icon for clear console button. */
    @Source("images/console/clear-logs.svg")
    SVGResource clear();

    /** Returns the icon for 'Execute Selected Command' action. */
    @Source("images/execute.svg")
    SVGResource execute();

    /** Returns the new icon for command bar. */
    @Source("images/cmd-icon.svg")
    SVGResource cmdIcon();

    /** Returns the new icon for devmachine. */
    @Source("images/cube.svg")
    SVGResource devMachine();

    /** Returns the icon for 'Edit Commands...' action. */
    @Source("images/recipe.svg")
    SVGResource recipe();

    /** Returns the icon for 'Arbitrary' command type. */
    @Source("command/arbitrary/arbitrary-command-type.svg")
    SVGResource customCommandTypeSubElementIcon();

    @Source("images/process/output-icon.svg")
    SVGResource output();

    @Source("images/process/terminal-icon.svg")
    SVGResource terminal();

    @Source("images/process/terminal-tree-icon.svg")
    SVGResource terminalTreeIcon();

    @Source("images/process/add-terminal.svg")
    SVGResource addTerminalIcon();

    @Source("images/process/re-run.svg")
    SVGResource reRunIcon();

    @Source("images/process/stop.svg")
    SVGResource stopIcon();

    @Source("images/process/clear-outputs.svg")
    SVGResource clearOutputsIcon();

    @Source("images/process/scroll-to-bottom.svg")
    SVGResource scrollToBottomIcon();

    @Source("images/process/line-wrap.svg")
    SVGResource lineWrapIcon();

    /** Returns the icon for 'Custom' command type. */
    @Source("command/arbitrary/custom-command-type.svg")
    SVGResource customCommandType();

    @Source("images/project-perspective.svg")
    SVGResource projectPerspective();

    @Source("images/machine-perspective.svg")
    SVGResource operationsPerspective();

    @Source("images/machines-part-icon.svg")
    SVGResource machinesPartIcon();

    @Source("images/recipes-part-icon.svg")
    SVGResource recipesPartIcon();

    @Source("images/edit-commands.svg")
    SVGResource editCommands();

    @Source("recipe-template.txt")
    TextResource recipeTemplate();

    /** Returns the CSS resource for the Machine extension. */
    @Source({"machine.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css getCss();

    /** The CssResource interface for the Machine extension. */
    interface Css extends CssResource {

        /** Returns the CSS class name for 'Machine' console panel. */
        String machineConsole();

        /** Returns the CSS class name for 'Machine' console toolbar. */
        String consoleToolbar();

        /** Returns the CSS class name for tab-panel in 'Outputs' console. */
        String outputsConsoleTabsPanel();

        /** Returns the CSS class name for tab button in 'Outputs' console. */
        String outputsContainerConsoleTab();

        /** Returns the CSS class name for selected tab button in 'Outputs' console. */
        String outputsContainerConsoleTabSelected();

        String outputsContainerConsoleTabPanel();

        /** Returns the CSS class name for text label of tab button in 'Outputs' console. */
        String outputsContainerConsoleTabLabel();

        /** Returns the CSS class name for close button of tab button in 'Outputs' console. */
        String outputsContainerConsoleTabCloseButton();

        String activeTab();

        String disableTab();

        String activeTabText();

        String fullSize();

        String unavailableLabel();

        String selectRecipe();

        String opacityButton();

        String unSelectRecipe();

        String selectCommandBox();

        String selectCommandBoxIconPanel();

        String processTree();

        String processTreeNode();

        String differentMachineLabel();

        String dockerMachineLabel();

        String machineStatus();

        String machineStatusRunning();

        String machineStatusPausedLeft();

        String machineStatusPausedRight();

        String nameLabel();

        String processIconPanel();

        String processIcon();

        String processBadge();

        String badgeVisible();

        String newTerminalButton();

        String sshButton();

        String processNavigation();

        String processOutputPanel();

        String machineMonitors();

        /** Returns the CSS class name for close button of process in 'Consoles' panel. */
        String processesPanelCloseButtonForProcess();

        /** Returns the CSS class name for stop button of process in 'Consoles' panel. */
        String processesPanelStopButtonForProcess();

        String hideStopButton();

        /** Returns the CSS class name for the active state of tool button of 'Consoles' panel. */
        String consolesActiveToolButton();
    }
}
