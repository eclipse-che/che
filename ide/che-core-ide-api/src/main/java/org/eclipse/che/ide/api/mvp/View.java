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
package org.eclipse.che.ide.api.mvp;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Common interface for Views
 *
 * @author Andrey Plotnikov
 */
public interface View<T> extends IsWidget {
  /** Sets the delegate to receive events from this view. */
  void setDelegate(T delegate);
}
