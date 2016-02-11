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
package org.eclipse.che.api.ssh.gwt.client;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;

import java.util.List;

/**
 * The client service for working with ssh keys.
 *
 * @author Sergii Leschenko
 */
public interface SshServiceClient {
    /**
     * Gets ssh pairs of given service
     */
    Promise<List<SshPairDto>> getPairs(String service);

    /**
     * Generates new ssh key pair with given service and name
     */
    Promise<SshPairDto> generatePair(String service, String name);

    /**
     * Deletes ssh pair with given service and name
     */
    Promise<Void> deletePair(String service, String name);
}
