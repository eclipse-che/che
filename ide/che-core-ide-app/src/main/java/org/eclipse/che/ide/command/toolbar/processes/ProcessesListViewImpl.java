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

import elemental.dom.Element;
import elemental.html.DivElement;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.ide.command.toolbar.ToolbarResources;
import org.eclipse.che.ide.command.toolbar.ddw.DropDownListBox;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.eclipse.che.ide.util.dom.Elements;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Singleton
public class ProcessesListViewImpl implements ProcessesListView {

    private final FlowPanel                                rootPanel;
    private final Label                                    label;
    private final DropDownWidget<GetProcessesResponseDto>  dropDownWidget;
    private final DropDownListBox<GetProcessesResponseDto> dropDownListBox;

    private ActionDelegate delegate;
    private ArrayList<GetProcessesResponseDto> processesList;

    @Inject
    public ProcessesListViewImpl(DropDownWidget.Resources resources, ToolbarResources toolbarResources) {
        rootPanel = new FlowPanel();
        label = new Label("EXEC:");
        label.addStyleName(toolbarResources.css().commandListLabel());

        dropDownWidget = new DropDownWidget<>(resources, new DropDownWidget.ItemRenderer<GetProcessesResponseDto>() {
            @Override
            public Element render(GetProcessesResponseDto item) {
                final DivElement divElement = Elements.createDivElement();
                divElement.setInnerText(item.getName());
                return divElement;
            }
        }, new DropDownWidget.ItemSelectionHandler<GetProcessesResponseDto>() {
            @Override
            public void onItemSelected(GetProcessesResponseDto item) {

            }
        });

        dropDownListBox = new DropDownListBox<>(null);
        dropDownListBox.addStyleName(toolbarResources.css().commandList());

        dropDownWidget.addStyleName(toolbarResources.css().commandList());

        rootPanel.add(label);
        rootPanel.add(dropDownListBox);

        processesList = new ArrayList<>();
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
    public void setProcesses(List<GetProcessesResponseDto> processes) {
        processesList.addAll(processes);

        dropDownWidget.setData(processesList);
    }
}
