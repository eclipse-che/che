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
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.console.linkifiers.CSharpCompilationWarnErrOutputLinkifier;
import org.eclipse.che.ide.console.linkifiers.CSharpLineAtOutputLinkifier;
import org.eclipse.che.ide.console.linkifiers.CppCompilationMsgOutputLinkifier;
import org.eclipse.che.ide.console.linkifiers.CppLinkerMsgOutputLinkifier;
import org.eclipse.che.ide.console.linkifiers.JavaOutputLinkifier;
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

  @Inject
  public DefaultOutputConsole(
      OutputConsoleView view,
      MachineResources resources,
      AppContext appContext,
      EditorAgent editorAgent,
      @Assisted String title) {
    this.view = view;
    this.title = title;
    this.resources = resources;

    view.registerLinkifier(new JavaOutputLinkifier(appContext, editorAgent));
    view.registerLinkifier(new CppCompilationMsgOutputLinkifier(appContext, editorAgent));
    view.registerLinkifier(new CppLinkerMsgOutputLinkifier(appContext, editorAgent));
    view.registerLinkifier(new CSharpCompilationWarnErrOutputLinkifier(appContext, editorAgent));
    view.registerLinkifier(new CSharpLineAtOutputLinkifier(appContext, editorAgent));

    view.setDelegate(this);

    view.hideCommand();
    view.hidePreview();
    view.setReRunButtonVisible(false);
    view.setStopButtonVisible(false);
  }

  /**
   * Print text in the console.
   *
   * @param text text to be printed
   */
  public void printText(String text) {
    view.print(text);

    for (ActionDelegate actionDelegate : actionDelegates) {
      actionDelegate.onConsoleOutput(this);
    }
  }

  /**
   * Print colored text in the console in the true color format.
   *
   * @param text text to print;
   * @param background background color component;
   * @param red red color component;
   * @param blue color component;
   * @param green color component;
   */
  public void printText(String text, int background, int red, int blue, int green) {
    view.print(text, background, red, blue, green);

    for (ActionDelegate actionDelegate : actionDelegates) {
      actionDelegate.onConsoleOutput(this);
    }
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
    for (ActionDelegate actionDelegate : actionDelegates) {
      actionDelegate.onDownloadOutput(this);
    }
  }
}
