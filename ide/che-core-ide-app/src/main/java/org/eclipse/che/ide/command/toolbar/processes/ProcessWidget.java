/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.toolbar.processes;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import elemental.dom.Element;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.toolbar.processes.ProcessItemRenderer.RerunProcessHandler;
import org.eclipse.che.ide.command.toolbar.processes.ProcessItemRenderer.StopProcessHandler;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;

/**
 * Widget for representing a {@link Process}. Has different states for representing stopped and
 * running processes.
 */
class ProcessWidget extends FlowPanel {

  private static final CommandResources RESOURCES = GWT.create(CommandResources.class);

  private final Label pidLabel;
  private final Label durationLabel;
  private final ActionButton stopButton;
  private final ActionButton reRunButton;
  private final Timer updateDurationTimer;

  /** Stores true if widget displays stopped process and false for running process. */
  private boolean stopped;

  ProcessWidget(
      BaseListItem<Process> item,
      StopProcessHandler stopProcessHandler,
      RerunProcessHandler rerunProcessHandler) {
    super();

    final Process process = item.getValue();
    stopped = !process.isAlive();

    durationLabel = new Label();
    durationLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
    durationLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetPidLabel());
    durationLabel.ensureDebugId("dropdown-processes-label-duration");

    updateDurationTimer = new UpdateDurationTimer();
    if (!stopped) {
      updateDurationTimer.scheduleRepeating(1000);
    }

    pidLabel = new Label('#' + Integer.toString(process.getPid()));
    pidLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
    pidLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetPidLabel());
    pidLabel.ensureDebugId("dropdown-processes-label-pid");
    Tooltip.create((Element) pidLabel.getElement(), BOTTOM, MIDDLE, "PID");

    add(createMachineNameLabel(process));
    add(createCommandNameLabel(process));
    add(stopButton = createStopButton(process, stopProcessHandler));
    add(reRunButton = createRerunButton(process, rerunProcessHandler));
    add(durationLabel);
    add(pidLabel);

    checkStopped();
  }

  private Label createMachineNameLabel(Process process) {
    final Label label = new InlineHTML(process.getMachineName() + ":&nbsp;");

    label.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
    label.addStyleName(RESOURCES.commandToolbarCss().processWidgetMachineNameLabel());
    label.ensureDebugId("dropdown-processes-machine-name");

    Tooltip.create(
        (Element) label.getElement(), BOTTOM, MIDDLE, process.getCommandLine().split("\\n"));

    return label;
  }

  private Label createCommandNameLabel(Process process) {
    final Label label = new InlineHTML(process.getName());

    label.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
    label.addStyleName(RESOURCES.commandToolbarCss().processWidgetCommandNameLabel());
    label.ensureDebugId("dropdown-processes-command-name");

    Tooltip.create(
        (Element) label.getElement(), BOTTOM, MIDDLE, process.getCommandLine().split("\\n"));

    return label;
  }

  private ActionButton createStopButton(Process process, StopProcessHandler handler) {
    final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.appendHtmlConstant(FontAwesome.STOP);

    final ActionButton button = new ActionButton(safeHtmlBuilder.toSafeHtml());
    button.addClickHandler(
        event -> {
          event.stopPropagation(); // prevent dropdown list from opening/closing
          handler.onStopProcess(process);
        });
    button.ensureDebugId("dropdown-processes-stop");

    Tooltip.create((Element) button.getElement(), BOTTOM, MIDDLE, "Stop");

    return button;
  }

  private ActionButton createRerunButton(Process process, RerunProcessHandler handler) {
    final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.appendHtmlConstant(FontAwesome.REPEAT);

    final ActionButton button = new ActionButton(safeHtmlBuilder.toSafeHtml());
    button.addClickHandler(
        event -> {
          event.stopPropagation(); // prevent dropdown list from opening/closing
          handler.onRerunProcess(process);
        });
    button.ensureDebugId("dropdown-processes-rerun");

    Tooltip.create((Element) button.getElement(), BOTTOM, MIDDLE, "Re-run");

    return button;
  }

  /** Toggle widget's state for displaying running or stopped process. */
  void toggleStopped() {
    stopped = !stopped;
    checkStopped();
    updateDurationTimer.cancel();
  }

  /**
   * Check whether widget displays stopped or running process and changes widget's state if it's
   * required.
   */
  private void checkStopped() {
    pidLabel.setVisible(!stopped);
    reRunButton.setVisible(stopped);
    stopButton.setVisible(!stopped);
  }

  private static class ActionButton extends FocusWidget {
    ActionButton(SafeHtml content) {
      super(Document.get().createDivElement());

      getElement().setInnerSafeHtml(content);
      asWidget().addStyleName(RESOURCES.commandToolbarCss().processWidgetActionButton());
    }
  }

  /** Timer that updates duration label. */
  private class UpdateDurationTimer extends Timer {
    final Duration duration = new Duration();

    @Override
    public void run() {
      durationLabel.setText(getElapsedTime());
    }

    /** Returns the time (mm:ss) that have elapsed since this timer was created. */
    private String getElapsedTime() {
      final int elapsedSec = duration.elapsedMillis() / 1000;
      final int minutesPart = elapsedSec / 60;
      final int secondsPart = elapsedSec - minutesPart * 60;

      return (minutesPart < 10 ? "0" + minutesPart : minutesPart)
          + ":"
          + (secondsPart < 10 ? "0" + secondsPart : secondsPart);
    }

    @Override
    public void cancel() {
      super.cancel();

      durationLabel.setVisible(false);
    }
  }
}
