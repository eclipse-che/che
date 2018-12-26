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
package org.eclipse.che.plugin.typescript.dto;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.plugin.typescript.dto.model.DtoModel;
import org.eclipse.che.plugin.typescript.dto.model.DtoNamespace;
import org.stringtemplate.v4.ST;

/** Write all DTOs found in classpath into a specific '.d.ts' file */
public class TypeScriptDtoDTSGenerator extends TypeScriptDtoGenerator {

  /** Name of the template */
  public static final String TEMPLATE_NAME =
      "/"
          .concat(TypeScriptDtoDTSGenerator.class.getPackage().getName().replace(".", "/"))
          .concat("/typescript.d.ts.template");

  /** String template instance used */
  private ST st;

  private Map<String, DtoNamespace> dtoNamespaces = new HashMap<>();

  public static void main(String[] args) {
    TypeScriptDtoGenerator.main(args);
  }

  @Override
  protected void analyze(Class<?> dto) {
    // for each dto class, store some data about it
    DtoModel model = new DtoModel(dto);
    String namespace = model.getDTSPackageName();
    if (!dtoNamespaces.containsKey(namespace)) {
      dtoNamespaces.put(namespace, new DtoNamespace(namespace));
    }

    dtoNamespaces.get(namespace).addModel(model);
  }

  @Override
  public String execute() {
    init();
    ST template = getTemplate();
    template.add("dtoNamespaces", this.dtoNamespaces.values());
    String output = template.render();
    return output;
  }

  /**
   * Get the template for typescript
   *
   * @return the String Template
   */
  protected ST getTemplate() {
    if (st == null) {
      URL url = Resources.getResource(TypeScriptDtoGenerator.class, TEMPLATE_NAME);
      try {
        st = new ST(Resources.toString(url, UTF_8));
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to read template", e);
      }
    }
    return st;
  }
}
