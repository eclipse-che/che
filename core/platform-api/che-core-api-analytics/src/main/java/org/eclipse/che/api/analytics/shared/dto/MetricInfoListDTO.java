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
package org.eclipse.che.api.analytics.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Simple interface to contain list of metric infos.
 *
 * @author <a href="mailto:dkuleshov@codenvy.com">Dmitry Kuleshov</a>
 */
@DTO
public interface MetricInfoListDTO {

    List<MetricInfoDTO> getMetrics();

    void setMetrics(List<MetricInfoDTO> metricInfoDTOs);
}

