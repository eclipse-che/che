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
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.Location;

import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class LocationImpl implements Location {
    private final String  target;
    private final int     lineNumber;
    private final String  resourcePath;
    private final boolean externalResource;
    private final int     externalResourceId;
    private final String  resourceProjectPath;

    public LocationImpl(String target,
                        int lineNumber,
                        String resourcePath,
                        boolean externalResource,
                        int externalResourceId,
                        String resourceProjectPath) {
        this.target = target;
        this.lineNumber = lineNumber;
        this.resourcePath = resourcePath;
        this.externalResource = externalResource;
        this.externalResourceId = externalResourceId;
        this.resourceProjectPath = resourceProjectPath;
    }

    public LocationImpl(String target, int lineNumber) {
        this(target, lineNumber, null, false, 0, null);
    }

    public LocationImpl(String target) {
        this(target, 0, null, false, 0, null);
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public boolean isExternalResource() {
        return externalResource;
    }

    @Override
    public int getExternalResourceId() {
        return externalResourceId;
    }

    @Override
    public String getResourceProjectPath() {
        return resourceProjectPath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationImpl)) return false;

        LocationImpl location = (LocationImpl)o;

        return lineNumber == location.lineNumber &&
               externalResourceId == location.externalResourceId &&
               externalResource == location.externalResource &&
               Objects.equals(resourcePath ,location.resourcePath) &&
               Objects.equals(resourceProjectPath, location.resourceProjectPath) &&
               !(target != null ? !target.equals(location.target) : location.target != null);
    }

    @Override
    public int hashCode() {
        int result = target != null ? target.hashCode() : 0;
        result = 31 * result + lineNumber;
        result = 31 * result + externalResourceId;
        result = 31 * result + Objects.hashCode(resourcePath);
        result = 31 * result + (externalResource ? 1 : 0);
        result = 31 * result + Objects.hashCode(resourceProjectPath);
        return result;
    }

    @Override
    public String toString() {
        return "LocationImpl{" +
               "target='" + target + '\'' +
               ", lineNumber=" + lineNumber +
               ", resourcePath='" + resourcePath + '\'' +
               ", externalResource=" + externalResource +
               ", externalResourceId=" + externalResourceId +
               ", resourceProjectPath='" + resourceProjectPath + '\'' +
               '}';
    }
}
