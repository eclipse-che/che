/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.core.client.view;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;

/**
 * View for the result of java tests.
 *
 * @author Mirage Abeysekara
 */
public interface TestResultView extends View<TestResultView.ActionDelegate> {
    /**
     * Sets whether this panel is visible.
     *
     * @param visible
     *         visible - true to show the object, false to hide it
     */
    void setVisible(boolean visible);

    /**
     * Activate Test results part.
     *
     * @param result
     *         test results which comes from the server
     */
    void showResults(TestResult result);

    interface ActionDelegate extends BaseActionDelegate {

    }

}
