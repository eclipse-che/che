/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.dns;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Injects DNS resolvers and ensures that it is neither empty array nor single value array with null
 * or empty string.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DnsResolversProvider implements Provider<List<String>> {
  private final List<String> dnsResolvers;

  @Inject
  public DnsResolversProvider(@Nullable @Named("che.dns.resolvers") String[] dnsResolvers) {
    if (dnsResolvers == null
        || dnsResolvers.length == 0
        || (dnsResolvers.length == 1 && isNullOrEmpty(dnsResolvers[0]))) {
      this.dnsResolvers = emptyList();
    } else {
      this.dnsResolvers = Arrays.asList(dnsResolvers);
    }
  }

  @Nullable
  @Override
  public List<String> get() {
    return dnsResolvers;
  }
}
