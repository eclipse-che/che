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

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Named;
import org.eclipse.che.selenium.core.utils.UrlUtil;

/** @author Dmytro Nochevnov */
@Singleton
public class OpenShiftWebConsoleUrlProvider implements Provider<URL> {

  private static final int PORT = 8443;
  private static final String PROTOCOL = "https";

  // extract openshift host from the url like 'che-eclipse-che.172.19.20.137.nip.io'
  private static final Pattern OPENSHIFT_HOST_REGEXP =
      Pattern.compile(".*[\\.]([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})[.].*");

  @Inject
  @Named("che.host")
  private String cheHost;

  @Inject(optional = true)
  @Named("env.openshift.url")
  private String openShiftUrl;

  @Override
  public URL get() {
    if (openShiftUrl != null) {
      try {
        return new URL(openShiftUrl);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    String openShiftHost = obtainOpenShiftHost();
    return UrlUtil.url(PROTOCOL, openShiftHost, PORT, "/");
  }

  private String obtainOpenShiftHost() {
    if (openShiftUrl != null) {
      return openShiftUrl;
    }

    Matcher matcher = OPENSHIFT_HOST_REGEXP.matcher(cheHost);
    if (!matcher.matches()) {
      throw new RuntimeException(
          format(
              "It's impossible to extract OpenShift host from Eclipse Che host '%s'. Make sure that correct value is set for `CHE_INFRASTRUCTURE`.",
              cheHost));
    }

    return matcher.group(1);
  }
}
