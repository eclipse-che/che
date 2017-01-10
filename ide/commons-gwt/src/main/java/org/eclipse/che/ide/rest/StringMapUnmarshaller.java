/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.rest;

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.json.JsonHelper;
import com.google.gwt.http.client.Response;

import java.util.Map;

/**
 * @author Eugene Voevodin
 */
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
