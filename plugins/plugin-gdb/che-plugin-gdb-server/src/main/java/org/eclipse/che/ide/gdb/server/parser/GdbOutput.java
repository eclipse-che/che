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
package org.eclipse.che.ide.gdb.server.parser;

/**
 * Wrapper for GDB output.
 *
 * @author Anatoliy Bazko
 */
public class GdbOutput {

    private final String output;

    private GdbOutput(String output) {this.output = output;}

    public static GdbOutput of(String output) {
        return new GdbOutput(output);
    }

    public String getOutput() {
        return output;
    }
}
