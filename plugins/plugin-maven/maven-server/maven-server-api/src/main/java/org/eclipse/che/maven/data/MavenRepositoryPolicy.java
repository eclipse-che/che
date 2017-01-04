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
 * Data class for org.apache.maven.model.RepositoryPolicy
 *
 * @author Evgen Vidolob
 */
public class MavenRepositoryPolicy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean enabled;
    private final String  updatePolicy;
    private final String  checksumPolicy;

    public MavenRepositoryPolicy(boolean enabled, String updatePolicy, String checksumPolicy) {
        this.enabled = enabled;
        this.updatePolicy = updatePolicy;
        this.checksumPolicy = checksumPolicy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public String getChecksumPolicy() {
        return checksumPolicy;
    }
}
