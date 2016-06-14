/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.quickopen;

import com.google.inject.Inject;

/**
 * @author Evgen Vidolob
 */
public class QuickOpenPresenter implements QuickOpenView.ActionDelegate {


    private final QuickOpenView          view;
    private       QuickOpenPresenterOpts opts;

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
        view.setModel(opts.getModel(value));
    }

    @Override
    public void onClose(boolean canceled) {
        opts.onClose(canceled);
    }

    public interface QuickOpenPresenterOpts {

        QuickOpenModel getModel(String value);

        void onClose(boolean canceled);
    }
}
