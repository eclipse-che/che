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
package org.eclipse.che.ide.ext.java.testing.junit4x.shared;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;


import java.util.List;
/**
 * TestResult dto for JUnit
 * @author Mirage Abeysekara
 */
@DTO
public interface JUnitTestResult extends TestResult {
    /**
     * Get the running JUnit framework version.
     * @return JUnit framework version.
     */
    String getFrameworkVersion();

    /**
     * Sets the JUnit framework version.
     * @param framework framework version string
     */
    void setFrameworkVersion(String framework);

}
