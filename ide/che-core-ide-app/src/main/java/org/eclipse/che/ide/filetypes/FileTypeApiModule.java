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
package org.eclipse.che.ide.filetypes;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;

/**
 * GIN module for configuring File Type API components.
 *
 * @author Evgen Vidolob
 */
public class FileTypeApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(FileTypeRegistry.class).to(FileTypeRegistryImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("defaultFileType")
    protected FileType provideDefaultFileType(Resources resources) {
        return new FileType(resources.defaultFile(), null);
    }

    @Provides
    @Singleton
    @Named("XMLFileType")
    protected FileType provideXMLFile(Resources resources) {
        return new FileType(resources.xmlFile(), "xml");
    }

    @Provides
    @Singleton
    @Named("TXTFileType")
    protected FileType provideTXTFile(Resources resources) {
        return new FileType(resources.defaultFile(), "txt");
    }

    @Provides
    @Singleton
    @Named("JsonFileType")
    protected FileType provideJsonFile(Resources resources) {
        return new FileType(resources.jsonFile(), "json");
    }

    @Provides
    @Singleton
    @Named("MDFileType")
    protected FileType provideMDFile(Resources resources) {
        return new FileType(resources.mdFile(), "md");
    }

    @Provides
    @Singleton
    @Named("PNGFileType")
    protected FileType providePNGFile(Resources resources) {
        return new FileType(resources.defaultImage(), "png");
    }

    @Provides
    @Singleton
    @Named("BMPFileType")
    protected FileType provideBMPFile(Resources resources) {
        return new FileType(resources.defaultImage(), "bmp");
    }

    @Provides
    @Singleton
    @Named("GIFFileType")
    protected FileType provideGIFFile(Resources resources) {
        return new FileType(resources.defaultImage(), "gif");
    }

    @Provides
    @Singleton
    @Named("ICOFileType")
    protected FileType provideICOFile(Resources resources) {
        return new FileType(resources.defaultImage(), "ico");
    }

    @Provides
    @Singleton
    @Named("SVGFileType")
    protected FileType provideSVGFile(Resources resources) {
        return new FileType(resources.defaultImage(), "svg");
    }

    @Provides
    @Singleton
    @Named("JPEFileType")
    protected FileType provideJPEFile(Resources resources) {
        return new FileType(resources.defaultImage(), "jpe");
    }

    @Provides
    @Singleton
    @Named("JPEGFileType")
    protected FileType provideJPEGFile(Resources resources) {
        return new FileType(resources.defaultImage(), "jpeg");
    }

    @Provides
    @Singleton
    @Named("JPGFileType")
    protected FileType provideJPGFile(Resources resources) {
        return new FileType(resources.defaultImage(), "jpg");
    }
}
