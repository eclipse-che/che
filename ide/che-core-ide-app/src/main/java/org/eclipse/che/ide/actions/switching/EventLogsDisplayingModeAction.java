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
package org.eclipse.che.ide.actions.switching;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;

/**
 * Switches Event Logs display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Event Logs part is invisible -> make it visible and active
 *   <li>Event Logs part is active -> make it invisible
 *   <li>Event Logs part is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EventLogsDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;
  private Provider<NotificationManager> notificationManagerProvider;

  @Inject
  public EventLogsDisplayingModeAction(
      Resources resources,
      EditorAgent editorAgent,
      CoreLocalizationConstant localizedConstant,
      WorkspaceAgent workspaceAgent,
      Provider<NotificationManager> notificationManagerProvider) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchEventLogsDisplayingTitle(),
        localizedConstant.switchEventLogsDisplayingDescription(),
        resources.eventsPartIcon());
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
    this.notificationManagerProvider = notificationManagerProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    NotificationManager notificationManager = notificationManagerProvider.get();
    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart != null && activePart instanceof NotificationManager) {
      workspaceAgent.hidePart(notificationManager);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    workspaceAgent.openPart(notificationManager, INFORMATION);
    workspaceAgent.setActivePart(notificationManager);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
