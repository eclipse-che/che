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
package org.eclipse.che.plugin.testing.ide.model.info;

/**
 * Describes error information about test.
 */
public class TestErrorInfo extends TestFailedInfo {
    public TestErrorInfo(String message, String stackTrace) {
        super(message, stackTrace);
    }

    @Override
    public TestStateDescription getDescription() {
        return TestStateDescription.ERROR;
    }
}
