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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

/** Contains set of default language descriptions */
@Singleton
public class DefaultLanguages {

  private final Set<LanguageDescription> languages = new HashSet<>();

  public DefaultLanguages() {
    register("bat", "bat", "text/plain");
    register("bibtex", "bib", "text/plain");
    register("clojure", "clj", "text/x-clojure");
    register("coffeescript", "coffee", "text/x-coffeescript");
    register("c", "c", "text/x-csrc");
    register("cpp", "cpp", "text/x-c++src");
    register("csharp", "cs", "text/x-csharp");
    register("css", "css", "text/css");
    register("diff", "diff", "text/x-diff");
    register("fsharp", "fs", "text/x-fsharp");
    register("go", "go", "text/x-go");
    register("groovy", "groovy", "text/x-groovy");
    register("handlebars", "hbs", "text/plain");
    register("html", "html", "text/html");
    register("ini", "ini", "text/plain");
    register("jade", "jade", "text/x-jade");
    register("java", "java", "text/x-java");
    register("javascript", "js", "text/javascript");
    register("json", "json", "application/json");
    register("scala", "scala", "text/x-scala");
    register("kt", "kotlin", "text/x-kotlin");
    register("latex", "tex", "text/x-latex");
    register("lisp", "lisp", "text/x-commonlisp");
    register("lua", "lua", "text/x-lua");
    register("makefile", "Makefile", "text/plain");
    register("markdown", "markdown", "text/x-markdown");
    register("objective-c", "m", "text/x-objective-c");
    register("objective-cpp", "mm", "text/x-objective-cpp");
    register("perl", "pl", "text/x-perl");
    register("php", "php", "text/x-php");
    register("powershell", "ps1", "text/plain");
    register("pascal", "pas", "text/x-pascal");
    register("python", "py", "text/x-python");
    register("r", "r", "text/x-rsrc");
    register("ruby", "rb", "text/x-ruby");
    register("rust", "rs", "text/x-rustsrc");
    register("shellscript", "sh", "text/x-sh");
    register("sql", "sql", "text/x-sql");
    register("swift", "swift", "text/x-swift");
    register("typescript", "ts", "application/typescript");
    register("xml", "xml", "application/xml");
    register("xsl", "xsl", "text/plain");
    register("yaml", "yaml", "text/x-yaml");

    registerWithName("dockerfile", "Dockerfile", "text/x-dockerfile");
  }

  public Set<LanguageDescription> getAll() {
    return Collections.unmodifiableSet(languages);
  }

  private void register(String id, String extension, String mimeType) {
    LanguageDescription languageDescription = new LanguageDescription();
    languageDescription.setLanguageId(id);
    languageDescription.setMimeType(mimeType);
    languageDescription.setFileExtensions(Collections.singletonList(extension));
    languages.add(languageDescription);
  }

  private void registerWithName(String id, String fileName, String mimeType) {
    LanguageDescription languageDescription = new LanguageDescription();
    languageDescription.setLanguageId(id);
    languageDescription.setMimeType(mimeType);
    languageDescription.setFileNames(Collections.singletonList(fileName));
    languages.add(languageDescription);
  }
}
