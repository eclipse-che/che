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
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.EventHandler;
import javax.validation.constraints.NotNull;

/**
 * Handler for step event.
 *
 * @author Kevin Pollet
 */
public interface StepHandler extends EventHandler {
  /**
   * Called when a step is successfully done.
   *
   * @param event the step event.
   */
  void onStepDone(@NotNull StepEvent event);

  /**
   * Called when a step is in error.
   *
   * @param event the step event.
   */
  void onStepError(@NotNull StepEvent event);
}
