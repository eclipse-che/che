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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Alexander Garagatyi */
@Singleton
public class IngressAnnotationsProvider implements Provider<Map<String, String>> {

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
  private static final Type type = new TypeToken<Map<String, String>>() {}.getType();
  private final Map<String, String> annotations;
  private static final Logger LOG = LoggerFactory.getLogger(IngressAnnotationsProvider.class);

  @Inject
  public IngressAnnotationsProvider(
      @Nullable @Named("che.infra.kubernetes.ingress.annotations_json") String annotationsString) {

    if (annotationsString != null) {
      annotations = GSON.fromJson(annotationsString, type);
    } else {
      annotations = Collections.emptyMap();
      LOG.warn(
          "Ingresses annotations are absent. Make sure that workspace ingresses don't need "
              + "to be configured according to ingress controller.");
    }
  }

  @Override
  public Map<String, String> get() {
    return annotations;
  }
}
