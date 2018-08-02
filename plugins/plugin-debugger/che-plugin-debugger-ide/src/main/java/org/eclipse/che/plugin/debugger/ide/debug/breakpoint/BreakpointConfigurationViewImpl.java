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
package org.eclipse.che.plugin.debugger.ide.debug.breakpoint;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.valueOf;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
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

  @UiField CheckBox enabled;
  @UiField CheckBox breakpointConditionEnabled;
  @UiField TextBox breakpointCondition;
  @UiField CheckBox hitCountEnabled;
  @UiField TextBox hitCount;
  @UiField RadioButton breakpointSuspendNone;
  @UiField RadioButton breakpointSuspendThread;
  @UiField RadioButton breakpointSuspendAll;

  private ActionDelegate delegate;

  @Inject
  public BreakpointConfigurationViewImpl(DebuggerLocalizationConstant locale) {
    Widget widget = uiBinder.createAndBindUi(this);

    this.setWidget(widget);
    this.setTitle(locale.breakpointConfigurationTitle());
    ensureDebugId("breakpoint-configuration-window");

    addFooterButton(
        locale.evaluateExpressionViewCloseButtonTitle(),
        UIObject.DEBUG_ID_PREFIX + "close-btn",
        clickEvent -> delegate.onCloseClicked());

    addFooterButton(
        locale.viewBreakpointConfigurationApplyButton(),
        UIObject.DEBUG_ID_PREFIX + "apply-btn",
        clickEvent -> delegate.onApplyClicked(),
        true);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    show(breakpointCondition);
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
    setTitle(labelText.toString());

    enabled.setValue(breakpoint.isEnabled());

    BreakpointConfiguration conf = breakpoint.getBreakpointConfiguration();
    breakpointConditionEnabled.setValue(conf.isConditionEnabled());
    breakpointCondition.setEnabled(conf.isConditionEnabled());
    breakpointCondition.setText(nullToEmpty(conf.getCondition()));

    hitCountEnabled.setValue(conf.isHitCountEnabled());
    hitCount.setEnabled(conf.isHitCountEnabled());
    hitCount.setText(conf.getHitCount() <= 0 ? "" : valueOf(conf.getHitCount()));

    switch (conf.getSuspendPolicy()) {
      case NONE:
        breakpointSuspendNone.setValue(true);
        break;
      case THREAD:
        breakpointSuspendThread.setValue(true);
        break;
      default:
        breakpointSuspendAll.setValue(true);
        break;
    }
  }

  @Override
  public BreakpointConfiguration getBreakpointConfiguration() {
    int hit;
    try {
      hit = Integer.parseInt(hitCount.getValue());
    } catch (NumberFormatException e) {
      hit = 0;
    }

    SuspendPolicy suspendPolicy =
        breakpointSuspendNone.getValue()
            ? SuspendPolicy.NONE
            : (breakpointSuspendThread.getValue() ? SuspendPolicy.THREAD : SuspendPolicy.ALL);

    return new BreakpointConfigurationImpl(
        breakpointConditionEnabled.getValue(),
        breakpointCondition.getText(),
        hitCountEnabled.getValue(),
        hit,
        suspendPolicy);
  }

  @Override
  public boolean isBreakpointEnabled() {
    return enabled.getValue();
  }

  @UiHandler("breakpointConditionEnabled")
  public void onBreakpointConditionEnabledChanged(ValueChangeEvent<Boolean> valueChangeEvent) {
    breakpointCondition.setEnabled(valueChangeEvent.getValue());
  }

  @UiHandler("hitCountEnabled")
  public void onBreakpointHitCountEnabledChanged(ValueChangeEvent<Boolean> valueChangeEvent) {
    hitCount.setEnabled(valueChangeEvent.getValue());
  }
}
