/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.terminal.TerminalGeometryJso;
import org.eclipse.che.ide.terminal.TerminalInitializePromiseHolder;
import org.eclipse.che.ide.terminal.TerminalJso;
import org.eclipse.che.ide.terminal.TerminalOptionsJso;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.requirejs.ModuleHolder;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * View representation of output console.
 *
 * @author Artem Zatsarynnyi
 * @author Vitaliy Guliy
 */
public class OutputConsoleViewImpl extends Composite implements OutputConsoleView, RequiresResize {

  private TerminalJso terminalJso;

  interface OutputConsoleViewUiBinder extends UiBinder<Widget, OutputConsoleViewImpl> {}

  private static final OutputConsoleViewUiBinder UI_BINDER =
          GWT.create(OutputConsoleViewUiBinder.class);

  private static final String DEFAULT_TEXT_COLOR = "\\x1b[0m";

  private ActionDelegate delegate;

  private final TerminalInitializePromiseHolder promiseHolder;
  private final ModuleHolder moduleHolder;
  private static final int SCROLL_BACK = 5000;

  @UiField protected DockLayoutPanel consolePanel;

  @UiField protected FlowPanel commandPanel;

  @UiField protected FlowPanel previewPanel;

  @UiField Label commandTitle;

  @UiField Label commandLabel;

  @UiField FlowPanel consoleLines;

  @UiField Anchor previewUrlLabel;

  @UiField protected FlowPanel reRunProcessButton;

  @UiField protected FlowPanel stopProcessButton;

  @UiField protected FlowPanel clearOutputsButton;

  @UiField protected FlowPanel downloadOutputsButton;

  @Inject
  public OutputConsoleViewImpl(
      final ModuleHolder moduleHolder,
      MachineResources resources,
      CoreLocalizationConstant localization,
      TerminalInitializePromiseHolder promiseHolder) {
    this.promiseHolder = promiseHolder;
    this.moduleHolder = moduleHolder;

    initWidget(UI_BINDER.createAndBindUi(this));

    reRunProcessButton.add(new SVGImage(resources.reRunIcon()));
    stopProcessButton.add(new SVGImage(resources.stopIcon()));
    clearOutputsButton.add(new SVGImage(resources.clearOutputsIcon()));
    downloadOutputsButton.getElement().setInnerHTML(FontAwesome.DOWNLOAD);

    reRunProcessButton.addDomHandler(
        event -> {
          if (!reRunProcessButton.getElement().hasAttribute("disabled") && delegate != null) {
            delegate.reRunProcessButtonClicked();
          }
        },
        ClickEvent.getType());

    stopProcessButton.addDomHandler(
        event -> {
          if (!stopProcessButton.getElement().hasAttribute("disabled") && delegate != null) {
            delegate.stopProcessButtonClicked();
          }
        },
        ClickEvent.getType());

    clearOutputsButton.addDomHandler(
        event -> {
          if (!clearOutputsButton.getElement().hasAttribute("disabled") && delegate != null) {
            delegate.clearOutputsButtonClicked();
          }
        },
        ClickEvent.getType());

    downloadOutputsButton.addDomHandler(
        event -> {
          if (delegate != null) {
            delegate.downloadOutputsButtonClicked();
          }
        },
        ClickEvent.getType());

    Tooltip.create(
        (elemental.dom.Element) reRunProcessButton.getElement(),
        BOTTOM,
        MIDDLE,
        localization.consolesReRunButtonTooltip());

    Tooltip.create(
        (elemental.dom.Element) stopProcessButton.getElement(),
        BOTTOM,
        MIDDLE,
        localization.consolesStopButtonTooltip());

    Tooltip.create(
        (elemental.dom.Element) clearOutputsButton.getElement(),
        BOTTOM,
        MIDDLE,
        localization.consolesClearOutputsButtonTooltip());

    promiseHolder
        .getInitializerPromise()
        .then(
            arg -> {
              JavaScriptObject terminalSource = moduleHolder.getModule("Xterm");
              TerminalOptionsJso termOps =
                  TerminalOptionsJso.createDefault()
                      .withFocusOnOpen(false)
                      .withScrollBack(SCROLL_BACK);

              this.terminalJso = TerminalJso.create(terminalSource, termOps);
              terminalJso.open(consoleLines.asWidget().getElement());
              TerminalGeometryJso geometryJso = terminalJso.proposeGeometry();
              terminalJso.resize(geometryJso.getCols(), geometryJso.getRows());
            });
  }

  private Timer resizeTimer =
      new Timer() {
        @Override
        public void run() {
          resize();
        }
      };

  @Override
  public void onResize() {
    resizeTimer.schedule(200);
  }

  private int rows = 24;
  private int cols;

  private void resize() {
    TerminalGeometryJso geometryJso = terminalJso.proposeGeometry();
    rows = geometryJso.getRows();

    if (rows > 0 && cols > 0) {
      terminalJso.resize(cols, rows);
    }
  }

  @Override
  public void print(String text) {
    if (text.length() > cols) {
      cols = text.length();
      terminalJso.resize(cols, rows);
    }
    terminalJso.writeln(text);
  }

  /**
   * Note: current widget doesn't support yet all true color palette.
   * It renders colors by approximation algorithm.
   *
   * @param text text to print;
   * @param background background color component;
   * @param red red color component;
   * @param blue color component;
   * @param green color component;
   */
  @Override
  public void print(String text, int background, int red, int blue, int green) {
    String color = "\\x1b[" + background + ";2;" + red + ";" + blue + ";" + green + "m";
    text = color + text + DEFAULT_TEXT_COLOR;
    print(text);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void hideCommand() {
    consolePanel.setWidgetHidden(commandPanel, true);
  }

  @Override
  public void hidePreview() {
    consolePanel.setWidgetHidden(previewPanel, true);
  }

  @Override
  public void clearConsole() {
    consoleLines.getElement().setInnerHTML("");
  }

  @Override
  public void setReRunButtonVisible(boolean visible) {
    reRunProcessButton.setVisible(visible);
  }

  @Override
  public void setStopButtonVisible(boolean visible) {
    stopProcessButton.setVisible(visible);
  }

  @Override
  public void enableStopButton(boolean enable) {
    if (enable) {
      stopProcessButton.getElement().removeAttribute("disabled");
    } else {
      stopProcessButton.getElement().setAttribute("disabled", "");
    }
  }

  @Override
  public void showCommandLine(String commandLine) {
    commandLabel.setText(commandLine);
    Tooltip.create((elemental.dom.Element) commandLabel.getElement(), BOTTOM, MIDDLE, commandLine);
  }

  @Override
  public void showPreviewUrl(String previewUrl) {
    if (Strings.isNullOrEmpty(previewUrl)) {
      hidePreview();
    } else {
      previewUrlLabel.setText(previewUrl);
      previewUrlLabel.setHref(previewUrl);
      Tooltip.create(
          (elemental.dom.Element) previewUrlLabel.getElement(), BOTTOM, MIDDLE, previewUrl);
    }
  }

  @Override
  public String getText() {
    // todo complete this method. This method used for download output
    return "";
  }
}
