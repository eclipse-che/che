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
package org.eclipse.che.plugin.languageserver.ide.quickopen;

import com.google.inject.Inject;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;

/** @author Evgen Vidolob */
public class QuickOpenPresenter implements QuickOpenView.ActionDelegate {

  private final QuickOpenView view;
  private QuickOpenPresenterOpts opts;

  @Inject
  public QuickOpenPresenter(QuickOpenView view) {
    this.view = view;
    view.setDelegate(this);
  }

  public void run(QuickOpenPresenterOpts opts) {
    this.opts = opts;
    view.show("");
  }

  @Override
  public void valueChanged(String value) {
    opts.getModel(value)
        .then(
            new Operation<QuickOpenModel>() {
              @Override
              public void apply(QuickOpenModel model) throws OperationException {
                view.setModel(model);
              }
            });
  }

  @Override
  public void onClose(boolean canceled) {
    opts.onClose(canceled);
  }

  public interface QuickOpenPresenterOpts {

    Promise<QuickOpenModel> getModel(String value);

    void onClose(boolean canceled);
  }
}
