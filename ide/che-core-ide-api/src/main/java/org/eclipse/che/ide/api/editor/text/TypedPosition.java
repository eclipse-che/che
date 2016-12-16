/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.text;


/**
 * Convenience class for positions that have a type, similar to
 * {@link TypedRegion}.
 * <p/>
 * As {@link Position},<code>TypedPosition</code> can
 * not be used as key in hash tables as it overrides <code>isEquals</code> and
 * <code>hashCode</code> as it would be a value object.
 */
public class TypedPosition extends Position {

    /** The type of the region described by this position */
    private String fType;

    /**
     * Creates a position along the given specification.
     *
     * @param offset
     *         the offset of this position
     * @param length
     *         the length of this position
     * @param type
     *         the content type of this position
     */
    public TypedPosition(int offset, int length, String type) {
        super(offset, length);
        fType = type;
    }

    /**
     * Creates a position based on the typed region.
     *
     * @param region
     *         the typed region
     */
    public TypedPosition(TypedRegion region) {
        super(region.getOffset(), region.getLength());
        fType = region.getType();
    }

    /**
     * Returns the content type of the region.
     *
     * @return the content type of the region
     */
    public String getType() {
        return fType;
    }

    /*
     * @see java.lang.Object#isEquals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TypedPosition) {
            if (super.equals(o)) {
                TypedPosition p = (TypedPosition)o;
                return (fType == null && p.getType() == null) || fType.equals(p.getType());
            }
        }
        return false;
    }

    /*
    * @see java.lang.Object#hashCode()
    */
    @Override
    public int hashCode() {
        int type = fType == null ? 0 : fType.hashCode();
        return super.hashCode() | type;
    }

    /*
     * @see org.eclipse.jface.text.Region#toString()
     */
    @Override
    public String toString() {
        return fType + " - " + super.toString(); //$NON-NLS-1$
    }
}
