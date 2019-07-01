/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package io.tonlabs.ide.action;

import static io.tonlabs.shared.Constants.TON_C_PROJECT_TYPE_ID;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.base.Optional;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Project specific action. */
public abstract class TonProjectAction extends AbstractPerspectiveAction {

  private AppContext appContext;

  /**
   * Constructor.
   *
   * @param appContext the IDE application context
   * @param text the text of the action
   * @param description the description of the action
   * @param svgResource the icon of the resource
   */
  public TonProjectAction(
      AppContext appContext,
      @NotNull String text,
      @NotNull String description,
      @Nullable SVGResource svgResource) {
    super(Collections.singletonList(PROJECT_PERSPECTIVE_ID), text, description, svgResource);
    this.appContext = appContext;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {

    final Resource[] resources = this.appContext.getResources();

    if (resources == null || resources.length != 1) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Optional<Project> project = resources[0].getRelatedProject();

    event
        .getPresentation()
        .setEnabledAndVisible(project.isPresent() && project.get().isTypeOf(TON_C_PROJECT_TYPE_ID));
  }
}
