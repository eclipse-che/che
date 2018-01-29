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
package org.eclipse.che.workspace.infrastructure.docker;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class URLRewriterProvider implements Provider<URLRewriter> {

  private URLRewriter instance;
  private String rewriterValue;
  private final String PROPERTY_NAME = "che.docker.url_rewriter";

  @Inject
  public URLRewriterProvider(
      Map<String, URLRewriter> rewriters, @Nullable @Named(PROPERTY_NAME) String rewriter) {
    if (rewriter != null && rewriters.containsKey(rewriter)) {
      this.instance = rewriters.get(rewriter);
      rewriterValue = rewriter;
    } else {
      this.instance = rewriters.get("default");
    }
  }

  @PostConstruct
  private void checkRewriterIsPresent() throws Exception {
    if (instance == null) {
      throw new IllegalStateException(
          String.format(
              "Value of the property %s=%s doesn't match any installed URL rewriters.",
              PROPERTY_NAME, rewriterValue));
    }
  }

  @Override
  public URLRewriter get() {
    return instance;
  }
}
