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
package org.eclipse.che.ide.ext.java.client.navigation.paraminfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;

/** @author Dmitry Shnurenko */
@ImplementedBy(ParametersHintsViewImpl.class)
public interface ParametersHintsView extends IsWidget {

  /**
   * Shows popup which contains parameters hints for particular method. Each group of parameters are
   * displayed in separated widget.
   *
   * @param parameters parameters which will be displayed
   * @param x x coordinate of popup
   * @param y y coordinate of popup
   */
  void show(List<MethodParameters> parameters, int x, int y);
}
