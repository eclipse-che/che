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
package org.eclipse.che.ide.api.editor.filetype;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;

/** @author Dmytro Kulieshov */
@Singleton
public class DefaultExtensionToMimeTypeMappings {
  private final Map<String, Set<String>> mappings =
      ImmutableMap.<String, Set<String>>builder()
          .put("c", ImmutableSet.of("text/x-csrc"))
          .put("C", ImmutableSet.of("text/x-c++src"))
          .put("cc", ImmutableSet.of("text/x-c++src"))
          .put("cpp", ImmutableSet.of("text/x-c++src"))
          .put("ino", ImmutableSet.of("text/x-c++src"))
          .put("h", ImmutableSet.of("text/x-chdr"))
          .put("hh", ImmutableSet.of("text/x-c++hdr"))
          .put("c++", ImmutableSet.of("text/x-c++src"))
          .put("cs", ImmutableSet.of("text/x-csharp"))
          .put("m", ImmutableSet.of("text/x-objective-c"))
          .put("java", ImmutableSet.of("text/x-java"))
          .put("class", ImmutableSet.of("text/x-java"))
          .put("scala", ImmutableSet.of("text/x-scala"))
          .put("sbt", ImmutableSet.of("text/x-scala"))
          .put("clj", ImmutableSet.of("text/x-clojure"))
          .put("groovy", ImmutableSet.of("text/x-groovy"))
          .put("gvy", ImmutableSet.of("text/x-groovy"))
          .put("gy", ImmutableSet.of("text/x-groovy"))
          .put("gradle", ImmutableSet.of("text/x-groovy"))
          .put("kt", ImmutableSet.of("text/x-kotlin"))
          .put("js", ImmutableSet.of("application/javascript", "text/javascript"))
          .put("coffee", ImmutableSet.of("text/x-coffeescript"))
          .put("json", ImmutableSet.of("application/json"))
          .put("ts", ImmutableSet.of("application/javascript", "application/typescript"))
          .put("es6", ImmutableSet.of("application/javascript", "text/javascript"))
          .put("jsx", ImmutableSet.of("application/javascript", "text/javascript"))
          .put("css", ImmutableSet.of("text/css"))
          .put("scss", ImmutableSet.of("text/x-scss"))
          .put("less", ImmutableSet.of("text/x-less"))
          .put("sass", ImmutableSet.of("text/x-sass"))
          .put("xml", ImmutableSet.of("application/xml"))
          .put("html", ImmutableSet.of("text/html"))
          .put("xhtml", ImmutableSet.of("application/xml+xhtml", "text/html"))
          .put("htm", ImmutableSet.of("text/html"))
          .put("dtd", ImmutableSet.of("application/xml-dtd"))
          .put("yaml", ImmutableSet.of("text/x-yaml"))
          .put("yml", ImmutableSet.of("text/x-yaml"))
          .put("markdown", ImmutableSet.of("text/x-markdown"))
          .put("mdown", ImmutableSet.of("text/x-markdown"))
          .put("mkdn", ImmutableSet.of("text/x-markdown"))
          .put("mkd", ImmutableSet.of("text/x-markdown"))
          .put("md", ImmutableSet.of("text/x-markdown"))
          .put("mdwn", ImmutableSet.of("text/x-markdown"))
          .put("rest", ImmutableSet.of("text/x-rst"))
          .put("rst", ImmutableSet.of("text/x-rst"))
          .put("tex", ImmutableSet.of("text/x-latex"))
          .put("cls", ImmutableSet.of("text/x-latex"))
          .put("sty", ImmutableSet.of("text/x-latex"))
          .put("py", ImmutableSet.of("text/x-python"))
          .put("pyx", ImmutableSet.of("text/x-cython"))
          .put("rb", ImmutableSet.of("text/x-ruby"))
          .put("erb", ImmutableSet.of("text/html"))
          .put("gemspec", ImmutableSet.of("text/x-ruby"))
          .put("go", ImmutableSet.of("text/x-go"))
          .put("rs", ImmutableSet.of("text/x-rustsrc"))
          .put("erl", ImmutableSet.of("text/x-erlang"))
          .put("lua", ImmutableSet.of("text/x-lua"))
          .put("tcl", ImmutableSet.of("text/x-tcl"))
          .put("pl", ImmutableSet.of("text/x-perl"))
          .put("pm", ImmutableSet.of("text/x-perl"))
          .put("php", ImmutableSet.of("text/x-php"))
          .put("phtml", ImmutableSet.of("text/x-php"))
          .put("ejs", ImmutableSet.of("application/x-ejs"))
          .put("jsp", ImmutableSet.of("application/x-jsp"))
          .put("asp", ImmutableSet.of("application/x-aspx"))
          .put("aspx", ImmutableSet.of("application/x-aspx"))
          .put("slim", ImmutableSet.of("text/x-slim"))
          .put("ml", ImmutableSet.of("text/x-ocaml"))
          .put("fs", ImmutableSet.of("text/x-fsharp"))
          .put("lisp", ImmutableSet.of("text/x-commonlisp"))
          .put("cl", ImmutableSet.of("text/x-commonlisp"))
          .put("hs", ImmutableSet.of("text/x-haskell"))
          .put("scm", ImmutableSet.of("text/x-scheme"))
          .put("sql", ImmutableSet.of("text/x-sql"))
          .put("properties", ImmutableSet.of("text/x-properties"))
          .put("diff", ImmutableSet.of("text/x-diff"))
          .put("r", ImmutableSet.of("text/x-rsrc"))
          .put("R", ImmutableSet.of("text/x-rsrc"))
          .put("csv", ImmutableSet.of("text/csv"))
          .put("sh", ImmutableSet.of("text/x-sh"))
          .put("pas", ImmutableSet.of("text/x-pascal"))
          .put("p", ImmutableSet.of("text/x-pascal"))
          .put("fpm", ImmutableSet.of("text/x-pascal"))
          .put("st", ImmutableSet.of("text/x-stsrc"))
          .put("cob", ImmutableSet.of("text/x-cobol"))
          .put("cbl", ImmutableSet.of("text/x-cobol"))
          .put("cpy", ImmutableSet.of("text/x-cobol"))
          .put("f", ImmutableSet.of("text/x-fortran"))
          .put("for", ImmutableSet.of("text/x-fortran"))
          .put("f90", ImmutableSet.of("text/x-fortran"))
          .put("f95", ImmutableSet.of("text/x-fortran"))
          .put("f03", ImmutableSet.of("text/x-fortran"))
          .put("vb", ImmutableSet.of("text/x-vb"))
          .put("vbs", ImmutableSet.of("text/vbscript"))
          .put("pp", ImmutableSet.of("text/x-puppet"))
          .put("docker", ImmutableSet.of("text/x-dockerfile"))
          .put("jag", ImmutableSet.of("text/jaggery"))
          .build();

  public Set<String> getExtensions() {
    return ImmutableSet.copyOf(mappings.keySet());
  }

  public Set<String> getMimeTypes() {
    Set<String> mimeTypes =
        mappings.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

    return ImmutableSet.copyOf(mimeTypes);
  }

  public Map<String, Set<String>> getMappings() {
    return mappings;
  }
}
