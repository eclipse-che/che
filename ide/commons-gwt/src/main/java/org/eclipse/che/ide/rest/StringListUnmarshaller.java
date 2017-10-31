/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.rest;

import com.google.gwt.http.client.Response;
import java.util.List;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.json.JsonHelper;

/** @author Yevhen Vydolob */
public class StringListUnmarshaller implements Unmarshallable<List<String>> {

  private List<String> payload;

  @Override
  public void unmarshal(Response response) throws UnmarshallerException {
    payload = JsonHelper.toList(response.getText());
  }

  @Override
  public List<String> getPayload() {
    return payload;
  }
}
