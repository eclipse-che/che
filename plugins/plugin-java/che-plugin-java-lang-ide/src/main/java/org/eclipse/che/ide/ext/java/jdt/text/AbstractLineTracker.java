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

import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.Region;

import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of <code>ILineTracker</code>. It lets the definition of line delimiters to subclasses. Assuming that
 * '\n' is the only line delimiter, this abstract implementation defines the following line scheme:
 * <ul>
 * <li>"" -> [0,0]
 * <li>"a" -> [0,1]
 * <li>"\n" -> [0,1], [1,0]
 * <li>"a\n" -> [0,2], [2,0]
 * <li>"a\nb" -> [0,2], [2,1]
 * <li>"a\nbc\n" -> [0,2], [2,3], [5,0]
 * </ul>
 * <p>
 * This class must be subclassed.
 * </p>
 */
public abstract class AbstractLineTracker implements LineTracker {

    /**
     * Combines the information of the occurrence of a line delimiter. <code>delimiterIndex</code> is the index where a line
     * delimiter starts, whereas <code>delimiterLength</code>, indicates the length of the delimiter.
     */
    protected static class DelimiterInfo {
        public int delimiterIndex;

        public int delimiterLength;

        public String delimiter;
    }

    /**
     * Representation of replace and set requests.
     *
     * @since 3.1
     */
    protected static class Request {
        public final int offset;

        public final int length;

        public final String text;

        public Request(int offset, int length, String text) {
            this.offset = offset;
            this.length = length;
            this.text = text;
        }

        public Request(String text) {
            this.offset = -1;
            this.length = -1;
            this.text = text;
        }

        public boolean isReplaceRequest() {
            return this.offset > -1 && this.length > -1;
        }
    }

    /** The list of pending requests. */
    private List<Request> fPendingRequests;

    /** The implementation that this tracker delegates to. */
    private LineTracker fDelegate = new ListLineTracker() {
        public String[] getLegalLineDelimiters() {
            return AbstractLineTracker.this.getLegalLineDelimiters();
        }

        protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
            return AbstractLineTracker.this.nextDelimiterInfo(text, offset);
        }
    };

    /** Whether the delegate needs conversion when the line structure is modified. */
    private boolean fNeedsConversion = true;

    /** Creates a new line tracker. */
    protected AbstractLineTracker() {
    }

    /*
     * @see org.eclipse.jface.text.ILineTracker#computeNumberOfLines(java.lang.String)
     */
    public int computeNumberOfLines(String text) {
        return fDelegate.computeNumberOfLines(text);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineDelimiter(int) */
    public String getLineDelimiter(int line) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getLineDelimiter(line);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineInformation(int) */
    public Region getLineInformation(int line) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getLineInformation(line);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineInformationOfOffset(int) */
    public Region getLineInformationOfOffset(int offset) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getLineInformationOfOffset(offset);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineLength(int) */
    public int getLineLength(int line) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getLineLength(line);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineNumberOfOffset(int) */
    public int getLineNumberOfOffset(int offset) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getLineNumberOfOffset(offset);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineOffset(int) */
    public int getLineOffset(int line) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getLineOffset(line);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getNumberOfLines() */
    public int getNumberOfLines() {
        // try
        // {
        // checkRewriteSession();
        // }
        // catch (BadLocationException x)
        // {
        // // TODO there is currently no way to communicate that exception back to the document
        // }
        return fDelegate.getNumberOfLines();
    }

    /* @see org.eclipse.jface.text.ILineTracker#getNumberOfLines(int, int) */
    public int getNumberOfLines(int offset, int length) throws BadLocationException {
        // checkRewriteSession();
        return fDelegate.getNumberOfLines(offset, length);
    }

    /* @see org.eclipse.jface.text.ILineTracker#set(java.lang.String) */
    public void set(String text) {
        // if (hasActiveRewriteSession())
        // {
        // fPendingRequests.clear();
        // fPendingRequests.add(new Request(text));
        // return;
        // }

        fDelegate.set(text);
    }

    /*
     * @see org.eclipse.jface.text.ILineTracker#replace(int, int, java.lang.String)
     */
    public void replace(int offset, int length, String text) throws BadLocationException {
        // if (hasActiveRewriteSession())
        // {
        // fPendingRequests.add(new Request(offset, length, text));
        // return;
        // }

        checkImplementation();

        fDelegate.replace(offset, length, text);
    }

    /**
     * Converts the implementation to be a {@link TreeLineTracker} if it isn't yet.
     *
     * @since 3.2
     */
    private void checkImplementation() {
        if (fNeedsConversion) {
            fNeedsConversion = false;
            fDelegate = new TreeLineTracker((ListLineTracker)fDelegate) {
                protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
                    return AbstractLineTracker.this.nextDelimiterInfo(text, offset);
                }

                public String[] getLegalLineDelimiters() {
                    return AbstractLineTracker.this.getLegalLineDelimiters();
                }
            };
        }
    }

    /**
     * Returns the information about the first delimiter found in the given text starting at the given offset.
     *
     * @param text
     *         the text to be searched
     * @param offset
     *         the offset in the given text
     * @return the information of the first found delimiter or <code>null</code>
     */
    protected abstract DelimiterInfo nextDelimiterInfo(String text, int offset);

    // /*
    // * @see org.eclipse.jface.text.ILineTrackerExtension#startRewriteSession(org.eclipse.jface.text.DocumentRewriteSession)
    // * @since 3.1
    // */
    // public final void startRewriteSession(DocumentRewriteSession session) {
    // if (fActiveRewriteSession != null)
    // throw new IllegalStateException();
    // fActiveRewriteSession= session;
    // fPendingRequests= new ArrayList(20);
    // }
    //
    // /*
    // * @see org.eclipse.jface.text.ILineTrackerExtension#stopRewriteSession(org.eclipse.jface.text.DocumentRewriteSession,
    // java.lang.String)
    // * @since 3.1
    // */
    // public final void stopRewriteSession(DocumentRewriteSession session, String text) {
    // if (fActiveRewriteSession == session) {
    // fActiveRewriteSession= null;
    // fPendingRequests= null;
    // set(text);
    // }
    // }

    // /**
    // * Tells whether there's an active rewrite session.
    // *
    // * @return <code>true</code> if there is an active rewrite session, <code>false</code>
    // * otherwise
    // * @since 3.1
    // */
    // protected final boolean hasActiveRewriteSession() {
    // return fActiveRewriteSession != null;
    // }

    /**
     * Flushes the active rewrite session.
     *
     * @throws BadLocationException
     *         in case the recorded requests cannot be processed correctly
     * @since 3.1
     */
    protected final void flushRewriteSession() throws BadLocationException {
        Iterator<Request> e = fPendingRequests.iterator();

        fPendingRequests = null;

        while (e.hasNext()) {
            Request request = (Request)e.next();
            if (request.isReplaceRequest())
                replace(request.offset, request.length, request.text);
            else
                set(request.text);
        }
    }

    // /**
    // * Checks the presence of a rewrite session and flushes it.
    // *
    // * @throws BadLocationException in case flushing does not succeed
    // * @since 3.1
    // */
    // protected final void checkRewriteSession() throws BadLocationException
    // {
    // flushRewriteSession();
    // }
}
