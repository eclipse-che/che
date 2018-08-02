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
package org.eclipse.che.selenium.core.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import javax.inject.Named;
import org.eclipse.che.selenium.core.utils.UrlUtil;

/** @author Anatolii Bazko */
@Singleton
public class CheTestDashboardUrlProvider implements TestDashboardUrlProvider {
  @Inject
  @Named("che.protocol")
  private String protocol;

  @Inject
  @Named("che.host")
  private String host;

  @Inject
  @Named("che.port")
  private int port;

  @Override
  public URL get() {
    return UrlUtil.url(protocol, host, port, "/dashboard/");
  }
}
