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
package org.eclipse.che.plugin.github.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;


/**
 * List of GitHub repositories for paging view.
 * 
 * @author <a href="mailto:ashumilova@codenvy.com">Ann Shumilova</a>
 * @version $Id:
 */
@DTO
public interface GitHubRepositoryList {

    /**
     * @return {@link List} the list of repositories
     */
    List<GitHubRepository> getRepositories();
    
    void setRepositories(List<GitHubRepository> repositories);


    /**
     * Link to the first page of the repositories list, if paging is used.
     * 
     * @return {@link String} first page link
     */
    String getFirstPage();
    
    void setFirstPage(String page);

    /**
     * Link to the previous page of the repositories list, if paging is used.
     * 
     * @return {@link String} previous page link
     */
    String getPrevPage();
    
    void setPrevPage(String page);

    /**
     * Link to the next page of the repositories list, if paging is used.
     * 
     * @return {@link String} next page link
     */
    String getNextPage();

    void setNextPage(String page);
    
    /**
     * Link to the last page of the repositories list, if paging is used.
     * 
     * @return {@link String} last page's link
     */
    String getLastPage();
    
    void setLastPage(String page);
}
