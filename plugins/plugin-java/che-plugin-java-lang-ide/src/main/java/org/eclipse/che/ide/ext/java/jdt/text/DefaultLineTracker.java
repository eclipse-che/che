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
 * Standard implementation of {@link org.eclipse.che.ide.legacy.client.api.text.eclipse.LineTracker.text.ILineTracker}.
 * <p>
 * The line tracker considers the three common line delimiters which are '\n', '\r', '\r\n'.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DefaultLineTracker extends AbstractLineTracker {

    /** The predefined delimiters of this tracker */
    public final static String[] DELIMITERS = {"\r", "\n", "\r\n"}; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$

    /** A predefined delimiter information which is always reused as return value */
    private DelimiterInfo fDelimiterInfo = new DelimiterInfo();

    /** Creates a standard line tracker. */
    public DefaultLineTracker() {
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLegalLineDelimiters() */
    public String[] getLegalLineDelimiters() {
        return new String[]{"\n"};
    }

    /*
     * @see org.eclipse.jface.text.AbstractLineTracker#nextDelimiterInfo(java.lang .String, int)
     */
    protected DelimiterInfo nextDelimiterInfo(String text, int offset) {

        char ch;
        int length = text.length();
        for (int i = offset; i < length; i++) {

            ch = text.charAt(i);
            if (ch == '\r') {

                if (i + 1 < length) {
                    if (text.charAt(i + 1) == '\n') {
                        fDelimiterInfo.delimiter = DELIMITERS[2];
                        fDelimiterInfo.delimiterIndex = i;
                        fDelimiterInfo.delimiterLength = 2;
                        return fDelimiterInfo;
                    }
                }

                fDelimiterInfo.delimiter = DELIMITERS[0];
                fDelimiterInfo.delimiterIndex = i;
                fDelimiterInfo.delimiterLength = 1;
                return fDelimiterInfo;

            } else if (ch == '\n') {

                fDelimiterInfo.delimiter = DELIMITERS[1];
                fDelimiterInfo.delimiterIndex = i;
                fDelimiterInfo.delimiterLength = 1;
                return fDelimiterInfo;
            }
        }

        return null;
    }
}
