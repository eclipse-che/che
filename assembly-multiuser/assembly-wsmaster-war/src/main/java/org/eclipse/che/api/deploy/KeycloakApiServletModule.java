/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.deploy;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.keycloak.server.deploy.KeycloakServletModule;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@DynaModule
public class KeycloakApiServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    install(new KeycloakServletModule());
  }
}
