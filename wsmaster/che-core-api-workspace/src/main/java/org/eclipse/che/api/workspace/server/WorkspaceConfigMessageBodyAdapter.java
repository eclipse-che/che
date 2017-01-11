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
package org.eclipse.che.api.workspace.server;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.regex.Pattern;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.regex.Pattern.DOTALL;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * Adapts an old format of {@link WorkspaceConfig} to a new one.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceConfigMessageBodyAdapter implements MessageBodyAdapter {

    /**
     * Matches those string which contain substring like <b>"environments" : [</b>.
     * Old format defines "environments" as json array while new format defines
     * "environments" as json object.
     */
    private static final Pattern CONTAINS_ENVIRONMENTS_ARRAY_PATTERN = Pattern.compile(".*\"environments\"\\s*:\\s*\\[.*", DOTALL);

    @Inject
    protected WorkspaceConfigJsonAdapter configAdapter;

    @Override
    public Set<Class<?>> getTriggers() {
        return ImmutableSet.of(WorkspaceConfig.class, WorkspaceConfigDto.class);
    }

    @Override
    public InputStream adapt(InputStream entityStream) throws WebApplicationException, IOException {
        try (Reader r = new InputStreamReader(entityStream)) {
            return new ByteArrayInputStream(adapt(CharStreams.toString(r)).getBytes(defaultCharset()));
        } catch (IllegalArgumentException x) {
            throw new WebApplicationException(x.getMessage(), x, BAD_REQUEST);
        } catch (RuntimeException x) {
            throw new WebApplicationException(x.getMessage(), x, INTERNAL_SERVER_ERROR);
        }
    }

    /** Adapts a string to a string instead of adapting streams */
    protected String adapt(String body) {
        if (!isOldFormat(body)) {
            return body;
        }
        final JsonParser parser = new JsonParser();
        final JsonElement rootEl = parser.parse(body);
        if (!rootEl.isJsonObject()) {
            return body;
        }
        final JsonObject workspaceConfObj = getWorkspaceConfigObj(rootEl.getAsJsonObject());
        if (workspaceConfObj == null) {
            return body;
        }
        configAdapter.adaptModifying(getWorkspaceConfigObj(rootEl.getAsJsonObject()));
        return rootEl.toString();
    }

    /** Checks whether the body is in old workspace config format. */
    protected boolean isOldFormat(String body) {
        return CONTAINS_ENVIRONMENTS_ARRAY_PATTERN.matcher(body).matches();
    }

    /** Gets workspace configuration from the json root. */
    protected JsonObject getWorkspaceConfigObj(JsonObject root) { return root; }
}
