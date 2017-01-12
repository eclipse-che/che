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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Retrieves hosts entries for docker machines from property.
 *
 * </p> Property {@value PROPERTY} contains hosts entries separated by coma "," sign.
 *
 * @author Alexander Garagatyi
 */
public class DockerExtraHostsFromPropertyProvider implements Provider<Set<String>> {
    private static final String PROPERTY = "che.workspace.hosts";

    private final Set<String> extraHosts;

    @Inject
    public DockerExtraHostsFromPropertyProvider(@Nullable @Named(PROPERTY) String extraHosts) {
        if (isNullOrEmpty(extraHosts)) {
            this.extraHosts = Collections.emptySet();
        } else {
            this.extraHosts = new HashSet<>(Arrays.asList(extraHosts.split(",")));
        }
    }

    @Override
    public Set<String> get() {
        return extraHosts;
    }
}
