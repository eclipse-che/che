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
package org.eclipse.che.api.agent.server;

import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
public interface Agent {
    Map<String, String> getEnvVariables();

    List<String> getVolumes();

    List<String> getPorts();

    List<String> getDependencies();

    String getScript();
}
