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
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.ide.api.resources.VirtualFile;

import java.util.Objects;

/**
 * Immutable object represents a breakpoint. It isn't designed to be preserved.
 * {@link org.eclipse.che.ide.api.debug.dto.StorableBreakpointDto} should be used then.
 *
 * @author Evgen Vidolob
 * @author Anatoliy Bazko
 */
public class Breakpoint {
    protected int         lineNumber;
    protected VirtualFile file;
    private   Type        type;
    private   String      path;

    /**
     * Breakpoint becomes active if is added to a JVM, otherwise it is just a user mark.
     */
    private boolean active;

    public Breakpoint(Type type, int lineNumber, String path, VirtualFile file, boolean active) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.path = path;
        this.file = file;
        this.active = active;
    }

    /**
     * Getter for {@link #active}
     */
    public boolean isActive() {
        return active;
    }

    /** @return the type */
    public Type getType() {
        return type;
    }

    /** @return the lineNumber */
    public int getLineNumber() {
        return lineNumber;
    }

    /** @return file path */
    public String getPath() {
        return path;
    }

    /**
     * Returns the file with which this breakpoint is associated.
     *
     * @return file with which this breakpoint is associated
     */
    public VirtualFile getFile() {
        return file;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Breakpoint [lineNumber=").append(lineNumber)
               .append(", type=").append(type)
               .append(", active=").append(active)
               .append(", path=").append(path)
               .append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Breakpoint)) return false;

        Breakpoint that = (Breakpoint)o;

        return lineNumber == that.lineNumber && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        int result = lineNumber;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    public enum Type {
        BREAKPOINT, CURRENT
    }
}
