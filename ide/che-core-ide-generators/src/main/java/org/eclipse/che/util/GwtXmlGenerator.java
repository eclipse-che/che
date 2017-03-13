/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.Boolean.parseBoolean;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.getConfigurationBuilder;

/**
 * Generates uber IDE.gwt.xml from part found in class path.
 *
 * @author Sergii Kabashniuk
 */
public class GwtXmlGenerator {

    /**
     * Name of the template
     */
    public static final String TEMPLATE_NAME =
            "/".concat(GwtXmlGenerator.class.getPackage().getName().replace(".", "/")).concat("/gwt.xml.template");

    public static final String  DEFAULT_GWT_XML_PATH    = "org/eclipse/che/ide/IDE.gwt.xml";
    public static final String  DEFAULT_GWT_ETNRY_POINT = "org.eclipse.che.ide.client.IDE";
    public static final String  DEFAULT_STYLE_SHEET     = "IDE.css";
    public static final Boolean DEFAULT_LOGGING         = false;

    private final GwtXmlGeneratorConfig config;

    public GwtXmlGenerator(GwtXmlGeneratorConfig config) {
        this.config = config;
    }

    public File generateGwtXml() throws IOException {
        File gwtXml = new File(config.getGenerationRoot(), config.getGwtFileName());
        if (gwtXml.isDirectory() || gwtXml.exists()) {
            throw new IOException(gwtXml.getAbsolutePath() + " already exists or directory");
        }
        ST template = getTemplate();
        template.add("config", config);
        // flush content
        FileUtils.writeStringToFile(gwtXml, template.render());
        return gwtXml;
    }


    /**
     * Get the template for typescript
     *
     * @return the String Template
     */
    protected ST getTemplate() {
        URL url = Resources.getResource(GwtXmlGenerator.class, TEMPLATE_NAME);
        try {
            return new ST(Resources.toString(url, UTF_8), '$', '$');
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read template", e);
        }

    }

    /**
     * Generated consolidated gwt.xml based on exists XX.gwt.xml in class path
     *
     * @param args
     *         possible arguments
     *         --rootDir - directory where gwt.xml file will be generated, default "."
     *         --gwtFileName - Name of the generated file in rootDir, default "org/eclipse/che/ide/IDE.gwt.xml"
     *         --entryPoint - gwt xml entry point, default "org.eclipse.che.ide.client.IDE"
     *         --styleSheet - gwt xml stylesheet, default "IDE.css"
     *         --loggingEnabled - Enable or disable gwt logging, default "false"
     *         --includePackages - Include only specific packages where *.gwt.xml can be found, default "No value. Means all packages"
     *         --excludePackages - Exclude packages from *.gwt.xml scanning	, default "com.google", "elemental","java.util","java.lang"
     */
    public static void main(String[] args) {

        try {
            System.out.println(" ------------------------------------------------------------------------ ");
            System.out.println("Searching for GWT");
            System.out.println(" ------------------------------------------------------------------------ ");
            Map<String, Set<String>> parsedArgs = GeneratorUtils.parseArgs(args);

            GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(parsedArgs.getOrDefault("excludePackages",
                                                                                             ImmutableSet.of("com.google",
                                                                                                             "elemental",
                                                                                                             "java.util",
                                                                                                             "java.lang"
                                                                                                            )),
                                                                     parsedArgs.getOrDefault("includePackages", Collections.emptySet()),
                                                                     Collections.emptySet());
            Set<String> gwtModules = searcher.getGwtModulesFromClassPath();
            gwtModules.forEach(System.out::println);
            System.out.println("Found " + gwtModules.size() + " gwt modules");


            GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                    new GwtXmlGeneratorConfig(gwtModules,
                                              new File(getSingleValueOrDefault(parsedArgs, "rootDir", ".")),
                                              getSingleValueOrDefault(parsedArgs, "gwtFileName", DEFAULT_GWT_XML_PATH),
                                              getSingleValueOrDefault(parsedArgs, "entryPoint", DEFAULT_GWT_ETNRY_POINT),
                                              getSingleValueOrDefault(parsedArgs, "styleSheet", DEFAULT_STYLE_SHEET),
                                              parseBoolean(
                                                      getSingleValueOrDefault(parsedArgs, "loggingEnabled", DEFAULT_LOGGING.toString()))
                    );
            GwtXmlGenerator gwtXmlGenerator = new GwtXmlGenerator(gwtXmlGeneratorConfig);
            gwtXmlGenerator.generateGwtXml();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // error
            System.exit(1);//NOSONAR
        }
    }

