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
package org.eclipse.che.ide.ext.java.client.settings.property;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions;

/**
 * The interface provides methods to control property's widget which contains name of property and
 * list box with all possible values.
 *
 * @author Dmitry Shnurenko
 */
public interface PropertyWidget extends View<PropertyWidget.ActionDelegate> {

  /**
   * Selects need values in list box.
   *
   * @param value value which will be selected
   */
  void selectPropertyValue(@NotNull String value);

  /** @return property value selected in the property widget */
  String getSelectedValue();

  /** @return unique error(warning) options id for the property widget. */
  ErrorWarningsOptions getOptionId();

  interface ActionDelegate {
    /** Performs some action when user change value of property. */
    void onPropertyChanged();
  }
}
