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
package org.eclipse.che.ide.rest;

import com.google.gwt.http.client.Response;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.json.JsonHelper;

/** @author Eugene Voevodin */
public class StringMapListUnmarshaller implements Unmarshallable<Map<String, List<String>>> {

  private Map<String, List<String>> payload;

  @Override
  public void unmarshal(Response response) throws UnmarshallerException {
    payload = JsonHelper.toMapOfLists(response.getText());
  }

  @Override
  public Map<String, List<String>> getPayload() {
    return payload;
  }
}
