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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.ide.command.toolbar.ToolbarResources;
import org.eclipse.che.ide.ui.dropdown.DropDownList;
import org.eclipse.che.ide.ui.dropdown.ItemRenderer;
import org.eclipse.che.ide.ui.dropdown.ListItem;
import org.eclipse.che.ide.ui.dropdown.old.DropDownWidget;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.gwt.dom.client.Style.Float.LEFT;
import static com.google.gwt.dom.client.Style.Float.RIGHT;

/**
 *
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
    public void clearProcesses() {
        dropDownList.clear();
    }

    @Override
    public void addProcess(GetProcessesResponseDto process, Machine machine) {
        dropDownList.addItem(new ProcessListItem(process, machine), getRenderer(process, machine));
    }

    private ItemRenderer getRenderer(final GetProcessesResponseDto process, final Machine machine) {
        return new ItemRenderer() {
            @Override
            public Widget render(ListItem item) {
                final String labelText = machine.getConfig().getName() + ": <b>" + process.getName() + "</b>";
                final Label nameLabel = new InlineHTML(labelText);
                nameLabel.getElement().getStyle().setFloat(LEFT);
                nameLabel.setTitle(process.getCommandLine());

                final Label pidLabel = new Label('#' + Integer.toString(process.getNativePid()));
                pidLabel.getElement().getStyle().setFloat(RIGHT);

                final Button runButton = new Button(process.isAlive() ? "stop" : "re-run");
                runButton.getElement().getStyle().setFloat(RIGHT);
                runButton.addDomHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        delegate.onRunProcess(process, machine);
                    }
                }, ClickEvent.getType());

                final FlowPanel panel = new FlowPanel();
                panel.setHeight("25px");
                panel.add(nameLabel);
                panel.add(runButton);
                panel.add(pidLabel);

                return panel;
            }
        };
    }
}
