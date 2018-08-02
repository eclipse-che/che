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
package org.eclipse.che.plugin.json.languageserver;

import java.util.Map;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

/** */
public interface JsonExtension {

  @JsonNotification(value = "json/schemaAssociations", useSegment = false)
  void jsonSchemaAssociation(Map<String, String[]> associations);
}
