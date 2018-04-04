/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.monitoring;

import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages and provides widgets for displaying CPU, memory and disk usages for the machines.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class MachineMonitors {

  private Map<String, Map<Object, MonitorWidget>> monitorWidgets = new HashMap<>();

  /**
   * Creates new or returns existed widget for the given machine and referrer. Referrer is an UI
   * object containing monitor widgets attached as children.
   *
   * @param machineID machines ID
   * @param referrer referrer object
   * @return monitor widget
   */
  public MonitorWidget getMonitorWidget(String machineID, Object referrer) {
    Map<Object, MonitorWidget> widgets = monitorWidgets.get(machineID);
    if (widgets == null) {
      widgets = new HashMap<>();
      monitorWidgets.put(machineID, widgets);
    }

    MonitorWidget widget = widgets.get(referrer);
    if (widget == null) {
      widget = new MonitorWidget();
      widgets.put(referrer, widget);
    }

    return widget;
  }

  /**
   * Sets new CPU usage value for machine with given ID.
   *
   * @param machineID machine ID
   * @param cpuUsage new cpu usage
   */
  public void setCpuUsage(String machineID, int cpuUsage) {
    Map<Object, MonitorWidget> widgets = monitorWidgets.get(machineID);
    if (widgets != null) {
      for (MonitorWidget widget : widgets.values()) {
        widget.setCpuUsage(cpuUsage);
      }
    }
  }

  /**
   * Sets new memory usage value for machine with given ID.
   *
   * @param machineID machine ID
   * @param mem new memory usage
   * @param max maximum amount of memory
   */
  public void setMemoryUsage(String machineID, int mem, int max) {
    Map<Object, MonitorWidget> widgets = monitorWidgets.get(machineID);
    if (widgets != null) {
      for (MonitorWidget widget : widgets.values()) {
        widget.setMemoryUsage(mem, max);
      }
    }
  }

  /**
   * Sets new disk usage value for machine with given ID.
   *
   * @param machineID machine ID
   * @param disk new disk usage
   * @param max maximum amount of disk space
   */
  public void setDiskUsage(String machineID, int disk, int max) {
    Map<Object, MonitorWidget> widgets = monitorWidgets.get(machineID);
    if (widgets != null) {
      for (MonitorWidget widget : widgets.values()) {
        widget.setDiskUsage(disk, max);
      }
    }
  }
}
