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
package org.eclipse.che.commons.json;

import org.everrest.core.impl.provider.json.JsonHandler;
import org.everrest.core.impl.provider.json.JsonParser;

/**
 * JSON parser that support transformation of names in JSON document.
 *
 * @see JsonNameConvention
 * @see JsonNameConventions
 */
public class NameConventionJsonParser extends JsonParser {
  public NameConventionJsonParser(JsonNameConvention nameConvention) {
    super(new NameConventionJsonHandler(nameConvention));
  }

  private static class NameConventionJsonHandler extends JsonHandler {
    private final JsonNameConvention nameConvention;

    private NameConventionJsonHandler(JsonNameConvention nameConvention) {
      this.nameConvention = nameConvention;
    }

    @Override
    public void key(String key) {
      super.key(nameConvention.toJavaName(key));
    }
  }
}
