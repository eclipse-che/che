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
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.environment.server.compose.model.ComposeEnvironmentImpl;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Parses containers description in an environment to {@link ComposeEnvironmentImpl}.
 *
 * @author Alexander Garagatyi
 */
public class ComposeFileParser {
    private static final Logger       LOG         = getLogger(ComposeFileParser.class);
    private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

    private final URI apiEndpoint;

    @Inject
    public ComposeFileParser(@Named("api.endpoint") URI apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    /**
     * Parses recipe environment into Docker Compose model.
     *
     * @param environment environment to parse
     * @throws IllegalArgumentException
     *         when environment or environment recipe is invalid
     * @throws ServerException
     *         when environment recipe can not be retrieved
     */
    public ComposeEnvironmentImpl parse(Environment environment) throws IllegalArgumentException,
                                                                        ServerException {
        checkNotNull(environment, "Environment should not be null");
        checkNotNull(environment.getRecipe(), "Environment recipe should not be null");
        checkNotNull(environment.getRecipe().getContentType(), "Content type of environment recipe should not be null");
        checkArgument(environment.getRecipe().getContent() != null || environment.getRecipe().getLocation() != null,
                      "Recipe of environment must contain location or content");

        String recipeContent = getContentOfRecipe(environment.getRecipe());
        return parseEnvironmentRecipeContent(recipeContent,
                                             environment.getRecipe().getContentType());
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

    private String getContentOfRecipe(EnvironmentRecipe environmentRecipe) throws ServerException {
        if (environmentRecipe.getContent() != null) {
            return environmentRecipe.getContent();
        } else {
            return getRecipe(environmentRecipe.getLocation());
        }
    }

    private ComposeEnvironmentImpl parseEnvironmentRecipeContent(String recipeContent, String contentType) {
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

    private String getRecipe(String location) throws ServerException {
        URL recipeUrl;
        File file = null;
        try {
            UriBuilder targetUriBuilder = UriBuilder.fromUri(location);
            // add user token to be able to download user's private recipe
            final String apiEndPointHost = apiEndpoint.getHost();
            final String host = targetUriBuilder.build().getHost();
            if (apiEndPointHost.equals(host)) {
                if (EnvironmentContext.getCurrent().getSubject() != null
                    && EnvironmentContext.getCurrent().getSubject().getToken() != null) {
                    targetUriBuilder.queryParam("token", EnvironmentContext.getCurrent().getSubject().getToken());
                }
            }
            recipeUrl = targetUriBuilder.build().toURL();
            file = IoUtil.downloadFileWithRedirect(null, "recipe", null, recipeUrl);

            return IoUtil.readAndCloseQuietly(new FileInputStream(file));
        } catch (IOException | IllegalArgumentException e) {
            throw new MachineException(format("Recipe downloading failed. Recipe url %s. Error: %s",
                                              location,
                                              e.getLocalizedMessage()));
        } finally {
            if (file != null && !file.delete()) {
                LOG.error(String.format("Removal of recipe file %s failed.", file.getAbsolutePath()));
            }
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
