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

import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;

/**
 * Checks that current subject has privileges to perform some operation without required
 * permissions.
 *
 * <p>Super privileges is designed to give some extra abilities for users who have permission to
 * perform {@link SystemDomain#MANAGE_SYSTEM_ACTION manage system}.<br>
 * Super privileges are optional, they can be disabled by configuration.
 *
 * <p>User has super privileges if he has {@link SystemDomain#MANAGE_SYSTEM_ACTION manage system}
 * permission and system configuration property {@link #SYSTEM_SUPER_PRIVILEGED_MODE} is true.
 *
 * <p>It is required to perform {@link #hasSuperPrivileges()} checks manually before permissions
 * checking if user should be able to perform some operation.
 *
 * <pre>
 * public class ExamplePermissionsFilter extends CheMethodInvokerFilter {
 *     &#064;Inject
 *     private SuperPrivilegesChecker superPrivilegesChecker;
 *
 *     &#064;Override
 *     protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {
 *         if (superPrivilegesChecker.hasSuperPrivileges()) {
 *             return;
 *         }
 *         EnvironmentContext.getCurrent().getSubject().checkPermissions("domain", "domain123", "action");
 *     }
 * }
 * </pre>
 *
 * If user should be able to manage permissions for some permission domain then this domain should
 * be present in multibinder named with {@link #SUPER_PRIVILEGED_DOMAINS}.<br>
 * Binding example:
 *
 * <pre>
 * public class ExampleModule extends AbstractModule {
 *     &#064;Override
 *     protected void configure() {
 *         Multibinder.newSetBinder(binder(), PermissionsDomain.class, Names.named(SuperPrivilegesChecker.SUPER_PRIVILEGED_DOMAINS))
 *                    .addBinding().to(ExampleDomain.class);
 *     }
 * }
 * </pre>
 *
 * @author Sergii Leschenko
 */
public class SuperPrivilegesChecker {
  /**
   * Configuration parameter that indicates extended abilities for users who have {@link
   * SystemDomain#MANAGE_SYSTEM_ACTION manageSytem} permission.
   */
  public static final String SYSTEM_SUPER_PRIVILEGED_MODE = "che.system.super_privileged_mode";

  /** Permissions of these domains can be managed by any user who has super privileges. */
  public static final String SUPER_PRIVILEGED_DOMAINS = "system.super_privileged_domains";

  private final boolean superPrivilegedMode;
  private final Set<String> privilegesDomainsIds;

  @Inject
  public SuperPrivilegesChecker(
      @Named(SYSTEM_SUPER_PRIVILEGED_MODE) boolean superPrivilegedMode,
      @Named(SUPER_PRIVILEGED_DOMAINS) Set<PermissionsDomain> domains) {
    this.superPrivilegedMode = superPrivilegedMode;
    this.privilegesDomainsIds =
        domains.stream().map(PermissionsDomain::getId).collect(Collectors.toSet());
  }

  /**
   * Checks that current subject has super privileges.
   *
   * @return true if current subject has super privileges, false otherwise
   */
  public boolean hasSuperPrivileges() {
    return superPrivilegedMode
        && EnvironmentContext.getCurrent()
            .getSubject()
            .hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }

  /**
   * Checks that current subject is privileged to manage permissions of specified domain.
   *
   * @return true if current subject is privileged to manage permissions of specified domain, false
   *     otherwise
   */
  public boolean isPrivilegedToManagePermissions(String domainId) {
    return superPrivilegedMode
        && privilegesDomainsIds.contains(domainId)
        && EnvironmentContext.getCurrent()
            .getSubject()
            .hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }
}
