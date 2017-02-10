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
package org.eclipse.che.ide.ext.java.client.dependenciesupdater;

import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;

import java.util.LinkedList;
import java.util.List;

/**
 * The class contains information about components which are associated with channel. For each channel we connected we need console
 * for output, output and status if output from server is ended or not.
 *
 * @author Dmitry Shnurenko
 */
public class ChannelParameters {

    private final DefaultOutputConsole console;
    private final LinkedList<String>   output;

    private boolean isOutputComes;

    private ChannelParameters(DefaultOutputConsole console, LinkedList<String> output) {
        this.console = console;
        this.output = output;
        this.isOutputComes = false;
    }

    public static ChannelParameters of(DefaultOutputConsole console, LinkedList<String> output) {
        return new ChannelParameters(console, output);
    }

    public DefaultOutputConsole getConsole() {
        return console;
    }

    public LinkedList<String> getOutput() {
        return output;
    }

    public boolean isOutputEnded() {
        return isOutputComes;
    }

    public void addAllLines(List<String> lines) {
        output.addAll(lines);
    }

    public void outputEnded() {
        isOutputComes = true;
    }
}
