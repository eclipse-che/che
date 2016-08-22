/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */

// imports
import {RemoteIp} from './remoteip';
import {RecipeBuilder} from './recipebuilder';
import {Workspace} from './workspace';
import {WorkspaceDto} from './dto/workspacedto';
import {Websocket} from './websocket';
import {CheFileStructWorkspace} from './chefile-struct/che-file-struct';
import {CheFileStruct} from './chefile-struct/che-file-struct';
import {CheFileServerTypeStruct} from "./chefile-struct/che-file-struct";
import {Log} from "./log";
import {AuthData} from "./auth-data";
import {CreateWorkspaceConfig} from "./workspace";
import {resolve} from "url";
import {MessageBus} from "./messagebus";
import {MessageBusSubscriber} from "./messagebus-subscriber";
import {WorkspaceDisplayOutputMessageBusSubscriber} from "./workspace-log-output-subscriber";
import {Project} from "./project";
import {ContainerVersion} from "./container-version";
import {CheFileStructWorkspaceCommand} from "./chefile-struct/che-file-struct";
import {CheFileStructWorkspaceCommandImpl} from "./chefile-struct/che-file-struct";
import {UUID} from "./uuid";
import {HttpJsonRequest} from "./default-http-json-request";
import {HttpJsonResponse} from "./default-http-json-request";
import {DefaultHttpJsonRequest} from "./default-http-json-request";
import {I18n} from "./i18n/i18n";
import {Message} from "./i18n/message";


/**
 * Entrypoint for the Chefile handling in a directory.
 * It can either generate or reuse an existing Chefile and then boot Che.
 * @author Florent Benoit
 */
export class CheDir {

  // Try 30s to ping a che server when booting it
  times: number = 30;

  // gloabl var
  waitDone = false;

  chefileStruct: CheFileStruct;
  chefileStructWorkspace: CheFileStructWorkspace;
  dockerContent;

  // requirements
  path = require('path');
  http = require('http');
  fs = require('fs');
  vm = require('vm');
  readline = require('readline');
  exec = require('child_process').exec;

  // init folder/files variables
  currentFolder: string = this.path.resolve('./');
  folderName: any;
  cheFile : any;
  dotCheFolder : any;
  confFolder : any;
  workspacesFolder : any;
  chePropertiesFile : any;
  dotCheIdFile : any;

  mode;
  args : Array<string>;


  websocket: Websocket;
  authData: AuthData;
  workspace: Workspace;

  @Message('che-dir-constant')
  i18n : I18n;

  constructor(args) {
    this.args = args;


    this.currentFolder = this.path.resolve(args[0]);
    this.folderName = this.path.basename(this.currentFolder);
    this.cheFile = this.path.resolve(this.currentFolder, 'Chefile');
    this.dotCheFolder = this.path.resolve(this.currentFolder, '.che');
    this.dotCheIdFile = this.path.resolve(this.dotCheFolder, 'id');
    this.confFolder = this.path.resolve(this.dotCheFolder, 'conf');
    this.workspacesFolder = this.path.resolve(this.dotCheFolder, 'workspaces');
    this.chePropertiesFile = this.path.resolve(this.confFolder, 'che.properties');

    this.initDefault();

    this.websocket = new Websocket();

    this.authData = new AuthData();
  }



  initDefault() {
    this.chefileStruct = new CheFileStruct();
    this.chefileStruct.server.ip = new RemoteIp().getIp();
    this.chefileStruct.server.port = 8080;
    this.chefileStruct.server.type =  'local';

    this.chefileStructWorkspace = new CheFileStructWorkspace();
    this.chefileStructWorkspace.name = 'local';
    this.chefileStructWorkspace.ram = 2048;

  }



  startsWith(value:string, searchString: string) : boolean {
    return value.substr(0, searchString.length) === searchString;
  }


  parseArgument() : Promise<string> {
    return new Promise<string>( (resolve, reject) => {
      let invalidCommand : string = 'only init, up, down and status commands are supported.';
      if (this.args.length == 0) {
        reject(invalidCommand);
      } else if ('init' === this.args[1] || 'up' === this.args[1] || 'down' === this.args[1] || 'status' === this.args[1]) {
        // return method found based on arguments
        resolve(this.args[1]);
      } else {
        reject('Invalid arguments ' + this.args +': ' + invalidCommand);
      }
    });

  }

