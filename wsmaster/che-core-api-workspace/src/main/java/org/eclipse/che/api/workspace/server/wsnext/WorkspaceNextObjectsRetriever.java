/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.CheFeature;
import org.eclipse.che.api.workspace.server.wsnext.model.CheService;
import org.eclipse.che.api.workspace.server.wsnext.model.CheServiceParameter;
import org.eclipse.che.api.workspace.server.wsnext.model.CheServiceReference;
import org.eclipse.che.api.workspace.server.wsnext.model.Container;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches Workspace.Next objects corresponding to attributes of a workspace.
 *
 * @author Oleksander Garagatyi
 */
@Beta
public class WorkspaceNextObjectsRetriever {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceNextObjectsRetriever.class);
  private static final Pattern PARAMETER_ENV_VAR_VALUE = Pattern.compile("^\\$\\{.+}$");
  private static final String FEATURE_OBJECT_ERROR = "Feature '%s/%s' configuration is invalid. %s";
  private static final String SERVICE_OBJECT_ERROR = "Service '%s/%s' configuration is invalid. %s";

  protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private final UriBuilder featureApi;

  @Inject
  public WorkspaceNextObjectsRetriever(
      @Nullable @Named("che.workspace.feature.api") String featureApi) {
    if (featureApi == null) {
      LOG.info(
          "Workspace.Next is disabled - Feature API endpoint property 'che.workspace.feature.api' is not configured");
      this.featureApi = null;
    } else {
      this.featureApi = UriBuilder.fromUri(featureApi);
    }
  }

  /**
   * Gets Workspace.Next features list from provided workspace attributes, fetches corresponding
   * objects from Feature API and returns list of {@link CheService} needed to provide features
   * functionality in a workspace.
   *
   * <p>This method resolves feature dependencies and parameters, so returned list of {@code
   * CheServices} is ready to be applied to a workspace runtime.
   *
   * @param attributes workspace attributes
   * @throws InfrastructureException when features list contains invalid entries or Workspace.Next
   *     objects retrieval from Feature API fails
   */
  public Collection<CheService> get(Map<String, String> attributes) throws InfrastructureException {
    if (featureApi == null || attributes == null || attributes.isEmpty()) {
      return emptyList();
    }
    String featuresAttribute = attributes.get(Constants.WORKSPACE_NEXT_FEATURES);
    if (isNullOrEmpty(featuresAttribute)) {
      return emptyList();
    }
    String[] features = featuresAttribute.split(" *, *");
    if (features.length == 0) {
      return emptyList();
    }

    Collection<Pair<String, String>> featuresNameVersion = parseFeatures(features);

    return toServices(getFeatures(featuresNameVersion));
  }

  private Collection<Pair<String, String>> parseFeatures(String[] features)
      throws InfrastructureException {
    Map<String, Pair<String, String>> featuresNameVersion = new HashMap<>();
    for (String feature : features) {
      String[] featureAndVersion = feature.split("/");
      if (featureAndVersion.length != 2
          || featureAndVersion[0].isEmpty()
          || featureAndVersion[1].isEmpty()) {
        throw new InfrastructureException(
            "Features format is illegal. Problematic feature entry:" + feature);
      }
      String key = featureAndVersion[0] + '/' + featureAndVersion[1];
      if (featuresNameVersion.containsKey(key)) {
        throw new InfrastructureException(
            format("Invalid Workspace.Next configuration: feature %s is duplicated", key));
      }
      featuresNameVersion.put(key, Pair.of(featureAndVersion[0], featureAndVersion[1]));
    }
    return featuresNameVersion.values();
  }

  private Collection<CheFeature> getFeatures(Collection<Pair<String, String>> featureAndVersion)
      throws InfrastructureException {
    List<CheFeature> features = new ArrayList<>();
    for (Pair<String, String> nameVersion : featureAndVersion) {
      features.add(getFeature(nameVersion.first, nameVersion.second));
    }

    return features;
  }

  private CheFeature getFeature(String featureName, String featureVersion)
      throws InfrastructureException {
    try {
      URI getFeatureURI =
          featureApi.clone().path("feature").path(featureName).path(featureVersion).build();

      CheFeature feature = getBody(getFeatureURI, CheFeature.class);
      validateFeature(feature, featureName, featureVersion);
      return feature;
    } catch (IllegalArgumentException | UriBuilderException e) {
      throw new InternalInfrastructureException(
          format("Feature %s/%s retrieval failed", featureName, featureVersion));
    } catch (IOException e) {
      throw new InfrastructureException(
          format(
              "Error occurred on retrieval of feature %s. Error: %s",
              featureName + '/' + featureVersion, e.getMessage()));
    }
  }

  private Collection<CheService> toServices(Collection<CheFeature> features)
      throws InfrastructureException {
    Collection<CheService> services = getServices(features);
    Map<String, List<String>> parameters = getServicesParameters(features);

    for (CheService service : services) {
      String serviceName = service.getMetadata().getName();
      String serviceVersion = service.getSpec().getVersion();
      String serviceKey = serviceName + '/' + serviceVersion;

      // for now we match whole env variable value against '${<parameter name>}'
      service
          .getSpec()
          .getContainers()
          .stream()
          .flatMap(container -> container.getEnv().stream())
          .filter(this::isParameter)
          .forEach(
              envVar -> {
                String parameterKey = serviceKey + '/' + envVar.getValue();
                List<String> remove = parameters.remove(parameterKey);
                String envVarValue;
                if (remove != null) {
                  envVarValue = String.join(",", remove);
                } else {
                  envVarValue = "";
                }
                envVar.setValue(envVarValue);
              });
    }

    if (!parameters.isEmpty()) {
      throw new InfrastructureException(
          "Parameters not supported by services found: " + parameters.keySet());
    }

    return services;
  }

  private Collection<CheService> getServices(Collection<CheFeature> features)
      throws InfrastructureException {
    Map<String, CheService> services = new HashMap<>();
    for (CheFeature feature : features) {
      for (CheServiceReference serviceReference : feature.getSpec().getServices()) {
        String serviceName = serviceReference.getName();
        String serviceVersion = serviceReference.getVersion();
        String key = serviceName + '/' + serviceVersion;
        if (!services.containsKey(key)) {
          CheService service = getService(serviceName, serviceVersion);
          services.put(key, service);
        }
      }
    }
    return services.values();
  }

  private boolean isParameter(EnvVar envVar) {
    return PARAMETER_ENV_VAR_VALUE.matcher(envVar.getValue()).matches();
  }

  /**
   * Returns map where key is concatenation of service name, version and parameter name separated by
   * slash symbol. < serviceName/serviceVersion/${parameter} > to < parameterValue >
   */
  private Map<String, List<String>> getServicesParameters(Collection<CheFeature> features) {
    Map<String, List<String>> parameters = new HashMap<>();
    for (CheFeature feature : features) {
      for (CheServiceReference serviceReference : feature.getSpec().getServices()) {
        if (serviceReference.getParameters().isEmpty()) {
          continue;
        }

        String serviceName = serviceReference.getName();
        String serviceVersion = serviceReference.getVersion();

        for (CheServiceParameter cheServiceParameter : serviceReference.getParameters()) {
          // add dollar sign and curly brackets because parameter is easier to find with these signs
          // in the map keys
          String parameterKey =
              serviceName + "/" + serviceVersion + "/${" + cheServiceParameter.getName() + "}";
          List<String> cheServiceParameters =
              parameters.computeIfAbsent(parameterKey, key -> new ArrayList<>());
          cheServiceParameters.add(cheServiceParameter.getValue());
        }
      }
    }
    return parameters;
  }

  private CheService getService(String serviceName, String serviceVersion)
      throws InfrastructureException {
    try {
      URI getServiceURI =
          featureApi.clone().path("service").path(serviceName).path(serviceVersion).build();

      CheService service = getBody(getServiceURI, CheService.class);
      validateService(service, serviceName, serviceVersion);
      return service;
    } catch (IllegalArgumentException | UriBuilderException e) {
      throw new InternalInfrastructureException(
          format("Service %s/%s retrieval failed", serviceName, serviceVersion));
    } catch (IOException e) {
      throw new InfrastructureException(
          format(
              "Error occurred on retrieval of service %s. Error: %s",
              serviceName + '/' + serviceVersion, e.getMessage()));
    }
  }

  private void validateFeature(CheFeature feature, String name, String version)
      throws InfrastructureException {
    requireNotNull(
        feature.getMetadata(), FEATURE_OBJECT_ERROR, name, version, "Metadata is missing.");
    requireNotNullNorEmpty(
        feature.getMetadata().getName(), FEATURE_OBJECT_ERROR, name, version, "Name is missing.");
    requireNotNull(feature.getSpec(), FEATURE_OBJECT_ERROR, name, version, "Spec is missing.");
    requireNotNullNorEmpty(
        feature.getSpec().getVersion(), FEATURE_OBJECT_ERROR, name, version, "Version is missing.");
    requireNotNullNorEmpty(
        feature.getSpec().getServices(),
        FEATURE_OBJECT_ERROR,
        name,
        version,
        "Services are missing.");
    for (CheServiceReference serviceReference : feature.getSpec().getServices()) {
      requireNotNull(
          serviceReference, FEATURE_OBJECT_ERROR, name, version, "A service is missing.");
      requireNotNullNorEmpty(
          serviceReference.getVersion(),
          FEATURE_OBJECT_ERROR,
          name,
          version,
          "Service version is missing.");
      requireNotNullNorEmpty(
          serviceReference.getName(),
          FEATURE_OBJECT_ERROR,
          name,
          version,
          "Service name is missing.");
    }
  }

  private void validateService(CheService service, String name, String version)
      throws InfrastructureException {
    requireNotNull(
        service.getMetadata(), SERVICE_OBJECT_ERROR, name, version, "Metadata is missing.");
    requireNotNullNorEmpty(
        service.getMetadata().getName(), SERVICE_OBJECT_ERROR, name, version, "Name is missing.");
    requireEqual(
        name,
        service.getMetadata().getName(),
        "Service name in feature and Service objects didn't match. Service object seems broken.");
    requireNotNull(service.getSpec(), SERVICE_OBJECT_ERROR, name, version, "Spec is missing.");
    requireNotNullNorEmpty(
        service.getSpec().getVersion(), SERVICE_OBJECT_ERROR, name, version, "Version is missing.");
    requireEqual(
        version,
        service.getSpec().getVersion(),
        "Service version in feature and Service objects didn't match. Service object seems broken.");
    requireNotNullNorEmpty(
        service.getSpec().getContainers(),
        SERVICE_OBJECT_ERROR,
        name,
        version,
        "Containers are missing.");
    for (Container container : service.getSpec().getContainers()) {
      requireNotNull(container, SERVICE_OBJECT_ERROR, name, version, "A container is missing.");
      requireNotNullNorEmpty(
          container.getImage(), SERVICE_OBJECT_ERROR, name, version, "Container image is missing.");
    }
  }

  @VisibleForTesting
  protected <T> T getBody(URI uri, Class<T> clas) throws IOException {
    HttpURLConnection httpURLConnection = null;
    try {
      httpURLConnection = (HttpURLConnection) uri.toURL().openConnection();

      int responseCode = httpURLConnection.getResponseCode();
      if (responseCode != 200) {
        throw new IOException(
            format(
                "Can't get object by URI '%s'. Error: %s",
                uri.toString(), getError(httpURLConnection)));
      }

      return parseResponseStreamAndClose(httpURLConnection.getInputStream(), clas);
    } finally {
      if (httpURLConnection != null) {
        httpURLConnection.disconnect();
      }
    }
  }

  private String getError(HttpURLConnection httpURLConnection) throws IOException {
    try (InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream())) {
      return CharStreams.toString(isr);
    }
  }

  protected <T> T parseResponseStreamAndClose(InputStream inputStream, Class<T> clazz)
      throws IOException {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
      T objectFromJson = GSON.fromJson(reader, clazz);
      if (objectFromJson == null) {
        throw new IOException(
            "Internal server error. Unexpected response body received from Feature API.");
      }
      return objectFromJson;
    } catch (JsonParseException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  private void requireNotNullNorEmpty(String s, String error, String... errorArgs)
      throws InfrastructureException {
    if (s == null || s.isEmpty()) {
      throw new InfrastructureException(format(error, (Object[]) errorArgs));
    }
  }

  private void requireNotNullNorEmpty(Collection objects, String error, String... errorArgs)
      throws InfrastructureException {
    if (objects == null || objects.isEmpty()) {
      throw new InfrastructureException(format(error, (Object[]) errorArgs));
    }
  }

  private void requireNotNull(Object object, String error, String... errorArgs)
      throws InfrastructureException {
    if (object == null) {
      throw new InfrastructureException(format(error, (Object[]) errorArgs));
    }
  }

  private void requireEqual(String version, String version1, String error, String... errorArgs)
      throws InfrastructureException {
    if (!version.equals(version1)) {
      throw new InfrastructureException(format(error, (Object[]) errorArgs));
    }
  }
}
