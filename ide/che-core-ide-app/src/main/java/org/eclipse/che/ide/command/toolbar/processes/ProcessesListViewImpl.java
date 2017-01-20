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
import elemental.dom.Element;
import org.eclipse.che.ide.command.toolbar.ToolbarResources;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class ProcessesListViewImpl implements ProcessesListView {

    private final FlowPanel rootPanel;
    private final Label label;
    private final DropDownWidget<RunningProcess> dropDownWidget;
    private ActionDelegate delegate;

    @Inject
    public ProcessesListViewImpl(DropDownWidget.Resources resources, ToolbarResources toolbarResources) {
        rootPanel = new FlowPanel();
        label = new Label("EXEC:");
        label.addStyleName(toolbarResources.css().commandListLabel());
        dropDownWidget = new DropDownWidget<>(resources, new DropDownWidget.ItemRenderer<RunningProcess>() {
            @Override
            public Element render(RunningProcess item) {
                return null;
            }
        }, new DropDownWidget.ItemSelectionHandler<RunningProcess>() {
            @Override
            public void onItemSelected(RunningProcess item) {

            }
        });
        dropDownWidget.addStyleName(toolbarResources.css().commandList());
        rootPanel.add(label);
        rootPanel.add(dropDownWidget);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }
}
