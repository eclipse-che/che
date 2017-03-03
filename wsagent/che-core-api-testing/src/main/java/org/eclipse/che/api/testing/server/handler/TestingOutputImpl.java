/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial commit
 *******************************************************************************/
package org.eclipse.che.api.testing.server.handler;

import org.eclipse.che.api.testing.shared.TestingOutput;

/**
 * @author David Festal
 */
public class TestingOutputImpl implements TestingOutput {

    public TestingOutputImpl(String output, LineType lineType) {
        this.output = output;
        this.lineType = lineType;
    }

    private String   output;
    private LineType lineType;

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public LineType getState() {
        return lineType;
    }
}
