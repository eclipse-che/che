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

import java.util.List;

/**
 * Makes use of the 'any' (JsonElement) property type feature.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface DtoWithAny {
    int getId();

    Object getStuff();

    void setStuff(Object stuff);

    DtoWithAny withStuff(Object stuff);

    List<Object> getObjects();

    void setObjects(List<Object> objects);

    DtoWithAny withObjects(List<Object> objects);
}
