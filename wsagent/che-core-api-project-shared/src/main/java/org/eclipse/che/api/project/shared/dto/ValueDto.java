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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Data transfer object (DTO) for Value
 *
 * @author gazarenkov
 */
@DTO
public interface ValueDto extends Value {

    @Override
    String getString();

    @Override
    List<String> getList();

    ValueDto withList(List<String> list);
}
