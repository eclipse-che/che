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
package org.eclipse.che.plugin.typescript.dto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.reflections.util.ClasspathHelper.forClassLoader;
import static org.reflections.util.ClasspathHelper.forJavaClassPath;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.plugin.typescript.dto.model.DtoModel;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.stringtemplate.v4.ST;

/**
 * Write all DTOs found in classpath into a specific file. It will contains both DTO interface and
 * DTO implementation. It will generate TypeScript code
 *
 * @author Florent Benoit
 */
public class TypeScriptDtoGenerator {

  /** Name of the template */
  public static final String TEMPLATE_NAME =
      "/"
          .concat(TypeScriptDtoGenerator.class.getPackage().getName().replace(".", "/"))
          .concat("/typescript.template");

  /** String template instance used */
  private ST st;

  /** Model of DTOs that will be provided to the String Template */
  private List<DtoModel> dtoModels;

  /** Use of classpath */
  private boolean useClassPath;

  /** Setup a new generator */
  public TypeScriptDtoGenerator() {
    this.dtoModels = new ArrayList<>();
  }

  public static void main(String[] args) {
    new TypeScriptDtoGenerator().execute();
  }

  /**
   * Init stuff is responsible to grab all DTOs found in classpath (classloader) and setup model for
   * String Template
   */
  protected void init() {

    ConfigurationBuilder configurationBuilder =
        new ConfigurationBuilder().setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
    if (useClassPath) {
      configurationBuilder.setUrls(forJavaClassPath());
    } else {
      configurationBuilder.setUrls(forClassLoader());
    }

    // keep only DTO interfaces
    Reflections reflections = new Reflections(configurationBuilder);
    List<Class<?>> annotatedWithDtos =
        new ArrayList<>(reflections.getTypesAnnotatedWith(DTO.class));
    List<Class<?>> interfacesDtos =
        annotatedWithDtos
            .stream()
            .filter(clazz -> clazz.isInterface())
            .collect(Collectors.toList());
    interfacesDtos.stream().forEach(this::analyze);
  }

  /**
   * Analyze a DTO interface by registering the associate model.
   *
   * @param dto the DTO to analyze
   */
  protected void analyze(Class<?> dto) {
    // for each dto class, store some data about it
    this.dtoModels.add(new DtoModel(dto));
  }

  /** Execute this generator. */
  public String execute() {
    init();
    ST template = getTemplate();
    template.add("dtos", this.dtoModels);
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

  public void setUseClassPath(boolean useClassPath) {
    this.useClassPath = useClassPath;
  }
}
