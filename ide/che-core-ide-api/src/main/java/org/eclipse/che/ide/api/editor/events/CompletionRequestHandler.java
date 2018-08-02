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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.EventHandler;

/** Handler for {@link CompletionRequestEvent} events. */
public interface CompletionRequestHandler extends EventHandler {

  /**
   * Method called when reacting to the event.
   *
   * @param event the event
   */
  void onCompletionRequest(CompletionRequestEvent event);
}
