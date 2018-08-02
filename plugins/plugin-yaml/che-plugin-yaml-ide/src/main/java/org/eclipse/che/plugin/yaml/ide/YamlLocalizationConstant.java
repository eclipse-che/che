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
package org.eclipse.che.plugin.yaml.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization for Yaml Language Server.
 *
 * @author Joshua Pinkney
 */
public interface YamlLocalizationConstant extends Messages {

  @Key("addUrlText")
  String addUrlText();

  @Key("deleteUrlText")
  String deleteUrlText();

  @Key("headerUiMessage")
  String headerUiMessage();

  @Key("addUrlLabel")
  String addUrlLabel();

  @Key("deleteUrlLabel")
  String deleteUrlLabel();

  @Key("urlColumnHeader")
  String urlColumnHeader();

  @Key("globColumnHeader")
  String globColumnHeader();

  @Key("deleteColumnHeader")
  String deleteColumnHeader();

  @Key("addSchemaButtonText")
  String addSchemaButtonText();
}
