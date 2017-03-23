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
package org.eclipse.che.plugin.github.factory.resolver;

import com.google.common.base.Strings;

/**
 * Representation of a github URL, allowing to get details from it.
 * <p> like
 * https://github.com/<username>/<repository>
 * https://github.com/<username>/<repository>/tree/<branch>
 *
 * @author Florent Benoit
 */
public class GithubUrl {

    /**
     * Master branch is the default.
     */
    private static final String DEFAULT_BRANCH_NAME = "master";

    /**
     * Username part of github URL
     */
    private String username;

    /**
     * Repository part of the URL.
     */
    private String repository;

    /**
     * Branch name (by default if it is omitted it is "master" branch)
     */
    private String branch = DEFAULT_BRANCH_NAME;

    /**
     * Subfolder if any
     */
    private String subfolder;


    /**
     * Dockerfile filename
     */
    private String dockerfileFilename;

    /**
     * Factory json filename
     */
    private String factoryFilename;


    /**
     * Creation of this instance is made by the parser so user may not need to create a new instance directly
     */
    protected GithubUrl() {

    }

    /**
     * Gets username of this github url
     *
     * @return the username part
     */
    public String getUsername() {
        return this.username;
    }

    public GithubUrl withUsername(String userName) {
        this.username = userName;
        return this;
    }

    /**
     * Gets repository of this github url
     *
     * @return the repository part
     */
    public String getRepository() {
        return this.repository;
    }

    protected GithubUrl withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Gets dockerfile file name of this github url
     *
     * @return the dockerfile file name
     */
    public String getDockerfileFilename() {
        return this.dockerfileFilename;
    }

    protected GithubUrl withDockerfileFilename(String dockerfileFilename) {
        this.dockerfileFilename = dockerfileFilename;
        return this;
    }

    /**
     * Gets factory file name of this github url
     *
     * @return the factory file name
     */
    public String getFactoryFilename() {
        return this.factoryFilename;
    }

    protected GithubUrl withFactoryFilename(String factoryFilename) {
        this.factoryFilename = factoryFilename;
        return this;
    }

    /**
     * Gets branch of this github url
     *
     * @return the branch part
     */
    public String getBranch() {
        return this.branch;
    }

    protected GithubUrl withBranch(String branch) {
        if (!Strings.isNullOrEmpty(branch)) {
            this.branch = branch;
        }
        return this;
    }

    /**
     * Gets subfolder of this github url
     *
     * @return the subfolder part
     */
    public String getSubfolder() {
        return this.subfolder;
    }

    /**
     * Sets the subfolder represented by the URL.
     *
     * @param subfolder
     *         path inside the getRepository
     * @return current github instance
     */
    protected GithubUrl withSubfolder(String subfolder) {
        this.subfolder = subfolder;
        return this;
    }

    /**
     * Provides the location to dockerfile
     *
     * @return location of dockerfile in a getRepository
     */
    protected String dockerFileLocation() {
        return "https://raw.githubusercontent.com/" + this.username + "/" + this.repository + "/" + this.branch + "/" +
               dockerfileFilename;
    }

    /**
     * Provides the location to factory json file
     *
     * @return location of factory json file in a getRepository
     */
    protected String factoryJsonFileLocation() {
        return "https://raw.githubusercontent.com/" + this.username + "/" + this.repository + "/" + this.branch + "/" + factoryFilename;
    }

    /**
     * Provides location to the repository part of the full github URL.
     *
     * @return location of the repository.
     */
    protected String repositoryLocation() {
        return "https://github.com/" + this.username + "/" + this.repository;
    }


}
