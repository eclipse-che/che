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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.command.toolbar.ToolbarResources;
import org.eclipse.che.ide.ui.dropdown.DropDownList;
import org.eclipse.che.ide.ui.dropdown.old.DropDownWidget;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of {@link ProcessesListView} that displays processes in a drop down list.
 */
@Singleton
public class ProcessesListViewImpl implements ProcessesListView {

    private final FlowPanel    rootPanel;
    private final DropDownList dropDownList;

    private ActionDelegate delegate;

    @Inject
    public ProcessesListViewImpl(DropDownWidget.Resources resources, ToolbarResources toolbarResources) {
        final Label label = new Label("EXEC:");
        label.addStyleName(toolbarResources.css().commandListLabel());

        dropDownList = new DropDownList();
        dropDownList.addStyleName(toolbarResources.css().commandList());

        rootPanel = new FlowPanel();
        rootPanel.add(label);
        rootPanel.add(dropDownList);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public void clearList() {
        dropDownList.clear();

        // TODO: set `empty list widget` to the dropdown list's header
    }

    @Override
    public void addProcess(Process process) {
        if (process instanceof StoppedProcess) {
            dropDownList.addItem((StoppedProcess)process, new StoppedProcessRenderer(new StoppedProcessRenderer.ReRunProcessHandler() {
                @Override
                public void onReRunProcess(StoppedProcess process) {
                    delegate.onReRunProcess(process);
                }
            }));
        } else if (process instanceof RunningProcess) {
            dropDownList.addItem((RunningProcess)process, new RunningProcessRenderer(new RunningProcessRenderer.StopProcessHandler() {
                @Override
                public void onStopProcess(RunningProcess process) {
                    delegate.onStopProcess(process);
                }
            }));
        }
    }

    @Override
    public void removeProcess(Process process) {

    }
}
