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
package org.eclipse.che.ide.api.project.tree;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;

/**
 * Information about custom virtual file.
 *
 * @author Valeriy Svydenko
 */
public class VirtualFileInfo {
    private boolean          isReadOnly;
    private String           path;
    private String           name;
    private String           displayName;
    private String           mediaType;
    private String           contentUrl;
    private String           content;
    private HasProjectConfig project;
    private Promise<Void>    updateContent;

    private VirtualFileInfo() {
    }

    /** get file path */
    public String getPath() {
        return path;
    }

    /** get name */
    public String getName() {
        return name;
    }

    /** get display name */
    public String getDisplayName() {
        return displayName;
    }

    /** get media type */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Some file type can't represent their content as string.
     * So virtual file provide url where it content.
     * For example if this virtual file represent image,
     * image viewer may use this URL as src for {@link com.google.gwt.user.client.ui.Image}
     *
     * @return url content url
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /** get project config */
    public HasProjectConfig getProject() {
        return project;
    }

    /** Get content of the file which this node represents. */
    public String getContent() {
        return content;
    }

    /**
     * Update content of the file.
     * Note: this method is optional, some implementations may not support updating their content
     *
     * @return update content
     */
    public Promise<Void> getUpdateContent() {
        return updateContent;
    }

    /** if user doesn't have wright rights, or file comes from external sources thad doesn't support modifying file content */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Create new builder for {@link VirtualFileInfo}
     *
     * @return builder for {@link VirtualFileInfo}
     */
    public static Builder newBuilder() {
        return new VirtualFileInfo().new Builder();
    }

    /**
     * Builder utility class for {@link VirtualFileInfo}
     */
    public class Builder {
        private Builder() {
        }

        /**
         * Set file path.
         *
         * @param path
         *         file path
         * @return builder
         */
        public Builder setPath(String path) {
            VirtualFileInfo.this.path = path;

            return this;
        }

        /**
         * Set file name.
         *
         * @param name
         *         file name
         * @return builder
         */
        public Builder setName(String name) {
            VirtualFileInfo.this.name = name;

            return this;
        }

        /**
         * Set file display name.
         *
         * @param displayName
         *         file name
         * @return builder
         */
        public Builder setDisplayName(String displayName) {
            VirtualFileInfo.this.displayName = displayName;

            return this;
        }

        /**
         * Set display name.
         *
         * @param mediaType
         *         media type
         * @return builder
         */
        public Builder setMediaType(String mediaType) {
            VirtualFileInfo.this.mediaType = mediaType;

            return this;
        }

        /**
         * Set content url.
         *
         * @param contentUrl
         *         url of the content
         * @return builder
         */
        public Builder setContentUrl(String contentUrl) {
            VirtualFileInfo.this.contentUrl = contentUrl;

            return this;
        }

        /**
         * Set project config.
         *
         * @param project
         *         current project configuration
         * @return builder
         */
        public Builder setProject(HasProjectConfig project) {
            VirtualFileInfo.this.project = project;

            return this;
        }

        /**
         * Set content.
         *
         * @param content
         *         content of the file
         * @return builder
         */
        public Builder setContent(String content) {
            VirtualFileInfo.this.content = content;

            return this;
        }

        /**
         * Set update content.
         *
         * @param updateContent
         *         update content
         * @return builder
         */
        public Builder setUpdateContent(Promise<Void> updateContent) {
            VirtualFileInfo.this.updateContent = updateContent;

            return this;
        }

        /**
         * Set read only value.
         *
         * @param isReadOnly
         *         read only properties
         * @return builder
         */
        public Builder isReadOnly(boolean isReadOnly) {
            VirtualFileInfo.this.isReadOnly = isReadOnly;

            return this;
        }

        public VirtualFileInfo build() {
            return VirtualFileInfo.this;
        }
    }
}
