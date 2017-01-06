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

import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * Data transfer object (DTO) for generate project.
 *
 * @author Vladyslav Zhukovskiy
 */
@DTO
@ApiModel(description = "Generate new project")
public interface GeneratorDescription {

    /** Get options needed for generator. */
    @ApiModelProperty("Options needed for generator")
    Map<String, String> getOptions();

    /** Set options needed for generator. */
    void setOptions(Map<String, String> options);

    GeneratorDescription withOptions(Map<String, String> options);
}
