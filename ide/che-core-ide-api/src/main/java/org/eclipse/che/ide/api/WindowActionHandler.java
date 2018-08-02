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
package org.eclipse.che.ide.api;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handles {@link WindowActionEvent}.
 *
 * @author Artem Zatsarynnyi
 */
public interface WindowActionHandler extends EventHandler {
  /**
   * Fired just before the Codenvy browser's tab closes or navigates to a different site.
   *
   * @param event {@link WindowActionEvent}
   */
  void onWindowClosing(WindowActionEvent event);

  /**
   * Fired after the Codenvy browser's tab closed or navigated to a different site.
   *
   * @param event {@link WindowActionEvent}
   */
  void onWindowClosed(WindowActionEvent event);
}
