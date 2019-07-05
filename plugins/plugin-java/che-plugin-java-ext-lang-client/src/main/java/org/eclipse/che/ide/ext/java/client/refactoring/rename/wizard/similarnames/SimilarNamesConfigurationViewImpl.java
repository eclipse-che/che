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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.jdt.ls.extension.api.MatchStrategy;

/** @author Valeriy Svydenko */
@Singleton
final class SimilarNamesConfigurationViewImpl extends Window
    implements SimilarNamesConfigurationView {

  interface SimilarNamesConfigurationViewImplUiBinder
      extends UiBinder<Widget, SimilarNamesConfigurationViewImpl> {}

  private static SimilarNamesConfigurationViewImplUiBinder UI_BINDER =
      GWT.create(SimilarNamesConfigurationViewImplUiBinder.class);

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  @UiField Label errorLabel;
  @UiField RadioButton findExactNames;
  @UiField RadioButton findEmbeddedNames;
  @UiField RadioButton findNameSuffixes;

  private ActionDelegate delegate;

  @Inject
  public SimilarNamesConfigurationViewImpl(JavaLocalizationConstant locale) {
    this.locale = locale;

    setTitle(locale.renameSimilarNamesConfigurationTitle());

    setWidget(UI_BINDER.createAndBindUi(this));

    createButtons(locale);
  }

  /** {@inheritDoc} */
  @Override
  public MatchStrategy getMatchStrategy() {
    if (findExactNames.getValue()) {
      return MatchStrategy.EXACT;
    } else if (findEmbeddedNames.getValue()) {
      return MatchStrategy.EMBEDDED;
    } else {
      return MatchStrategy.SUFFIX;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    findExactNames.setValue(true);
    findNameSuffixes.setValue(false);
    findEmbeddedNames.setValue(false);

    super.show();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  private void createButtons(JavaLocalizationConstant locale) {
    addFooterButton(
        locale.moveDialogButtonCancel(),
        "similar-cancel-button",
        event -> {
          findExactNames.setValue(true);
          findNameSuffixes.setValue(false);
          findEmbeddedNames.setValue(false);

          hide();
        });

    addFooterButton(locale.moveDialogButtonOk(), "similar-accept-button", event -> hide());
  }
}
