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
public interface Failure {

    /**
     * Returns the fully qualified class name of the failing test class.
     *
     * @return fully qualified class name of the failing test class
     */
    String getFailingClass();

    /**
     * Sets the fully qualified class name of the failing test class.
     * 
     * @param className
     */
    void setFailingClass(String className);

    /**
     * Returns the method name of the failing test case.
     *
     * @return the method name of the failing test case.
     */
    String getFailingMethod();

    /**
     * Sets the method name of the failing test case.
     * 
     * @param methodName
     */
    void setFailingMethod(String methodName);

    /**
     * Returns the line number of the failing test case according to the stack trace.
     *
     * @return the line number of the failing test case.
     */
    Integer getFailingLine();

    /**
     * Sets the line number of the failing test case according to the stack trace.
     * 
     * @param lineNumber
     */
    void setFailingLine(Integer lineNumber);

    /**
     * Returns the error message for the test failure.
     *
     * @return the error message
     */
    String getMessage();

    /**
     * Sets the error message for the test failure.
     * 
     * @param message
     */
    void setMessage(String message);

    /**
     * Returns the stack trace of the failing test case.
     *
     * @return the stack trace of the failing test case.
     */
    String getTrace();

    /**
     * Sets the stack trace of the failing test case.
     * 
     * @param trace
     */
    void setTrace(String trace);
}
