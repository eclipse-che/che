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
package org.eclipse.che.api.core.model.workspace.compose;

import java.util.Map;

/**
 * Description of docker compose services file.
 *
 * @author Alexander Garagatyi
 */
public interface ComposeEnvironment {
    /**
     * Version of compose syntax.
     */
    String getVersion();

    /**
     * Mapping of compose services names to services configuration.
     */
    Map<String, ? extends ComposeService> getServices();
}
