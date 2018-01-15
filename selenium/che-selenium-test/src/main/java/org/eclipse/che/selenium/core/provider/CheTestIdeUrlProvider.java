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
package org.eclipse.che.selenium.core.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import javax.inject.Named;
import org.eclipse.che.selenium.core.UrlUtil;

/** @author Anatolii Bazko */
@Singleton
public class CheTestIdeUrlProvider implements TestIdeUrlProvider {
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
    return UrlUtil.url(protocol, host, port, "/");
  }
}