  run() : Promise<string> {
    Log.context = 'che(dir)';

    // call the method analyzed from the argument
    return this.parseArgument().then((methodName) => {
      return this[methodName]();
    })


  }

  parse() {

    try {
      this.fs.statSync(this.cheFile);
      // we have a file
    } catch (e) {
      Log.getLogger().debug('No chefile defined, use default settings');
      return;
    }

    // load the chefile script if defined
    var script_code = this.fs.readFileSync(this.cheFile).toString();

    // strip the lines that are beginning with # as it may be comments
    script_code = script_code.replace(/#[^\n]*/g, '');

    // create sandboxed object
    var sandbox = { "che": this.chefileStruct,  "workspace": this.chefileStructWorkspace, "console": console};

    var script = this.vm.createScript(script_code);
    script.runInNewContext(sandbox);


    // now, cleanup invalid commands
    for (let i : number = this.chefileStructWorkspace.commands.length - 1; i >= 0 ; i--) {
      // no name, drop it
      if (!this.chefileStructWorkspace.commands[i].name) {
        this.chefileStructWorkspace.commands.splice(i, 1);
      }
    }

    Log.getLogger().debug('Che file parsing object is ', JSON.stringify(this.chefileStruct));
    Log.getLogger().debug('Che workspace parsing object is ', JSON.stringify(this.chefileStructWorkspace));

    this.authData.port = this.chefileStruct.server.port;




  }

  /**
   * Check if directory has been initialized or not
   * @return true if initialization has been done
   */
  isInitialized() : Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      try {
        this.fs.statSync(this.chePropertiesFile);
        resolve(true);
      } catch (e) {
        resolve(false);
      }
    });

  }


  /**
   * Write a default chefile
   */
  writeDefaultChefile() {

    // get json string from object
    let stringified = JSON.stringify(this.chefileStruct, null, 4);


    let content = '';
    let flatChe = this.flatJson('che', this.chefileStruct);
    flatChe.forEach((value, key) => {
      Log.getLogger().debug( 'the value is ' + value.toString() + ' for key' + key);
      content += key + '=' + value.toString() + '\n';
    });

    let flatWorkspace = this.flatJson('workspace', this.chefileStructWorkspace);
    flatWorkspace.forEach((value, key) => {
      Log.getLogger().debug( 'the flatWorkspace value is ' + value.toString() + ' for key' + key);
      content += key + '=' + value.toString() + '\n';
    });


    // write content of this.che object
    this.fs.writeFileSync(this.cheFile, content);
    Log.getLogger().info('GENERATING DEFAULT', this.cheFile);

  }


  init() : Promise<any> {
    return this.isInitialized().then((isInitialized) => {
      if (isInitialized) {
        Log.getLogger().warn('Che already initialized');
      } else {
        // needs to create folders
        this.initCheFolders();
        this.setupConfigFile();

        // write a default chefile if there is none
        try {
          this.fs.statSync(this.cheFile);
          Log.getLogger().debug('Chefile is present at ', this.cheFile);
        } catch (e) {
          // write default
          Log.getLogger().debug('Write a default Chefile at ', this.cheFile);
          this.writeDefaultChefile();
        }
        Log.getLogger().info('ADDING', this.dotCheFolder, 'DIRECTORY');
        return true;
      }

    });

  }

  status() : Promise<any> {
    return this.isInitialized().then((isInitialized) => {
      if (!isInitialized) {
        return Promise.reject('This directory has not been initialized. So, status is not available.');
      }

      return new Promise<string>((resolve, reject) => {
        this.parse();
        resolve('parsed');
      }).then(() => {
        return this.checkCheIsRunning();
      }).then((isRunning) => {
        if (!isRunning) {
          return Promise.reject('No Eclipse Che Instance Running.');
        }

        // check workspace exists
        this.workspace = new Workspace(this.authData);
        return this.workspace.existsWorkspace(':' + this.chefileStructWorkspace.name);
      }).then((workspaceDto) => {
        // found it
        if (!workspaceDto) {
          return Promise.reject('Eclipse Che is running ' + this.buildLocalCheURL() + ' but workspace (' + this.chefileStructWorkspace.name + ') has not been found');
        }

        // search IDE url link
        let ideUrl : string;
        workspaceDto.getContent().links.forEach((link) => {
          if ('ide url' === link.rel) {
            ideUrl = link.href;
          }
        });
        Log.getLogger().info(this.i18n.get('status.workspace.name', this.chefileStructWorkspace.name));
        Log.getLogger().info(this.i18n.get('status.workspace.url', ideUrl));
        return true;
      });
    });

  }


  /**
   * Search a che instance and stop it it it's currently running
   */
  down() : Promise<void> {

    // call init if not initialized and then call up
    return this.isInitialized().then((isInitialized) => {
      if (!isInitialized) {
        throw new Error('Unable to stop current instance as this directory has not been initialized.')
      }
    }).then(() => {
      return this.performDown();
    });
  }


  performDown() : Promise<void> {

    return new Promise<string>((resolve, reject) => {
      this.parse();

      resolve();
    }).then(() => {
      Log.getLogger().info('Search For A Running Eclipse Che...');

      // Try to connect to Eclipse Che instance
      return this.checkCheIsRunning();
    }).then((isRunning) => {
      if (!isRunning) {
        return Promise.reject('No Eclipse Che Instance Running.');
      }
      Log.getLogger().info('Found Running Eclipse Che At', this.buildLocalCheURL());
      Log.getLogger().info('Stopping Eclipse Che...');

      // call docker stop on the docker launcher
      return this.cheStop();
    }).then(() => {
      Log.getLogger().info('Eclipse Che Instance Successfully Stopped');
    });
  }



  up() : Promise<string> {

    // call init if not initialized and then call up
    return this.isInitialized().then((isInitialized) => {
      if (!isInitialized) {
        return this.init();
      }
    }).then(() => {
      return this.performUp();
    });
  }


  performUp() : Promise<string> {

    var ideUrl : string;

    var needToSetupProject: boolean = true;

    return new Promise<string>((resolve, reject) => {
      this.parse();

      // test if conf is existing
      try {
        var statsPropertiesFile = this.fs.statSync(this.chePropertiesFile);
      } catch (e) {
        reject('No che configured. che init has been done ?');
      }
      resolve('parsed');
    }).then(() => {
        return this.checkCheIsNotRunning();
    }).then((isNotRunning) => {
      return new Promise<boolean>((resolve, reject) => {
        if (isNotRunning) {
          resolve(true);
        } else {
          reject('Found existing instance of Che running on the same host/port. So aborting as che is already up.');
        }
      });
    }).then((checkOk) => {
      Log.getLogger().info('Starting Eclipse Che Silently');
      // needs to invoke docker run
      return this.cheBoot();
    }).then((data) => {
      // loop to check startup (during 30seconds)
      return this.loopWaitChePing();
    }).then((value) => {
      Log.getLogger().info('Eclipse Che Is Now Running At', this.buildLocalCheURL());
      // check workspace exists
      this.workspace = new Workspace(this.authData);
      return this.workspace.existsWorkspace(':' + this.chefileStructWorkspace.name);
    }).then((workspaceDto) => {
      // found it
      if (!workspaceDto) {
        // workspace is not existing
        // now create the workspace
        let createWorkspaceConfig:CreateWorkspaceConfig = new CreateWorkspaceConfig();
        createWorkspaceConfig.commands = this.chefileStructWorkspace.commands;
        createWorkspaceConfig.name = this.chefileStructWorkspace.name;
        createWorkspaceConfig.ram = this.chefileStructWorkspace.ram;
        Log.getLogger().info('Workspace Created');
        return this.workspace.createWorkspace(createWorkspaceConfig)
      } else {
        // do not create it, just return current one
        Log.getLogger().info('Workspace Exists From A Previous Start');
        needToSetupProject = false;
        return workspaceDto;
      }
    }).then((workspaceDto) => {
        Log.getLogger().info('Workspace Booting...');
        return this.workspace.startWorkspace(workspaceDto.getId());
    }).then((workspaceDto) => {
      return this.workspace.getWorkspace(workspaceDto.getId())
    }).then((workspaceDto) => {
      // search IDE url link
      workspaceDto.getContent().links.forEach((link) => {
        if ('ide url' === link.rel) {
          ideUrl = link.href;
        }
      });
      if (needToSetupProject) {
        Log.getLogger().info('Updating Project...');
        var project:Project = new Project(workspaceDto);
        // update created project to blank
        return project.updateType(this.folderName, 'blank');
      } else {
        Promise.resolve('existing project')
      }
    }).then(() => {
      Log.getLogger().info('Workspace Booted And Ready For Development');
      Log.getLogger().info('Connect to', ideUrl);
      return ideUrl;
    });




  }





  buildLocalCheURL() : string {
    return 'http://' + this.chefileStruct.server.ip + ':' + this.chefileStruct.server.port;
  }

  displayUrlWorkspace(workspace) {

    var found = false;
    var i = 0;
    var links = workspace.links;
    while (i < links.length && !found) {
      // display the ide url link
      var link = links[i];
      if (link.rel === 'ide url') {
        found = true;
        Log.getLogger().info('Workspace At ' + link.href);
      }
      i++;

    }

    if (!found) {
      Log.getLogger().warn('Workspace successfully started but unable to find workspace link');
    }

  }

  initCheFolders() {

    // create .che folder
    try {
      this.fs.mkdirSync(this.dotCheFolder, 0o744);
    } catch (e) {
      // already exist
    }

    try {
      this.fs.writeFileSync(this.dotCheIdFile, UUID.build());
    } catch (e) {
      // already exist
    }

    // create .che/workspaces folder
    try {
      this.fs.mkdirSync(this.workspacesFolder, 0o744);
    } catch (e) {
      // already exist
    }

    // create .che/conf folder

    try {
      this.fs.mkdirSync(this.confFolder, 0o744);
    } catch (e) {
      // already exist
    }


    // copy configuration file

    try {
      var stats = this.fs.statSync(this.chePropertiesFile);
    } catch (e) {
      // file does not exist, copy it
      this.fs.writeFileSync(this.chePropertiesFile, this.fs.readFileSync(this.path.resolve(__dirname, 'che.properties')));
    }

  }


  setupConfigFile() {
    // need to setup che.properties file with workspaces folder

    // update che.user.workspaces.storage
    this.updateConfFile('che.user.workspaces.storage', this.workspacesFolder);

    // update extra volumes
    this.updateConfFile('machine.server.extra.volume', this.currentFolder + ':/projects/' + this.folderName);

  }

  updateConfFile(propertyName, propertyValue) {

    var content = '';
    var foundLine = false;
    this.fs.readFileSync(this.chePropertiesFile).toString().split('\n').forEach((line) => {

      var updatedLine;



      if (this.startsWith(line, propertyName)) {
        foundLine = true;
        updatedLine = propertyName + '=' + propertyValue + '\n';
      } else {
        updatedLine = line  + '\n';
      }

      content += updatedLine;
    });

    // add property if not present
    if (!foundLine) {
      content += propertyName + '=' + propertyValue + '\n';
    }

    this.fs.writeFileSync(this.chePropertiesFile, content);

  }

  cheBoot() : Promise<any> {

    let promise : Promise<any> = new Promise<any>((resolve, reject) => {
      let containerVersion : string = new ContainerVersion().getVersion();

      // create command line to execute
      var commandLine: string = 'docker run ';

      // add extra properties
      for(var property in this.chefileStruct.server.properties){
        let envProperty : string = ' --env ' + property + '=\"' + this.chefileStruct.server.properties[property] + '\"';
        commandLine += envProperty;
      }

      // continue with own properties
      commandLine +=
          ' -v /var/run/docker.sock:/var/run/docker.sock' +
          ' -e CHE_PORT=' + this.chefileStruct.server.port +
          ' -e CHE_DATA_FOLDER=' + this.workspacesFolder +
          ' -e CHE_CONF_FOLDER=' + this.confFolder +
          ' codenvy/che-launcher:' + containerVersion + ' start';

      Log.getLogger().debug('Executing command line', commandLine);


      var child = this.exec(commandLine , (error, stdout, stderr) => {
            if (error) {
              Log.getLogger().error('Error when starting che with che-launcher: ' + error.toString() + '. exit code was ' + error.code);
              Log.getLogger().error('Startup traces were on stdout:\n', stdout.toString());
              Log.getLogger().error('Startup traces were on stderr:\n', stderr.toString());
            } else {
              resolve({
                childProcess: child,
                stdout: stdout,
                stderr: stderr
              });
            }
          });

      child.stdout.on('data', (data) => {
        Log.getLogger().debug(data.toString());
      });


      child.on('exit', (exitCode) => {
        if (exitCode == 0) {
          resolve('success');
        } else {
          reject('process has exited');
        }

      });

      this.dockerContent = new RecipeBuilder().getDockerContent();
    });

    return promise;

  }



  cheStop() : Promise<any> {

    let promise : Promise<any> = new Promise<any>((resolve, reject) => {
      let containerVersion : string = new ContainerVersion().getVersion();

      var commandLine: string = 'docker run ' +
          ' -v /var/run/docker.sock:/var/run/docker.sock' +
          ' -e CHE_PORT=' + this.chefileStruct.server.port +
          ' -e CHE_DATA_FOLDER=' + this.workspacesFolder +
          ' -e CHE_CONF_FOLDER=' + this.confFolder +
          ' codenvy/che-launcher:' + containerVersion + ' stop';

      Log.getLogger().debug('Executing command line', commandLine);


      var child = this.exec(commandLine , (error, stdout, stderr) => {
        if (error) {
          Log.getLogger().error('Error when stopping che with che-launcher: ' + error.toString() + '. exit code was ' + error.code);
          Log.getLogger().error('Stopping traces were on stdout:\n', stdout.toString());
          Log.getLogger().error('Stopping traces were on stderr:\n', stderr.toString());
        } else {
          resolve({
            childProcess: child,
            stdout: stdout,
            stderr: stderr
          });
        }
      });

      child.stdout.on('data', (data) => {
        Log.getLogger().debug(data.toString());
      });


      child.on('exit', (exitCode) => {
        if (exitCode == 0) {
          resolve('success');
        } else {
          reject('process has exited');
        }

      });

    });

    return promise;

  }


  checkCheIsNotRunning() : Promise <boolean> {
    var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace', 200);
    return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
      return false;
    }, (error) => {
      // find error when connecting so probaly not running
      return true;
    });
  }


  checkCheIsRunning() : Promise<boolean> {
    var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace', 200);
    return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
      return true;
    }, (error) => {
      // find error when connecting so probaly not running
      return false;
    });
  }


  pingCheAction() : void {
    var options = {
      hostname: this.chefileStruct.server.ip,
      port: this.chefileStruct.server.port,
      path: '/api/workspace',
      method: 'GET',
      headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json;charset=UTF-8'
      }
    };

    var req = this.http.request(options, (res) => {
      res.on('data', (body) => {
        Log.getLogger().debug('got rest status code =', res.statusCode);
        if (res.statusCode === 200 && !this.waitDone) {
          Log.getLogger().debug('got 200 status, stop waiting');
          this.waitDone = true;
        }
      });
    });
    req.on('error', (e) => {
      Log.getLogger().debug('Got error when waiting che boot: ' + e.message);
    });
    req.end();

  }


  /**
   * Loop to send ping action each second and then try to see if we need to wait or not again
   */
  loopWaitChePing() : Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      var loop = () => {
        this.pingCheAction();
        if (this.waitDone) {
          resolve(true);
        } else {
          Log.getLogger().debug('pinging che was not ready, continue ', this.times, ' times.');
          this.times--;
          if (this.times === 0) {
            reject('Timeout for pinging Eclipse Che has been reached. Please check logs.');
          } else {
            // loop again
            setTimeout(loop, 1000);
          }
        }
      };
      process.nextTick(loop);
    });
  }





  /**
   * Flatten a JSON object and return a map of string with key and value
   * @param prefix the prefix to use in order to flatten given object instance
   * @param data the JSON object
   * @returns {Map<any, any><string, string>} the flatten map
   */
  flatJson(prefix, data) : Map<string, string> {
    var map = new Map<string, string>();

    this.recurseFlatten(map, data, prefix);
    return map;
  }


  /**
   * Recursive method to iterate on elements of a JSON object.
   * @param map the map containing the key being the property name and the value the JSON element value
   * @param jsonData the data to parse
   * @param prop the nme of the property being analyzed
     */
  recurseFlatten(map : Map<string, string>, jsonData: any, prop: string) : void {

    if (Object(jsonData) !== jsonData) {
      if (this.isNumber(jsonData)) {
        map.set(prop, jsonData);
      } else {
        map.set(prop, "'" + jsonData + "'");
      }
    } else if (Array.isArray(jsonData)) {
      let arr : Array<any> = jsonData;
      let l: number = arr.length;
      if (l == 0) {
        map.set(prop, '[]');
      } else {
        for(var i : number =0 ; i<l; i++) {
          this.recurseFlatten(map, arr[i], prop + "[" + i + "]");
        }
      }
    } else {
      var isEmpty = true;
      for (var p in jsonData) {
        isEmpty = false;
        this.recurseFlatten(map, jsonData[p], prop ? prop + "." + p : p);
      }
      if (isEmpty && prop)
        map.set(prop, '{}');
    }
  }

  isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }


}
