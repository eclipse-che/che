/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace.devfile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Component {

  /** Returns the alias of the component. Is optional and must be unique per components set. */
  String getAlias();

  /**
   * Returns type of the component, e.g. whether it is an plugin or editor or other type. It is
   * mandatory.
   */
  String getType();

  /** Returns the plugin/editor FQN. Is mandatory only for cheEditor/chePlugin components types. */
  String getId();

  /**
   * Returns the preferences of the plugin. Example value of preference: {@code java.home:
   * /home/user/jdk11}
   */
  Map<String, Serializable> getPreferences();

  /**
   * For 'kubernetes' and 'openshift' components types, returns absolute or devfile-relative
   * location of Kubernetes list yaml file.
   *
   * <p>For 'cheEditor' and 'chePlugin' components types, returns absolute location of plugin
   * descriptor (typically, named meta.yaml). For those types this field is optional.
   */
  String getReference();

  /**
   * Returns address of custom plugin registry. It is optional and applicable only for 'cheEditor'
   * and 'chePlugin' components types.
   */
  String getRegistryUrl();

  /**
   * Returns inlined content of a file specified in field 'reference'. It is optional and applicable
   * only for 'kubernetes' and 'openshift' components types.
   */
  String getReferenceContent();

  /**
   * Returns selector that should be used for picking up objects from specified content, if all
   * objects should be picked up then empty map is returned. It is optional and applicable only for
   * 'kubernetes' and 'openshift' components types.
   */
  Map<String, String> getSelector();

  /**
   * Returns entrypoints that should be overridden for specified objects. If components does not
   * have overridden entrypoints then empty map is returned. It is optional and applicable only for
   * 'kubernetes' and 'openshift' components types.
   */
  List<? extends Entrypoint> getEntrypoints();

  /**
   * Returns the docker image that should be used for component. It is mandatory and applicable only
   * for 'dockerimage' component type.
   */
  String getImage();

  /**
   * Returns memory limit for the component.
   *
   * <p>You can express memory as a plain integer or as a fixed-point integer using one of these
   * suffixes: E, P, T, G, M, K. You can also use the power-of-two equivalents: Ei, Pi, Ti, Gi, Mi,
   * Ki
   */
  String getMemoryLimit();

  /**
   * Returns memory request for the component.
   *
   * <p>You can express memory as a plain integer or as a fixed-point integer using one of these
   * suffixes: E, P, T, G, M, K. You can also use the power-of-two equivalents: Ei, Pi, Ti, Gi, Mi,
   * Ki
   */
  String getMemoryRequest();

  /**
   * Returns CPU limit for the component.
   *
   * <p>You can express CPU request as a floating-point cores or as a fixed-point integer millicores
   * using 'm' suffix. Examples: 1.5, 1500m.
   */
  String getCpuLimit();

  /**
   * Returns CPU request for the component.
   *
   * <p>You can express CPU request as a floating-point cores or as a fixed-point integer millicores
   * using 'm' suffix. Examples: 1.5, 1500m.
   */
  String getCpuRequest();

  /**
   * Returns true if projects sources should be mount to the component or false otherwise. It is
   * optional and applicable only for 'dockerimage' component type. `CHE_PROJECTS_ROOT` environment
   * variable should contains a path where projects sources are mount.
   */
  Boolean getMountSources();

  /**
   * Returns the command to run in the dockerimage component instead of the default one provided in
   * the image. It is optional, if missing then empty list is returned and command which is defined
   * in the image will be used. Applicable only for 'dockerimage' component type.
   */
  List<String> getCommand();

  /**
   * Returns the arguments to supply to the command running the dockerimage component. The arguments
   * are supplied either to the default command provided in the image or to the overridden command.
   * It is optional, if missing then empty list is returned and args which are defined in the image
   * will be used. Applicable only for 'dockerimage' component type.
   */
  List<String> getArgs();

  /**
   * Returns volumes which should be mount to component. It is optional and applicable only for
   * 'dockerimage' component type.
   */
  List<? extends Volume> getVolumes();

  /**
   * Returns the environment variables list that should be set to docker container. It is optional
   * and applicable only for 'dockerimage' component type.
   */
  List<? extends Env> getEnv();

  /**
   * Returns endpoints configuration. It is optional and applicable only for 'dockerimage' component
   * type.
   */
  List<? extends Endpoint> getEndpoints();

  /** Indicates whether namespace secrets should be mount into containers of this component. */
  Boolean getAutomountWorkspaceSecrets();
}
