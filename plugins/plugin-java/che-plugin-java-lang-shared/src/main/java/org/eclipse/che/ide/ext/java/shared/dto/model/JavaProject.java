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
package org.eclipse.che.ide.ext.java.shared.dto.model;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * DTO represents Java project in terms of JavaModel
 *
 * @author Evgen Vidolob
 */
@DTO
public interface JavaProject {

    /**
     * Project workspace path
     * @return the path
     */
    String getPath();

    void setPath(String path);

    /**
     * Project name;
     * @return name of the project
     */
    String getName();

    void setName(String name);

    /**
     * Get all package fragment roots from this project
     * @return list of the package fragment roots
     */
    List<PackageFragmentRoot> getPackageFragmentRoots();

    void setPackageFragmentRoots(List<PackageFragmentRoot> roots);

}
