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

import io.swagger.annotations.ApiModelProperty;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 *
 * @author andrew00x
 */
@DTO
public interface SourceEstimation {

    /** Gets unique id of type of project. */
    @ApiModelProperty(value = "type ID", position = 1)
    String getType();

    SourceEstimation withType(String type);


    /** Gets attributes of this project. */
    @ApiModelProperty(value = "Project attributes", position = 2)
    Map<String, List<String>> getAttributes();


    SourceEstimation withAttributes(Map<String, List<String>> attributes);

    @ApiModelProperty(value = "if matched", position = 3)
    boolean isMatched();

    SourceEstimation withMatched(boolean matched);


    /** Gets resolution  - the reason that source code not matches project type requirements. */
    @ApiModelProperty(value = "Resolution", position = 4)
    String getResolution();

    SourceEstimation withResolution(String resolution);
}
