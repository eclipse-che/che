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
package org.eclipse.che.ide.ext.java.client.progressor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link ProgressMonitorView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProgressMonitorViewImpl extends Window implements ProgressMonitorView {

  interface ProgressMonitorViewImplUIBinder extends UiBinder<Widget, ProgressMonitorViewImpl> {}

  private static ProgressMonitorViewImplUIBinder uiBinder =
      GWT.create(ProgressMonitorViewImplUIBinder.class);

  @UiField FlowPanel container;

  @Inject
  public ProgressMonitorViewImpl(JavaLocalizationConstant constants) {
    setTitle(constants.progressMonitorTitle());
    setWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void showDialog() {
    super.show();
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  public void add(ProgressView progressView) {
    container.add(progressView);
  }

  @Override
  public void remove(ProgressView progressView) {
    container.remove(progressView);
  }
}
