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

import org.eclipse.che.api.analytics.AnalyticsService;
import org.eclipse.che.api.analytics.Constants;
import org.eclipse.che.api.analytics.MetricHandler;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoListDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueListDTO;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.server.JsonStringMapImpl;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Implementation provides means to perform remote REST requests to receive analytics data from remote rest service.
 *
 * @author Dmitry Kuleshov
 * @author Anatoliy Bazko
 */
public class RemoteMetricHandler implements MetricHandler {

    private static final String PROXY_URL = "analytics.api.proxy_url";

    private String proxyUrl;

    public RemoteMetricHandler(Properties properties) {
        this.proxyUrl = properties.getProperty(PROXY_URL);
        if (this.proxyUrl == null) {
            throw new IllegalArgumentException("Not defined mandatory property " + PROXY_URL);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetricValueDTO getValue(String metricName,
                                   Map<String, String> executionContext,
                                   UriInfo uriInfo) {
        String proxyUrl = getProxyURL("getValue", metricName);
        try {
            List<Pair<String, String>> pairs = mapToParisList(executionContext);
            return request(MetricValueDTO.class,
                           proxyUrl,
                           HttpMethod.GET,
                           null,
                           pairs.toArray(new Pair[pairs.size()]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetricValueListDTO getListValues(String metricName,
                                            List<Map<String, String>> parameters,
                                            Map<String, String> executionContext,
                                            UriInfo uriInfo) throws Exception {
        String proxyUrl = getProxyURL("getListValues", metricName);
        try {
            for (int i = 0; i < parameters.size(); i++) {
                parameters.set(i, new JsonStringMapImpl<>(parameters.get(i)));
            }

            List<Pair<String, String>> pairs = mapToParisList(executionContext);
            return request(MetricValueListDTO.class,
                           proxyUrl,
                           HttpMethod.POST,
                           new JsonArrayImpl<>(parameters),
                           pairs.toArray(new Pair[pairs.size()]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetricValueDTO getValueByJson(String metricName,
                                         Map<String, String> parameters,
                                         Map<String, String> executionContext,
                                         UriInfo uriInfo) throws Exception {
        String proxyUrl = getProxyURL("getValueByJson", metricName);
        try {
            List<Pair<String, String>> pairs = mapToParisList(executionContext);
            return request(MetricValueDTO.class,
                           proxyUrl,
                           HttpMethod.POST,
                           parameters,
                           pairs.toArray(new Pair[pairs.size()]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetricValueDTO getPublicValue(String metricName,
                                         Map<String, String> executionContext,
                                         UriInfo uriInfo) {
        String proxyUrl = getProxyURL("getPublicValue", metricName);
        try {
            List<Pair<String, String>> pairs = mapToParisList(executionContext);
            return request(MetricValueDTO.class,
                           proxyUrl,
                           HttpMethod.GET,
                           null,
                           pairs.toArray(new Pair[pairs.size()]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetricValueListDTO getUserValues(List<String> metricNames,
                                            Map<String, String> executionContext,
                                            UriInfo uriInfo) {
        String proxyUrl = getProxyURL("getUserValues", "");
        try {
            List<Pair<String, String>> pairs = mapToParisList(executionContext);
            return request(MetricValueListDTO.class,
                           proxyUrl,
                           HttpMethod.POST,
                           metricNames,
                           pairs.toArray(new Pair[pairs.size()]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetricInfoDTO getInfo(String metricName, UriInfo uriInfo) {
        String proxyUrl = getProxyURL("getInfo", metricName);
        try {
            List<Pair<String, String>> pairs = mapToParisList(Collections.<String, String>emptyMap());
            MetricInfoDTO metricInfoDTO = request(MetricInfoDTO.class,
                                                  proxyUrl,
                                                  HttpMethod.GET,
                                                  null,
                                                  pairs.toArray(new Pair[pairs.size()]));
            updateLinks(uriInfo, metricInfoDTO);
            return metricInfoDTO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetricInfoListDTO getAllInfo(UriInfo uriInfo) {
        String proxyUrl = getProxyURL("getAllInfo", "");
        try {
            List<Pair<String, String>> pairs = mapToParisList(Collections.<String, String>emptyMap());
            @SuppressWarnings("unchecked")
            MetricInfoListDTO metricInfoListDTO = request(MetricInfoListDTO.class,
                                                          proxyUrl,
                                                          HttpMethod.GET,
                                                          null,
                                                          pairs.toArray(new Pair[pairs.size()]));
            updateLinks(uriInfo, metricInfoListDTO);
            return metricInfoListDTO;
        } catch (Exception e) {
            throw new RuntimeException(
                    "We have received an error code from the server. For some reason, " +
                    "we are unable to generate the list of metrics.");
        }
    }

    private void updateLinks(UriInfo uriInfo, MetricInfoDTO metricInfoDTO) {
        metricInfoDTO.setLinks(getLinks(metricInfoDTO.getName(), uriInfo));
    }

    private void updateLinks(UriInfo uriInfo, MetricInfoListDTO metricInfoListDTO) {
        for (MetricInfoDTO metricInfoDTO : metricInfoListDTO.getMetrics()) {
            updateLinks(uriInfo, metricInfoDTO);
        }
    }

    private List<Pair<String, String>> mapToParisList(Map<String, String> executionContext) {
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : executionContext.entrySet()) {
            pairs.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        putAuthenticationToken(pairs);
        return pairs;
    }

    private void putAuthenticationToken(List<Pair<String, String>> pairs) {
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null) {
            String authToken = user.getToken();
            if (authToken != null) {
                pairs.add(new Pair<>("token", authToken));
            }
        }
    }

    private String getProxyURL(String methodName, String metricName) {
        String path = getMethod(methodName).getAnnotation(Path.class).value();
        return proxyUrl + path.replace("{name}", metricName);
    }

    private <DTO> DTO request(Class<DTO> dtoInterface,
                              String proxyUrl,
                              String method,
                              Object body,
                              Pair<String, ?>... parameters) throws IOException {

        if (parameters != null && parameters.length > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(proxyUrl);
            sb.append('?');
            for (int i = 0, l = parameters.length; i < l; i++) {
                String name = URLEncoder.encode(parameters[i].first, "UTF-8");
                String value = parameters[i].second == null ? null : URLEncoder
                        .encode(String.valueOf(parameters[i].second), "UTF-8");
                if (i > 0) {
                    sb.append('&');
                }
                sb.append(name);
                if (value != null) {
                    sb.append('=');
                    sb.append(value);
                }
            }
            proxyUrl = sb.toString();
        }
        final HttpURLConnection conn = (HttpURLConnection)new URL(proxyUrl).openConnection();
        conn.setConnectTimeout(30 * 1000);
        try {
            conn.setRequestMethod(method);
            if (body != null) {
                conn.addRequestProperty("content-type", MediaType.APPLICATION_JSON);
                conn.setDoOutput(true);
                try (OutputStream output = conn.getOutputStream()) {
                    output.write(DtoFactory.getInstance().toJson(body).getBytes());
                }
            }

            final int responseCode = conn.getResponseCode();

            if ((responseCode / 100) != 2) {
                InputStream in = conn.getErrorStream();
                if (in == null) {
                    in = conn.getInputStream();
                }
                throw new IOException(IoUtil.readAndCloseQuietly(in));
            }
            final String contentType = conn.getContentType();
            if (!contentType.startsWith(MediaType.APPLICATION_JSON)) {
                throw new IOException("Unsupported type of response from remote server. ");
            }
            try (InputStream input = conn.getInputStream()) {
                return DtoFactory.getInstance().createDtoFromJson(input, dtoInterface);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static List<Link> getLinks(String metricName, UriInfo uriInfo) {
        final UriBuilder servicePathBuilder = uriInfo.getBaseUriBuilder();
        List<Link> links = new ArrayList<>();

        final Link statusLink = DtoFactory.getInstance().createDto(Link.class);
        statusLink.setRel(Constants.LINK_REL_GET_METRIC_VALUE);
        statusLink.setHref(servicePathBuilder
                                   .clone()
                                   .path("analytics")
                                   .path(getMethod("getValue"))
                                   .build(metricName, "name")
                                   .toString());
        statusLink.setMethod(HttpMethod.GET);
        statusLink.setProduces(MediaType.APPLICATION_JSON);
        links.add(statusLink);
        return links;
    }

    private static Method getMethod(String name) {
        for (Method analyticsMethod : AnalyticsService.class.getMethods()) {
            if (analyticsMethod.getName().equals(name)) {
                return analyticsMethod;
            }
        }

        throw new RuntimeException("No '" + name + "' method found in " + AnalyticsService.class + "class");
    }
}
