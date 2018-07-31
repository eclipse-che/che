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
package org.eclipse.che.ide.websocket.rest;

import com.google.gwt.json.client.JSONParser;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.websocket.Message;

/**
 * DTO unmarshaller for websocket messages.
 *
 * @author Artem Zatsarynnyi
 */
public class DtoUnmarshaller<T> implements Unmarshallable<T> {
  protected T payload;
  private Class<?> dtoInterface;
  private DtoFactory dtoFactory;

  public DtoUnmarshaller(Class<?> dtoInterface, DtoFactory dtoFactory) {
    this.dtoInterface = dtoInterface;
    this.dtoFactory = dtoFactory;
  }

  /** {@inheritDoc} */
  @Override
  public void unmarshal(Message message) {
    if (message.getBody() != null) {
      if (isJsonArray(message)) {
        payload = (T) dtoFactory.createListDtoFromJson(message.getBody(), dtoInterface);
      } else {
        payload = (T) dtoFactory.createDtoFromJson(message.getBody(), dtoInterface);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public T getPayload() {
    return payload;
  }

  private boolean isJsonArray(Message message) {
    return JSONParser.parseStrict(message.getBody()).isArray() != null;
  }
}
