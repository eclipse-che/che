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

/**
 * Request DTO for test framework and/or test method detection, result should be {@link TestDetectionResult}
 */
@DTO
public interface TestDetectionContext {

    String getProjectPath();

    void setProjectPath(String projectPath);

    String getFilePath();

    void setFilePath(String filePath);

    int getOffset();

    void setOffset(int offset);
}
