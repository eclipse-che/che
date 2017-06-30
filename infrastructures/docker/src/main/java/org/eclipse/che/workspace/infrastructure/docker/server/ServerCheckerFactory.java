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
package org.eclipse.che.workspace.infrastructure.docker.server;

import java.net.URL;
import java.util.Timer;

/**
 * Creates {@link HttpConnectionServerChecker} for server readiness checking.
 *
 * @author Alexander Garagatyi
 */
public interface ServerCheckerFactory {
    HttpConnectionServerChecker httpChecker(URL url, String machineName, String serverRef, Timer timer);
}
