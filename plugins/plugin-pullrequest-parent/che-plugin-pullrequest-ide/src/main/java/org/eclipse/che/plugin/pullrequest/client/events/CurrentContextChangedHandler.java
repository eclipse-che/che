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
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;

/**
 * Handler for {@link CurrentContextChangedEvent}.
 *
 * @author Yevhenii Voevodin
 */
public interface CurrentContextChangedHandler extends EventHandler {

  /**
   * Called when the current context changed.
   *
   * @param context new context
   */
  void onContextChanged(final Context context);
}
