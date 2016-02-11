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
package org.eclipse.che.api.analytics;

import org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoListDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueListDTO;

import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 * Defines methods to interact with analytic system metrics to get needed metric values or info. In order to proper
 * instantiate an
 * implementation, it must either have default constructor or
 * constructor with single parameter {@link java.util.Properties}.
 *
 * @author <a href="mailto:dkuleshov@codenvy.com">Dmitry Kuleshov</a>
 */
public interface MetricHandler {
    public MetricValueDTO getValue(String metricName,
                                   Map<String, String> metricContext,
                                   UriInfo uriInfo) throws Exception;

    public MetricValueListDTO getListValues(String metricName,
                                            List<Map<String, String>> parameters,
                                            Map<String, String> context,
                                            UriInfo uriInfo) throws Exception;

    public MetricValueDTO getValueByJson(String metricName,
                                         Map<String, String> parameters,
                                         Map<String, String> metricContext,
                                         UriInfo uriInfo) throws Exception;

    public MetricValueDTO getPublicValue(String metricName,
                                         Map<String, String> metricContext,
                                         UriInfo uriInfo) throws Exception;

    public MetricValueListDTO getUserValues(List<String> metricNames,
                                            Map<String, String> metricContext,
                                            UriInfo uriInfo) throws Exception;

    public MetricInfoDTO getInfo(String metricName, UriInfo uriInfo) throws Exception;

    public MetricInfoListDTO getAllInfo(UriInfo uriInfo) throws Exception;
}
