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
package org.eclipse.che.ide.ext.help.client.about.info;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.help.client.HelpExtensionLocalizationConstant;
import org.eclipse.che.ide.ui.button.ButtonAlignment;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.ClipboardUtils;
import org.eclipse.che.ide.util.loging.Log;

/**
 * View to display `Show Build Details` dialog.
 *
 * @author Vlad Zhukovskyi
 * @since 6.7.0
 */
@Singleton
public class BuildDetailsViewImpl extends Window implements BuildDetailsView {
  interface DebugSummaryViewImplUiBinder extends UiBinder<Widget, BuildDetailsViewImpl> {}

  private static DebugSummaryViewImplUiBinder uiBinder =
      GWT.create(DebugSummaryViewImplUiBinder.class);
  private static final String BUILD_DETAILS_TEXT_AREA_ID = "build-details-text-area";
  private static final String WINDOW_DEBUG_ID = "build-details";
  private static final String BUILD_DETAILS_OK_BUTTON_ID = "build-details-ok";
  private static final String BUILD_DETAILS_COPY_BUTTON_ID = "build-details-copy";

  @UiField TextArea buildDetails;

  @SuppressWarnings("unused")
  private ActionDelegate delegate;

  @Inject
  public BuildDetailsViewImpl(
      HelpExtensionLocalizationConstant coreLocale, BuildDetailsLocalizationConstant locale) {
    this.ensureDebugId(WINDOW_DEBUG_ID);

    setTitle(locale.title());
    setWidget(uiBinder.createAndBindUi(this));

    buildDetails.getElement().setAttribute("id", BUILD_DETAILS_TEXT_AREA_ID);
    buildDetails.setReadOnly(true);

    addFooterButton(coreLocale.ok(), BUILD_DETAILS_OK_BUTTON_ID, event -> hide(), true);
    addFooterButton(
        locale.copyToClipboardButton(),
        BUILD_DETAILS_COPY_BUTTON_ID,
        event -> {
          if (!ClipboardUtils.copyElementContents(BUILD_DETAILS_TEXT_AREA_ID)) {
            Log.info(
                BuildDetailsViewImpl.class, "Failed to copy debug summary value to clipboard.");
          }
        },
        false,
        ButtonAlignment.LEFT);
  }

  @Override
  public void setBuildDetails(String details) {
    buildDetails.setValue(details);
  }

  @Override
  public void showDialog() {
    show(buildDetails);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
