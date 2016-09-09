/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.environment.server.compose.model.ComposeEnvironmentImpl;

import java.io.IOException;

import static java.lang.String.format;

/**
 * Converters compose file to {@link ComposeEnvironmentImpl} and vise versa.
 *
 * @author Alexander Garagatyi
 */
public class ComposeFileParser {
    private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

    /**
     * Parses compose file into Docker Compose model.
     *
     * @param recipeContent compose file to parse
     * @throws IllegalArgumentException
     *         when environment or environment recipe is invalid
     * @throws ServerException
     *         when environment recipe can not be retrieved
     */
    public ComposeEnvironmentImpl parse(String recipeContent, String contentType) throws IllegalArgumentException,
                                                                                         ServerException {
        ComposeEnvironmentImpl composeEnvironment;
        switch (contentType) {
            case "application/x-yaml":
            case "text/yaml":
            case "text/x-yaml":
                try {
                    composeEnvironment = YAML_PARSER.readValue(recipeContent, ComposeEnvironmentImpl.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Parsing of environment configuration failed. " + e.getLocalizedMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Provided environment recipe content type '" +
                                                   contentType +
                                                   "' is unsupported. Supported values are: application/x-yaml");
        }
        return composeEnvironment;
    }

    /**
     * Converts Docker Compose environment model into YAML file.
     *
     * @param composeEnvironment Docker Compose environment model file
     * @throws IllegalArgumentException
     *         when argument is null or conversion to YAML fails
     */
    public String toYaml(ComposeEnvironmentImpl composeEnvironment) throws IllegalArgumentException {
        checkNotNull(composeEnvironment, "Compose environment should not be null");
        try {
            return YAML_PARSER.writeValueAsString(composeEnvironment);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Checks that object reference is not null, throws {@link IllegalArgumentException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkNotNull(Object object, String errorMessageTemplate, Object... errorMessageParams) {
        if (object == null) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageParams));
        }
    }
}
