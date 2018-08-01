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
package org.eclipse.che.plugin.debugger.ide.debug.expression;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link EvaluateExpressionPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface EvaluateExpressionView extends View<EvaluateExpressionView.ActionDelegate> {
  /** Needs for delegate some function into EvaluateExpression view. */
  interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Close button. */
    void onCloseClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Evaluate button.
     */
    void onEvaluateClicked();

    /** Performs any actions appropriate in response to the user having changed expression. */
    void onExpressionValueChanged();
  }

  /**
   * Get expression field value.
   *
   * @return {@link String}
   */
  @NotNull
  String getExpression();

  /**
   * Set expression field value.
   *
   * @param expression
   */
  void setExpression(@NotNull String expression);

  /**
   * Set result field value.
   *
   * @param value result field value
   */
  void setResult(@NotNull String value);

  /**
   * Change the enable state of the evaluate button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableEvaluateButton(boolean enabled);

  /** Give focus to expression field. */
  void focusInExpressionField();

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
