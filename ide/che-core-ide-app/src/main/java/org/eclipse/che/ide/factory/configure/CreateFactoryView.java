/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.factory.configure;

import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Representation of create factory popup.
 *
 * @author Anton Korneta
 */
@ImplementedBy(CreateFactoryViewImpl.class)
public interface CreateFactoryView extends View<CreateFactoryView.ActionDelegate> {

  interface ActionDelegate {

    /** Performs any actions appropriate in response to the user having pressed the Create button */
    void onCreateClicked();

    /**
     * Performs any actions appropriate in response to the user having type into Factory name input
     */
    void onFactoryNameChanged(String factoryName);

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();
  }

  /** Preforms closing create factory popup */
  void close();

  /** Preforms showing create factory popup */
  void showDialog();

  /** Gets factory name from input */
  String getFactoryName();

  /** Set accept factory link */
  void setAcceptFactoryLink(@NotNull String acceptLink);

  /** Set accept factory link */
  void setConfigureFactoryLink(@NotNull String configureLink);

  /** Set enable create factory button */
  void enableCreateFactoryButton(boolean enabled);

  /** Shows error if factory name invalid */
  void showFactoryNameError(@NotNull String labelMessage, @Nullable String tooltipMessage);

  /** Hide error of factory name is valid */
  void hideFactoryNameError();
}
