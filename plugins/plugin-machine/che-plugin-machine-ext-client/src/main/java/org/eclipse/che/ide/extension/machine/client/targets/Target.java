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
package org.eclipse.che.ide.extension.machine.client.targets;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;

/**
 * Wrapper fot the Recipe.
 *
 * @author Vitaliy Guliy
 */
public class Target {

    private String name;

    private String host;

    private String port;

    private String userName;

    private String password;

    private String type;

    private RecipeDescriptor recipe;

    /**
     * Indicates this target has unsaved changes.
     */
    private boolean dirty;

    private boolean connected;

    public Target(String name, String type, RecipeDescriptor recipe) {
        this.name = name;
        this.type = type;
        this.recipe = recipe;

        dirty = false;
    }

    public Target(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public RecipeDescriptor getRecipe() {
        return recipe;
    }

    public void setRecipe(RecipeDescriptor recipe) {
        this.recipe = recipe;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

}
