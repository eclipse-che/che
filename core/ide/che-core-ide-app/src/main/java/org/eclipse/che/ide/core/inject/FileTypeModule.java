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
package org.eclipse.che.ide.core.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;

/**
 * @author Evgen Vidolob
 */
@ExtensionGinModule
public class FileTypeModule extends AbstractGinModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("XMLFileType")
    protected FileType provideXMLFile(Resources resources) {
        return new FileType("XML file", resources.xmlFile(), MimeType.TEXT_XML, "xml");
    }

    @Provides
    @Singleton
    @Named("TXTFileType")
    protected FileType provideTXTFile(Resources resources) {
        return new FileType("TXT File", resources.defaultFile(), MimeType.TEXT_PLAIN, "txt");
    }

    @Provides
    @Singleton
    @Named("JsonFileType")
    protected FileType provideJsonFile(Resources resources) {
        return new FileType("Json file", resources.jsonFile(), MimeType.APPLICATION_JSON, "json");
    }

    @Provides
    @Singleton
    @Named("MDFileType")
    protected FileType provideMDFile(Resources resources) {
        return new FileType("MD File", resources.mdFile(), MimeType.TEXT_MARKDOWN, "md");
    }

    @Provides
    @Singleton
    @Named("PNGFileType")
    protected FileType providePNGFile(Resources resources) {
        return new FileType("PNG Image", resources.defaultImage(), MimeType.IMAGE_PNG, "png");
    }

    @Provides
    @Singleton
    @Named("BMPFileType")
    protected FileType provideBMPFile(Resources resources) {
        return new FileType("Bitmap Image", resources.defaultImage(), MimeType.IMAGE_BMP, "bmp");
    }

    @Provides
    @Singleton
    @Named("GIFFileType")
    protected FileType provideGIFFile(Resources resources) {
        return new FileType("GIF Image", resources.defaultImage(), MimeType.IMAGE_GIF, "gif");
    }

    @Provides
    @Singleton
    @Named("ICOFileType")
    protected FileType provideICOFile(Resources resources) {
        return new FileType("ICO Image", resources.defaultImage(), MimeType.IMAGE_X_ICON, "ico");
    }

    @Provides
    @Singleton
    @Named("SVGFileType")
    protected FileType provideSVGFile(Resources resources) {
        return new FileType("SVG Image", resources.defaultImage(), MimeType.IMAGE_SVG_XML, "svg");
    }

    @Provides
    @Singleton
    @Named("JPEFileType")
    protected FileType provideJPEFile(Resources resources) {
        return new FileType("JPEG Image", resources.defaultImage(), MimeType.IMAGE_JPEG, "jpe");
    }

    @Provides
    @Singleton
    @Named("JPEGFileType")
    protected FileType provideJPEGFile(Resources resources) {
        return new FileType("JPEG Image", resources.defaultImage(), MimeType.IMAGE_JPEG, "jpeg");
    }

    @Provides
    @Singleton
    @Named("JPGFileType")
    protected FileType provideJPGFile(Resources resources) {
        return new FileType("JPEG Image", resources.defaultImage(), MimeType.IMAGE_JPEG, "jpg");
    }
}
