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
package org.eclipse.che.inject;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Utility for finding Guice modules annotated with &#064DynaModule. */
@HandlesTypes({DynaModule.class})
public class ModuleScanner implements ServletContainerInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleScanner.class);

    @VisibleForTesting
    static final List<Module> modules = new ArrayList<>();

    public static List<Module> findModules() {
        return new ArrayList<>(modules);
    }

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        if (c != null) {
            for (Class<?> clazz : c) {
                if (Module.class.isAssignableFrom(clazz)) {
                    try {
                        modules.add((Module)clazz.newInstance());
                    } catch (Exception e) {
                        LOG.error("Problem with instantiating Module {} : {}", clazz, e.getMessage());
                    }
                } else {
                    LOG.warn("Ignored non {} class annotated with {}", Module.class.getName(), DynaModule.class.getName());
                }
            }
        }
    }
}
