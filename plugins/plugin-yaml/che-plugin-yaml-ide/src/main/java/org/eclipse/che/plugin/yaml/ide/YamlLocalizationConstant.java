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
