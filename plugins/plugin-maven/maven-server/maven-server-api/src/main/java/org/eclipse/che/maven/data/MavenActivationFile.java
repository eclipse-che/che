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
package org.eclipse.che.maven.data;

import java.io.Serializable;

/**
 * Data class for maven file activation.
 *
 * @author Evgen Vidolob
 */
public class MavenActivationFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String exist;
    private final String missing;

    public MavenActivationFile(String exist, String missing) {

        this.exist = exist;
        this.missing = missing;
    }

    public String getExist() {
        return exist;
    }

    public String getMissing() {
        return missing;
    }
}
