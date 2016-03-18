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
package org.eclipse.che.api.vfs.server;

/**
 * Check is it allowed to use {@link VirtualFileSystem} for client that makes specified HttpServletRequest. Implementation of this
 * interface
 * may check any parameter of HTTP request and throw {@link RuntimeException} if tested parameter has unexpected value.
 *
 * @author andrew00x
 */
public interface RequestValidator {
    void validate(javax.servlet.http.HttpServletRequest request) throws RuntimeException;
}
