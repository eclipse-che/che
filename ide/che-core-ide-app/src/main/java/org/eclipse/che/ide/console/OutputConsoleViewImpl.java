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
package org.eclipse.che.ide.console;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
import org.eclipse.che.ide.console.colorizer.OutputConsoleColorizer;
import org.eclipse.che.ide.console.linkifiers.OutputLinkifier;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.terminal.Terminal;
import org.eclipse.che.ide.terminal.TerminalOptions;
import org.eclipse.che.ide.terminal.helpers.TerminalGeometry;
import org.eclipse.che.ide.terminal.linkifier.LinkMatcherOptions;
import org.eclipse.che.ide.ui.Tooltip;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * View representation of output console.
 *
 * @author Artem Zatsarynnyi
 * @author Vitaliy Guliy
 */
public class OutputConsoleViewImpl extends Composite implements OutputConsoleView, RequiresResize {

  private final Terminal xtermWidget;
  private final OutputConsoleColorizer consoleColorizer;

  interface OutputConsoleViewUiBinder extends UiBinder<Widget, OutputConsoleViewImpl> {}

  private static final OutputConsoleViewUiBinder UI_BINDER =
      GWT.create(OutputConsoleViewUiBinder.class);

  private static final String RESET_TEXT_COLOR = "\u001B[0m";

  private ActionDelegate delegate;

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
      MachineResources resources,
      CoreLocalizationConstant localization,
      OutputConsoleColorizer consoleColorizer) {
    this.consoleColorizer = consoleColorizer;

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

    TerminalOptions termOptions = new TerminalOptions();
    termOptions.setFocusOnOpen(false);
    termOptions.setReadOnly(true);
    termOptions.setDisableStdin(true);
    termOptions.setConvertEol(true);
    termOptions.setScrollBack(SCROLL_BACK);

    this.xtermWidget = new Terminal(termOptions);
    xtermWidget.attachCustomKeyDownHandler(new OutputConsoleCustomKeyDownHandler(xtermWidget));
    xtermWidget.open(consoleLines.getElement());
  }

  @Override
  public void onResize() {
    if (consoleIsFit()) {
      resize();
    }
  }

  private boolean consoleIsFit() {
    return consoleLines.getElement().getClientHeight() > 0
        && consoleLines.getElement().getClientWidth() > 0;
  }

  private void resize() {
    TerminalGeometry geometry = xtermWidget.proposeGeometry();
    int visibleCols = geometry.getCols();
    int visibleRows = geometry.getRows();
    //    int visibleCols = evaluateVisibleCols();
    //    int visibleRows = evaluateVisibleRows();

    if (visibleRows > 0 && visibleCols > 0) {
      int cols = Math.max(xtermWidget.getMaxLineLength(), visibleCols);
      xtermWidget.resize(cols, visibleRows);
    }
  }

  private int evaluateVisibleRows() {
    return Math.round(
        (consoleLines.getElement().getClientHeight()
                - xtermWidget.getScrollBarMeasure().getVerticalWidth())
            / xtermWidget.getCharMeasure().getHeight());
  }

  private int evaluateVisibleCols() {
    return Math.round(
        (consoleLines.getElement().getClientWidth()
                - xtermWidget.getScrollBarMeasure().getHorizontalWidth())
            / xtermWidget.getCharMeasure().getWidth());
  }

  @Override
  public void print(String text) {
    if (consoleColorizer != null) {
      text = consoleColorizer.colorize(text);
    }
    if (text.endsWith("\n|\r\n")) {
      xtermWidget.write(text);
    } else {
      xtermWidget.writeln(text);
    }
  }

  /**
   * Note: current widget doesn't support yet all true color palette. It renders colors by
   * approximation algorithm.
   *
   * @param text text to print;
   * @param background background color component;
   * @param red red color component;
   * @param blue color component;
   * @param green color component;
   */
  @Override
  public void print(
      String text, int background, int red, int blue, int green) { // Todo simplify this stuff
    String color = "\u001B[" + background + ";2;" + red + ";" + blue + ";" + green + "m";
    text = color + text + RESET_TEXT_COLOR;
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
    xtermWidget.reset();
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
  public void registerLinkifier(OutputLinkifier linkifier) {
    LinkMatcherOptions linkOptions = new LinkMatcherOptions();
    linkOptions.setMatchIndex(linkifier.getMatchIndex());
    linkOptions.setPriority(0);

    xtermWidget.registerLinkMatcher(
        linkifier.getRegExpr(),
        (event, link, lineContent) -> {
          linkifier.onClickLink(lineContent);
        },
        linkOptions);
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
    return xtermWidget.getText();
  }
}
