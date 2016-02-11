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
package org.eclipse.che.api.analytics.impl;

import org.eclipse.che.api.analytics.MetricHandler;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoListDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueListDTO;
import org.eclipse.che.dto.server.DtoFactory;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Simple dummy extension to avoid huge operations. Instead default values returned.
 *
 * @author <a href="mailto:dkuleshov@codenvy.com">Dmitry Kuleshov</a>
 */
public class DummyMetricHandler implements MetricHandler {

    @Override
    public MetricValueDTO getValue(String metricName,
                                   Map<String, String> metricContext,
                                   UriInfo uriInfo) {
        return createDummyMetricValueDTO(metricName);
    }

    @Override
    public MetricValueListDTO getListValues(String metricName,
                                            List<Map<String, String>> parameters,
                                            Map<String, String> context,
                                            UriInfo uriInfo) throws Exception {
        MetricValueListDTO metricValueListDTO = DtoFactory.getInstance().createDto(MetricValueListDTO.class);

        List<MetricValueDTO> metricValues = new ArrayList<>();
        metricValues.add(createDummyMetricValueDTO(metricName));
        metricValues.add(createDummyMetricValueDTO(metricName));
        metricValues.add(createDummyMetricValueDTO(metricName));

        metricValueListDTO.setMetrics(metricValues);
        return metricValueListDTO;
    }

    @Override
    public MetricValueDTO getValueByJson(String metricName,
                                         Map<String, String> parameters,
                                         Map<String, String> metricContext,
                                         UriInfo uriInfo) throws Exception {
        return createDummyMetricValueDTO(metricName);
    }

    @Override
    public MetricValueDTO getPublicValue(String metricName,
                                         Map<String, String> metricContext,
                                         UriInfo uriInfo) {
        return createDummyMetricValueDTO(metricName);
    }

    @Override
    public MetricValueListDTO getUserValues(List<String> metricNames,
                                            Map<String, String> metricContext,
                                            UriInfo uriInfo) {
        MetricValueListDTO metricValueListDTO = DtoFactory.getInstance().createDto(MetricValueListDTO.class);
        List<MetricValueDTO> metricValues = new ArrayList<>();
        for (String metricName : metricNames) {
            metricValues.add(createDummyMetricValueDTO(metricName));
        }
        metricValueListDTO.setMetrics(metricValues);
        return metricValueListDTO;
    }

    @Override
    public MetricInfoDTO getInfo(String metricName, UriInfo uriInfo) {
        return createDummyMetricInfoDto(metricName);
    }

    @Override
    public MetricInfoListDTO getAllInfo(UriInfo uriInfo) {
        return createDummyMetricInfoListDTO();
    }

    private MetricInfoDTO createDummyMetricInfoDto(String metricName) {
        MetricInfoDTO metricInfoDTO = DtoFactory.getInstance().createDto(MetricInfoDTO.class);
        metricInfoDTO.setName(metricName);
        metricInfoDTO.setDescription(metricName + " description");

        return metricInfoDTO;
    }

    private MetricValueDTO createDummyMetricValueDTO(String metricName) {
        MetricValueDTO metricValueDTO = DtoFactory.getInstance().createDto(MetricValueDTO.class);
        metricValueDTO.setName(metricName);
        if ("FACTORY_USED".equalsIgnoreCase(metricName)) {
            metricValueDTO.setValue(String.valueOf(new Random().nextInt(256)));
        } else {
            metricValueDTO.setValue(metricName + " value");
        }

        return metricValueDTO;
    }

    private MetricInfoListDTO createDummyMetricInfoListDTO() {
        MetricInfoListDTO metricInfoListDTO = DtoFactory.getInstance().createDto(MetricInfoListDTO.class);
        int counter = 10;
        List<MetricInfoDTO> metricInfoDTOs = new ArrayList<>();

        while (counter-- > 0) {
            metricInfoDTOs.add(createDummyMetricInfoDto("Metric " + counter));
        }
        metricInfoListDTO.setMetrics(metricInfoDTOs);
        return metricInfoListDTO;
    }
}
