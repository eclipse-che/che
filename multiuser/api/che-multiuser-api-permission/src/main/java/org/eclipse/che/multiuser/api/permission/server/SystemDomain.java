/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.multiuser.api.permission.server.model.impl.SystemPermissionsImpl;

/**
 * Domain for storing actions that are used for managing system e.g. user management, configuration properties management.
 *
 * <p>The list of supported actions by system domain can be configured by following lines
 * <pre>
 *   Multibinder<String> binder = Multibinder.newSetBinder(binder(), String.class, Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));
 *   binder.addBinding().toInstance("customAction");
 * <pre/>
 *
 * @author Sergii Leschenko
 */
public class SystemDomain extends AbstractPermissionsDomain<SystemPermissionsImpl> {
  public static final String SYSTEM_DOMAIN_ACTIONS = "system.domain.actions";
  public static final String DOMAIN_ID = "system";
  public static final String MANAGE_SYSTEM_ACTION = "manageSystem";

  @Inject
  public SystemDomain(@Named(SYSTEM_DOMAIN_ACTIONS) Set<String> allowedActions) {
    super(
        DOMAIN_ID,
        Stream.concat(allowedActions.stream(), Stream.of(MANAGE_SYSTEM_ACTION))
            .collect(Collectors.toList()),
        false);
  }

  @Override
  public SystemPermissionsImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new SystemPermissionsImpl(userId, allowedActions);
  }
}
