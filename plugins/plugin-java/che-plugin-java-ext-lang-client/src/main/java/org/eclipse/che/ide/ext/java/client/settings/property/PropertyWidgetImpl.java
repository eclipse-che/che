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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions;
import org.eclipse.che.ide.ui.listbox.CustomListBox;

/** @author Dmitry Shnurenko */
public class PropertyWidgetImpl extends Composite implements PropertyWidget {
  interface PropertyWidgetImplUiBinder extends UiBinder<Widget, PropertyWidgetImpl> {}

  private static final PropertyWidgetImplUiBinder UI_BINDER =
      GWT.create(PropertyWidgetImplUiBinder.class);

  public static final String IGNORE = "ignore";
  public static final String WARNING = "warning";
  public static final String ERROR = "error";

  private final ErrorWarningsOptions optionId;

  @UiField Label title;
  @UiField CustomListBox property;

  private ActionDelegate delegate;

  @Inject
  public PropertyWidgetImpl(
      PropertyNameManager nameManager, @Assisted ErrorWarningsOptions optionId) {
    initWidget(UI_BINDER.createAndBindUi(this));

    this.optionId = optionId;

    this.title.setText(nameManager.getName(optionId));

    property.addItem(IGNORE);
    property.addItem(WARNING);
    property.addItem(ERROR);
  }

  /** {@inheritDoc} */
  @Override
  public void selectPropertyValue(@NotNull String value) {
    for (int i = 0; i < property.getItemCount(); i++) {
      if (property.getValue(i).equals(value)) {
        property.setItemSelected(i, true);
        return;
      }
    }
  }

  @UiHandler("property")
  public void onPropertyChanged(@SuppressWarnings("UnusedParameters") ChangeEvent event) {
    delegate.onPropertyChanged();
  }

  @Override
  public String getSelectedValue() {
    int index = property.getSelectedIndex();

    return index != -1 ? property.getValue(index) : "";
  }

  @Override
  public ErrorWarningsOptions getOptionId() {
    return optionId;
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
