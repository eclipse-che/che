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
package org.eclipse.che.ide.rest;

/**
 * Describe information available service like fqn of class and path.
 *
 * @author Vitalii Parfonov
 */
public class RestServiceInfo {

    private String fqn;

    private String regex;

    private String path;

    public RestServiceInfo(String fqn, String regex, String path) {
        this.fqn = fqn;
        this.regex = regex;
        this.path = path;
    }

    /**
     * FQN of REST service class
     * @return fqn
     */
    public String getFqn() {
        return fqn;
    }

    /**
     * Regular expressions for URI pattern.
     * @return
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Describe the Path annotation, see {@link javax.ws.rs.Path}
     * @return
     */
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
         return "RestServiceInfo{" +
                "fqn='" + fqn + '\'' +
                ", regex='" + regex + '\'' +
                ", path='" + path + '\'' +
                   '}';
    }

}
