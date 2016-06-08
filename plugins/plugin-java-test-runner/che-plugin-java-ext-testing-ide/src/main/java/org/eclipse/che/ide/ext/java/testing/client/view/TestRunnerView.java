/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.client.view;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.testing.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;


@ImplementedBy(TestRunnerViewImpl.class)
public interface TestRunnerView extends View<TestRunnerView.ActionDelegate> {

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();

        /** Performs any actions appropriate in response to the user having pressed the Run button. */
        void onRunClicked();

        void onRunAllClicked();

        void onGotoError();

        /** Performs any actions appropriate in response to the user having clicked the Enter key. */
        void onEnterClicked();
    }

    /** Show dialog. */
    void showDialog();

    /** Close dialog. */
    void close();

    void setText(String message);

    void setTestResult(TestResult result);

    Failure getSelectedFailure();
}
