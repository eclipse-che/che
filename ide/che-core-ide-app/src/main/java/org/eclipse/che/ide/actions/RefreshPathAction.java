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
package org.eclipse.che.ide.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Refresh current selected container.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class RefreshPathAction extends AbstractPerspectiveAction
    implements ActivePartChangedHandler {

  private final AppContext appContext;

  private PartPresenter activePart;

  @Inject
  public RefreshPathAction(
      AppContext appContext, CoreLocalizationConstant localizationConstant, EventBus eventBus) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizationConstant.refreshActionTitle(),
        localizationConstant.refreshActionDescription());
    this.appContext = appContext;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    if (!(activePart instanceof ProjectExplorerPresenter)) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    event.getPresentation().setText("Refresh");
    event.getPresentation().setVisible(true);

    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length != 1) {
      event.getPresentation().setEnabled(false);
      return;
    }

    final Resource resource = resources[0];

    if (resource instanceof Container) {
      event.getPresentation().setText("Refresh '" + resource.getName() + "'");
    } else {
      final Container parent = resource.getParent();

      if (parent != null) {
        event.getPresentation().setText("Refresh '" + parent.getName() + "'");
      } else {
        event.getPresentation().setEnabled(false);
        return;
      }
    }

    event.getPresentation().setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length != 1) {
      return;
    }

    final Resource resource = resources[0];

    if (resource instanceof Container) {
      ((Container) resource).synchronize();
    } else {
      final Container parent = resource.getParent();

      if (parent != null) {
        parent.synchronize();
      }
    }
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    this.activePart = event.getActivePart();
  }
}
