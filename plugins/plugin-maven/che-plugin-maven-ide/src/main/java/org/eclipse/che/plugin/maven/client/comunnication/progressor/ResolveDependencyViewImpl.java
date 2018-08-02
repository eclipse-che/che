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
package org.eclipse.che.plugin.maven.client.comunnication.progressor;

import static com.google.gwt.dom.client.Style.Unit.PCT;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * Implementation of {@link ResolveDependencyView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ResolveDependencyViewImpl extends Window implements ResolveDependencyView {
  interface ResolveDependencyViewImplUIBinder extends UiBinder<Widget, ResolveDependencyViewImpl> {}

  private static ResolveDependencyViewImplUIBinder uiBinder =
      GWT.create(ResolveDependencyViewImplUIBinder.class);

  @UiField SimplePanel progressContainer;
  @UiField SimplePanel progress;
  @UiField Label operationLabel;

  private ActionDelegate delegate;

  @Inject
  public ResolveDependencyViewImpl(MavenLocalizationConstant locale) {
    setTitle(locale.windowLoaderTitle());
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
  public void setOperationLabel(String text) {
    operationLabel.setText(text);
  }

  @Override
  public void updateProgressBar(int percent) {
    progress.getElement().getStyle().setWidth(percent, PCT);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
