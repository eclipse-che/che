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

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.dom.client.Style.Unit.PCT;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

/**
 * Implementation of {@link ProgressView}.
 *
 * @author Valeriy Svydenko
 */
public class ProgressViewImpl implements ProgressView {
  private FlowPanel rootElement;

  @UiField Label operationLabel;
  @UiField SimplePanel progress;
  @UiField SimplePanel progressContainer;

  @Inject
  public ProgressViewImpl(ProgressorViewImplUiBinder uiBinder) {
    rootElement = uiBinder.createAndBindUi(this);
  }

  @Override
  public void updateProgressBar(ProgressReport progressor) {
    String label =
        isNullOrEmpty(progressor.getSubTask()) ? progressor.getStatus() : progressor.getSubTask();
    operationLabel.setText(label);
    int totalWork = progressor.getTotalWork();
    if (totalWork > 0) {
      double percent = ((double) progressor.getWorkDone() / progressor.getTotalWork());
      progress.getElement().getStyle().setWidth(percent * 100, PCT);
    }
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  interface ProgressorViewImplUiBinder extends UiBinder<FlowPanel, ProgressViewImpl> {}
}
