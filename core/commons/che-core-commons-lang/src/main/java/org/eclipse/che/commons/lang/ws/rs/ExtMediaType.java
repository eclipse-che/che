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
package org.eclipse.che.commons.lang.ws.rs;

import javax.ws.rs.core.MediaType;

/**
 * Extended media type.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public interface ExtMediaType {
    /**
     * A {@code String} constant representing "{@value #APPLICATION_ZIP}" media type.
     */
    public final static String APPLICATION_ZIP = "application/zip";
    /**
     * A {@link MediaType} constant representing "{@value #APPLICATION_ZIP}" media type.
     */
    public final static MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");
    /**
     * A {@code String} constant representing "{@value #APPLICATION_X_TAR}" media type.
     */
    String APPLICATION_X_TAR = "application/x-tar";
    /**
     * A {@link MediaType} constant representing "{@value #APPLICATION_X_TAR}" media type.
     */
    MediaType APPLICATION_X_TAR_TYPE = new MediaType("application", "x-tar");
}
