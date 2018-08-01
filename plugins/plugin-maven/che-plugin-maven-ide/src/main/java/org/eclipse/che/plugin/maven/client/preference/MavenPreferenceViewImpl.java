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
package org.eclipse.che.plugin.maven.client.preference;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * Implementation of {@link MavenPreferenceView}.
 *
 * @author Igor Vinokur
 */
@Singleton
public class MavenPreferenceViewImpl implements MavenPreferenceView {

  private static MavenPreferenceViewImplUiBinder uiBinder =
      GWT.create(MavenPreferenceViewImplUiBinder.class);
  private final FlowPanel rootElement;

  @UiField(provided = true)
  final MavenLocalizationConstant locale;

  private ActionDelegate delegate;

  @UiField CheckBox showArtifactId;

  @Inject
  public MavenPreferenceViewImpl(MavenLocalizationConstant locale) {
    this.locale = locale;

    rootElement = uiBinder.createAndBindUi(this);
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
  public void setSelectedShowArtifactIdCheckBox(boolean selected) {
    showArtifactId.setValue(selected);
  }

  @UiHandler("showArtifactId")
  void handleShowArtifactIdCheckBoxSelection(ClickEvent event) {
    delegate.onArtifactIdCheckBoxValueChanged(showArtifactId.getValue());
  }

  interface MavenPreferenceViewImplUiBinder extends UiBinder<FlowPanel, MavenPreferenceViewImpl> {}
}
