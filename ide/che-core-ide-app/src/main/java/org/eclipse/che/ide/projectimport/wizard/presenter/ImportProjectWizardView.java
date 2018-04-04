/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard.presenter;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Import project wizard dialog's view.
 *
 * @author Ann Shumilova
 */
@ImplementedBy(ImportProjectWizardViewImpl.class)
public interface ImportProjectWizardView extends View<ImportProjectWizardView.ActionDelegate> {
  /** Required for delegating Enter key pressed function in a wizard model. */
  public interface EnterPressedDelegate {
    /** Performs some actions in response to a user's pressed Enter key. */
    void onEnterKeyPressed();
  }

  /**
   * Show wizard page.
   *
   * @param presenter
   */
  void showPage(Presenter presenter);

  /** Show wizard dialog. */
  void showDialog();

  /** Close wizard dialog. */
  void close();

  /**
   * Set the enabled state of the next button.
   *
   * @param enabled <code>true</code> if enabled.
   */
  void setNextButtonEnabled(boolean enabled);

  /**
   * Set the enabled state of the import button.
   *
   * @param enabled <code>true</code> if enabled.
   */
  void setImportButtonEnabled(boolean enabled);

  /**
   * Set the enabled state of the back button.
   *
   * @param enabled <code>true</code> if enabled.
   */
  void setBackButtonEnabled(boolean enabled);

  /**
   * Set the visibility state of the loader.
   *
   * @param isVisible <code>true</code> if visible.
   */
  void setLoaderVisibility(boolean isVisible);

  public interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Next button */
    void onNextClicked();

    /** Performs any actions appropriate in response to the user having pressed the Back button */
    void onBackClicked();

    /** Performs any actions appropriate in response to the user having pressed the Import button */
    void onImportClicked();

    /** Performs any actions appropriate in response to the user having pressed the Cancel button */
    void onCancelClicked();
  }
}
