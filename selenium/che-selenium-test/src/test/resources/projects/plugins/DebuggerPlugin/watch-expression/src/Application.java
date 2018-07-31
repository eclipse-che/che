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
public class Application {

    public static void main(String[] args) {
        String messageLevel = "INFO";
        String content = "Simple test message";

        Message message = new Message(messageLevel, content);
        message.setLevel("WARN");

        System.out.println(message);
    }
}

class Message {
    private String level;
    private String content;

    public Message(String level, String content) {
        this.level = level;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getLevel() {
        return level;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "{Level: " + level.toString() + "} {Content: " + content + "}";
    }
}


