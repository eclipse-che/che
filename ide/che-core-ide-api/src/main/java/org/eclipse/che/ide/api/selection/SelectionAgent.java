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
package org.eclipse.che.ide.api.selection;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.extension.SDK;

/**
 * Selection API allows to provide a way of data-based communication, when Parts provide a static
 * access to the data selected in active Part. In order to listen to dynamic Selection changes,
 * please subscribe to {@link SelectionChangedEvent} on {@link EventBus}.
 *
 * @author Nikolay Zamosenchuk
 */
@SDK(title = "ide.api.ui.selection")
public interface SelectionAgent {

  /**
   * Provides a way of getting current app-wide Selection.
   *
   * @return
   */
  Selection<?> getSelection();
}
