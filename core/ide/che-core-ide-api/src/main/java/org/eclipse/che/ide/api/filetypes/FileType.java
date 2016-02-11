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
package org.eclipse.che.ide.api.filetypes;

import com.google.gwt.resources.client.ImageResource;

import org.eclipse.che.ide.collections.ListHelper;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * FileType is meta information about file.
 * It's contains
 * <ul>
 * <li> <code>contentDescription</code> - string description associated with file content
 * <li> <code>image</code> - image resource associated with file
 * <li> <code>mimeTypes</code> - array of mime types associated with file
 * <li> <code>extension</code> - extension associated with file
 * <li> <code>namePattern</code> - name pattern
 * </ul>
 * <p>Mime types is array in case when one file type can describe several mime types.(For example JavaScript file can have:
 * "application/javascript", "application/x-javascript", "text/javascript" mime types)
 * <p/>
 *
 * @author Evgen Vidolob
 */
public class FileType {

    private String id;

    private ImageResource image;

    private SVGResource imageSVG;

    private List<String> mimeTypes;

    private String extension;

    private String namePattern;

    private String contentDescription;

    @SuppressWarnings("unchecked")
    public FileType(ImageResource image, String mimeType, String extension) {
        this(null, image, null, Arrays.asList(mimeType), extension, null);
    }

    public FileType(SVGResource imageSVG, String mimeType, String extension) {
        this(null, null, imageSVG, Arrays.asList(mimeType), extension, null);
    }

    public FileType(String contentDescription, ImageResource image, String mimeType, String extension) {
        this(contentDescription, image, null, Arrays.asList(mimeType), extension, null);
    }

    public FileType(String contentDescription, SVGResource imageSVG, String mimeType, String extension) {
        this(contentDescription, null, imageSVG, Arrays.asList(mimeType), extension, null);
    }

    public FileType(ImageResource image, List<String> mimeTypes, String extension) {
        this(null, image, null, mimeTypes, extension, null);
    }

    public FileType(SVGResource imageSVG, List<String> mimeTypes, String extension) {
        this(null, null, imageSVG, mimeTypes, extension, null);
    }

    public FileType(String contentDescription, ImageResource image, List<String> mimeTypes, String extension) {
        this(contentDescription, image, null, mimeTypes, extension, null);
    }

    public FileType(String contentDescription, SVGResource imageSVG, List<String> mimeTypes, String extension) {
        this(contentDescription, null, imageSVG, mimeTypes, extension, null);
    }

    public FileType(ImageResource image, String namePattern) {
        this(null, image, null, null, null, namePattern);
    }

    public FileType(SVGResource imageSVG, String namePattern) {
        this(null, null, imageSVG, null, null, namePattern);
    }

    public FileType(String contentDescription, ImageResource image, String namePattern) {
        this(contentDescription, image, null, null, null, namePattern);
    }

    public FileType(String contentDescription, SVGResource imageSVG, String namePattern) {
        this(contentDescription, null, imageSVG, null, null, namePattern);
    }

    private FileType(String contentDescription, ImageResource image, SVGResource imageSVG, List<String> mimeTypes, String extension,
                     String namePattern) {
        super();
        this.contentDescription = contentDescription;
        this.image = image;
        this.imageSVG = imageSVG;
        this.mimeTypes = mimeTypes == null ? Collections.<String>emptyList() : mimeTypes;
        this.extension = extension;
        this.namePattern = namePattern;
        id = contentDescription + (mimeTypes == null ? "noMimeType" : ListHelper.join(mimeTypes, ",") + namePattern);
    }


    /** @return the contentDescription */
    public String getContentDescription() {
        return contentDescription;
    }

    /** @return the mimeTypes */
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    /** @return the extension */
    public String getExtension() {
        return extension;
    }

    /** @return the namePatterns */
    public String getNamePattern() {
        return namePattern;
    }

    /** @return the image resource */
    public ImageResource getImage() {
        return image;
    }

    /** @return the SVG resource */
    public SVGResource getSVGImage() {
        return imageSVG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileType fileType = (FileType)o;

        if (contentDescription != null ? !contentDescription.equals(fileType.contentDescription) : fileType.contentDescription != null)
            return false;
        if (extension != null ? !extension.equals(fileType.extension) : fileType.extension != null) return false;
        if (mimeTypes != null ? !mimeTypes.equals(fileType.mimeTypes) : fileType.mimeTypes != null) return false;
        if (namePattern != null ? !namePattern.equals(fileType.namePattern) : fileType.namePattern != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mimeTypes != null ? mimeTypes.hashCode() : 0;
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (namePattern != null ? namePattern.hashCode() : 0);
        result = 31 * result + (contentDescription != null ? contentDescription.hashCode() : 0);
        return result;
    }

    public String getId() {
        return id;
    }
}
