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
package org.eclipse.che.ide.ext.java.jdt.text;

import org.eclipse.che.ide.runtime.Assert;

/**
 * Standard implementation of a generic
 * {@link org.eclipse.che.ide.legacy.client.api.text.eclipse.LineTracker.text.ILineTracker}.
 * <p>
 * The line tracker can be configured with the set of legal line delimiters.
 * Line delimiters are unconstrained. The line delimiters are used to compute
 * the tracker's line structure. In the case of overlapping line delimiters, the
 * longest line delimiter is given precedence of the shorter ones.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ConfigurableLineTracker extends AbstractLineTracker {

    /** The strings which are considered being the line delimiter */
    private String[] fDelimiters;

    /** A predefined delimiter information which is always reused as return value */
    private DelimiterInfo fDelimiterInfo = new DelimiterInfo();

    /**
     * Creates a standard line tracker for the given line delimiters.
     *
     * @param legalLineDelimiters
     *         the tracker's legal line delimiters,
     *         may not be <code>null</code> and must be longer than 0
     */
    public ConfigurableLineTracker(String[] legalLineDelimiters) {
        Assert.isTrue(legalLineDelimiters != null && legalLineDelimiters.length > 0);
        fDelimiters = TextUtilities.copy(legalLineDelimiters);
    }

    /*
     * @see org.eclipse.jface.text.ILineTracker#getLegalLineDelimiters()
     */
    public String[] getLegalLineDelimiters() {
        return TextUtilities.copy(fDelimiters);
    }

    /*
     * @see org.eclipse.jface.text.AbstractLineTracker#nextDelimiterInfo(java.lang.String, int)
     */
    protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
        if (fDelimiters.length > 1) {
            int[] info = TextUtilities.indexOf(fDelimiters, text, offset);
            if (info[0] == -1)
                return null;
            fDelimiterInfo.delimiterIndex = info[0];
            fDelimiterInfo.delimiter = fDelimiters[info[1]];
        } else {
            int index = text.indexOf(fDelimiters[0], offset);
            if (index == -1)
                return null;
            fDelimiterInfo.delimiterIndex = index;
            fDelimiterInfo.delimiter = fDelimiters[0];
        }

        fDelimiterInfo.delimiterLength = fDelimiterInfo.delimiter.length();
        return fDelimiterInfo;
    }
}
