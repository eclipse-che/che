/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


#include <string>
#include <cstdio>
#include <X11/Xlib.h>
#include <iostream>
#include "SystemUtils.h"
#include "Messages.h"



int	SystemUtils::DEFAULT_WINDOW_WIDTH = 1024;
int	SystemUtils::DEFAULT_WINDOW_HEIGHT = 1024;

std::string	SystemUtils::BIN_BASH = "/bin/bash";
std::string	SystemUtils::APPLICATION_BASE_PATH;
std::string	SystemUtils::WSO2STUDIO_CHE_SH_AND = "/bin/che.sh start &";
std::string	SystemUtils::WSO2STUDIO_CHE_SH_STOP_AND = "/bin/che.sh stop &";
std::string	SystemUtils::WSO2STUDIO_WORKSPACE = "/bin/workspace.sh";
std::string	SystemUtils::BIN_URL_TXT = "/bin/url.txt";
std::string SystemUtils::BIN_PORT = "/bin/PORT";



int SystemUtils::GetScreenSize(int *w, int*h) {
	Display* display = NULL;
	Screen* screen = NULL;

	display = XOpenDisplay( NULL);
	if (!display) {
		std::cout << Messages::ERROR_GETTING_DEFAULT_DISPLAY;
		return -1;
	}

	screen = DefaultScreenOfDisplay(display);
	if (!screen) {
		std::cout << Messages::ERROR_GETTING_DEFAULT_SCREEN;
		return -2;
	}

	*w = screen->width;
	*h = screen->height;

	XCloseDisplay(display);
	return 0;
}





//Move with c++ libraries
std::string SystemUtils::GetFileContents(const char *filename) {
    std::FILE *file = std::fopen(filename, "rb");
    if (file) {
        std::string contents;
        std::fseek(file, 0, SEEK_END);
        contents.resize(std::ftell(file));
        std::rewind(file);
        if (std::fread(&contents[0], 1, contents.size(), file)){
			std::fclose(file);
			}        
        return (contents);
    } else {
        return "0";
    }
}
