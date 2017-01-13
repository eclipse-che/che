/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.json.languageserver;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

/**
 * Implements the specifics related to the JSON language server.
 * 
 * After the Initialize Request the client must send a 'json/schemaAssociations'
 * notification in order to associate JSON schemas to popular JSON files. This
 * automatically enables code completion, validation, hover, etc. capabilities
 * for these files without the need of adding a "$schema" key.
 * 
 * @author Kaloyan Raev
 */
public class JsonLanguageServer extends JsonBasedLanguageServer implements ServerInitializerObserver {

    private final static String JSON_SCHEMA_ASSOCIATIONS = "json/schemaAssociations";

    @Override
    public void onServerInitialized(LanguageServer server, ServerCapabilities capabilities,
            LanguageDescription languageDescription, String projectPath) {
        registerSchemaAssociations();
    }

    private void registerSchemaAssociations() {
        Map<String, String[]> associations = new HashMap<>();
        associations.put("/*.schema.json", new String[] { "http://json-schema.org/draft-04/schema#" });
        associations.put("/bower.json", new String[] { "http://json.schemastore.org/bower" });
        associations.put("/.bower.json", new String[] { "http://json.schemastore.org/bower" });
        associations.put("/.bowerrc", new String[] { "http://json.schemastore.org/bowerrc" });
        associations.put("/composer.json", new String[] { "https://getcomposer.org/schema.json" });
        associations.put("/package.json", new String[] { "http://json.schemastore.org/package" });
        associations.put("/jsconfig.json", new String[] { "http://json.schemastore.org/jsconfig" });
        associations.put("/tsconfig.json", new String[] { "http://json.schemastore.org/tsconfig" });

        sendNotification(JSON_SCHEMA_ASSOCIATIONS, associations);
    }

}