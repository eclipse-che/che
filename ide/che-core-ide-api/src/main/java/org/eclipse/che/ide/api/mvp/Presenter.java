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
package org.eclipse.che.ide.api.mvp;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Common interface for Presenters that are responsible for driving the UI
 *
 * <p>
 *
 * @author Nikolay Zamosenchuk Jul 24, 2012
 */
public interface Presenter {
  /**
   * Allows presenter to expose it's view to the container.
   *
   * @param container
   */
  void go(final AcceptsOneWidget container);
}
