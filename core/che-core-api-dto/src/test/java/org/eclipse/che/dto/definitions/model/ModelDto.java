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
package org.eclipse.che.dto.definitions.model;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Test DTO extension for {@link Model}
 *
 * @author Eugene Voevodin
 */
@DTO
public interface ModelDto extends Model {

    @Override
    List<ModelComponentDto> getComponents();

    void setComponents(List<ModelComponentDto> components);

    ModelDto withComponents(List<ModelComponentDto> components);

    @Override
    ModelComponentDto getPrimary();

    void setPrimary(ModelComponentDto primary);

    ModelDto withPrimary(ModelComponentDto primary);
}

