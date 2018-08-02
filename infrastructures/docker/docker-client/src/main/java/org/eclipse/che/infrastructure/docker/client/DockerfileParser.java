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
package org.eclipse.che.infrastructure.docker.client;

import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.eclipse.che.commons.lang.Pair;

/** @author andrew00x */
public class DockerfileParser {

  /** Parse content of Dockerfile from the specified File. */
  public static Dockerfile parse(File file) throws DockerFileException {
    try {
      return parse(CharStreams.readLines(Files.newReader(file, Charset.defaultCharset())));
    } catch (IOException e) {
      throw new DockerFileException("Error happened parsing the Docker file." + e.getMessage(), e);
    }
  }

  /** Parse content of Dockerfile from the specified URL. */
  public static Dockerfile parse(final URL file) throws DockerFileException {
    try {
      return parse(
          CharStreams.readLines(
              Resources.asCharSource(file, Charset.defaultCharset()).openStream()));
    } catch (IOException e) {
      throw new DockerFileException("Error happened parsing the Docker file:" + e.getMessage(), e);
    }
  }

  /** Parse content of Dockerfile from the specified Reader. */
  public static Dockerfile parse(Reader reader) throws DockerFileException {
    try {
      return parse(CharStreams.readLines(reader));
    } catch (IOException e) {
      throw new DockerFileException("Error happened parsing the Docker file:" + e.getMessage(), e);
    }
  }

  /** Parse content of Dockerfile that is represented by String value. */
  public static Dockerfile parse(String contentOfDockerFile) throws DockerFileException {
    try {
      return parse(CharStreams.readLines(CharSource.wrap(contentOfDockerFile).openStream()));
    } catch (IOException e) {
      throw new DockerFileException("Error happened parsing the Docker file:" + e.getMessage(), e);
    }
  }

  private static Dockerfile parse(Iterable<String> lines) throws DockerFileException {
    final Dockerfile dockerfile = new Dockerfile();
    DockerImage current = null;
    for (String line : lines) {
      line = line.trim();
      dockerfile.getLines().add(line);
      if (!line.isEmpty()) {
        Instruction instruction;
        if ((instruction = getInstruction(line)) == null) {
          continue;
        }

        if (instruction == Instruction.FROM) {
          if (current != null) {
            dockerfile.getImages().add(current);
          }
          current = new DockerImage();
          instruction.setInstructionArgumentsToModel(current, line);
        } else {
          if (current == null) {
            if (instruction != Instruction.COMMENT) {
              throw new DockerFileException(
                  "Error happened parsing the Docker file: Docker file must start with 'FROM' instruction");
            }
          } else {
            instruction.setInstructionArgumentsToModel(current, line);
          }
        }
      }
    }
    if (current != null) {
      dockerfile.getImages().add(current);
    }
    return dockerfile;
  }

  private static Instruction getInstruction(String line) {
    if (line.startsWith("#")) {
      return Instruction.COMMENT;
    }
    // By convention instruction should be UPPERCASE but it is not required.
    final String lowercase = line.toLowerCase();
    if (lowercase.startsWith("from")) {
      return Instruction.FROM;
    } else if (lowercase.startsWith("maintainer")) {
      return Instruction.MAINTAINER;
    } else if (lowercase.startsWith("run")) {
      return Instruction.RUN;
    } else if (lowercase.startsWith("cmd")) {
      return Instruction.CMD;
    } else if (lowercase.startsWith("expose")) {
      return Instruction.EXPOSE;
    } else if (lowercase.startsWith("env")) {
      return Instruction.ENV;
    } else if (lowercase.startsWith("add")) {
      return Instruction.ADD;
    } else if (lowercase.startsWith("entrypoint")) {
      return Instruction.ENTRYPOINT;
    } else if (lowercase.startsWith("volume")) {
      return Instruction.VOLUME;
    } else if (lowercase.startsWith("user")) {
      return Instruction.USER;
    } else if (lowercase.startsWith("workdir")) {
      return Instruction.WORKDIR;
    } else if (lowercase.startsWith("onbuild")) {
      return Instruction.ONBUILD;
    }
    return null;
  }

