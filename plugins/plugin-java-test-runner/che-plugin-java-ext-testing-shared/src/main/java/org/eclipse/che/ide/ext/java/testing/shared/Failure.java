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
package org.eclipse.che.ide.ext.java.testing.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Failure {

    String getFailingClass();

    void setFailingClass(String className);

    String getFailingMethod();

    void setFailingMethod(String methodName);

    Integer getFailingLine();

    void setFailingLine(Integer lineNumber);

    String getMessage();

    void setMessage(String message);

    String getTrace();

    void setTrace(String trace);
}