    private static String getSingleValueOrDefault(Map<String, Set<String>> parsedArgs, String key, String defaultValue) {
        Set<String> values = parsedArgs.get(key);
        return values != null ? values.iterator().next() : defaultValue;
    }


    /**
     * Class provides functionality of searching XXX.gwt.xml files in class path
     */
    public static class GwtXmlModuleSearcher {
        private final Set<String> excludePackages;
        private final Set<String> includePackages;
        private final Set<URL>    urls;

        public GwtXmlModuleSearcher(Set<String> excludePackages, Set<String> includePackages, Set<URL> urls) {
            this.excludePackages = excludePackages;
            this.includePackages = includePackages;
            this.urls = urls;
        }

        /**
         * Searches XXX.gwt.xml files in class path
         *
         * @return - set of XXX.gwt.xml files found in class path
         */
        public Set<String> getGwtModulesFromClassPath() {
            ConfigurationBuilder configurationBuilder = getConfigurationBuilder();
            if (urls != null && urls.size() > 0) {
                configurationBuilder.addUrls(urls);
            }
            FilterBuilder filterBuilder = new FilterBuilder();
            for (String includePackage : includePackages) {
                filterBuilder.includePackage(includePackage);
            }
            for (String excludePackage : excludePackages) {
                filterBuilder.excludePackage(excludePackage);
            }


            configurationBuilder.setScanners(new ResourcesScanner()).filterInputsBy(filterBuilder);

            Reflections reflection = new Reflections(configurationBuilder);
            return new TreeSet<>(reflection.getResources(name -> name.endsWith(".gwt.xml")));

        }
    }

    public static class GwtXmlGeneratorConfig {
        private final Set<String> gwtXmlModules;

        private final File generationRoot;

        private final String gwtFileName;

        private final String entryPoint;

        private final String stylesheet;

        private final boolean isLoggingEnabled;

        public GwtXmlGeneratorConfig(Set<String> gwtXmlModules,
                                     File generationRoot,
                                     String gwtFileName,
                                     String entryPoint,
                                     String stylesheet,
                                     boolean isLoggingEnabled) {
            this.gwtXmlModules = gwtXmlModules;
            this.generationRoot = generationRoot;
            this.gwtFileName = gwtFileName;
            this.entryPoint = entryPoint;
            this.stylesheet = stylesheet;
            this.isLoggingEnabled = isLoggingEnabled;
        }

        public GwtXmlGeneratorConfig(Set<String> gwtXmlModules,
                                     File generationRoot) {
            this(gwtXmlModules,
                 generationRoot,
                 DEFAULT_GWT_XML_PATH,
                 DEFAULT_GWT_ETNRY_POINT,
                 DEFAULT_STYLE_SHEET,
                 DEFAULT_LOGGING);
        }

        public Set<String> getGwtXmlModules() {
            return gwtXmlModules;
        }

        /**
         * @return set of gwt modules where '/' replaced with '.' and removed 'gwt.xml' from end.
         * used to insert as inherits in gwt.xml
         */
        public Set<String> getInherits() {
            return gwtXmlModules
                    .stream()
                    .map(gwtModule -> gwtModule.replace("/", ".").substring(0, gwtModule.length() - 8))
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        public File getGenerationRoot() {
            return generationRoot;
        }

        public String getGwtFileName() {
            return gwtFileName;
        }

        public String getEntryPoint() {
            return entryPoint;
        }

        public String getStylesheet() {
            return stylesheet;
        }

        public boolean isLoggingEnabled() {
            return isLoggingEnabled;
        }
    }
}
