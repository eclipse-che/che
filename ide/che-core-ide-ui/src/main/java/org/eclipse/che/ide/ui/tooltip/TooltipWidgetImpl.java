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
package org.eclipse.che.ide.ui.tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * The class contains methods which allow change view representation of tooltip widget.
 *
 * @author Dmitry Shnurenko
 */
public class TooltipWidgetImpl extends PopupPanel implements TooltipWidget {
  interface TooltipWidgetImplUiBinder extends UiBinder<Widget, TooltipWidgetImpl> {}

  private static final TooltipWidgetImplUiBinder UI_BINDER =
      GWT.create(TooltipWidgetImplUiBinder.class);

  private static final String GWT_POPUP_STANDARD_STYLE = "gwt-PopupPanel";

  @UiField public Label description;

  @Inject
  public TooltipWidgetImpl() {
    add(UI_BINDER.createAndBindUi(this));
    removeStyleName(GWT_POPUP_STANDARD_STYLE);
  }

  /** {@inheritDoc} */
  @Override
  public void setDescription(@NotNull String description) {
    this.description.setText(description);
  }
}
