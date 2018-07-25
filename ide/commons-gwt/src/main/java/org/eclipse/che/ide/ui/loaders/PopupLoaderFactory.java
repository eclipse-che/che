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
package org.eclipse.che.ide.ui.loaders;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;

/**
 * Factory to create instances of PopupLoaderImpl.
 *
 * @author Vitaliy Guliy
 */
public interface PopupLoaderFactory {

  /**
   * Creates an instance of PopupLoaderImpl
   *
   * @param title loader title
   * @param description description
   * @return instance of PopupLoaderImpl
   */
  PopupLoaderImpl getPopup(
      @NotNull @Assisted("title") String title,
      @NotNull @Assisted("description") String description);

  /**
   * Creates an instance of PopupLoaderImpl
   *
   * @param title loader title
   * @param description description
   * @return instance of PopupLoaderImpl
   */
  PopupLoaderImpl getPopup(
      @NotNull @Assisted("title") String title,
      @NotNull @Assisted("description") String description,
      @NotNull @Assisted("widget") Widget content);
}
