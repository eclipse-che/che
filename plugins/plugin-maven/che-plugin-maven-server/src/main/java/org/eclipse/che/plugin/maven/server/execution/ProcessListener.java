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

import java.util.EventListener;

/**
 * Listener for {@link ProcessHandler}
 *
 * @author Evgen Vidolob
 */
public interface ProcessListener extends EventListener {

    void onStart(ProcessEvent event);

    void onText(ProcessEvent event, ProcessOutputType outputType);

    void onProcessTerminated(ProcessEvent event);

    void onProcessWillTerminate(ProcessEvent event);
}
