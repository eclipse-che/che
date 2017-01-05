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
package org.eclipse.che.dto.definitions;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

/**
 * Makes use of the JsonFieldName annotation
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface DtoWithFieldNames {

    public String THENAME_FIELD = "the name";
    public String THEDEFAULT_FIELD = "default";

    @JsonFieldName(THENAME_FIELD)
    String getTheName();

    void setTheName(String v);

    DtoWithFieldNames withTheName(String v);

    @JsonFieldName(THEDEFAULT_FIELD)
    String getTheDefault();

    void setTheDefault(String v);

    DtoWithFieldNames withTheDefault(String v);
}
