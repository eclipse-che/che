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
package org.eclipse.che.api.core.model.machine;

import java.util.Map;

/**
 * Command that can be used to create {@link Process} in a machine
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 */
public interface Command {

    /**
     * Returns command name (i.e. 'start tomcat')
     * <p>
     * The name should be unique per user in one workspace,
     * which means that user may create only one command with the same name in the same workspace
     */
    String getName();

    /**
     * Returns command line (i.e. 'mvn clean install') which is going to be executed
     * <p>
     * Serves as a base for {@link Process} creation.
     */
    String getCommandLine();

    /**
     * Returns command type (i.e. 'maven')
     */
    String getType();

    /**
     * Returns attributes related to this command.
     *
     * @return command attributes
     */
    Map<String, String> getAttributes();
}