  private enum Instruction {
    FROM {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.setFrom(getInstructionArguments(line));
      }
    },
    MAINTAINER {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.getMaintainer().add(getInstructionArguments(line));
      }
    },
    RUN {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.getRun().add(getInstructionArguments(line));
      }
    },
    CMD {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.setCmd(getInstructionArguments(line));
      }
    },
    EXPOSE {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        final String args = getInstructionArguments(line);
        final int l = args.length();
        int i = 0, j = 0;
        while (j < l) {
          while (j < l && !Character.isWhitespace(args.charAt(j))) {
            j++;
          }
          model.getExpose().add(args.substring(i, j));
          i = j;
          while (i < l && Character.isWhitespace(args.charAt(i))) {
            i++;
          }
          j = i;
        }
      }
    },
    ENV {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        final String args = getInstructionArguments(line);
        final int l = args.length();
        int i = 0;
        while (i < l && !Character.isWhitespace(args.charAt(i))) {
          i++;
        }
        if (i < l) {
          int j = i;
          while (j < l && Character.isWhitespace(args.charAt(j))) {
            j++;
          }
          if (j < l) {
            model.getEnv().put(args.substring(0, i), args.substring(j));
          } else {
            model.getEnv().put(args.substring(0, i), null);
          }
        } else {
          model.getEnv().put(args, null);
        }
      }
    },
    ADD {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        final String args = getInstructionArguments(line);
        final int l = args.length();
        int i = 0;
        while (i < l && !Character.isWhitespace(args.charAt(i))) {
          i++;
        }
        if (i < l) {
          int j = i;
          while (j < l && Character.isWhitespace(args.charAt(j))) {
            j++;
          }
          if (j < l) {
            model.getAdd().add(Pair.of(args.substring(0, i), args.substring(j)));
          } else {
            // respect this even it's not legal for docker file
            model.getAdd().add(Pair.of(args.substring(0, i), (String) null));
          }
        } else {
          // respect this even it's not legal for docker file
          model.getAdd().add(Pair.of(args, (String) null));
        }
      }
    },
    ENTRYPOINT {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.setEntrypoint(getInstructionArguments(line));
      }
    },
    VOLUME {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        String args = getInstructionArguments(line);
        if (!args.isEmpty()) {
          final int l = args.length();
          if (args.charAt(0) != '[' || args.charAt(l - 1) != ']') {
            throw new DockerFileException(
                String.format(
                    "Error happened parsing the Docker file: Invalid argument '%s' for 'VOLUME' instruction",
                    args));
          }
          int i = 1, j = 1, end = l - 1;
          while (j < end) {
            while (j < end && args.charAt(j) != ',') {
              j++;
            }
            String volume = args.substring(i, j);
            if (!volume.isEmpty()) {
              if ((volume.charAt(0) == '"' && volume.charAt(volume.length() - 1) == '"')
                  || (volume.charAt(0) == '\'' && volume.charAt(volume.length() - 1) == '\'')) {
                volume = volume.substring(1, volume.length() - 1);
              }
              if (!volume.isEmpty()) {
                model.getVolume().add(volume);
              }
            }
            i = j + 1;
            while (i < end && Character.isWhitespace(args.charAt(i))) {
              i++;
            }
            j = i;
          }
        }
      }
    },
    USER {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.setUser(getInstructionArguments(line));
      }
    },
    WORKDIR {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.setWorkdir(getInstructionArguments(line));
      }
    },
    COMMENT {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.getComments().add(getInstructionArguments(line));
      }

      @Override
      String getInstructionArguments(String line) {
        return line.substring(1).trim();
      }
    },
    ONBUILD {
      @Override
      void setInstructionArgumentsToModel(DockerImage model, String line)
          throws DockerFileException {
        model.getOnbuild().add(line.substring(name().length()).trim());
      }
    };

    abstract void setInstructionArgumentsToModel(DockerImage model, String line)
        throws DockerFileException;

    String getInstructionArguments(String line) {
      return line.substring(name().length()).trim();
    }
  }

  private DockerfileParser() {}
}
