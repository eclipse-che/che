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
package org.eclipse.che.api.core.rest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author andrew00x
 */
@DTO
public interface ApiInfo {
    String getSpecificationVendor();

    ApiInfo withSpecificationVendor(String specificationVendor);

    void setSpecificationVendor(String specificationVendor);

    String getImplementationVendor();

    ApiInfo withImplementationVendor(String implementationVendor);

    void setImplementationVendor(String implementationVendor);

    String getSpecificationTitle();

    ApiInfo withSpecificationTitle(String specificationTitle);

    void setSpecificationTitle(String specificationTitle);

    String getSpecificationVersion();

    ApiInfo withSpecificationVersion(String specificationVersion);

    void setSpecificationVersion(String specificationVersion);

    String getImplementationVersion();

    ApiInfo withImplementationVersion(String implementationVersion);

    void setImplementationVersion(String implementationVersion);

    String getScmRevision();

    ApiInfo withScmRevision(String scmRevision);

    void setScmRevision(String scmRevision);

    String getIdeVersion();

    ApiInfo withIdeVersion(String ideVersion);

    void setIdeVersion(String ideVersion);
}
