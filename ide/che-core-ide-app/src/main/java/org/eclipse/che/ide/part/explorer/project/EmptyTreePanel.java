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
package org.eclipse.che.ide.part.explorer.project;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.CreateProjectAction;
import org.eclipse.che.ide.actions.ImportProjectAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.newresource.NewFileAction;
import org.eclipse.che.ide.part.editor.EmptyEditorsPanel;

/** Represent empty state of project explorer */
public class EmptyTreePanel extends EmptyEditorsPanel {

  @Inject
  public EmptyTreePanel(
      ActionManager actionManager,
      Provider<PerspectiveManager> perspectiveManagerProvider,
      KeyBindingAgent keyBindingAgent,
      AppContext appContext,
      EventBus eventBus,
      CoreLocalizationConstant localizationConstant,
      NewFileAction newFileAction,
      CreateProjectAction createProjectAction,
      ImportProjectAction importProjectAction) {
    super(
        actionManager,
        perspectiveManagerProvider,
        keyBindingAgent,
        appContext,
        localizationConstant,
        newFileAction,
        createProjectAction,
        importProjectAction);
    eventBus.addHandler(ResourceChangedEvent.getType(), this);

    root.getStyle().setTop(46, Style.Unit.PX);

    // Sometimes initialization of Create/Import Project actions are completed after the Empty
    // editor page is rendered.
    // In this case we need to wait when actions will be initialized.
    new Timer() {
      @Override
      public void run() {
        renderNoProjects();
      }
    }.schedule(500);
  }

  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                if (appContext.getProjects().length != 0) {
                  getElement().removeFromParent();
                }
              }
            });
  }
}
