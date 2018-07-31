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
package org.eclipse.che.ide.api.action;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Container for the information necessary to execute or update an {@link BaseAction}.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class ActionEvent {
  private final ActionManager actionManager;
  private final Presentation presentation;
  private final Map<String, String> parameters;

  /**
   * Create new action event.
   *
   * @param presentation the presentation which represents the action in the place from where it is
   *     invoked or updated
   * @param actionManager the manager for actions
   */
  public ActionEvent(@NotNull Presentation presentation, @NotNull ActionManager actionManager) {
    this(presentation, actionManager, null);
  }

  /**
   * Create new action event.
   *
   * @param presentation the presentation which represents the action in the place from where it is
   *     invoked or updated
   * @param actionManager the manager for actions
   * @param parameters the parameters with which the action is invoked or updated
   */
  public ActionEvent(
      @NotNull Presentation presentation,
      @NotNull ActionManager actionManager,
      @Nullable Map<String, String> parameters) {
    this.actionManager = actionManager;
    this.presentation = presentation;
    this.parameters = parameters;
  }

  /**
   * Returns the presentation which represents the action in the place from where it is invoked or
   * updated.
   *
   * @return the presentation instance
   */
  public Presentation getPresentation() {
    return presentation;
  }

  /**
   * Returns the parameters with which the action is invoked or updated.
   *
   * @return action's parameters
   */
  @Nullable
  public Map<String, String> getParameters() {
    return parameters;
  }

  public ActionManager getActionManager() {
    return actionManager;
  }
}
