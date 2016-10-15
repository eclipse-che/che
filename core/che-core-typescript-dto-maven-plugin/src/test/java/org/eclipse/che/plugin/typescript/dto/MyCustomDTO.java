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
package org.eclipse.che.plugin.typescript.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * @author Florent Benoit
 */
@DTO
public interface MyCustomDTO {

    String getName();
    void setName(String name);
    MyCustomDTO withName(String name);

    MyOtherDTO getConfig();
    MyCustomDTO withConfig(MyOtherDTO otherDTO);
    void setConfig(MyOtherDTO otherDTO);

    void setStatus(Status status);
    MyCustomDTO withStatus(Status status);
    Status getStatus();

    Map<String, MyOtherDTO> getCustomMap();
    void setCustomMap(Map<String, MyOtherDTO> map);
    MyCustomDTO withCustomMap(Map<String, MyOtherDTO> map);

}
