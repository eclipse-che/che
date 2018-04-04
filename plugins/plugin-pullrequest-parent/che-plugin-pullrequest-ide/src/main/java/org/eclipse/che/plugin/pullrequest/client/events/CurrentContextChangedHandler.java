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
