/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.deploy;

import com.google.inject.AbstractModule;
import org.eclipse.che.multiuser.keycloak.server.KeycloakUserRemover.RemoveUserListener;

public class KeycloakUserRemoverModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(RemoveUserListener.class).asEagerSingleton();
  }
}
