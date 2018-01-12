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
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;

/**
 * Provides methods to control panel of properties.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ErrorWarningsViewImpl.class)
public interface ErrorWarningsView extends IsWidget {

  /**
   * Adds special property widget on special panel on view.
   *
   * @param propertyWidget widget which will be added
   */
  void addProperty(@NotNull PropertyWidget propertyWidget);
}
