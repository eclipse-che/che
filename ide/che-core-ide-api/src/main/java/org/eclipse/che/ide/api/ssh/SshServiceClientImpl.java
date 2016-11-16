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
package org.eclipse.che.ide.api.ssh;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.RestContext;

import java.util.List;

/**
 * The implementation of {@link SshServiceClient}.
 *
 * @author Sergii Leschenko
 */
public class SshServiceClientImpl implements SshServiceClient {
    private final DtoFactory             dtoFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final String                 sshApi;

    @Inject
    protected SshServiceClientImpl(@RestContext String baseUrl,
                                   DtoFactory dtoFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory unmarshallerFactory) {
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.unmarshallerFactory = unmarshallerFactory;
        this.sshApi = baseUrl + "/ssh";
    }

    /**
     * Gets ssh pair of given service and specific name
     * @param service the service name
     * @param name the identifier of one the pair
     */
    @Override
    public Promise<SshPairDto> getPair(String service, String name) {
        return asyncRequestFactory.createGetRequest(sshApi + "/" + service + "/" + name)
                                  .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                                  .send(unmarshallerFactory.newUnmarshaller(SshPairDto.class));
    }

    @Override
    public Promise<List<SshPairDto>> getPairs(String service) {
        return asyncRequestFactory.createGetRequest(sshApi + "/" + service)
                                  .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                                  .send(unmarshallerFactory.newListUnmarshaller(SshPairDto.class));
    }

    @Override
    public Promise<SshPairDto> generatePair(String service, String name) {
        return asyncRequestFactory.createPostRequest(sshApi + "/generate", dtoFactory.createDto(GenerateSshPairRequest.class)
                                                                                     .withService(service)
                                                                                     .withName(name))
                                  .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                                  .send(unmarshallerFactory.newUnmarshaller(SshPairDto.class));
    }

    @Override
    public Promise<Void> deletePair(String service, String name) {
        return asyncRequestFactory.createDeleteRequest(sshApi + "/" + service + "?name=" + name)
                                  .send();
    }
}
