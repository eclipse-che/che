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
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class UrlRewriterProvider implements Provider<URLRewriter> {

  private URLRewriter instance;
  private final String REWRITER_PROPERTY_NAME = "che.infra.docker.url_rewriter";

  @Inject
  public UrlRewriterProvider(
      Map<String, URLRewriter> rewriters,
      @Nullable @Named(REWRITER_PROPERTY_NAME) String rewriter) {
    if (rewriter != null) {
      this.instance = rewriters.get(rewriter);
    } else {
      this.instance = rewriters.get("default");
    }

    if (instance == null) {
      throw new IllegalStateException(
          String.format(
              "Value of the property %s=%s doesn't match any installed URL rewriters.",
              REWRITER_PROPERTY_NAME, rewriter));
    }
  }

  @Override
  public URLRewriter get() {
    return instance;
  }
}
