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
package org.eclipse.che.workspace.infrastructure.openshift;

/**
 * Constants for OpenShift implementation of spi.
 *
 * @author Sergii Leshchenko
 */
public final class Constants {

    public static final String CHE_POD_NAME_LABEL = "che.pod.name";

    public static final String CHE_SERVER_NAME_ANNOTATION     = "che.server.name";
    public static final String CHE_SERVER_PROTOCOL_ANNOTATION = "che.server.protocol";
    public static final String CHE_SERVER_PATH_ANNOTATION     = "che.server.path";

    private Constants() {}
}
