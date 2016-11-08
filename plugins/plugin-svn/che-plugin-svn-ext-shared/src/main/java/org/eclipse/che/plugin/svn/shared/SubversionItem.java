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
package org.eclipse.che.plugin.svn.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Definition of subversion item.
 */
@DTO
public interface SubversionItem {

    /**************************************************************************
     *
     *  Path
     *
     **************************************************************************/

    String getPath();

    void setPath(String path);

    SubversionItem withPath(String path);

    /**************************************************************************
     *
     *  Name
     *
     **************************************************************************/

    String getName();

    void setName(String name);

    SubversionItem withName(String name);

    /**************************************************************************
     *
     *  URL
     *
     **************************************************************************/

    String getURL();

    void setURL(String url);

    SubversionItem withURL(String url);

    /**************************************************************************
     *
     *  Relative URL
     *
     **************************************************************************/

    String getRelativeURL();

    void setRelativeURL(String relativeURL);

    SubversionItem withRelativeURL(String relativeURL);

    /**************************************************************************
     *
     *  Repository Root
     *
     **************************************************************************/

    String getRepositoryRoot();

    void setRepositoryRoot(String repositoryRoot);

    SubversionItem withRepositoryRoot(String repositoryRoot);

    /**************************************************************************
     *
     *  Repository UUID
     *
     **************************************************************************/

    String getRepositoryUUID();

    void setRepositoryUUID(String repositoryUUID);

    SubversionItem withRepositoryUUID(String repositoryUUID);

    /**************************************************************************
     *
     *  Revision
     *
     **************************************************************************/

    String getRevision();

    void setRevision(String revision);

    SubversionItem withRevision(String revision);

    /**************************************************************************
     *
     *  Node Kind
     *
     **************************************************************************/

    String getNodeKind();

    void setNodeKind(String nodeKind);

    SubversionItem withNodeKind(String nodeKind);

    /**************************************************************************
     *
     *  Schedule
     *
     **************************************************************************/

    String getSchedule();

    void setSchedule(String schedule);

    SubversionItem withSchedule(String schedule);

    /**************************************************************************
     *
     *  Last Changed Revision
     *
     **************************************************************************/

    String getLastChangedRev();

    void setLastChangedRev(String lastChangedRev);

    SubversionItem withLastChangedRev(String lastChangedRev);

    /**************************************************************************
     *
     *  Last Changed Date
     *
     **************************************************************************/

    String getLastChangedDate();

    void setLastChangedDate(String lastChangedDate);

    SubversionItem withLastChangedDate(String lastChangedDate);


    /**************************************************************************
     *
     *  Project URL
     *
     **************************************************************************/

    String getProjectUri();

    void setProjectUri(String projectUri);

    SubversionItem withProjectUri(String projectUri);

}
