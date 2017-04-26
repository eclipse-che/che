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
 *
 */
@DTO
public interface TestExecutionContext {

    String getFrameworkName();

    void setFrameworkName(String name);

    String getProjectPaht();

    void setProjectPath(String projectPath);

    String getFilePath();

    void setFilePath(String filePath);

    TestType getTestType();

    void setTestType(TestType testType);

    int getCursorOffset();

    void setCursorOffset(int offset);

    enum TestType{
        FILE, FOLDER, PROJECT, CURSOR_POSITION
    }
}
