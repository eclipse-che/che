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
package org.eclipse.che.plugin.pullrequest.client.preference;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** @author Roman Nikitenko */
@Singleton
public class ContributePreferenceViewImpl implements ContributePreferenceView {
  private static ContributePreferenceViewImplUiBinder ourUiBinder =
      GWT.create(ContributePreferenceViewImplUiBinder.class);

  @UiField CheckBox activateByProjectSelection;
  private ActionDelegate delegate;
  private final FlowPanel rootElement;

  @Inject
  public ContributePreferenceViewImpl() {
    rootElement = ourUiBinder.createAndBindUi(this);
  }

  @UiHandler("activateByProjectSelection")
  void handleActivateByProjectSelection(ValueChangeEvent<Boolean> event) {
    delegate.onActivateByProjectSelectionChanged(event.getValue());
  }

  @Override
  public void setActivateByProjectSelection(boolean isActivate) {
    activateByProjectSelection.setValue(isActivate);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  interface ContributePreferenceViewImplUiBinder
      extends UiBinder<FlowPanel, ContributePreferenceViewImpl> {}
}
