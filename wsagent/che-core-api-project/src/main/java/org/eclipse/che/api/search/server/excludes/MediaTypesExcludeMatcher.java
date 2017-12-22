/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.search.server.excludes;

import static com.google.common.collect.Sets.newHashSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;

/**
 * Filter based on media type of the file. The filter includes in result files with media type
 * different from the specified types in the set {@link MediaTypesExcludeMatcher#excludedMediaTypes}
 * Note: if media type can not be detected a file will be not include in result as well.
 *
 * @author Valeriy Svydenko
 * @author Roman Nikitenko
 */
@Singleton
public class MediaTypesExcludeMatcher implements PathMatcher {

  private final Set<MediaType> excludedMediaTypes;
  private final Set<String> excludedTypes;

  private final FsManager fsManager;
  private final PathTransformer pathTransformer;

  @Inject
  public MediaTypesExcludeMatcher(FsManager fsManager, PathTransformer pathTransformer) {
    this.pathTransformer = pathTransformer;
    this.excludedMediaTypes = newHashSet(MediaType.APPLICATION_ZIP, MediaType.OCTET_STREAM);
    this.excludedTypes = newHashSet("video", "audio", "image");
    this.fsManager = fsManager;
  }

  public void addExcludedMediaType(MediaType mediaType) {
    excludedMediaTypes.add(mediaType);
  }

  public void addExcludedTypes(String type) {
    excludedTypes.add(type);
  }

  @Override
  public boolean matches(Path fsPath) {
    if (!fsPath.toFile().exists()) {
      return false;
    }

    if (!fsPath.toFile().isFile()) {
      return false;
    }

    if (fsPath.toFile().length() == 0) {
      return false;
    }

    String wsPath = pathTransformer.transform(fsPath);

    MediaType mimeType;
    try (InputStream content = fsManager.read(wsPath)) {
      mimeType = new TikaConfig().getDetector().detect(content, new Metadata());
    } catch (TikaException
        | IOException
        | NotFoundException
        | ServerException
        | ConflictException e0) {
      try {
        // https://issues.apache.org/jira/browse/TIKA-2395
        byte[] content = fsManager.readAsString(wsPath).getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        mimeType = new TikaConfig().getDetector().detect(bais, new Metadata());
      } catch (TikaException
          | IOException
          | NotFoundException
          | ServerException
          | ConflictException e1) {
        return true;
      }
    }

    return excludedMediaTypes.contains(mimeType) || excludedTypes.contains(mimeType.getType());
  }
}
