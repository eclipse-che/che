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

import org.eclipse.che.api.vfs.shared.ItemType;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * Representation of abstract item used to interaction with client via JSON.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
@DTO
public interface Item {
    /** @return id of virtual file system that contains object */
    String getVfsId();

    Item withVfsId(String vfsId);

    void setVfsId(String vfsId);

    /** @return id of object */
    String getId();

    Item withId(String id);

    void setId(String id);

    /** @return name of object */
    String getName();

    Item withName(String name);

    void setName(String name);

    /** @return type of item */
    ItemType getItemType();

    Item withItemType(ItemType itemType);

    void setItemType(ItemType itemType);

    /** @return path */
    String getPath();

    Item withPath(String path);

    void setPath(String path);

    /** @return id of parent folder and <code>null</code> if current item is root folder */
    String getParentId();

    Item withParentId(String parentId);

    void setParentId(String parentId);

    /** @return creation date */
    long getCreationDate();

    Item withCreationDate(long creationDate);

    void setCreationDate(long creationDate);

    /** @return media type */
    String getMimeType();

    Item withMimeType(String mimeType);

    void setMimeType(String mimeType);

    /**
     * Other properties.
     *
     * @return properties. If there is no properties then empty list returned, never <code>null</code>
     */
    List<Property> getProperties();

    Item withProperties(List<Property> properties);

    void setProperties(List<Property> properties);

    /**
     * Links for retrieved or(and) manage item.
     *
     * @return links map. Never <code>null</code> but empty map instead
     */
    Map<String, Link> getLinks();

    Item withLinks(Map<String, Link> links);

    void setLinks(Map<String, Link> links);

    /**
     * Get permissions of current user. Current user is user who retrieved this item.
     *
     * @return set of permissions of current user.
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions
     */
    List<String> getPermissions();

    Item withPermissions(List<String> permissions);

    void setPermissions(List<String> permissions);
}
