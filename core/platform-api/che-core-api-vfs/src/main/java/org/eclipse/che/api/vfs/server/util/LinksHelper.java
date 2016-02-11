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
package org.eclipse.che.api.vfs.server.util;

import org.eclipse.che.api.vfs.server.VirtualFileSystemFactory;
import org.eclipse.che.api.vfs.shared.dto.Link;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for creation links. See {@link org.eclipse.che.api.vfs.shared.dto.Item#getLinks()}
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class LinksHelper {
    public static Map<String, Link> createFileLinks(URI baseUri,
                                                    String wsName,
                                                    String itemId,
                                                    String latestVersionId,
                                                    String itemPath,
                                                    String mediaType,
                                                    boolean locked,
                                                    String parentId) {
        // This method is used extremely actively so it is imported to set size of map directly
        // and prevent unnecessary growth of map.
        final Map<String, Link> links = new HashMap<>(16, 1.0f);
        final UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri).path(VirtualFileSystemFactory.class)
                                                    .path(VirtualFileSystemFactory.class, "getFileSystem");

        links.put(Link.REL_SELF, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "item", itemId), Link.REL_SELF, MediaType.APPLICATION_JSON));

        links.put(Link.REL_ACL, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "acl", itemId), Link.REL_ACL, MediaType.APPLICATION_JSON));

        links.put(Link.REL_CONTENT, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "content", itemId), Link.REL_CONTENT, mediaType));

        links.put(Link.REL_DOWNLOAD_FILE, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "downloadfile", itemId), Link.REL_DOWNLOAD_FILE, mediaType));

        links.put(Link.REL_CONTENT_BY_PATH, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "contentbypath", itemPath.substring(1)),
                               Link.REL_CONTENT_BY_PATH, mediaType));

        links.put(Link.REL_VERSION_HISTORY, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "version-history", itemId), Link.REL_VERSION_HISTORY,
                               MediaType.APPLICATION_JSON));

        links.put(Link.REL_CURRENT_VERSION, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "item", latestVersionId), Link.REL_CURRENT_VERSION,
                               MediaType.APPLICATION_JSON));

        if (locked) {
            links.put(Link.REL_UNLOCK, //
                      createLink(createURI(baseUriBuilder.clone(), wsName, "unlock", itemId, "lockToken", "[lockToken]"),
                                   Link.REL_UNLOCK, null));
        } else {
            links.put(Link.REL_LOCK, //
                      createLink(createURI(baseUriBuilder.clone(), wsName, "lock", itemId, "timeout", "[timeout]"),
                                   Link.REL_LOCK, MediaType.APPLICATION_JSON));
        }

        links.put(Link.REL_DELETE, //
                  createLink(locked
                               ? createURI(baseUriBuilder.clone(), wsName, "delete", itemId, "lockToken", "[lockToken]")
                               : createURI(baseUriBuilder.clone(), wsName, "delete", itemId),
                               Link.REL_DELETE, null));

        links.put(Link.REL_COPY, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "copy", itemId, "parentId", "[parentId]"), Link.REL_COPY,
                               MediaType.APPLICATION_JSON));

        links.put(Link.REL_MOVE, //
                  createLink(locked
                               ? createURI(baseUriBuilder.clone(), wsName, "move", itemId, "parentId", "[parentId]", "lockToken",
                                           "[lockToken]")
                               : createURI(baseUriBuilder.clone(), wsName, "move", itemId, "parentId", "[parentId]"),
                               Link.REL_MOVE, MediaType.APPLICATION_JSON));

        links.put(Link.REL_PARENT, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "item", parentId), Link.REL_PARENT, MediaType.APPLICATION_JSON));

        links.put(Link.REL_RENAME, //
                  createLink(locked
                               ? createURI(baseUriBuilder.clone(), wsName, "rename", itemId, "newname", "[newname]", "mediaType",
                                           "[mediaType]", "lockToken", "[lockToken]")
                               : createURI(baseUriBuilder.clone(), wsName, "rename", itemId, "newname", "[newname]", "mediaType",
                                           "[mediaType]"),
                               Link.REL_RENAME, MediaType.APPLICATION_JSON));

        return links;
    }

    public static Map<String, Link> createFolderLinks(URI baseUri,
                                                      String wsName,
                                                      String itemId,
                                                      boolean isRoot,
                                                      String parentId) {
        final UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri).path(VirtualFileSystemFactory.class)
                                                    .path(VirtualFileSystemFactory.class, "getFileSystem");
        final Map<String, Link> links = new HashMap<>(32, 1.0f);
        addBaseFolderLinks(links, baseUriBuilder, wsName, itemId, isRoot, parentId);
        return links;
    }

    private static void addBaseFolderLinks(Map<String, Link> links,
                                           UriBuilder baseUriBuilder,
                                           String wsName,
                                           String id,
                                           boolean isRoot,
                                           String parentId) {
        links.put(Link.REL_SELF, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "item", id), Link.REL_SELF, MediaType.APPLICATION_JSON));

        links.put(Link.REL_ACL, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "acl", id), Link.REL_ACL, MediaType.APPLICATION_JSON));

        if (!isRoot) {
            links.put(Link.REL_PARENT, //
                      createLink(createURI(baseUriBuilder.clone(), wsName, "item", parentId), Link.REL_PARENT,
                                   MediaType.APPLICATION_JSON));

            links.put(Link.REL_DELETE, //
                      createLink(createURI(baseUriBuilder.clone(), wsName, "delete", id), Link.REL_DELETE, null));

            links.put(Link.REL_COPY, //
                      createLink(createURI(baseUriBuilder.clone(), wsName, "copy", id, "parentId", "[parentId]"), Link.REL_COPY,
                                   MediaType.APPLICATION_JSON));

            links.put(Link.REL_MOVE, //
                      createLink(createURI(baseUriBuilder.clone(), wsName, "move", id, "parentId", "[parentId]"), Link.REL_MOVE,
                                   MediaType.APPLICATION_JSON));

            links.put(
                    Link.REL_RENAME, //
                    createLink(createURI(baseUriBuilder.clone(), wsName, "rename", id, "newname", "[newname]", "mediaType", "[mediaType]"),
                                 Link.REL_RENAME, MediaType.APPLICATION_JSON));
        }

        links.put(Link.REL_CHILDREN, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "children", id), Link.REL_CHILDREN, MediaType.APPLICATION_JSON));

        links.put(Link.REL_TREE, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "tree", id), Link.REL_TREE, MediaType.APPLICATION_JSON));

        links.put(Link.REL_CREATE_FOLDER, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "folder", id, "name", "[name]"), Link.REL_CREATE_FOLDER,
                               MediaType.APPLICATION_JSON));

        links.put(Link.REL_CREATE_FILE, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "file", id, "name", "[name]"), Link.REL_CREATE_FILE,
                               MediaType.APPLICATION_JSON));

        links.put(Link.REL_UPLOAD_FILE, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "uploadfile", id), Link.REL_UPLOAD_FILE, MediaType.TEXT_HTML));

        links.put(Link.REL_EXPORT, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "export", id), Link.REL_EXPORT, ExtMediaType.APPLICATION_ZIP));

        links.put(Link.REL_IMPORT, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "import", id), Link.REL_IMPORT, ExtMediaType.APPLICATION_ZIP));

        links.put(Link.REL_DOWNLOAD_ZIP, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "downloadzip", id), Link.REL_DOWNLOAD_ZIP, ExtMediaType.APPLICATION_ZIP));

        links.put(Link.REL_UPLOAD_ZIP, //
                  createLink(createURI(baseUriBuilder.clone(), wsName, "uploadzip", id), Link.REL_UPLOAD_ZIP, MediaType.TEXT_HTML));
    }

    public static Map<String, Link> createUrlTemplates(URI baseUri, String wsName) {
        final Map<String, Link> templates = new HashMap<>(16, 1.0f);
        final UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri).path(VirtualFileSystemFactory.class)
                                                    .path(VirtualFileSystemFactory.class, "getFileSystem");

        templates.put(Link.REL_ITEM,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "item", "[id]"), Link.REL_ITEM, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_ITEM_BY_PATH,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "itembypath", "[path]"), Link.REL_ITEM_BY_PATH,
                                   MediaType.APPLICATION_JSON));

        templates.put(Link.REL_TREE,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "tree", "[id]"), Link.REL_TREE, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_CREATE_FILE,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "file", "[parentId]", "name", "[name]"),
                                   Link.REL_CREATE_FILE, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_CREATE_FOLDER,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "folder", "[parentId]", "name", "[name]"),
                                   Link.REL_CREATE_FOLDER, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_COPY,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "copy", "[id]", "parentId", "[parentId]"),
                                   Link.REL_COPY, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_MOVE,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "move", "[id]", "parentId", "[parentId]", "lockToken",
                                             "[lockToken]"), Link.REL_MOVE, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_LOCK,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "lock", "[id]", "timeout", "[timeout]"),
                                   Link.REL_LOCK, MediaType.APPLICATION_JSON));

        templates.put(Link.REL_UNLOCK,
                      createLink(createURI(baseUriBuilder.clone(), wsName, "unlock", "[id]", "lockToken", "[lockToken]"),
                                   Link.REL_UNLOCK, null));

        templates.put(
                Link.REL_SEARCH_FORM,
                createLink(createURI(baseUriBuilder.clone(), wsName, "search", null, "maxItems", "[maxItems]", "skipCount",
                                       "[skipCount]", "propertyFilter", "[propertyFilter]"), Link.REL_SEARCH_FORM,
                             MediaType.APPLICATION_JSON));

        templates.put(
                Link.REL_SEARCH,
                createLink(createURI(baseUriBuilder.clone(), wsName, "search", null, "statement", "[statement]", "maxItems",
                                       "[maxItems]", "skipCount", "[skipCount]"), Link.REL_SEARCH, MediaType.APPLICATION_JSON));

        return templates;
    }

    private static Link createLink(String href, String rel, String type) {
        return DtoFactory.getInstance().createDto(Link.class).withHref(href).withRel(rel).withType(type);
    }

    private static String createURI(UriBuilder baseUriBuilder, String wsName, String rel, String id, String... query) {
        final UriBuilder myUriBuilder = baseUriBuilder.path(rel);
        if (id != null) {
            myUriBuilder.path(id);
        }
        if (query != null && query.length > 0) {
            for (int i = 0; i < query.length; i++) {
                String name = query[i];
                String value = i < query.length ? query[++i] : "";
                myUriBuilder.queryParam(name, value);
            }
        }

        return myUriBuilder.build(wsName).toString();
    }

    private static String createURI(UriBuilder baseUriBuilder, String wsName, String rel, String id) {
        final UriBuilder myUriBuilder = baseUriBuilder.path(rel);
        if (id != null) {
            myUriBuilder.path(id);
        }
        return myUriBuilder.build(wsName).toString();
    }

    private LinksHelper() {
    }
}
