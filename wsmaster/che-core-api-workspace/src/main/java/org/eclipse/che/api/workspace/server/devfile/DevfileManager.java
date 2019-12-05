/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.exception.OverrideParameterException;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;

/**
 * Facade for devfile related operations.
 *
 * @author Max Shaposhnyk
 */
@Beta
@Singleton
public class DevfileManager {

  private ObjectMapper yamlMapper;
  private ObjectMapper jsonMapper;
  private final DevfileSchemaValidator schemaValidator;
  private final DevfileIntegrityValidator integrityValidator;
  private final OverridePropertiesApplier overridePropertiesApplier;

  @Inject
  public DevfileManager(
      DevfileSchemaValidator schemaValidator, DevfileIntegrityValidator integrityValidator) {
    this(
        schemaValidator,
        integrityValidator,
        new ObjectMapper(new YAMLFactory()),
        new ObjectMapper());
  }

  @VisibleForTesting
  DevfileManager(
      DevfileSchemaValidator schemaValidator,
      DevfileIntegrityValidator integrityValidator,
      ObjectMapper yamlMapper,
      ObjectMapper jsonMapper) {
    this.schemaValidator = schemaValidator;
    this.integrityValidator = integrityValidator;
    this.yamlMapper = yamlMapper;
    this.jsonMapper = jsonMapper;
    this.overridePropertiesApplier = new OverridePropertiesApplier();
  }

  /**
   * Creates {@link DevfileImpl} from given devfile content in YAML. Performs schema and integrity
   * validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   */
  public DevfileImpl parseYaml(String devfileContent) throws DevfileFormatException {
    try {
      return parse(devfileContent, yamlMapper, emptyMap());
    } catch (OverrideParameterException e) {
      // should never happen as we send empty overrides map
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Creates {@link DevfileImpl} from given devfile content in YAML and provides possibility to
   * override its values using key-value map, where key is an json-pointer-like string and value is
   * desired property value. NOTE: unlike json pointers, objects in arrays should be pointed by
   * their names, not by index. Examples:
   *
   * <ul>
   *   <li>metadata.generateName : python-dev-
   *   <li>projects.foo.source.type : git // foo is an project name
   * </ul>
   *
   * Performs schema and integrity validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @param overrideProperties map of overridden values
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   * @throws OverrideParameterException when override properties is incorrect
   */
  public DevfileImpl parseYaml(String devfileContent, Map<String, String> overrideProperties)
      throws DevfileFormatException, OverrideParameterException {
    return parse(devfileContent, yamlMapper, overrideProperties);
  }

  /**
   * Creates {@link DevfileImpl} from given devfile content in JSON. Performs schema and integrity
   * validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   */
  public DevfileImpl parseJson(String devfileContent) throws DevfileFormatException {
    try {
      return parse(devfileContent, jsonMapper, emptyMap());
    } catch (OverrideParameterException e) {
      // should never happen as we send empty overrides map
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Creates {@link DevfileImpl} from given devfile content in JSON and provides possibility to
   * override its values using key-value map, where key is an json-pointer-like string and value is
   * desired property value. NOTE: unlike json pointers, objects in arrays should be pointed by
   * their names, not by index. Examples:
   *
   * <ul>
   *   <li>metadata.generateName : python-dev-
   *   <li>projects.foo.source.type : git // foo is an project name
   * </ul>
   *
   * Performs schema and integrity validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @param overrideProperties map of overridden values
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   * @throws OverrideParameterException when override properties is incorrect
   */
  public DevfileImpl parseJson(String devfileContent, Map<String, String> overrideProperties)
      throws DevfileFormatException, OverrideParameterException {
    return parse(devfileContent, jsonMapper, overrideProperties);
  }

  /**
   * Resolve devfile component references into their reference content.
   *
   * @param devfile input devfile
   * @param fileContentProvider provider to fetch reference content
   */
  public void resolveReference(DevfileImpl devfile, FileContentProvider fileContentProvider)
      throws DevfileException {
    List<ComponentImpl> toResolve =
        devfile
            .getComponents()
            .stream()
            .filter(
                c ->
                    c.getType().equals(KUBERNETES_COMPONENT_TYPE)
                        || c.getType().equals(OPENSHIFT_COMPONENT_TYPE))
            .filter(c -> !isNullOrEmpty(c.getReference()))
            .collect(Collectors.toList());
    for (ComponentImpl c : toResolve) {
      try {
        c.setReferenceContent(fileContentProvider.fetchContent(c.getReference()));
      } catch (IOException e) {
        throw new DevfileException(
            format(
                "Unable to resolve reference of component: %s",
                firstNonNull(c.getAlias(), c.getReference())),
            e);
      }
    }
  }

  private DevfileImpl parse(
      String content, ObjectMapper mapper, Map<String, String> overrideProperties)
      throws DevfileFormatException, OverrideParameterException {
    DevfileImpl devfile;
    try {
      JsonNode parsed = mapper.readTree(content);
      if (parsed == null) {
        throw new DevfileFormatException("Unable to parse Devfile - provided source is empty");
      }
      parsed = overridePropertiesApplier.applyPropertiesOverride(parsed, overrideProperties);
      schemaValidator.validate(parsed);
      devfile = mapper.treeToValue(parsed, DevfileImpl.class);
    } catch (JsonProcessingException e) {
      throw new DevfileFormatException(e.getMessage());
    } catch (IOException e) {
      throw new DevfileFormatException("Unable to parse Devfile. Error: " + e.getMessage());
    }
    integrityValidator.validateDevfile(devfile);
    return devfile;
  }
}
