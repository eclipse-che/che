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
package org.eclipse.che.plugin.maven.generator.archetype;

import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;

/**
 *
 * @author Vitalii Parfonov
 */
public class ArchetypeOutputImpl implements ArchetypeOutput {


    private String output;

    private State state;

    public ArchetypeOutputImpl(String output, State state) {
        this.output = output;
        this.state = state;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public State getState() {
        return state;
    }
}
