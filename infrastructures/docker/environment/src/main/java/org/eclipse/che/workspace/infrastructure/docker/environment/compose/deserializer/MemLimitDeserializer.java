/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer;

import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.eclipse.che.commons.lang.Size;

/**
 * Deserialize Compose memory limit value, which could be either a plain byte number, or a string
 * followed by byte units (e.g. "1mb"), which is parsed, using {@link Size} class.
 *
 * @author Mykhailo Kuznietsov
 */
public class MemLimitDeserializer extends JsonDeserializer<Long> {
  private static final String UNSUPPORTED_VALUE_MESSAGE = "Unsupported value '%s'.";

  @Override
  public Long deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    Object memLimit = jsonParser.readValueAs(Object.class);
    if (memLimit instanceof Long) {
      return (Long) memLimit;
    }
    if (memLimit instanceof Integer) {
      return ((Integer) memLimit).longValue();
    }
    if (memLimit instanceof String) {
      return Size.parseSize((String) memLimit);
    }
    throw ctxt.mappingException(format(UNSUPPORTED_VALUE_MESSAGE, memLimit));
  }
}
