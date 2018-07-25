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
package org.eclipse.che.ide.ext.git.client.outputconsole;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Git output View Part.
 *
 * @author Vitaly Parfonov
 */
public class GitOutputConsolePresenter
    implements GitOutputPartView.ActionDelegate, GitOutputConsole {

  private final GitOutputPartView view;
  private final GitResources resources;
  private final String title;

  private final List<ActionDelegate> actionDelegates = new ArrayList<>();

  /** Construct empty Part */
  @Inject
  public GitOutputConsolePresenter(
      GitOutputPartView view,
      GitResources resources,
      AppContext appContext,
      GitLocalizationConstant locale,
      @Assisted String title) {
    this.view = view;
    this.view.setDelegate(this);

    this.title = title;
    this.resources = resources;

    final Project project = appContext.getRootProject();

    if (project != null) {
      view.print(locale.consoleProjectName(project.getName()) + "\n");
    }
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  /**
   * Print text on console.
   *
   * @param text text that need to be shown
   */
  public void print(String text) {
    String[] lines = text.split("\n");
    for (String line : lines) {
      view.print(line.isEmpty() ? " " : line);
    }
    view.scrollBottom();

    for (ActionDelegate actionDelegate : actionDelegates) {
      actionDelegate.onConsoleOutput(this);
    }
  }

  @Override
  public void print(String text, String color) {
    view.print(text, color);
    view.scrollBottom();

    for (ActionDelegate actionDelegate : actionDelegates) {
      actionDelegate.onConsoleOutput(this);
    }
  }

  @Override
  public void printError(String text) {
    print(text, Style.getVcsConsoleErrorColor());
  }

  /** {@inheritDoc} */
  public void clear() {
    view.clear();
  }

  /** {@inheritDoc} */
  @Override
  public void onClearClicked() {
    clear();
  }

  @Override
  public void onScrollClicked() {
    view.scrollBottom();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public SVGResource getTitleIcon() {
    return resources.gitOutput();
  }

  @Override
  public boolean isFinished() {
    return true;
  }

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
}
