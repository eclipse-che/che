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
package org.eclipse.che.ide.factory.welcome;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;

/**
 * @author Vitaliy Guliy
 * @author Sergii Leschenko
 */
@Singleton
public class GreetingPartPresenter extends BasePresenter
    implements GreetingPartView.ActionDelegate {
  private static final String DEFAULT_TITLE = "Greeting";

  private final WorkspaceAgent workspaceAgent;
  private final GreetingPartView view;

  private String title = DEFAULT_TITLE;

  @Inject
  public GreetingPartPresenter(GreetingPartView view, WorkspaceAgent workspaceAgent) {
    this.view = view;
    this.workspaceAgent = workspaceAgent;

    view.setDelegate(this);
  }

  @NotNull
  @Override
  public String getTitle() {
    return title != null ? title : DEFAULT_TITLE;
  }

  @Nullable
  @Override
  public String getTitleToolTip() {
    return "Greeting the user";
  }

  @Override
  public int getSize() {
    return 320;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  public void showGreeting(Map<String, String> parameters) {
    showGreeting(
        parameters.get("greetingTitle"),
        parameters.get("greetingContentUrl"),
        parameters.get("greetingNotification"));
  }

  private void hideGreeting() {
    workspaceAgent.removePart(this);
  }

  /** Opens Greeting part and displays the URL in Frame. */
  private void showGreeting(
      @NotNull String title, String greetingContentURL, final String notification) {
    this.title = title;
    workspaceAgent.openPart(this, PartStackType.TOOLING, Constraints.FIRST);
    new Timer() {
      @Override
      public void run() {
        workspaceAgent.setActivePart(GreetingPartPresenter.this);
      }
    }.schedule(3000);

    view.setTitle(title);
    view.showGreeting(greetingContentURL);

    if (notification != null) {
      new Timer() {
        @Override
        public void run() {
          new TooltipHint(notification);
        }
      }.schedule(1000);
    }
  }

  @Override
  public View getView() {
    return view;
  }
}
