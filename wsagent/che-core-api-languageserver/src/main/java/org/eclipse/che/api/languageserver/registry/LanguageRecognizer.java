/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.registry;

import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

/**
 * The implementations of this interface are responsible for language recognition of a file under
 * defined path.
 */
public interface LanguageRecognizer {
  LanguageDescription UNIDENTIFIED =
      new LanguageDescription() {
        @Override
        public String getLanguageId() {
          return "unidentified";
        }

        @Override
        public String getMimeType() {
          return "";
        }

        @Override
        public String getHighlightingConfiguration() {
          return "";
        }
      };

  /**
   * Recognize a language by file path. If language cannot be recognized for any reason than the
   * implementation must return {@link LanguageRecognizer#UNIDENTIFIED}.
   *
   * @param wsPath workspace path of a file
   * @return description of language that was recognized
   */
  LanguageDescription recognizeByPath(String wsPath);

  /**
   * Recognize a language by it's identifier. If language cannot be recognized for any reason than
   * the implementation must return {@link LanguageRecognizer#UNIDENTIFIED}.
   *
   * @param id language identifier
   * @return description of language that was recognized
   */
  LanguageDescription recognizeById(String id);
}
