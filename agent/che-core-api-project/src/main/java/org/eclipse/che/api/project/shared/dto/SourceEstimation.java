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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * Data transfer object (DTO) for org.eclipse.che.api.project.shared.ProjectDescription.
 *
 * @author andrew00x
 */
@DTO
public interface SourceEstimation extends Hyperlinks {


    /** Gets unique id of type of project. */
    @ApiModelProperty(value = "type ID", position = 1)
    String getType();

    /** Sets unique id of type of project. */
    void setType(String type);

    SourceEstimation withType(String type);

    //

    /** Gets attributes of this project. */
    @ApiModelProperty(value = "Project attributes", position = 2)
    Map<String, List<String>> getAttributes();

    /** Sets attributes of this project. */
    void setAttributes(Map<String, List<String>> attributes);

    SourceEstimation withAttributes(Map<String, List<String>> attributes);

    SourceEstimation withLinks(List<Link> links);
}
