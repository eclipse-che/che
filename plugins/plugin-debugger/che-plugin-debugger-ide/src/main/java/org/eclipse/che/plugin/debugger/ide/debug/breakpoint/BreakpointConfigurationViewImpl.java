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
package org.eclipse.che.plugin.debugger.ide.debug.breakpoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/** @author Anatolii Bazko */
@Singleton
public class BreakpointConfigurationViewImpl extends Window implements BreakpointConfigurationView {

  interface BreakpointConfigurationViewImplUiBinder
      extends UiBinder<Widget, BreakpointConfigurationViewImpl> {}

  private static BreakpointConfigurationViewImpl.BreakpointConfigurationViewImplUiBinder uiBinder =
      GWT.create(BreakpointConfigurationViewImpl.BreakpointConfigurationViewImplUiBinder.class);

  @UiField Label breakpointLocation;
  @UiField TextArea breakpointCondition;
  @UiField CheckBox enabled;

  private ActionDelegate delegate;

  @Inject
  public BreakpointConfigurationViewImpl(DebuggerLocalizationConstant locale) {
    Widget widget = uiBinder.createAndBindUi(this);

    this.setWidget(widget);
    this.setTitle(locale.breakpointConfigurationTitle());
    ensureDebugId("breakpoint-configuration-window");

    Button closeButton =
        createButton(
            locale.evaluateExpressionViewCloseButtonTitle(),
            UIObject.DEBUG_ID_PREFIX + "close-btn",
            clickEvent -> delegate.onCloseClicked());
    addButtonToFooter(closeButton);

    Button applyButton =
        createButton(
            locale.viewBreakpointConfigurationApplyButton(),
            UIObject.DEBUG_ID_PREFIX + "apply-btn",
            clickEvent -> delegate.onApplyClicked());
    addButtonToFooter(applyButton);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    this.show();
    breakpointCondition.setFocus(true);
  }

  @Override
  public void close() {
    this.hide();
  }

  @Override
  public void setBreakpoint(Breakpoint breakpoint) {
    Location location = breakpoint.getLocation();

    StringBuilder labelText = new StringBuilder();
    labelText
        .append(Path.valueOf(location.getTarget()).lastSegment())
        .append(":")
        .append(location.getLineNumber());
    breakpointLocation.setText(labelText.toString());

    if (breakpoint.getCondition() != null) {
      breakpointCondition.setText(breakpoint.getCondition());
    } else {
      breakpointCondition.setText("");
    }

    enabled.setValue(breakpoint.isEnabled());
  }

  @Override
  public String getBreakpointCondition() {
    return breakpointCondition.getText();
  }

  @Override
  public boolean isBreakpointEnabled() {
    return enabled.getValue();
  }
}
