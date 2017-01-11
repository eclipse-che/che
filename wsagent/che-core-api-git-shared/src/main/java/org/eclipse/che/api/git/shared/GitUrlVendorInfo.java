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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Information about Git service which support operation for specific git url.
 * Explanation: if we have git url like: "git@bitbucket.org:user/reponame.git" than we send to server request for getting information
 * about provider that should process this url, than we try to search through all enabled Git services that service, which matches for
 * pattern, and in response to client we send all necessary information about this provider(e.g. its base host, short name, authorization
 * scopes if it support authorization via oauth and if our url is ssh url) and client based on this info can call authorization for user.
 *
 * @author andrew00x
 */
@DTO
public interface GitUrlVendorInfo {

    /**
     * Get short Git service name.
     *
     * @return short service name
     */
    String getVendorName();

    /**
     * Get base Git service host.
     *
     * @return base Git service host
     */
    String getVendorBaseHost();

    /**
     * If current url is SSH.
     *
     * @return true is current url is SSH
     */
    boolean isGivenUrlSSH();

    /**
     * Get list of authorization scopes for specific Git service.
     *
     * @return list of scopes
     */
    List<String> getOAuthScopes();

    /**
     * Set short Git service name.
     *
     * @param vendorName
     *         short service name
     */
    void setVendorName(String vendorName);

    /**
     * Set short Git service name.
     *
     * @param vendorName
     *         short service name
     */
    GitUrlVendorInfo withVendorName(String vendorName);

    /**
     * Set base Git service host.
     *
     * @param vendorBaseHost
     *         base Git service host
     */
    void setVendorBaseHost(String vendorBaseHost);

    /**
     * Set base Git service host.
     *
     * @param vendorBaseHost
     *         base Git service host
     */

    GitUrlVendorInfo withVendorBaseHost(String vendorBaseHost);

    /**
     * Set list of authorization scopes for specific Git service.
     *
     * @param oauthScopes
     *         list of scopes
     */
    void setOAuthScopes(List<String> oauthScopes);

    /**
     * Set list of authorization scopes for specific Git service.
     *
     * @param oauthScopes
     *         list of scopes
     */
    GitUrlVendorInfo withOAuthScopes(List<String> oauthScopes);

    /**
     * Set if current url is SSH.
     *
     * @param givenUrlSSH
     *         true is current url is SSH
     */
    void setGivenUrlSSH(boolean givenUrlSSH);

    /**
     * Set if current url is SSH.
     *
     * @param givenUrlSSH
     *         true is current url is SSH
     */
    GitUrlVendorInfo withGivenUrlSSH(boolean givenUrlSSH);
}
