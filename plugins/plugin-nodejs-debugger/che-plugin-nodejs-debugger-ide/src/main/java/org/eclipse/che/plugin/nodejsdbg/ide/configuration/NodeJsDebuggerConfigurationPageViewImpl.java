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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The implementation of {@link NodeJsDebuggerConfigurationPageView}.
 *
 * @author Anatolii Bazko
 */
public class NodeJsDebuggerConfigurationPageViewImpl
    implements NodeJsDebuggerConfigurationPageView {

  private static final NodeJsDebugConfigurationPageViewImplUiBinder UI_BINDER =
      GWT.create(NodeJsDebugConfigurationPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextBox scriptPath;

  private ActionDelegate delegate;

  public NodeJsDebuggerConfigurationPageViewImpl() {
    rootElement = UI_BINDER.createAndBindUi(this);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  @Override
  public String getScriptPath() {
    return scriptPath.getValue();
  }

  @Override
  public void setScriptPath(String path) {
    this.scriptPath.setValue(path);
  }

  @UiHandler({"scriptPath"})
  void onScriptPathKeyUp(KeyUpEvent event) {
    delegate.onScriptPathChanged();
  }

  interface NodeJsDebugConfigurationPageViewImplUiBinder
      extends UiBinder<FlowPanel, NodeJsDebuggerConfigurationPageViewImpl> {}
}
