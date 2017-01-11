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
package org.eclipse.che.plugin.maven.server.execution;

import java.util.EventObject;

/**
 * @author Evgen Vidolob
 */
public class ProcessEvent extends EventObject {

    private String text;
    private int    exitCode;

    /**
     * Constructs a prototypical Event.
     *
     * @param    source    The object on which the Event initially occurred.
     * @exception IllegalArgumentException  if source is null.
     */
    public ProcessEvent(ProcessHandler source) {
        super(source);
    }

    public ProcessEvent(ProcessHandler source, String text) {
        super(source);
        this.text = text;
    }

    public ProcessEvent(ProcessHandler source, String text, int exitCode) {
        super(source);
        this.text = text;
        this.exitCode = exitCode;
    }

    public ProcessEvent(ProcessHandler source, int exitCode) {
        super(source);
        this.exitCode = exitCode;
    }

    public String getText() {
        return text;
    }

    public int getExitCode() {
        return exitCode;
    }

    public ProcessHandler getProcessHandler() {
        return (ProcessHandler)source;
    }
}
