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
//TODO This is used in wizard/ProjectImporter, find a solution to move it to plugin-svn.
package org.eclipse.che.ide.api.subversion;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Dialog for retrieving credentials for SVN operations.
 *
 * @author Igor Vinokur
 */
public interface SubversionCredentialsDialog {

    /**
     * Returns credentials from dialog.
     *
     * @return {@link Credentials} that contains user name and password
     */
    Promise<Credentials> askCredentials();
}
