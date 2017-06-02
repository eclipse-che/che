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
 * DTO for representing the details of failing test case of a Java unit test.
 *
 * @author Mirage Abeysekara
 */
@DTO
@Deprecated
public interface TestCase {

    /**
     * Returns the fully qualified class name of the test class.
     *
     * @return fully qualified class name of the test class
     */
    String getClassName();

    /**
     * Sets the fully qualified class name of the test class.
     * 
     * @param className
     */
    void setClassName(String className);

    /**
     * Returns the method name of the test case.
     *
     * @return the method name of the test case.
     */
    String getMethod();

    /**
     * Sets the method name of the test case.
     * 
     * @param methodName
     */
    void setMethod(String methodName);

    /**
     * Returns whether a test case failed.
     *
     * @return true if the test case failed, and false if the test succeeded.
     */
    Boolean isFailed();

    /**
     * Sets the test case as failed.
     * 
     * @param failed status
     */
    void setFailed(Boolean failed);

    /**
     * Returns the line number of a failing test case according to the stack trace.
     *
     * @return the line number of the failing test case, or -1 if the test succeeded.
     */
    Integer getFailingLine();

    /**
     * Sets the line number of a failing test case according to the stack trace.
     * 
     * @param lineNumber
     */
    void setFailingLine(Integer lineNumber);

    /**
     * Returns the error message for a test failure.
     *
     * @return the error message, or an empty string if the test succeeded
     */
    String getMessage();

    /**
     * Sets the error message for the test failure.
     * 
     * @param message
     */
    void setMessage(String message);

    /**
     * Returns the stack trace of a failing test case.
     *
     * @return the stack trace of the failing test case, or an empty string if the test succeeded.
     */
    String getTrace();

    /**
     * Sets the stack trace of the failing test case.
     * 
     * @param trace
     */
    void setTrace(String trace);
}
