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
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;

/**
 * Handler for the {@link ContextInvalidatedEvent}.
 *
 * @author Yevhenii Voevodin
 */
public interface ContextInvalidatedHandler extends EventHandler {

  /**
   * Called when {@code context} is invalidated.
   *
   * @param context invalidated context
   */
  void onContextInvalidated(final Context context);
}
