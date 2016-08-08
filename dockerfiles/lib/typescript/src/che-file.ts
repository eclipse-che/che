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




export class CheFile {

  times: number = 10;

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

  mode;
  args : Array<string>;

  constructor(args) {
    this.args = args;


    this.currentFolder = this.path.resolve(args[0]);
    this.folderName = this.path.basename(this.currentFolder);
    this.cheFile = this.path.resolve(this.currentFolder, 'Chefile');
    this.dotCheFolder = this.path.resolve(this.currentFolder, '.che');
    this.confFolder = this.path.resolve(this.dotCheFolder, 'conf');
    this.workspacesFolder = this.path.resolve(this.dotCheFolder, 'workspaces');
    this.chePropertiesFile = this.path.resolve(this.confFolder, 'che.properties');

    this.initDefault();
  }



  initDefault() {
    this.chefileStruct = new CheFileStruct();
    this.chefileStruct.server.ip = new RemoteIp().getIp();
    this.chefileStruct.server.port = 8080;
    this.chefileStruct.server.type =  'local';

    this.chefileStructWorkspace = new CheFileStructWorkspace();
    this.chefileStructWorkspace.name = 'local';
    this.chefileStructWorkspace.commands[0] = {name: 'hello'};

  }



  startsWith(value:string, searchString: string) : boolean {
    return value.substr(0, searchString.length) === searchString;
  }

  run() {
    Log.context = 'ECLIPSE CHE FILE';

    if (this.args.length == 0) {
      Log.getLogger().error('only init and up commands are supported.');
      return;
    } else if ('init' === this.args[1]) {
      this.init();
    } else if ('up' === this.args[1]) {
      // call init if not already done
      if (!this.isInitialized()) {
        this.init();
      }
      this.up();
    } else {
      Log.getLogger().error('Invalid arguments ' + this.args +': Only init and up commands are supported.');
      return;
    }

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
    var script_code = this.fs.readFileSync(this.cheFile);

    // create sandboxed object
    var sandbox = { "che": this.chefileStruct,  "workspace": this.chefileStructWorkspace, "console": console};

    var script = this.vm.createScript(script_code);
    script.runInNewContext(sandbox);

    Log.getLogger().debug('Che file parsing object is ', this.chefileStruct);
  }

  /**
   * Check if directory has been initialized or not
   * @return true if initialization has been done
   */
  isInitialized() : boolean {
    try {
      this.fs.statSync(this.chePropertiesFile);
      return true;
    } catch (e) {
      return false;
    }

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
    Log.getLogger().info('File', this.cheFile, 'written.')

  }


  init() {
    // Check if we have internal che.properties file. If we have, throw error
   if (this.isInitialized()) {
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
    }

  }

  up() {
    this.parse();

    // test if conf is existing
    try {
      var statsPropertiesFile = this.fs.statSync(this.chePropertiesFile);
    } catch (e) {
      Log.getLogger().error('No che configured. che init has been done ?');
      return;
    }

    Log.getLogger().info('STARTING ECLIPSE CHE SILENTLY');
    // needs to invoke docker run
    this.cheBoot();

    this.dockerContent = new RecipeBuilder().getDockerContent();

    // loop to check startup (during 30seconds)
    this.waitCheBoot(this);
  }



  /**
   * Create workspace based on the remote hostname and workspacename
   * if custom docker content is provided, use it
   */
  createWorkspace(dockerContent) {
    var options = {
      hostname: this.chefileStruct.server.ip,
      port: this.chefileStruct.server.port,
      path: '/api/workspace?account=',
      method: 'POST',
      headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json;charset=UTF-8'
      }
    };
    var req = this.http.request(options, (res) => {
      res.on('data', (body) => {

        if (res.statusCode == 201) {
          // workspace created, continue
          this.displayUrlWorkspace(JSON.parse(body));
        } else {
          // error
          Log.getLogger().error('Invalid response from the server side. Aborting');
          Log.getLogger().error('response was ' + body);
        }
      });
    });
    req.on('error', (e) => {
      Log.getLogger().error('problem with request: ' + e.message);
    });

    var workspace = {
      "defaultEnv": "default",
      "commands": [],
      "projects": [],
      "environments": [{
        "machineConfigs": [{
          "dev": true,
          "servers": [],
          "envVariables": {},
          "limits": {"ram": 2500},
          "source": {"type": "dockerfile", "content": dockerContent},
          "name": "default",
          "type": "docker",
          "links": []
        }], "name": "default"
      }],
      "name": this.chefileStructWorkspace.name,
      "links": [],
      "description": null
    };


    req.write(JSON.stringify(workspace));
    req.end();

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
        Log.getLogger().info('WORKSPACE AT ' + link.href);
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

  cheBoot() {

    var commandLine: string = 'docker run ' +
        ' -v /var/run/docker.sock:/var/run/docker.sock' +
        ' -e CHE_PORT=' + this.chefileStruct.server.port +
        ' -e CHE_DATA_FOLDER=' + this.workspacesFolder +
        ' -e CHE_CONF_FOLDER=' + this.confFolder +
        ' codenvy/che-launcher:nightly start';

    Log.getLogger().debug('Executing command line', commandLine);
    var child = this.exec(commandLine , function callback(error, stdout, stderr) {
        }
    );


    child.stdout.on('data', (data) => {
      Log.getLogger().debug(data.toString());
    });

  }


// test if can connect on che port
  waitCheBoot(self: CheFile) {

    if(self.times < 1) {
      return;
    }
    var options = {
      hostname: self.chefileStruct.server.ip,
      port: self.chefileStruct.server.port,
      path: '/api/workspace',
      method: 'GET',
      headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json;charset=UTF-8'
      }
    };
    Log.getLogger().debug('using che ping options', options, 'and docker content', self.dockerContent);

    var req = self.http.request(options, (res) => {
      res.on('data', (body) => {

        if (res.statusCode === 200 && !self.waitDone) {
          self.waitDone = true;
          Log.getLogger().debug('status code is 200, creating workspace');
          self.createWorkspace(self.dockerContent);
        }
      });
    });
    req.on('error', (e) => {
      Log.getLogger().debug('with request: ' + e.message);
    });


    req.end();


    self.times--;
    if (self.times > 0 && !self.waitDone) {
      setTimeout(self.waitCheBoot, 5000, self);
    }


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
