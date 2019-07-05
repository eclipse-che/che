/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor.signature;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.editor.signature.ParameterInfo;
import org.eclipse.che.ide.api.editor.signature.SignatureInfo;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/** @author Evgen Vidolob */
class SignatureInfoImpl implements SignatureInfo {

  private final SignatureInformation dto;

  private List<ParameterInfo> parameterInfos;

  public SignatureInfoImpl(SignatureInformation dto) {
    this.dto = dto;
  }

  @Override
  public String getLabel() {
    return dto.getLabel();
  }

  @Override
  public Optional<String> getDocumentation() {
    Either<String, MarkupContent> doc = dto.getDocumentation();
    // both markdown and plain text are ok.
    if (doc == null) {
      return Optional.absent();
    }
    return Optional.fromNullable(doc.isLeft() ? doc.getLeft() : doc.getRight().getValue());
  }

  @Override
  public Optional<List<ParameterInfo>> getParameters() {
    if (parameterInfos == null && dto.getParameters() != null) {
      parameterInfos = new ArrayList<>(dto.getParameters().size());
      for (ParameterInformation informationDTO : dto.getParameters()) {
        parameterInfos.add(new ParamterInfoImpl(informationDTO));
      }
      return Optional.of(parameterInfos);
    }
    return Optional.fromNullable(parameterInfos);
  }
}
