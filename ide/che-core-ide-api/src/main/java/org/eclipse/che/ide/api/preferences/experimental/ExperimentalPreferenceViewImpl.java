/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.preferences.experimental;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;

@Singleton
public class ExperimentalPreferenceViewImpl implements ExperimentalPreferenceView {

  private static ExperimentalPreferenceViewImplUiBinder ourUiBinder =
      GWT.create(ExperimentalPreferenceViewImplUiBinder.class);

  private final FlowPanel rootElement;
  @UiField RadioButton enable;
  @UiField RadioButton disable;
  private ActionDelegate delegate;

  public ExperimentalPreferenceViewImpl() {
    rootElement = ourUiBinder.createAndBindUi(this);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public Widget asWidget() {
    return rootElement;
  }

  /** {@inheritDoc} */
  @Override
  public void setEnable(boolean enabled) {
    this.enable.setValue(enabled);
    this.disable.setValue(!enabled);
  }

  @UiHandler("enable")
  public void onEnableClicked(final ClickEvent event) {
    delegate.enabledChanged(enable.getValue());
    disable.setValue(!enable.getValue());
  }

  @UiHandler("disable")
  public void onDisableClicked(final ClickEvent event) {
    delegate.enabledChanged(!disable.getValue());
    enable.setValue(!disable.getValue());
  }

  interface ExperimentalPreferenceViewImplUiBinder
      extends UiBinder<FlowPanel, ExperimentalPreferenceViewImpl> {}
}
