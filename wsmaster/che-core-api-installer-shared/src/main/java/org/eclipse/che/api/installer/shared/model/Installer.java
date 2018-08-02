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
package org.eclipse.che.api.installer.shared.model;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;

/**
 * An entity that might additionally injected into machine and brings functionality.
 *
 * @author Anatoliy Bazko
 * @author Alexander Garagatyi
 */
public interface Installer {

  /**
   * Name of a property from {@link #getProperties()} that can contain environment variables that
   * should be injected into machine.
   *
   * <p>Example: { "environment" : "envVar1=value1,envVar2=value2" }
   */
  String ENVIRONMENT_PROPERTY = "environment";

  /** Returns the id of the installer. */
  String getId();

  /** Returns the name of the installer. */
  String getName();

  /** Returns the version of the installer. */
  String getVersion();

  /** Returns the description of the installer. */
  String getDescription();

  /**
   * Returns the depending installers, that must be applied before.
   *
   * <p>Values can be installer id with version separated with ':' symbol or just id then latest
   * version will be used. Values examples: org.exec-agent:v1.0, org.exec-agent,
   * org.exec-agent:latest.
   */
  List<String> getDependencies();

  /** Returns the script to be applied when machine is started. */
  String getScript();

  /** Returns any machine specific properties. */
  Map<String, String> getProperties();

  /** Returns Che servers in the machine. */
  Map<String, ? extends ServerConfig> getServers();
}
