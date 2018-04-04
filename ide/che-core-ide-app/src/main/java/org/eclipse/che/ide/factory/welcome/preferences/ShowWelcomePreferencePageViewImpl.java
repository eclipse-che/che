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
package org.eclipse.che.ide.factory.welcome.preferences;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** @author Vitaliy Guliy */
@Singleton
public class ShowWelcomePreferencePageViewImpl implements ShowWelcomePreferencePageView {

  interface ShowWelcomePreferencePageViewImplUiBinder
      extends UiBinder<FlowPanel, ShowWelcomePreferencePageViewImpl> {}

  private ActionDelegate delegate;

  private Widget widget;

  @UiField CheckBox showWelcome;

  @Inject
  public ShowWelcomePreferencePageViewImpl(ShowWelcomePreferencePageViewImplUiBinder uiBinder) {
    widget = uiBinder.createAndBindUi(this);

    showWelcome.addValueChangeHandler(
        booleanValueChangeEvent -> {
          if (delegate != null) {
            delegate.onDirtyChanged();
          }
        });
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HasValue<Boolean> welcomeField() {
    return showWelcome;
  }
}
