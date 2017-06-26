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
package org.testng.listeners;

import org.testng.CheTestNGListener;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

/**
 * Listener for events related to configuration methods.
 */
public class CheConfigurationListener implements IConfigurationListener {

    private final CheTestNGListener delegate;

    private boolean ignoreStarted;

    public CheConfigurationListener(CheTestNGListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        delegate.onConfigurationSuccess(itr, !ignoreStarted);
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        delegate.onConfigurationFailure(itr, !ignoreStarted);
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        //ignore
    }

    public void setIgnoreStarted() {
        ignoreStarted = true;
    }
}
