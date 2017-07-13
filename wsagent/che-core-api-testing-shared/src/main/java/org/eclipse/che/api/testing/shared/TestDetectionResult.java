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
package org.eclipse.che.api.testing.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Result of {@link TestExecutionContext} request
 */
@DTO
public interface TestDetectionResult {

    /**
     * @return true if requested document has tests, false otherwise
     */
    boolean isTestFile();

    void setTestFile(boolean testFile);

    /**
     * List of the test positions in document
     *
     * @return
     */
    List<TestPosition> getTestPosition();

    void setTestPosition(List<TestPosition> testPosition);
}
