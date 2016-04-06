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
package org.eclipse.che.ide.ext.java.jdi.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebugger;
import org.eclipse.che.ide.ext.debugger.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.JavaFqnResolver;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.JavaClassFqnResolver;

import static org.eclipse.che.ide.MimeType.APPLICATION_JAVA_CLASS;
import static org.eclipse.che.ide.MimeType.TEXT_X_JAVA;
import static org.eclipse.che.ide.MimeType.TEXT_X_JAVA_SOURCE;

/**
 * Extension allows debug Java web applications.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Anatoliy Bazko
 * @author Morhun Mykola
 */
@Singleton
@Extension(title = "Java Debugger", version = "4.1.0")
public class JavaRuntimeExtension {

    @Inject
    public JavaRuntimeExtension(DebuggerManager debuggerManager,
                                JavaDebugger javaDebugger,
                                FqnResolverFactory resolverFactory,
                                JavaFqnResolver javaFqnResolver,
                                JavaClassFqnResolver javaClassFqnResolver) {
        debuggerManager.registeredDebugger(JavaDebugger.ID, javaDebugger);
        resolverFactory.addResolver(TEXT_X_JAVA, javaFqnResolver);
        resolverFactory.addResolver("application/java", javaFqnResolver);
        resolverFactory.addResolver(APPLICATION_JAVA_CLASS, javaClassFqnResolver);
        resolverFactory.addResolver(TEXT_X_JAVA_SOURCE, javaFqnResolver);
    }
}
