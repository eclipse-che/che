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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsnext.model.CheFeature;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginParameter;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginReference;
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
  private static final String CHE_PLUGIN_OBJECT_ERROR =
      "ChePlugin '%s/%s' configuration is invalid. %s";

  private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

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
   * objects from Feature API and returns list of {@link ChePlugin} needed to provide features
   * functionality in a workspace.
   *
   * <p>This method resolves feature dependencies and parameters, so returned list of {@code
   * ChePlugins} is ready to be applied to a workspace runtime.
   *
   * @param attributes workspace attributes
   * @throws InfrastructureException when features list contains invalid entries or Workspace.Next
   *     objects retrieval from Feature API fails
   */
  public Collection<ChePlugin> get(Map<String, String> attributes) throws InfrastructureException {
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

    return toPlugins(getFeatures(featuresNameVersion));
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

  private Collection<ChePlugin> toPlugins(Collection<CheFeature> features)
      throws InfrastructureException {
    Collection<ChePlugin> plugins = getPlugins(features);
    Map<String, List<String>> parameters = getPluginsParameters(features);

    for (ChePlugin plugin : plugins) {
      String pluginName = plugin.getName();
      String pluginVersion = plugin.getVersion();
      String pluginKey = pluginName + '/' + pluginVersion;

      // for now we match whole env variable value against '${<parameter name>}'
      plugin
          .getContainers()
          .stream()
          .flatMap(container -> container.getEnv().stream())
          .filter(this::isParameter)
          .forEach(
              envVar -> {
                String parameterKey = pluginKey + '/' + envVar.getValue();
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
          "Parameters not supported by che plugins found: " + parameters.keySet());
    }

    return plugins;
  }

  private Collection<ChePlugin> getPlugins(Collection<CheFeature> features)
      throws InfrastructureException {
    Map<String, ChePlugin> plugins = new HashMap<>();
    for (CheFeature feature : features) {
      for (ChePluginReference pluginReference : feature.getSpec().getServices()) {
        String pluginName = pluginReference.getName();
        String pluginVersion = pluginReference.getVersion();
        String key = pluginName + '/' + pluginVersion;
        if (!plugins.containsKey(key)) {
          ChePlugin plugin = getPlugin(pluginName, pluginVersion);
          plugins.put(key, plugin);
        }
      }
    }
    return plugins.values();
  }

  private boolean isParameter(EnvVar envVar) {
    return PARAMETER_ENV_VAR_VALUE.matcher(envVar.getValue()).matches();
  }

  /**
   * Returns map where key is concatenation of plugin name, version and parameter name separated by
   * slash symbol. < pluginName/pluginVersion/${parameter} > to < parameterValue >
   */
  private Map<String, List<String>> getPluginsParameters(Collection<CheFeature> features) {
    Map<String, List<String>> parameters = new HashMap<>();
    for (CheFeature feature : features) {
      for (ChePluginReference pluginReference : feature.getSpec().getServices()) {
        if (pluginReference.getParameters().isEmpty()) {
          continue;
        }

        String pluginName = pluginReference.getName();
        String pluginVersion = pluginReference.getVersion();

        for (ChePluginParameter chePluginParameter : pluginReference.getParameters()) {
          // add dollar sign and curly brackets because parameter is easier to find with these signs
          // in the map keys
          String parameterKey =
              pluginName + "/" + pluginVersion + "/${" + chePluginParameter.getName() + "}";
          List<String> chePluginParameters =
              parameters.computeIfAbsent(parameterKey, key -> new ArrayList<>());
          chePluginParameters.add(chePluginParameter.getValue());
        }
      }
    }
    return parameters;
  }

  private ChePlugin getPlugin(String pluginName, String pluginVersion)
      throws InfrastructureException {
    try {
      URI getPluginURI =
          featureApi.clone().path("service").path(pluginName).path(pluginVersion).build();

      ChePlugin plugin = getBody(getPluginURI, ChePlugin.class);
      validatePlugin(plugin, pluginName, pluginVersion);
      return plugin;
    } catch (IllegalArgumentException | UriBuilderException e) {
      throw new InternalInfrastructureException(
          format("ChePlugin %s/%s retrieval failed", pluginName, pluginVersion));
    } catch (IOException e) {
      throw new InfrastructureException(
          format(
              "Error occurred on retrieval of ChePlugin %s. Error: %s",
              pluginName + '/' + pluginVersion, e.getMessage()));
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
        "Che plugins are missing.");
    for (ChePluginReference pluginReference : feature.getSpec().getServices()) {
      requireNotNull(pluginReference, FEATURE_OBJECT_ERROR, name, version, "A plugin is missing.");
      requireNotNullNorEmpty(
          pluginReference.getVersion(),
          FEATURE_OBJECT_ERROR,
          name,
          version,
          "Plugin version is missing.");
      requireNotNullNorEmpty(
          pluginReference.getName(),
          FEATURE_OBJECT_ERROR,
          name,
          version,
          "Plugin name is missing.");
    }
  }

  private void validatePlugin(ChePlugin plugin, String name, String version)
      throws InfrastructureException {
    requireNotNullNorEmpty(
        plugin.getName(), CHE_PLUGIN_OBJECT_ERROR, name, version, "Name is missing.");
    requireEqual(
        name,
        plugin.getName(),
        "Plugin name in feature and ChePlugin objects didn't match. ChePlugin object seems broken.");
    requireNotNullNorEmpty(
        plugin.getVersion(), CHE_PLUGIN_OBJECT_ERROR, name, version, "Version is missing.");
    requireEqual(
        version,
        plugin.getVersion(),
        "Plugin version in feature and ChePlugin objects didn't match. ChePlugin object seems broken.");
    requireNotNullNorEmpty(
        plugin.getContainers(), CHE_PLUGIN_OBJECT_ERROR, name, version, "Containers are missing.");
    for (CheContainer container : plugin.getContainers()) {
      requireNotNull(container, CHE_PLUGIN_OBJECT_ERROR, name, version, "A container is missing.");
      requireNotNullNorEmpty(
          container.getImage(),
          CHE_PLUGIN_OBJECT_ERROR,
          name,
          version,
          "Container image is missing.");
    }
    validatePorts(plugin.getEndpoints(), plugin.getContainers());
  }

  private void validatePorts(List<ChePluginEndpoint> endpoints, List<CheContainer> containers)
      throws InfrastructureException {
    List<Integer> containerPorts =
        containers
            .stream()
            .flatMap(cheContainer -> cheContainer.getPorts().stream())
            .mapToInt(CheContainerPort::getExposedPort)
            .boxed()
            .collect(Collectors.toList());
    HashSet<Integer> uniqueContainerPorts = new HashSet<>(containerPorts);
    requireEqual(
        uniqueContainerPorts.size(),
        containerPorts.size(),
        "Containers contain duplicated exposed ports.");
    HashSet<Integer> uniqueEndpointPorts =
        endpoints
            .stream()
            .mapToInt(ChePluginEndpoint::getTargetPort)
            .boxed()
            .collect(Collectors.toCollection(HashSet::new));
    SetView<Integer> portsDifference = Sets.difference(uniqueContainerPorts, uniqueEndpointPorts);
    requireEmpty(
        portsDifference,
        "Ports in containers and endpoints don't match. Difference: {}",
        portsDifference.toString());
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

      return parseYamlResponseStreamAndClose(httpURLConnection.getInputStream(), clas);
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

  protected <T> T parseYamlResponseStreamAndClose(InputStream inputStream, Class<T> clazz)
      throws IOException {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
      return YAML_PARSER.readValue(reader, clazz);
    } catch (IOException e) {
      throw new IOException(
          "Internal server error. Unexpected response body received from Feature API."
              + e.getLocalizedMessage(),
          e);
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

  private void requireEmpty(Collection objects, String error, String... errorArgs)
      throws InfrastructureException {
    if (objects != null && !objects.isEmpty()) {
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

  private void requireEqual(int i, int k, String error) throws InfrastructureException {
    if (i != k) {
      throw new InfrastructureException(error);
    }
  }
}
