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
package org.eclipse.che.api.workspace.server.stack;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.eclipse.che.api.workspace.server.WorkspaceConfigMessageBodyAdapter;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.api.workspace.shared.stack.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * Adapts {@link Stack#getWorkspaceConfig()} from an old format to a new one.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class StackMessageBodyAdapter extends WorkspaceConfigMessageBodyAdapter {

    @Inject
    private StackJsonAdapter stackJsonAdapter;

    @Override
    public Set<Class<?>> getTriggers() {
        return ImmutableSet.of(Stack.class, StackDto.class);
    }

    @Override
    protected String adapt(String body) {
        if (!isOldFormat(body)) {
            return body;
        }
        final JsonElement stackEl = new JsonParser().parse(body);
        if (!stackEl.isJsonObject()) {
            return body;
        }
        stackJsonAdapter.adaptModifying(stackEl.getAsJsonObject());
        return stackEl.toString();
    }
}
