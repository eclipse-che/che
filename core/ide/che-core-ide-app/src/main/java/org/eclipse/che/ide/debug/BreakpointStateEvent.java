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
package org.eclipse.che.ide.debug;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the fact that breakpoint changed state.
 *
 * @author Anatoliy Bazko
 */
public class BreakpointStateEvent extends GwtEvent<BreakpointStateEventHandler> {

    public static Type<BreakpointStateEventHandler> TYPE = new Type<>();

    private final BreakpointState state;
    private final String          filePath;
    private final int             lineNumber;

    /**
     * Creates new {@link BreakpointStateEvent}.
     *
     * @param state
     *         breakpoint state
     * @param filePath
     *         file where breakpoint changed its state
     * @param lineNumber
     *         the line number in the file
     */
    public BreakpointStateEvent(BreakpointState state, String filePath, int lineNumber) {
        this.state = state;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    /**
     * Getter for {@link #state}
     */
    public BreakpointState getState() {
        return state;
    }


    /**
     * Getter for {@link #filePath}
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Getter for {@link #lineNumber}
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type<BreakpointStateEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dispatch(BreakpointStateEventHandler handler) {
        handler.onStateChanged(this);
    }

    public enum BreakpointState {
        ACTIVE,
        INACTIVE
    }
}
