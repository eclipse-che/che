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
package org.eclipse.che.plugin.yaml.server.languageserver;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.plugin.yaml.shared.YamlDTO;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * The yaml server side endpoint for injecting the schema into yaml language server
 *
 * @author Joshua Pinkney
 */
@Path("yaml")
public class YamlService {

  /**
   * Route for getting getting schemas from client side and injecting them into yaml language server
   *
   * @param yamlDto A yamlDTO containing the list of schemas you would like to add
   */
  @POST
  @Path("schemas")
  @Consumes(MediaType.APPLICATION_JSON)
  public void putSchemas(YamlDTO yamlDto) throws ApiException {

    LanguageServer yamlLS = YamlLanguageServerConfig.getYamlLanguageServer();

    if (yamlDto != null && yamlLS != null) {

      Endpoint endpoint = ServiceEndpoints.toEndpoint(yamlLS);
      YamlSchemaAssociations serviceObject =
          ServiceEndpoints.toServiceObject(endpoint, YamlSchemaAssociations.class);

      Map<String, String[]> schemaAssociations = new HashMap<>();
      Map<String, String> yamlDtoSchemas = yamlDto.getSchemas();

      for (Map.Entry<String, String> schema : yamlDtoSchemas.entrySet()) {
        schemaAssociations.put(
            schema.getKey(), new Gson().fromJson(schema.getValue(), String[].class));
      }

      serviceObject.yamlSchemaAssociation(schemaAssociations);
    }
  }
}
