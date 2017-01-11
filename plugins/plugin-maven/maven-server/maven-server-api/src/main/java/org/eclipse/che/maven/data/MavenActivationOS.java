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
 * Data class for org.apache.maven.model.ActivationOS
 *
 * @author Evgen Vidolob
 */
public class MavenActivationOS implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String family;
    private final String arch;
    private final String version;

    public MavenActivationOS(String name, String family, String arch, String version) {
        this.name = name;
        this.family = family;
        this.arch = arch;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getFamily() {
        return family;
    }

    public String getArch() {
        return arch;
    }

    public String getVersion() {
        return version;
    }
}
