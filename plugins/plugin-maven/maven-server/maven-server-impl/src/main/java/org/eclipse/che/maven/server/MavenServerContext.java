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
package org.eclipse.che.maven.server;

/**
 *Maven server context, contains some top level configured components.
 *
 * @author Evgen Vidolob
 */
public class MavenServerContext {
    private static MavenServerLogger           logger;
    private static MavenServerDownloadListener listener;

    public static MavenServerLogger getLogger() {
        return logger;
    }

    public static MavenServerDownloadListener getListener() {
        return listener;
    }

    public static void setLoggerAndListener(MavenServerLogger logger, MavenServerDownloadListener listener) {
        MavenServerContext.logger = logger;
        MavenServerContext.listener = listener;
    }
}
