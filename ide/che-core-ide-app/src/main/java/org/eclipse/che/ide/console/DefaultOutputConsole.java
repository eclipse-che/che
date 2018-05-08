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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.console.OutputConsoleRenderer;
import org.eclipse.che.ide.api.console.OutputConsoleRendererRegistry;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.machine.MachineResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Console panel for some text outputs.
 *
 * @author Valeriy Svydenko
 */
public class DefaultOutputConsole implements OutputConsole, OutputConsoleView.ActionDelegate {

  private final OutputConsoleView view;
  private final MachineResources resources;
  private String title;

  private final List<ActionDelegate> actionDelegates = new ArrayList<>();

  private boolean wrapText;

  /** Follow output when printing text */
  private boolean followOutput = true;

  private OutputConsoleRenderer renderer = null;

  @Inject
  public DefaultOutputConsole(
      OutputConsoleView view,
      MachineResources resources,
      OutputConsoleRendererRegistry rendererRegistry,
      AppContext appContext,
      @Assisted String title) {
    this.view = view;
    this.title = title;
    this.resources = resources;
    this.view.enableAutoScroll(true);

    initOutputConsoleRenderer(appContext, rendererRegistry);

    view.setDelegate(this);

    view.hideCommand();
    view.hidePreview();
    view.setReRunButtonVisible(false);
    view.setStopButtonVisible(false);
  }

  /** Enables auto scroll when output. */
  public void enableAutoScroll(boolean enable) {
    view.enableAutoScroll(enable);
  }

  /**
   * Print text in the console.
   *
   * @param text text to be printed
   */
  public void printText(String text) {
    view.print(text, text.endsWith("\r"));

    actionDelegates.forEach(
        d -> {
          d.onConsoleOutput(this);
        });
  }

  /**
   * Print colored text in the console.
   *
   * @param text text to be printed
   * @param color color of the text or NULL
   */
  public void printText(String text, String color) {
    view.print(text, text.endsWith("\r"), color);

    actionDelegates.forEach(
        d -> {
          d.onConsoleOutput(this);
        });
  }

  /**
   * Returns the console text.
   *
   * @return console text
   */
  public String getText() {
    return view.getText();
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  /** {@inheritDoc} */
  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public SVGResource getTitleIcon() {
    return resources.output();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isFinished() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void stop() {}

  @Override
  public void close() {
    actionDelegates.clear();
  }

  @Override
  public void addActionDelegate(ActionDelegate actionDelegate) {
    actionDelegates.add(actionDelegate);
  }

  @Override
  public void reRunProcessButtonClicked() {}

  @Override
  public void stopProcessButtonClicked() {}

  @Override
  public void clearOutputsButtonClicked() {
    view.clearConsole();
  }

  @Override
  public void downloadOutputsButtonClicked() {
    actionDelegates.forEach(
        d -> {
          d.onDownloadOutput(this);
        });
  }

  @Override
  public void wrapTextButtonClicked() {
    wrapText = !wrapText;
    view.wrapText(wrapText);
    view.toggleWrapTextButton(wrapText);
  }

  @Override
  public void scrollToBottomButtonClicked() {
    followOutput = !followOutput;

    view.toggleScrollToEndButton(followOutput);
    view.enableAutoScroll(followOutput);
  }

  @Override
  public void onOutputScrolled(boolean bottomReached) {
    followOutput = bottomReached;
    view.toggleScrollToEndButton(bottomReached);
  }

  @Override
  public OutputConsoleRenderer getRenderer() {
    return renderer;
  }

  /** Sets up the text output renderer */
  public void setRenderer(OutputConsoleRenderer renderer) {
    this.renderer = renderer;
  }

  /*
   * Initializes the renderer for the console output
   */
  private void initOutputConsoleRenderer(
      AppContext appContext, OutputConsoleRendererRegistry rendererRegistry) {
    // No renderers.
  }
}
