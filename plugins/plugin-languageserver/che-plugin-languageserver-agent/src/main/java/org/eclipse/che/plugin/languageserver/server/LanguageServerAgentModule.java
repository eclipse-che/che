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
package org.eclipse.che.plugin.languageserver.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.agent.server.AgentFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.languageserver.server.agent.CSharpLanguageServerAgentFactory;
import org.eclipse.che.plugin.languageserver.server.agent.JsonLanguageServerAgentFactory;

@DynaModule
public class LanguageServerAgentModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), AgentFactory.class).addBinding().to(JsonLanguageServerAgentFactory.class);
        Multibinder.newSetBinder(binder(), AgentFactory.class).addBinding().to(CSharpLanguageServerAgentFactory.class);
    }
}
