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
package org.eclipse.che.ide.rest;

import com.google.gwt.http.client.Response;
import java.util.Map;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.json.JsonHelper;

/** @author Eugene Voevodin */
public class StringMapUnmarshaller implements Unmarshallable<Map<String, String>> {

  private Map<String, String> payload;

  @Override
  public void unmarshal(Response response) throws UnmarshallerException {
    payload = JsonHelper.toMap(response.getText());
  }

  @Override
  public Map<String, String> getPayload() {
    return payload;
  }
}
