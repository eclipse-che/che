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
package org.eclipse.che.plugin.typescript.dto.model;

/**
 * Defines the model link to parameter of a method
 *
 * @author Florent Benoit
 */
public class ParameterMethodModel {

    /**
     * Name of the parameter.
     */
    private String parameterName;

    /**
     * Type of the parameter. (Type is in TypeScript format)
     */
    private String parameterType;

    /**
     * Create a new instance of parameter model with specified name and type
     *
     * @param parameterName
     *         the name of the parameter like foo
     * @param parameterType
     *         the type of the parameter (like foo.bar.MyDTO or primitive value like string)
     */
    public ParameterMethodModel(String parameterName, String parameterType) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    /**
     * Getter for the name
     *
     * @return the name of the parameter
     */
    public String getName() {
        return this.parameterName;
    }

    /**
     * Getter for the type
     *
     * @return the type of the parameter
     */
    public String getType() {
        return this.parameterType;
    }

}
