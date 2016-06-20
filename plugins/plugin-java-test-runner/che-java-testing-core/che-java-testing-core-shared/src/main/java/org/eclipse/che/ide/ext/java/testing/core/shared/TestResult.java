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
package org.eclipse.che.ide.ext.java.testing.core.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

@DTO
public interface TestResult {
    String getTestFramework();

    void setTestFramework(String framework);

    boolean isSuccess();

    void setSuccess(boolean success);

    List<Failure> getFailures();

    void setFailures(List<Failure> failures);

    int getFailureCount();

    void setFailureCount(int count);
}
