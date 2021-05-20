/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps getting content type of file.
 *
 * @author andrew00x
 */
public class ContentTypeGuesser {
  private static final Logger LOG = LoggerFactory.getLogger(ContentTypeGuesser.class);

  private static String defaultContentType = MediaType.APPLICATION_OCTET_STREAM;

  public static synchronized void setDefaultContentType(String myDefaultContentType) {
    defaultContentType = myDefaultContentType;
  }

  public static synchronized String getDefaultContentType() {
    return defaultContentType;
  }

  private static Properties properties = new Properties();

  static {
    final String filePath = System.getProperty("org.eclipse.che.content-types");
    URL resource = null;
    if (filePath != null) {
      resource = Thread.currentThread().getContextClassLoader().getResource(filePath);
    }
    if (resource == null) {
      resource =
          Thread.currentThread().getContextClassLoader().getResource("content-types.properties");
    }
    if (resource != null) {
      try (InputStream stream = resource.openStream()) {
        properties.load(stream);
        LOG.info("Successfully loaded content types from {}", resource);
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  public static String guessContentType(java.io.File file) {
    String contentType = null;
    final String name = file.getName();
    final int dot = name.lastIndexOf('.');
    if (dot > 0) {
      final String ext = name.substring(dot + 1);
      if (!ext.isEmpty()) {
        // by file extensions
        contentType = properties.getProperty(ext);
      }
    }
    if (contentType == null) {
      // by full file name.
      contentType = properties.getProperty(name);
    }
    /* Commented due to on Amazon infra with power instances. JVM crashes with multi-thread access (no problem with single thread).

    *** glibc detected *** /usr/local/jdk/bin/java: double free or corruption (out): 0x00007f22f0007820 ***
    ======= Backtrace: =========
    /lib64/libc.so.6[0x3da68760e6]
    /lib64/libc.so.6[0x3da6878c13]
    /lib64/libglib-2.0.so.0(g_array_free+0x7a)[0x3da7c13dda]
    /lib64/libgio-2.0.so.0[0x3da9c352f4]
    /lib64/libgobject-2.0.so.0(g_object_unref+0x15f)[0x3da8c0dacf]
    /usr/local/jdk1.7.0_17/jre/lib/amd64/libnio.so(Java_sun_nio_fs_GnomeFileTypeDetector_probeUsingGio+0xa2)[0x7f24aa7c7232]
    [0x7f24d3186db1]

    if (contentType == null) {
        try {
            // if content type is null use JDK.
            contentType = java.nio.file.Files.probeContentType(file.toPath());
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
    }*/
    return contentType == null ? getDefaultContentType() : contentType;
  }

  public static String guessContentType(String name) {
    String contentType = null;
    final int dot = name.lastIndexOf('.');
    if (dot > 0) {
      final String ext = name.substring(dot + 1);
      if (!ext.isEmpty()) {
        // by file extensions
        contentType = properties.getProperty(ext);
      }
    }
    if (contentType == null) {
      // by full file name.
      contentType = properties.getProperty(name);
    }
    return contentType == null ? getDefaultContentType() : contentType;
  }

  private ContentTypeGuesser() {}
}
