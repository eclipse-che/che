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
