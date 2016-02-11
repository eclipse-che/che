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
package org.eclipse.che.api.vfs.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface Link {
    // Folder
    String REL_CHILDREN        = "children";
    String REL_TREE            = "tree";
    String REL_CREATE_FOLDER   = "create-folder";
    String REL_CREATE_FILE     = "create-file";
    String REL_UPLOAD_FILE     = "upload-file";
    String REL_EXPORT          = "export";
    String REL_IMPORT          = "import";
    String REL_DOWNLOAD_ZIP    = "download-zip";
    String REL_UPLOAD_ZIP      = "upload-zip";
    // File
    String REL_CURRENT_VERSION = "current-version";
    String REL_VERSION_HISTORY = "version-history";
    String REL_CONTENT         = "content";
    String REL_DOWNLOAD_FILE   = "download-file";
    String REL_CONTENT_BY_PATH = "content-by-path";
    String REL_UNLOCK          = "unlock";
    String REL_LOCK            = "lock";
    // Common
    String REL_PARENT          = "parent";
    String REL_DELETE          = "delete";
    String REL_MOVE            = "move";
    String REL_COPY            = "copy";
    String REL_SELF            = "self";
    String REL_ITEM            = "item";
    String REL_ITEM_BY_PATH    = "item-by-path";
    String REL_ACL             = "acl";
    String REL_RENAME          = "rename";
    String REL_SEARCH          = "search";
    String REL_SEARCH_FORM     = "search-form";

    String getHref();

    Link withHref(String href);

    void setHref(String href);

    String getRel();

    Link withRel(String rel);

    void setRel(String rel);

    String getType();

    Link withType(String type);

    void setType(String type);
}
