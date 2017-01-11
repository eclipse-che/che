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
package org.eclipse.che.ide.extension.machine.client.processes.panel;

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * View of {@link ProcessesPanelPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProcessesPanelView extends View<ProcessesPanelView.ActionDelegate> {

    /**
     * Sets whether this view is visible.
     *
     * @param visible
     *         <code>true</code> to show the view, <code>false</code> to
     *         hide it
     */
    void setVisible(boolean visible);

    /** Add process widget */
    void addWidget(String processId, String title, SVGResource icon, IsWidget widget, boolean machineConsole);

    /** Select given process node */
    void selectNode(ProcessTreeNode node);

    /** Returns index for node with given ID */
    int getNodeIndex(String processId);

    /** Returns the currently selected node of the Processes tree. */
    @Nullable
    ProcessTreeNode getSelectedTreeNode();

    /** Returns node by given index */
    ProcessTreeNode getNodeByIndex(@NotNull int index);

    /** Returns node by given ID */
    ProcessTreeNode getNodeById(@NotNull String nodeId);

    /** Add process node */
    void addProcessNode(ProcessTreeNode node);

    /** Remove process node */
    void removeProcessNode(ProcessTreeNode node);

    /**
     * Set process data to be displayed.
     *
     * @param root
     *         data which will be displayed
     */
    void setProcessesData(ProcessTreeNode root);

    /**
     * Sets visibility of 'Stop process' button for node with given ID
     *
     * @param nodeId
     *         id of process node
     */
    void setStopButtonVisibility(String nodeId, boolean visible);

    /** Displays output for process with given ID */
    void showProcessOutput(String processId);

    /** Hides output for process with given ID */
    void hideProcessOutput(String processId);

    /** Marks process with a badge in process tree */
    void markProcessHasOutput(String processId);

    /** Removes all process widgets from the view */
    void clear();

    /** Sets visibility for processes tree */
    void setProcessesTreeVisible(boolean visible);

    /** Determines whether process tree is visible */
    boolean isProcessesTreeVisible();

    interface ActionDelegate extends BaseActionDelegate {

        /**
         * Will be called when user clicks 'Close' button
         *
         * @param node
         *         terminal node to close
         */
        void onCloseTerminal(ProcessTreeNode node);

        void onTerminalTabClosing(ProcessTreeNode node);

        /**
         * Will be called when user clicks 'Add new terminal' button
         *
         * @param machineId
         *         id of machine in which the terminal will be added
         * @param source
         *         source object that called current method
         */
        void onAddTerminal(String machineId, Object source);

        /**
         * Will be called when user clicks 'Preview Ssh' button
         *
         * @param machineId
         *         id of machine in which ssh keys are located
         */
        void onPreviewSsh(String machineId);

        /**
         * Perform actions when tree node is selected.
         *
         * @param node
         *         selected tree node
         */
        void onTreeNodeSelected(@NotNull ProcessTreeNode node);

        /**
         * Will be called when user clicks 'Stop' button
         *
         * @param node
         *         node of process to stop without closing output
         */
        void onStopCommandProcess(ProcessTreeNode node);

        /**
         * Will be called when user clicks 'Close' button
         *
         * @param node
         *         node of process to stop with closing output
         */
        void onCloseCommandOutputClick(ProcessTreeNode node);

        void onCommandTabClosing(ProcessTreeNode node, SubPanel.RemoveCallback removeCallback);

        /**
         * Is called when user has clicked right mouse button.
         *
         * @param mouseX
         *         mouse x coordinate
         * @param mouseY
         *         mouse y coordinate
         * @param node
         *         process tree node
         */
        void onContextMenu(int mouseX, int mouseY, ProcessTreeNode node);

        /**
         * Is called when user has double clicked on console tab to maximize/restore the console.
         */
        void onToggleMaximizeConsole();

    }

}
