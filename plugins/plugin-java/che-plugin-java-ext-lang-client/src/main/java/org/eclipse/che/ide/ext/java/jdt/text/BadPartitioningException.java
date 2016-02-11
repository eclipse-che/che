/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdt.text;


/**
 * Represents the attempt to refer to a non-existing document partitioning.
 * <p>
 * This class is not intended to be serialized.
 * </p>
 *
 * @see Document
 */
public class BadPartitioningException extends Exception {

    /**
     * Serial version UID for this class.
     * <p>
     * Note: This class is not intended to be serialized.
     * </p>
     */
    private static final long serialVersionUID = 3256439205327876408L;

    /** Creates a new bad partitioning exception. */
    public BadPartitioningException() {
    }

    /**
     * Creates a new bad partitioning exception.
     *
     * @param message
     *         message describing the exception
     */
    public BadPartitioningException(String message) {
        super(message);
    }
}
