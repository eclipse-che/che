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
package org.eclipse.che.ide.ext.git.client.checkout;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/** @author Roman Nikitenko */
@ImplementedBy(CheckoutReferenceViewImpl.class)
public interface CheckoutReferenceView extends View<CheckoutReferenceView.ActionDelegate> {

  public interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Checkout button.
     */
    void onCheckoutClicked(String reference);

    /** Performs any actions appropriate in response to the user having changed the input value. */
    void referenceValueChanged(String reference);

    /** Performs any actions appropriate in response to the user having clicked the Enter key. */
    void onEnterClicked();
  }

  /** Show dialog. */
  void showDialog();

  /** Close dialog. */
  void close();

  /** Performs when user select generate keys. */
  String getReference();

  /**
   * Set the enable state of the Checkout button.
   *
   * @param isEnabled <code>true</code> if enabled, <code>false</code> if disabled
   */
  void setCheckoutButEnableState(boolean isEnabled);
}
