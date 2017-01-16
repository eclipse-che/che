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
package org.eclipse.che.api.ssh.server;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Facade for Ssh related operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SshManager {
    private final JSch   genJSch;
    private final SshDao sshDao;

    @Inject
    public SshManager(SshDao sshDao) {
        this.sshDao = sshDao;
        this.genJSch = new JSch();
    }

    /**
     * Generates and stores ssh pair for specified user.
     *
     * @param owner
     *         the id of the user who will be the owner of the ssh pair
     * @param service
     *         service name pf ssh pair
     * @param name
     *         name of pair
     * @return instance of generated ssh pair
     * @throws ConflictException
     *         when given ssh pair cannot be generated or created
     * @throws ServerException
     *         when any other error occurs during ssh pair generating or creating
     */
    public SshPairImpl generatePair(String owner, String service, String name) throws ServerException, ConflictException {
        KeyPair keyPair;
        try {
            keyPair = KeyPair.genKeyPair(genJSch, 2, 2048);
        } catch (JSchException e) {
            throw new ServerException("Failed to generate ssh pair.", e);
        }

        ByteArrayOutputStream privateBuff = new ByteArrayOutputStream();
        keyPair.writePrivateKey(privateBuff);

        ByteArrayOutputStream publicBuff = new ByteArrayOutputStream();
        keyPair.writePublicKey(publicBuff, null);

        final SshPairImpl generatedSshPair = new SshPairImpl(owner,
                                                             service,
                                                             name,
                                                             publicBuff.toString(),
                                                             privateBuff.toString());
        sshDao.create(generatedSshPair);
        return generatedSshPair;
    }

    /**
     * Creates new ssh pair for specified user.
     *
     * @param sshPair
     *         ssh pair to create
     * @throws ConflictException
     *         when given ssh pair cannot be created
     * @throws ServerException
     *         when any other error occurs during ssh pair creating
     */
    public void createPair(SshPairImpl sshPair) throws ServerException, ConflictException {
        sshDao.create(sshPair);
    }

    /**
     * Returns ssh pair by owner, service and name.
     *
     * @param owner
     *         the id of the user who is the owner of the ssh pair
     * @param service
     *         service name of ssh pair
     * @param name
     *         name of ssh pair
     * @return ssh pair instance
     * @throws NotFoundException
     *         when ssh pair is not found
     * @throws ServerException
     *         when any other error occurs during ssh pair fetching
     */
    public SshPairImpl getPair(String owner, String service, String name) throws NotFoundException, ServerException {
        return sshDao.get(owner, service, name);
    }

    /**
     * Returns ssh pairs by owner and service.
     *
     * @param owner
     *         the id of the user who is the owner of the ssh pairs
     * @param service
     *         service name of ssh pair
     * @return list of ssh pair with given service and owned by given service.
     * @throws ServerException
     *         when any other error occurs during ssh pair fetching
     */
    public List<SshPairImpl> getPairs(String owner, String service) throws ServerException {
        return sshDao.get(owner, service);
    }

    /**
     * Removes ssh pair by owner, service and name.
     *
     * @param owner
     *         the id of the user who is the owner of the ssh pair
     * @param service
     *         service name of ssh pair
     * @param name
     *         of ssh pair
     * @throws NotFoundException
     *         when ssh pair is not found
     * @throws ServerException
     *         when any other error occurs during ssh pair removing
     */
    public void removePair(String owner, String service, String name) throws ServerException, NotFoundException {
        sshDao.remove(owner, service, name);
    }
}
