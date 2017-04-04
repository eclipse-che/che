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
package org.eclipse.che.api.workspace.server.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Starting point of describing the contract which Infrastructure provider should implement
 * for making infrastructure suitable for serving workspace runtimes.
 *
 * @author gazarenkov
 */
public abstract class RuntimeInfrastructure {

    protected final Set<String> recipeTypes;
    protected final String      name;

    public RuntimeInfrastructure(String name, Set<String> types) {
        Preconditions.checkArgument(!types.isEmpty());
        this.name = Objects.requireNonNull(name);
        this.recipeTypes = ImmutableSet.copyOf(types);
    }

    /**
     * Returns the name of this runtime infrastructure.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the types of the recipes supported by this runtime infrastructure.
     * The set is never empty and contains at least one recipe type.
     */
    public final Set<String> getRecipeTypes() {
        return recipeTypes;
    }

    /**
     * An Infrastructure implementation should be able to preliminary estimate incoming Environment.
     * For example: for validating it before storing
     * The method SHOULD validate Environment. If it is valid, an Infrastructure MAY return more fine grained Environment
     * For example:
     * - if Machines are not described this method may add machine descriptions calculated against OldRecipe
     * - implementation may add additional Attributes based on incoming OldRecipe
     *
     * @param environment
     *         incoming Environment to estimate
     * @return calculated environment if any or same environment as incoming or null.
     * In all of this cases Environment is taken as valid.
     * @throws ValidationException
     *         if incoming Environment is not valid
     * @throws ServerException
     *         if some other error occurred
     */
    public abstract Environment estimate(Environment environment) throws ValidationException, ServerException;

    /**
     * An Infrastructure MAY track Runtimes. In this case the method should be overriden.
     *
     * One of the reason for infrastructure to support this is ability to recover infrastructure
     * after shutting down Master OldServer. For this purpose an Infrastructure should also implement
     * getRuntime(id) method
     *
     * @return list of tracked Runtimes' Identities.
     * @throws NotSupportedException
     *         if implementation does not support runtimes tracking
     */
    public Set<RuntimeIdentity> getIdentities() throws NotSupportedException {
        throw new NotSupportedException();
    }

    /**
     * An Infrastructure MAY track Runtimes. In this case the method should be overriden.
     *
     * One of the reason for infrastructure to support this is ability to recover infrastructure
     * after shutting down Master OldServer.
     *
     * @param id
     *         the RuntimeIdentity
     * @return the Runtime
     * @throws NotSupportedException
     *         if implementation does not support runtimes tracking
     */
    public InternalRuntime getRuntime(RuntimeIdentity id) throws NotSupportedException {
        throw new NotSupportedException();
    }

    /**
     * Making Runtime is a two phase process.
     * On the first phase implementation MUST prepare RuntimeContext, this is supposedly "fast" methid
     * On the second phase Runtime is created with RuntimeContext.start() which is supposedly "long" method
     *
     * @param id
     *         the RuntimeIdentity
     * @param environment
     *         incoming Environment (configuration)
     * @return new RuntimeContext object
     * @throws ValidationException
     *         if incoming Environment is not valid
     * @throws ServerException
     *         if some other error occured
     */
    public abstract RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException, ApiException,
                                                                                               IOException;


}
