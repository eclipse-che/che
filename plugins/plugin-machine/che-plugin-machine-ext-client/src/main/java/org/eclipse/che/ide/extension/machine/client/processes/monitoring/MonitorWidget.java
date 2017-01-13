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
package org.eclipse.che.ide.extension.machine.client.processes.monitoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;

/**
 * Resource monitor widget. Displays information about cpu, memory and disk usage in consoles tree.
 * The widget also creates a tooltip with additional information.
 *
 * @author Vitaliy Guliy
 */
public class MonitorWidget extends Composite {

    interface MonitorWidgetUiBinder extends UiBinder<Widget, MonitorWidget> {
    }

    private final static MonitorWidgetUiBinder UI_BINDER = GWT.create(MonitorWidgetUiBinder.class);

    @UiField
    FlowPanel cpuBar, memBar, diskBar;

    @UiField
    HTMLPanel tooltip;

    @UiField
    DivElement tooltipCpuBar, tooltipCpuValue;

    @UiField
    DivElement tooltipMemBar, tooltipMemValue, tooltipMemDescription;

    @UiField
    DivElement tooltipDiskBar, tooltipDiskValue, tooltipDiskDescription;

    public MonitorWidget() {
        initWidget(UI_BINDER.createAndBindUi(this));

        tooltip.removeFromParent();

        Tooltip.create((Element)getElement(), PositionController.VerticalAlign.BOTTOM, PositionController.HorizontalAlign.RIGHT, (Element)tooltip.getElement());
    }

    /**
     * Sets CPU usage value.
     *
     * @param cpuUsage
     *          cpuUsage usage
     */
    public void setCpuUsage(int cpuUsage) {
        setVisible(true);

        cpuBar.getElement().getFirstChildElement().getStyle().setProperty("height", cpuUsage + "%");

        tooltipCpuBar.getStyle().setProperty("height", cpuUsage + "%");
        tooltipCpuValue.setInnerText(cpuUsage + "%");
    }

    /**
     * Sets memory usage value.
     *
     * @param mem
     *          memory usage
     * @param max
     *          max memory available
     */
    public void setMemoryUsage(int mem, int max) {
        setVisible(true);

        int percentage = (int)((double)mem / ((double)max / 100) );

        memBar.getElement().getFirstChildElement().getStyle().setProperty("height", percentage + "%");

        tooltipMemBar.getStyle().setProperty("height", percentage + "%");
        tooltipMemValue.setInnerText(percentage + "%");
        tooltipMemDescription.setInnerText("- " + mem + " MB / " + max + " MB");
    }

    /**
     * Sets disk usage value.
     *
     * @param disk
     *          disk usage
     * @param max
     *          max disk available
     */
    public void setDiskUsage(int disk, int max) {
        setVisible(true);

        int percentage = (int)((double)disk / ((double)max / 100) );

        diskBar.getElement().getFirstChildElement().getStyle().setProperty("height", percentage + "%");

        tooltipDiskBar.getStyle().setProperty("height", percentage + "%");
        tooltipDiskValue.setInnerText(percentage + "%");

        disk = disk / 1024;
        max = max / 1024;

        tooltipDiskDescription.setInnerText("- " + disk + " MB / " + max + " MB");
    }

}
