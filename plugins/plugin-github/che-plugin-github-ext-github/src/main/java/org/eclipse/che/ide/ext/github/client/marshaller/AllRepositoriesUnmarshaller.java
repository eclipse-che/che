/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.github.client.marshaller;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.github.shared.GitHubRepository;
import org.eclipse.che.ide.rest.Unmarshallable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unmarshaller for all repositories list.
 *
 * @author <a href="mailto:dvishinskiy@codenvy.com">Dmitriy Vyshinskiy</a>
 */
public class AllRepositoriesUnmarshaller implements Unmarshallable<Map<String, List<GitHubRepository>>> {
    /** Repositories list. */
    private Map<String, List<GitHubRepository>> repositories;
    private DtoFactory                          dtoFactory;

    public AllRepositoriesUnmarshaller(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void unmarshal(Response response) throws UnmarshallerException {
        JSONObject jsonObj = JSONParser.parseStrict(response.getText()).isObject();

        if (jsonObj == null) {
            return;
        }

        Set<String> keys = jsonObj.keySet();
        repositories = new HashMap<>();

        for (String key : keys) {
            JSONArray jsonArray = jsonObj.get(key).isArray();
            List<GitHubRepository> repos = dtoFactory.createListDtoFromJson(jsonArray.toString(), GitHubRepository.class);
            repositories.put(key, repos);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, List<GitHubRepository>> getPayload() {
        return repositories;
    }
}