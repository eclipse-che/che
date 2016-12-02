// File has been generated automatically by Eclipse Che TypeScript DTO generator


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineConfigDto {
      getLimits(): org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;
      getServers(): Array<org.eclipse.che.api.machine.shared.dto.ServerConfDto>;
      setServers(arg0): void;
      withType(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      setType(arg0): void;
      getSource(): org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
      withSource(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      setSource(arg0): void;
      withName(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      withLinks(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      withServers(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      isDev(): boolean;
      withDev(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      setDev(arg0): void;
      withEnvVariables(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      setLimits(arg0): void;
      withLimits(arg0): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      getEnvVariables(): Map<string,string>;
      setEnvVariables(arg0): void;
      getName(): string;
      setName(arg0): void;
      getType(): string;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineConfigDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineConfigDto {

        servers : Array<org.eclipse.che.api.machine.shared.dto.ServerConfDto>;
        dev : boolean;
        envVariables : Map<string,string>;
        name : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        source : org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
        type : string;
        limits : org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.servers = new Array<org.eclipse.che.api.machine.shared.dto.ServerConfDto>();
            if (__jsonObject) {
              if (__jsonObject.servers) {
                  __jsonObject.servers.forEach((item) => {
                  this.servers.push(new org.eclipse.che.api.machine.shared.dto.ServerConfDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.dev) {
                this.dev = __jsonObject.dev;
              }
            }
            this.envVariables = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.envVariables) {
                let tmp : Array<any> = Object.keys(__jsonObject.envVariables);
                tmp.forEach((key) => {
                  this.envVariables.set(key, __jsonObject.envVariables[key]);
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.source) {
                this.source = new org.eclipse.che.api.machine.shared.dto.MachineSourceDtoImpl(__jsonObject.source);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.limits) {
                this.limits = new org.eclipse.che.api.machine.shared.dto.MachineLimitsDtoImpl(__jsonObject.limits);
              }
            }

    } 

        getLimits() : org.eclipse.che.api.machine.shared.dto.MachineLimitsDto {
          return this.limits;
        }
        getServers() : Array<org.eclipse.che.api.machine.shared.dto.ServerConfDto> {
          return this.servers;
        }
        setServers(servers : Array<org.eclipse.che.api.machine.shared.dto.ServerConfDto>) : void {
          this.servers = servers;
        }
        withType(type : string) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getSource() : org.eclipse.che.api.machine.shared.dto.MachineSourceDto {
          return this.source;
        }
        withSource(source : org.eclipse.che.api.machine.shared.dto.MachineSourceDto) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.source = source;
          return this;
        }
        setSource(source : org.eclipse.che.api.machine.shared.dto.MachineSourceDto) : void {
          this.source = source;
        }
        withName(name : string) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.name = name;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.links = links;
          return this;
        }
        withServers(servers : Array<org.eclipse.che.api.machine.shared.dto.ServerConfDto>) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.servers = servers;
          return this;
        }
        isDev() : boolean {
          return this.dev;
        }
        withDev(dev : boolean) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.dev = dev;
          return this;
        }
        setDev(dev : boolean) : void {
          this.dev = dev;
        }
        withEnvVariables(envVariables : Map<string,string>) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.envVariables = envVariables;
          return this;
        }
        setLimits(limits : org.eclipse.che.api.machine.shared.dto.MachineLimitsDto) : void {
          this.limits = limits;
        }
        withLimits(limits : org.eclipse.che.api.machine.shared.dto.MachineLimitsDto) : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          this.limits = limits;
          return this;
        }
        getEnvVariables() : Map<string,string> {
          return this.envVariables;
        }
        setEnvVariables(envVariables : Map<string,string>) : void {
          this.envVariables = envVariables;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getType() : string {
          return this.type;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.servers) {
                            let listArray = [];
                            this.servers.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.machine.shared.dto.ServerConfDtoImpl).toJson());
                            json.servers = listArray;
                            });
                        }
                        if (this.dev) {
                          json.dev = this.dev;
                        }
                        if (this.envVariables) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.envVariables.entries()) {
                            tmpMap[key] = value;
                           }
                          json.envVariables = tmpMap;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.source) {
                          json.source = (this.source as org.eclipse.che.api.machine.shared.dto.MachineSourceDtoImpl).toJson();
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.limits) {
                          json.limits = (this.limits as org.eclipse.che.api.machine.shared.dto.MachineLimitsDtoImpl).toJson();
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface ExtendedMachineDto {
      getServers(): Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>;
      setServers(arg0): void;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
      withServers(arg0): org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
      getAgents(): Array<string>;
      setAgents(arg0): void;
      withAgents(arg0): org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
      getAttributes(): Map<string,string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class ExtendedMachineDtoImpl implements org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto {

        servers : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>;
        attributes : Map<string,string>;
        agents : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.servers = new Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>();
            if (__jsonObject) {
              if (__jsonObject.servers) {
                let tmp : Array<any> = Object.keys(__jsonObject.servers);
                tmp.forEach((key) => {
                  this.servers.set(key, new org.eclipse.che.api.workspace.shared.dto.ServerConf2DtoImpl(__jsonObject.servers[key]));
                 });
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.agents = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.agents) {
                  __jsonObject.agents.forEach((item) => {
                  this.agents.push(item);
                  });
              }
            }

    } 

        getServers() : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto> {
          return this.servers;
        }
        setServers(servers : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>) : void {
          this.servers = servers;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto {
          this.attributes = attributes;
          return this;
        }
        withServers(servers : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>) : org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto {
          this.servers = servers;
          return this;
        }
        getAgents() : Array<string> {
          return this.agents;
        }
        setAgents(agents : Array<string>) : void {
          this.agents = agents;
        }
        withAgents(agents : Array<string>) : org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto {
          this.agents = agents;
          return this;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.servers) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.servers.entries()) {
                            tmpMap[key] = (value as org.eclipse.che.api.workspace.shared.dto.ServerConf2DtoImpl).toJson();
                           }
                          json.servers = tmpMap;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.agents) {
                            let listArray = [];
                            this.agents.forEach((item) => {
                            listArray.push(item);
                            json.agents = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface CommitRequest {
      setMessage(arg0): void;
      getFiles(): Array<string>;
      withMessage(arg0): org.eclipse.che.api.git.shared.CommitRequest;
      isAll(): boolean;
      setAmend(arg0): void;
      withAmend(arg0): org.eclipse.che.api.git.shared.CommitRequest;
      withAll(arg0): org.eclipse.che.api.git.shared.CommitRequest;
      setFiles(arg0): void;
      withFiles(arg0): org.eclipse.che.api.git.shared.CommitRequest;
      isAmend(): boolean;
      getMessage(): string;
      setAll(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class CommitRequestImpl implements org.eclipse.che.api.git.shared.CommitRequest {

        all : boolean;
        amend : boolean;
        files : Array<string>;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.all) {
                this.all = __jsonObject.all;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.amend) {
                this.amend = __jsonObject.amend;
              }
            }
            this.files = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.files) {
                  __jsonObject.files.forEach((item) => {
                  this.files.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        setMessage(message : string) : void {
          this.message = message;
        }
        getFiles() : Array<string> {
          return this.files;
        }
        withMessage(message : string) : org.eclipse.che.api.git.shared.CommitRequest {
          this.message = message;
          return this;
        }
        isAll() : boolean {
          return this.all;
        }
        setAmend(amend : boolean) : void {
          this.amend = amend;
        }
        withAmend(amend : boolean) : org.eclipse.che.api.git.shared.CommitRequest {
          this.amend = amend;
          return this;
        }
        withAll(all : boolean) : org.eclipse.che.api.git.shared.CommitRequest {
          this.all = all;
          return this;
        }
        setFiles(files : Array<string>) : void {
          this.files = files;
        }
        withFiles(files : Array<string>) : org.eclipse.che.api.git.shared.CommitRequest {
          this.files = files;
          return this;
        }
        isAmend() : boolean {
          return this.amend;
        }
        getMessage() : string {
          return this.message;
        }
        setAll(all : boolean) : void {
          this.all = all;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.all) {
                          json.all = this.all;
                        }
                        if (this.amend) {
                          json.amend = this.amend;
                        }
                        if (this.files) {
                            let listArray = [];
                            this.files.forEach((item) => {
                            listArray.push(item);
                            json.files = listArray;
                            });
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface ServerPropertiesDto {
      setPath(arg0): void;
      withPath(arg0): org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;
      getInternalUrl(): string;
      getInternalAddress(): string;
      setInternalAddress(arg0): void;
      withInternalAddress(arg0): org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;
      setInternalUrl(arg0): void;
      withInternalUrl(arg0): org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;
      getPath(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class ServerPropertiesDtoImpl implements org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto {

        path : string;
        internalAddress : string;
        internalUrl : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.internalAddress) {
                this.internalAddress = __jsonObject.internalAddress;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.internalUrl) {
                this.internalUrl = __jsonObject.internalUrl;
              }
            }

    } 

        setPath(path : string) : void {
          this.path = path;
        }
        withPath(path : string) : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto {
          this.path = path;
          return this;
        }
        getInternalUrl() : string {
          return this.internalUrl;
        }
        getInternalAddress() : string {
          return this.internalAddress;
        }
        setInternalAddress(internalAddress : string) : void {
          this.internalAddress = internalAddress;
        }
        withInternalAddress(internalAddress : string) : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto {
          this.internalAddress = internalAddress;
          return this;
        }
        setInternalUrl(internalUrl : string) : void {
          this.internalUrl = internalUrl;
        }
        withInternalUrl(internalUrl : string) : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto {
          this.internalUrl = internalUrl;
          return this;
        }
        getPath() : string {
          return this.path;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.internalAddress) {
                          json.internalAddress = this.internalAddress;
                        }
                        if (this.internalUrl) {
                          json.internalUrl = this.internalUrl;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface CommandDto {
      getCommandLine(): string;
      withType(arg0): org.eclipse.che.api.machine.shared.dto.CommandDto;
      setType(arg0): void;
      withName(arg0): org.eclipse.che.api.machine.shared.dto.CommandDto;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.machine.shared.dto.CommandDto;
      withCommandLine(arg0): org.eclipse.che.api.machine.shared.dto.CommandDto;
      setCommandLine(arg0): void;
      getName(): string;
      setName(arg0): void;
      getType(): string;
      getAttributes(): Map<string,string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class CommandDtoImpl implements org.eclipse.che.api.machine.shared.dto.CommandDto {

        name : string;
        attributes : Map<string,string>;
        commandLine : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.commandLine) {
                this.commandLine = __jsonObject.commandLine;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        getCommandLine() : string {
          return this.commandLine;
        }
        withType(type : string) : org.eclipse.che.api.machine.shared.dto.CommandDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withName(name : string) : org.eclipse.che.api.machine.shared.dto.CommandDto {
          this.name = name;
          return this;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.machine.shared.dto.CommandDto {
          this.attributes = attributes;
          return this;
        }
        withCommandLine(commandLine : string) : org.eclipse.che.api.machine.shared.dto.CommandDto {
          this.commandLine = commandLine;
          return this;
        }
        setCommandLine(commandLine : string) : void {
          this.commandLine = commandLine;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getType() : string {
          return this.type;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.commandLine) {
                          json.commandLine = this.commandLine;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.event {

  export interface MachineStatusEvent {
      getEventType(): string;
      getError(): string;
      setEventType(arg0): void;
      withError(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
      withEventType(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
      getWorkspaceId(): string;
      withWorkspaceId(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
      isDev(): boolean;
      withDev(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
      setDev(arg0): void;
      getMachineId(): string;
      setMachineId(arg0): void;
      withMachineId(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
      getMachineName(): string;
      withMachineName(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
      setError(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.event {

  export class MachineStatusEventImpl implements org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {

        machineId : string;
        dev : boolean;
        eventType : string;
        error : string;
        machineName : string;
        workspaceId : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.machineId) {
                this.machineId = __jsonObject.machineId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.dev) {
                this.dev = __jsonObject.dev;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.eventType) {
                this.eventType = __jsonObject.eventType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.error) {
                this.error = __jsonObject.error;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.machineName) {
                this.machineName = __jsonObject.machineName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceId) {
                this.workspaceId = __jsonObject.workspaceId;
              }
            }

    } 

        getEventType() : string {
          return this.eventType;
        }
        getError() : string {
          return this.error;
        }
        setEventType(eventType : string) : void {
          this.eventType = eventType;
        }
        withError(error : string) : org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {
          this.error = error;
          return this;
        }
        withEventType(eventType : string) : org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {
          this.eventType = eventType;
          return this;
        }
        getWorkspaceId() : string {
          return this.workspaceId;
        }
        withWorkspaceId(workspaceId : string) : org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {
          this.workspaceId = workspaceId;
          return this;
        }
        isDev() : boolean {
          return this.dev;
        }
        withDev(dev : boolean) : org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {
          this.dev = dev;
          return this;
        }
        setDev(dev : boolean) : void {
          this.dev = dev;
        }
        getMachineId() : string {
          return this.machineId;
        }
        setMachineId(machineId : string) : void {
          this.machineId = machineId;
        }
        withMachineId(machineId : string) : org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {
          this.machineId = machineId;
          return this;
        }
        getMachineName() : string {
          return this.machineName;
        }
        withMachineName(machineName : string) : org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent {
          this.machineName = machineName;
          return this;
        }
        setError(error : string) : void {
          this.error = error;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.machineId) {
                          json.machineId = this.machineId;
                        }
                        if (this.dev) {
                          json.dev = this.dev;
                        }
                        if (this.eventType) {
                          json.eventType = this.eventType;
                        }
                        if (this.error) {
                          json.error = this.error;
                        }
                        if (this.machineName) {
                          json.machineName = this.machineName;
                        }
                        if (this.workspaceId) {
                          json.workspaceId = this.workspaceId;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface RequestBodyDescriptor {
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class RequestBodyDescriptorImpl implements org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor {

        description : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }

    } 

        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor {
          this.description = description;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.description) {
                          json.description = this.description;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Remote {
      getUrl(): string;
      withName(arg0): org.eclipse.che.api.git.shared.Remote;
      withUrl(arg0): org.eclipse.che.api.git.shared.Remote;
      getName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RemoteImpl implements org.eclipse.che.api.git.shared.Remote {

        name : string;
        url : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.url) {
                this.url = __jsonObject.url;
              }
            }

    } 

        getUrl() : string {
          return this.url;
        }
        withName(name : string) : org.eclipse.che.api.git.shared.Remote {
          this.name = name;
          return this;
        }
        withUrl(url : string) : org.eclipse.che.api.git.shared.Remote {
          this.url = url;
          return this;
        }
        getName() : string {
          return this.name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.url) {
                          json.url = this.url;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface ServiceError {
      setMessage(arg0): void;
      withMessage(arg0): org.eclipse.che.api.core.rest.shared.dto.ServiceError;
      getMessage(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class ServiceErrorImpl implements org.eclipse.che.api.core.rest.shared.dto.ServiceError {

        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        setMessage(message : string) : void {
          this.message = message;
        }
        withMessage(message : string) : org.eclipse.che.api.core.rest.shared.dto.ServiceError {
          this.message = message;
          return this;
        }
        getMessage() : string {
          return this.message;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface PushRequest {
      setTimeout(arg0): void;
      getTimeout(): number;
      setPassword(arg0): void;
      getUsername(): string;
      getPassword(): string;
      setUsername(arg0): void;
      setRemote(arg0): void;
      withRemote(arg0): org.eclipse.che.api.git.shared.PushRequest;
      withPassword(arg0): org.eclipse.che.api.git.shared.PushRequest;
      getRefSpec(): Array<string>;
      setRefSpec(arg0): void;
      withRefSpec(arg0): org.eclipse.che.api.git.shared.PushRequest;
      getRemote(): string;
      isForce(): boolean;
      setForce(arg0): void;
      withForce(arg0): org.eclipse.che.api.git.shared.PushRequest;
      withTimeout(arg0): org.eclipse.che.api.git.shared.PushRequest;
      withUsername(arg0): org.eclipse.che.api.git.shared.PushRequest;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class PushRequestImpl implements org.eclipse.che.api.git.shared.PushRequest {

        password : string;
        refSpec : Array<string>;
        force : boolean;
        remote : string;
        timeout : number;
        username : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.password) {
                this.password = __jsonObject.password;
              }
            }
            this.refSpec = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.refSpec) {
                  __jsonObject.refSpec.forEach((item) => {
                  this.refSpec.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.force) {
                this.force = __jsonObject.force;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.remote) {
                this.remote = __jsonObject.remote;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.timeout) {
                this.timeout = __jsonObject.timeout;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.username) {
                this.username = __jsonObject.username;
              }
            }

    } 

        setTimeout(timeout : number) : void {
          this.timeout = timeout;
        }
        getTimeout() : number {
          return this.timeout;
        }
        setPassword(password : string) : void {
          this.password = password;
        }
        getUsername() : string {
          return this.username;
        }
        getPassword() : string {
          return this.password;
        }
        setUsername(username : string) : void {
          this.username = username;
        }
        setRemote(remote : string) : void {
          this.remote = remote;
        }
        withRemote(remote : string) : org.eclipse.che.api.git.shared.PushRequest {
          this.remote = remote;
          return this;
        }
        withPassword(password : string) : org.eclipse.che.api.git.shared.PushRequest {
          this.password = password;
          return this;
        }
        getRefSpec() : Array<string> {
          return this.refSpec;
        }
        setRefSpec(refSpec : Array<string>) : void {
          this.refSpec = refSpec;
        }
        withRefSpec(refSpec : Array<string>) : org.eclipse.che.api.git.shared.PushRequest {
          this.refSpec = refSpec;
          return this;
        }
        getRemote() : string {
          return this.remote;
        }
        isForce() : boolean {
          return this.force;
        }
        setForce(force : boolean) : void {
          this.force = force;
        }
        withForce(force : boolean) : org.eclipse.che.api.git.shared.PushRequest {
          this.force = force;
          return this;
        }
        withTimeout(timeout : number) : org.eclipse.che.api.git.shared.PushRequest {
          this.timeout = timeout;
          return this;
        }
        withUsername(username : string) : org.eclipse.che.api.git.shared.PushRequest {
          this.username = username;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.password) {
                          json.password = this.password;
                        }
                        if (this.refSpec) {
                            let listArray = [];
                            this.refSpec.forEach((item) => {
                            listArray.push(item);
                            json.refSpec = listArray;
                            });
                        }
                        if (this.force) {
                          json.force = this.force;
                        }
                        if (this.remote) {
                          json.remote = this.remote;
                        }
                        if (this.timeout) {
                          json.timeout = this.timeout;
                        }
                        if (this.username) {
                          json.username = this.username;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface GitUrlVendorInfo {
      getVendorBaseHost(): string;
      getOAuthScopes(): Array<string>;
      setVendorName(arg0): void;
      withVendorName(arg0): org.eclipse.che.api.git.shared.GitUrlVendorInfo;
      setVendorBaseHost(arg0): void;
      withVendorBaseHost(arg0): org.eclipse.che.api.git.shared.GitUrlVendorInfo;
      setOAuthScopes(arg0): void;
      withOAuthScopes(arg0): org.eclipse.che.api.git.shared.GitUrlVendorInfo;
      setGivenUrlSSH(arg0): void;
      withGivenUrlSSH(arg0): org.eclipse.che.api.git.shared.GitUrlVendorInfo;
      isGivenUrlSSH(): boolean;
      getVendorName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class GitUrlVendorInfoImpl implements org.eclipse.che.api.git.shared.GitUrlVendorInfo {

        vendorBaseHost : string;
        givenUrlSSH : boolean;
        oAuthScopes : Array<string>;
        vendorName : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.vendorBaseHost) {
                this.vendorBaseHost = __jsonObject.vendorBaseHost;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.givenUrlSSH) {
                this.givenUrlSSH = __jsonObject.givenUrlSSH;
              }
            }
            this.oAuthScopes = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.oAuthScopes) {
                  __jsonObject.oAuthScopes.forEach((item) => {
                  this.oAuthScopes.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.vendorName) {
                this.vendorName = __jsonObject.vendorName;
              }
            }

    } 

        getVendorBaseHost() : string {
          return this.vendorBaseHost;
        }
        getOAuthScopes() : Array<string> {
          return this.oAuthScopes;
        }
        setVendorName(vendorName : string) : void {
          this.vendorName = vendorName;
        }
        withVendorName(vendorName : string) : org.eclipse.che.api.git.shared.GitUrlVendorInfo {
          this.vendorName = vendorName;
          return this;
        }
        setVendorBaseHost(vendorBaseHost : string) : void {
          this.vendorBaseHost = vendorBaseHost;
        }
        withVendorBaseHost(vendorBaseHost : string) : org.eclipse.che.api.git.shared.GitUrlVendorInfo {
          this.vendorBaseHost = vendorBaseHost;
          return this;
        }
        setOAuthScopes(oAuthScopes : Array<string>) : void {
          this.oAuthScopes = oAuthScopes;
        }
        withOAuthScopes(oAuthScopes : Array<string>) : org.eclipse.che.api.git.shared.GitUrlVendorInfo {
          this.oAuthScopes = oAuthScopes;
          return this;
        }
        setGivenUrlSSH(givenUrlSSH : boolean) : void {
          this.givenUrlSSH = givenUrlSSH;
        }
        withGivenUrlSSH(givenUrlSSH : boolean) : org.eclipse.che.api.git.shared.GitUrlVendorInfo {
          this.givenUrlSSH = givenUrlSSH;
          return this;
        }
        isGivenUrlSSH() : boolean {
          return this.givenUrlSSH;
        }
        getVendorName() : string {
          return this.vendorName;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.vendorBaseHost) {
                          json.vendorBaseHost = this.vendorBaseHost;
                        }
                        if (this.givenUrlSSH) {
                          json.givenUrlSSH = this.givenUrlSSH;
                        }
                        if (this.oAuthScopes) {
                            let listArray = [];
                            this.oAuthScopes.forEach((item) => {
                            listArray.push(item);
                            json.oAuthScopes = listArray;
                            });
                        }
                        if (this.vendorName) {
                          json.vendorName = this.vendorName;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RemoteUpdateRequest {
      getBranches(): Array<string>;
      setBranches(arg0): void;
      isAddBranches(): boolean;
      setRemoveUrl(arg0): void;
      getAddPushUrl(): Array<string>;
      setAddBranches(arg0): void;
      getAddUrl(): Array<string>;
      setAddUrl(arg0): void;
      getRemoveUrl(): Array<string>;
      setAddPushUrl(arg0): void;
      getRemovePushUrl(): Array<string>;
      setRemovePushUrl(arg0): void;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RemoteUpdateRequestImpl implements org.eclipse.che.api.git.shared.RemoteUpdateRequest {

        removeUrl : Array<string>;
        removePushUrl : Array<string>;
        addPushUrl : Array<string>;
        addBranches : boolean;
        addUrl : Array<string>;
        name : string;
        branches : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.removeUrl = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.removeUrl) {
                  __jsonObject.removeUrl.forEach((item) => {
                  this.removeUrl.push(item);
                  });
              }
            }
            this.removePushUrl = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.removePushUrl) {
                  __jsonObject.removePushUrl.forEach((item) => {
                  this.removePushUrl.push(item);
                  });
              }
            }
            this.addPushUrl = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.addPushUrl) {
                  __jsonObject.addPushUrl.forEach((item) => {
                  this.addPushUrl.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.addBranches) {
                this.addBranches = __jsonObject.addBranches;
              }
            }
            this.addUrl = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.addUrl) {
                  __jsonObject.addUrl.forEach((item) => {
                  this.addUrl.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.branches = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.branches) {
                  __jsonObject.branches.forEach((item) => {
                  this.branches.push(item);
                  });
              }
            }

    } 

        getBranches() : Array<string> {
          return this.branches;
        }
        setBranches(branches : Array<string>) : void {
          this.branches = branches;
        }
        isAddBranches() : boolean {
          return this.addBranches;
        }
        setRemoveUrl(removeUrl : Array<string>) : void {
          this.removeUrl = removeUrl;
        }
        getAddPushUrl() : Array<string> {
          return this.addPushUrl;
        }
        setAddBranches(addBranches : boolean) : void {
          this.addBranches = addBranches;
        }
        getAddUrl() : Array<string> {
          return this.addUrl;
        }
        setAddUrl(addUrl : Array<string>) : void {
          this.addUrl = addUrl;
        }
        getRemoveUrl() : Array<string> {
          return this.removeUrl;
        }
        setAddPushUrl(addPushUrl : Array<string>) : void {
          this.addPushUrl = addPushUrl;
        }
        getRemovePushUrl() : Array<string> {
          return this.removePushUrl;
        }
        setRemovePushUrl(removePushUrl : Array<string>) : void {
          this.removePushUrl = removePushUrl;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.removeUrl) {
                            let listArray = [];
                            this.removeUrl.forEach((item) => {
                            listArray.push(item);
                            json.removeUrl = listArray;
                            });
                        }
                        if (this.removePushUrl) {
                            let listArray = [];
                            this.removePushUrl.forEach((item) => {
                            listArray.push(item);
                            json.removePushUrl = listArray;
                            });
                        }
                        if (this.addPushUrl) {
                            let listArray = [];
                            this.addPushUrl.forEach((item) => {
                            listArray.push(item);
                            json.addPushUrl = listArray;
                            });
                        }
                        if (this.addBranches) {
                          json.addBranches = this.addBranches;
                        }
                        if (this.addUrl) {
                            let listArray = [];
                            this.addUrl.forEach((item) => {
                            listArray.push(item);
                            json.addUrl = listArray;
                            });
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.branches) {
                            let listArray = [];
                            this.branches.forEach((item) => {
                            listArray.push(item);
                            json.branches = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface StartActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
      setType(arg0): void;
      setBreakpoints(arg0): void;
      withBreakpoints(arg0): org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
      getBreakpoints(): Array<org.eclipse.che.api.debug.shared.dto.BreakpointDto>;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class StartActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.StartActionDto {

        breakpoints : Array<org.eclipse.che.api.debug.shared.dto.BreakpointDto>;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.breakpoints = new Array<org.eclipse.che.api.debug.shared.dto.BreakpointDto>();
            if (__jsonObject) {
              if (__jsonObject.breakpoints) {
                  __jsonObject.breakpoints.forEach((item) => {
                  this.breakpoints.push(new org.eclipse.che.api.debug.shared.dto.BreakpointDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.StartActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setBreakpoints(breakpoints : Array<org.eclipse.che.api.debug.shared.dto.BreakpointDto>) : void {
          this.breakpoints = breakpoints;
        }
        withBreakpoints(breakpoints : Array<org.eclipse.che.api.debug.shared.dto.BreakpointDto>) : org.eclipse.che.api.debug.shared.dto.action.StartActionDto {
          this.breakpoints = breakpoints;
          return this;
        }
        getBreakpoints() : Array<org.eclipse.che.api.debug.shared.dto.BreakpointDto> {
          return this.breakpoints;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.breakpoints) {
                            let listArray = [];
                            this.breakpoints.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.debug.shared.dto.BreakpointDtoImpl).toJson());
                            json.breakpoints = listArray;
                            });
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface ShowFileContentResponse {
      setContent(arg0): void;
      withContent(arg0): org.eclipse.che.api.git.shared.ShowFileContentResponse;
      getContent(): string;
      getCommits(): Array<org.eclipse.che.api.git.shared.Revision>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class ShowFileContentResponseImpl implements org.eclipse.che.api.git.shared.ShowFileContentResponse {

        commits : Array<org.eclipse.che.api.git.shared.Revision>;
        content : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.commits = new Array<org.eclipse.che.api.git.shared.Revision>();
            if (__jsonObject) {
              if (__jsonObject.commits) {
                  __jsonObject.commits.forEach((item) => {
                  this.commits.push(new org.eclipse.che.api.git.shared.RevisionImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.content) {
                this.content = __jsonObject.content;
              }
            }

    } 

        setContent(content : string) : void {
          this.content = content;
        }
        withContent(content : string) : org.eclipse.che.api.git.shared.ShowFileContentResponse {
          this.content = content;
          return this;
        }
        getContent() : string {
          return this.content;
        }
        getCommits() : Array<org.eclipse.che.api.git.shared.Revision> {
          return this.commits;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commits) {
                            let listArray = [];
                            this.commits.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.git.shared.RevisionImpl).toJson());
                            json.commits = listArray;
                            });
                        }
                        if (this.content) {
                          json.content = this.content;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface ProjectTypeDto {
      getParents(): Array<string>;
      withAttributes(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      withId(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      withParents(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      isPrimaryable(): boolean;
      withPrimaryable(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      withDisplayName(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      isMixable(): boolean;
      withMixable(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      isPersisted(): boolean;
      withPersisted(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      getAncestors(): Array<string>;
      withAncestors(arg0): org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
      getId(): string;
      getAttributes(): Array<org.eclipse.che.api.project.shared.dto.AttributeDto>;
      getDisplayName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class ProjectTypeDtoImpl implements org.eclipse.che.api.project.shared.dto.ProjectTypeDto {

        primaryable : boolean;
        displayName : string;
        attributes : Array<org.eclipse.che.api.project.shared.dto.AttributeDto>;
        mixable : boolean;
        id : string;
        persisted : boolean;
        ancestors : Array<string>;
        parents : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.primaryable) {
                this.primaryable = __jsonObject.primaryable;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.displayName) {
                this.displayName = __jsonObject.displayName;
              }
            }
            this.attributes = new Array<org.eclipse.che.api.project.shared.dto.AttributeDto>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                  __jsonObject.attributes.forEach((item) => {
                  this.attributes.push(new org.eclipse.che.api.project.shared.dto.AttributeDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.mixable) {
                this.mixable = __jsonObject.mixable;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.persisted) {
                this.persisted = __jsonObject.persisted;
              }
            }
            this.ancestors = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.ancestors) {
                  __jsonObject.ancestors.forEach((item) => {
                  this.ancestors.push(item);
                  });
              }
            }
            this.parents = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.parents) {
                  __jsonObject.parents.forEach((item) => {
                  this.parents.push(item);
                  });
              }
            }

    } 

        getParents() : Array<string> {
          return this.parents;
        }
        withAttributes(attributes : Array<org.eclipse.che.api.project.shared.dto.AttributeDto>) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.attributes = attributes;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.id = id;
          return this;
        }
        withParents(parents : Array<string>) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.parents = parents;
          return this;
        }
        isPrimaryable() : boolean {
          return this.primaryable;
        }
        withPrimaryable(primaryable : boolean) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.primaryable = primaryable;
          return this;
        }
        withDisplayName(displayName : string) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.displayName = displayName;
          return this;
        }
        isMixable() : boolean {
          return this.mixable;
        }
        withMixable(mixable : boolean) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.mixable = mixable;
          return this;
        }
        isPersisted() : boolean {
          return this.persisted;
        }
        withPersisted(persisted : boolean) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.persisted = persisted;
          return this;
        }
        getAncestors() : Array<string> {
          return this.ancestors;
        }
        withAncestors(ancestors : Array<string>) : org.eclipse.che.api.project.shared.dto.ProjectTypeDto {
          this.ancestors = ancestors;
          return this;
        }
        getId() : string {
          return this.id;
        }
        getAttributes() : Array<org.eclipse.che.api.project.shared.dto.AttributeDto> {
          return this.attributes;
        }
        getDisplayName() : string {
          return this.displayName;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.primaryable) {
                          json.primaryable = this.primaryable;
                        }
                        if (this.displayName) {
                          json.displayName = this.displayName;
                        }
                        if (this.attributes) {
                            let listArray = [];
                            this.attributes.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.project.shared.dto.AttributeDtoImpl).toJson());
                            json.attributes = listArray;
                            });
                        }
                        if (this.mixable) {
                          json.mixable = this.mixable;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.persisted) {
                          json.persisted = this.persisted;
                        }
                        if (this.ancestors) {
                            let listArray = [];
                            this.ancestors.forEach((item) => {
                            listArray.push(item);
                            json.ancestors = listArray;
                            });
                        }
                        if (this.parents) {
                            let listArray = [];
                            this.parents.forEach((item) => {
                            listArray.push(item);
                            json.parents = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface PoliciesDto {
      getSince(): number;
      setSince(arg0): void;
      getReferer(): string;
      setMatch(arg0): void;
      withMatch(arg0): org.eclipse.che.api.factory.shared.dto.PoliciesDto;
      setReferer(arg0): void;
      withReferer(arg0): org.eclipse.che.api.factory.shared.dto.PoliciesDto;
      withSince(arg0): org.eclipse.che.api.factory.shared.dto.PoliciesDto;
      getUntil(): number;
      setUntil(arg0): void;
      withUntil(arg0): org.eclipse.che.api.factory.shared.dto.PoliciesDto;
      getMatch(): string;
      withCreate(arg0): org.eclipse.che.api.factory.shared.dto.PoliciesDto;
      getCreate(): string;
      setCreate(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class PoliciesDtoImpl implements org.eclipse.che.api.factory.shared.dto.PoliciesDto {

        referer : string;
        match : string;
        create : string;
        until : number;
        since : number;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.referer) {
                this.referer = __jsonObject.referer;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.match) {
                this.match = __jsonObject.match;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.create) {
                this.create = __jsonObject.create;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.until) {
                this.until = __jsonObject.until;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.since) {
                this.since = __jsonObject.since;
              }
            }

    } 

        getSince() : number {
          return this.since;
        }
        setSince(since : number) : void {
          this.since = since;
        }
        getReferer() : string {
          return this.referer;
        }
        setMatch(match : string) : void {
          this.match = match;
        }
        withMatch(match : string) : org.eclipse.che.api.factory.shared.dto.PoliciesDto {
          this.match = match;
          return this;
        }
        setReferer(referer : string) : void {
          this.referer = referer;
        }
        withReferer(referer : string) : org.eclipse.che.api.factory.shared.dto.PoliciesDto {
          this.referer = referer;
          return this;
        }
        withSince(since : number) : org.eclipse.che.api.factory.shared.dto.PoliciesDto {
          this.since = since;
          return this;
        }
        getUntil() : number {
          return this.until;
        }
        setUntil(until : number) : void {
          this.until = until;
        }
        withUntil(until : number) : org.eclipse.che.api.factory.shared.dto.PoliciesDto {
          this.until = until;
          return this;
        }
        getMatch() : string {
          return this.match;
        }
        withCreate(create : string) : org.eclipse.che.api.factory.shared.dto.PoliciesDto {
          this.create = create;
          return this;
        }
        getCreate() : string {
          return this.create;
        }
        setCreate(create : string) : void {
          this.create = create;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.referer) {
                          json.referer = this.referer;
                        }
                        if (this.match) {
                          json.match = this.match;
                        }
                        if (this.create) {
                          json.create = this.create;
                        }
                        if (this.until) {
                          json.until = this.until;
                        }
                        if (this.since) {
                          json.since = this.since;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export interface GitCheckoutEventDto {
      withType(arg0): org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
      withName(arg0): org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
      getName(): string;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export class GitCheckoutEventDtoImpl implements org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto {

        name : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto {
          this.type = type;
          return this;
        }
        withName(name : string) : org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto {
          this.name = name;
          return this;
        }
        getName() : string {
          return this.name;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.stack {

  export interface StackDto {
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      getSource(): org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
      withSource(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      getComponents(): Array<org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto>;
      setId(arg0): void;
      setSource(arg0): void;
      setScope(arg0): void;
      setComponents(arg0): void;
      setTags(arg0): void;
      withName(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      withLinks(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      withId(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      setCreator(arg0): void;
      withCreator(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      withTags(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      withComponents(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      getWorkspaceConfig(): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      setWorkspaceConfig(arg0): void;
      withWorkspaceConfig(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      withScope(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
      setName(arg0): void;
      getDescription(): string;
      getScope(): string;
      getTags(): Array<string>;
      getCreator(): string;
      getName(): string;
      getId(): string;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.stack {

  export class StackDtoImpl implements org.eclipse.che.api.workspace.shared.dto.stack.StackDto {

        components : Array<org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto>;
        creator : string;
        scope : string;
        name : string;
        description : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        source : org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
        id : string;
        workspaceConfig : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
        tags : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.components = new Array<org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto>();
            if (__jsonObject) {
              if (__jsonObject.components) {
                  __jsonObject.components.forEach((item) => {
                  this.components.push(new org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.creator) {
                this.creator = __jsonObject.creator;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.scope) {
                this.scope = __jsonObject.scope;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.source) {
                this.source = new org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDtoImpl(__jsonObject.source);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceConfig) {
                this.workspaceConfig = new org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl(__jsonObject.workspaceConfig);
              }
            }
            this.tags = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.tags) {
                  __jsonObject.tags.forEach((item) => {
                  this.tags.push(item);
                  });
              }
            }

    } 

        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.description = description;
          return this;
        }
        getSource() : org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto {
          return this.source;
        }
        withSource(source : org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.source = source;
          return this;
        }
        getComponents() : Array<org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto> {
          return this.components;
        }
        setId(id : string) : void {
          this.id = id;
        }
        setSource(source : org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto) : void {
          this.source = source;
        }
        setScope(scope : string) : void {
          this.scope = scope;
        }
        setComponents(components : Array<org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto>) : void {
          this.components = components;
        }
        setTags(tags : Array<string>) : void {
          this.tags = tags;
        }
        withName(name : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.name = name;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.links = links;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.id = id;
          return this;
        }
        setCreator(creator : string) : void {
          this.creator = creator;
        }
        withCreator(creator : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.creator = creator;
          return this;
        }
        withTags(tags : Array<string>) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.tags = tags;
          return this;
        }
        withComponents(components : Array<org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto>) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.components = components;
          return this;
        }
        getWorkspaceConfig() : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          return this.workspaceConfig;
        }
        setWorkspaceConfig(workspaceConfig : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto) : void {
          this.workspaceConfig = workspaceConfig;
        }
        withWorkspaceConfig(workspaceConfig : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.workspaceConfig = workspaceConfig;
          return this;
        }
        withScope(scope : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackDto {
          this.scope = scope;
          return this;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getDescription() : string {
          return this.description;
        }
        getScope() : string {
          return this.scope;
        }
        getTags() : Array<string> {
          return this.tags;
        }
        getCreator() : string {
          return this.creator;
        }
        getName() : string {
          return this.name;
        }
        getId() : string {
          return this.id;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.components) {
                            let listArray = [];
                            this.components.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDtoImpl).toJson());
                            json.components = listArray;
                            });
                        }
                        if (this.creator) {
                          json.creator = this.creator;
                        }
                        if (this.scope) {
                          json.scope = this.scope;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.source) {
                          json.source = (this.source as org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDtoImpl).toJson();
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.workspaceConfig) {
                          json.workspaceConfig = (this.workspaceConfig as org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl).toJson();
                        }
                        if (this.tags) {
                            let listArray = [];
                            this.tags.forEach((item) => {
                            listArray.push(item);
                            json.tags = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Status {
      setFormat(arg0): void;
      getFormat(): string;
      getMissing(): Array<string>;
      setMissing(arg0): void;
      getBranchName(): string;
      getRemoved(): Array<string>;
      setRemoved(arg0): void;
      getConflicting(): Array<string>;
      setClean(arg0): void;
      setUntracked(arg0): void;
      setBranchName(arg0): void;
      getAdded(): Array<string>;
      setAdded(arg0): void;
      getUntracked(): Array<string>;
      getUntrackedFolders(): Array<string>;
      setUntrackedFolders(arg0): void;
      setConflicting(arg0): void;
      getRepositoryState(): string;
      setRepositoryState(arg0): void;
      getChanged(): Array<string>;
      setChanged(arg0): void;
      isClean(): boolean;
      getModified(): Array<string>;
      setModified(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class StatusImpl implements org.eclipse.che.api.git.shared.Status {

        conflicting : Array<string>;
        removed : Array<string>;
        added : Array<string>;
        untrackedFolders : Array<string>;
        format : string;
        missing : Array<string>;
        branchName : string;
        untracked : Array<string>;
        modified : Array<string>;
        clean : boolean;
        repositoryState : string;
        changed : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.conflicting = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.conflicting) {
                  __jsonObject.conflicting.forEach((item) => {
                  this.conflicting.push(item);
                  });
              }
            }
            this.removed = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.removed) {
                  __jsonObject.removed.forEach((item) => {
                  this.removed.push(item);
                  });
              }
            }
            this.added = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.added) {
                  __jsonObject.added.forEach((item) => {
                  this.added.push(item);
                  });
              }
            }
            this.untrackedFolders = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.untrackedFolders) {
                  __jsonObject.untrackedFolders.forEach((item) => {
                  this.untrackedFolders.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.format) {
                this.format = __jsonObject.format;
              }
            }
            this.missing = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.missing) {
                  __jsonObject.missing.forEach((item) => {
                  this.missing.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.branchName) {
                this.branchName = __jsonObject.branchName;
              }
            }
            this.untracked = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.untracked) {
                  __jsonObject.untracked.forEach((item) => {
                  this.untracked.push(item);
                  });
              }
            }
            this.modified = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.modified) {
                  __jsonObject.modified.forEach((item) => {
                  this.modified.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.clean) {
                this.clean = __jsonObject.clean;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.repositoryState) {
                this.repositoryState = __jsonObject.repositoryState;
              }
            }
            this.changed = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.changed) {
                  __jsonObject.changed.forEach((item) => {
                  this.changed.push(item);
                  });
              }
            }

    } 

        setFormat(format : string) : void {
          this.format = format;
        }
        getFormat() : string {
          return this.format;
        }
        getMissing() : Array<string> {
          return this.missing;
        }
        setMissing(missing : Array<string>) : void {
          this.missing = missing;
        }
        getBranchName() : string {
          return this.branchName;
        }
        getRemoved() : Array<string> {
          return this.removed;
        }
        setRemoved(removed : Array<string>) : void {
          this.removed = removed;
        }
        getConflicting() : Array<string> {
          return this.conflicting;
        }
        setClean(clean : boolean) : void {
          this.clean = clean;
        }
        setUntracked(untracked : Array<string>) : void {
          this.untracked = untracked;
        }
        setBranchName(branchName : string) : void {
          this.branchName = branchName;
        }
        getAdded() : Array<string> {
          return this.added;
        }
        setAdded(added : Array<string>) : void {
          this.added = added;
        }
        getUntracked() : Array<string> {
          return this.untracked;
        }
        getUntrackedFolders() : Array<string> {
          return this.untrackedFolders;
        }
        setUntrackedFolders(untrackedFolders : Array<string>) : void {
          this.untrackedFolders = untrackedFolders;
        }
        setConflicting(conflicting : Array<string>) : void {
          this.conflicting = conflicting;
        }
        getRepositoryState() : string {
          return this.repositoryState;
        }
        setRepositoryState(repositoryState : string) : void {
          this.repositoryState = repositoryState;
        }
        getChanged() : Array<string> {
          return this.changed;
        }
        setChanged(changed : Array<string>) : void {
          this.changed = changed;
        }
        isClean() : boolean {
          return this.clean;
        }
        getModified() : Array<string> {
          return this.modified;
        }
        setModified(modified : Array<string>) : void {
          this.modified = modified;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.conflicting) {
                            let listArray = [];
                            this.conflicting.forEach((item) => {
                            listArray.push(item);
                            json.conflicting = listArray;
                            });
                        }
                        if (this.removed) {
                            let listArray = [];
                            this.removed.forEach((item) => {
                            listArray.push(item);
                            json.removed = listArray;
                            });
                        }
                        if (this.added) {
                            let listArray = [];
                            this.added.forEach((item) => {
                            listArray.push(item);
                            json.added = listArray;
                            });
                        }
                        if (this.untrackedFolders) {
                            let listArray = [];
                            this.untrackedFolders.forEach((item) => {
                            listArray.push(item);
                            json.untrackedFolders = listArray;
                            });
                        }
                        if (this.format) {
                          json.format = this.format;
                        }
                        if (this.missing) {
                            let listArray = [];
                            this.missing.forEach((item) => {
                            listArray.push(item);
                            json.missing = listArray;
                            });
                        }
                        if (this.branchName) {
                          json.branchName = this.branchName;
                        }
                        if (this.untracked) {
                            let listArray = [];
                            this.untracked.forEach((item) => {
                            listArray.push(item);
                            json.untracked = listArray;
                            });
                        }
                        if (this.modified) {
                            let listArray = [];
                            this.modified.forEach((item) => {
                            listArray.push(item);
                            json.modified = listArray;
                            });
                        }
                        if (this.clean) {
                          json.clean = this.clean;
                        }
                        if (this.repositoryState) {
                          json.repositoryState = this.repositoryState;
                        }
                        if (this.changed) {
                            let listArray = [];
                            this.changed.forEach((item) => {
                            listArray.push(item);
                            json.changed = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface MoveOptions {
      setOverWrite(arg0): void;
      getOverWrite(): boolean;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class MoveOptionsImpl implements org.eclipse.che.api.project.shared.dto.MoveOptions {

        name : string;
        overWrite : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.overWrite) {
                this.overWrite = __jsonObject.overWrite;
              }
            }

    } 

        setOverWrite(overWrite : boolean) : void {
          this.overWrite = overWrite;
        }
        getOverWrite() : boolean {
          return this.overWrite;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.overWrite) {
                          json.overWrite = this.overWrite;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface StepOutActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class StepOutActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineProcessDto {
      withType(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      setType(arg0): void;
      setPid(arg0): void;
      withName(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      withLinks(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      withCommandLine(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      withPid(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      setAlive(arg0): void;
      withAlive(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      withOutputChannel(arg0): org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
      setOutputChannel(arg0): void;
      setCommandLine(arg0): void;
      setName(arg0): void;
      getPid(): number;
      getOutputChannel(): string;
      isAlive(): boolean;
      getCommandLine(): string;
      getName(): string;
      getType(): string;
      getAttributes(): Map<string,string>;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineProcessDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineProcessDto {

        alive : boolean;
        outputChannel : string;
        name : string;
        pid : number;
        attributes : Map<string,string>;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        type : string;
        commandLine : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.alive) {
                this.alive = __jsonObject.alive;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.outputChannel) {
                this.outputChannel = __jsonObject.outputChannel;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.pid) {
                this.pid = __jsonObject.pid;
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.commandLine) {
                this.commandLine = __jsonObject.commandLine;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setPid(pid : number) : void {
          this.pid = pid;
        }
        withName(name : string) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.name = name;
          return this;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.attributes = attributes;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.links = links;
          return this;
        }
        withCommandLine(commandLine : string) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.commandLine = commandLine;
          return this;
        }
        withPid(pid : number) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.pid = pid;
          return this;
        }
        setAlive(alive : boolean) : void {
          this.alive = alive;
        }
        withAlive(alive : boolean) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.alive = alive;
          return this;
        }
        withOutputChannel(outputChannel : string) : org.eclipse.che.api.machine.shared.dto.MachineProcessDto {
          this.outputChannel = outputChannel;
          return this;
        }
        setOutputChannel(outputChannel : string) : void {
          this.outputChannel = outputChannel;
        }
        setCommandLine(commandLine : string) : void {
          this.commandLine = commandLine;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getPid() : number {
          return this.pid;
        }
        getOutputChannel() : string {
          return this.outputChannel;
        }
        isAlive() : boolean {
          return this.alive;
        }
        getCommandLine() : string {
          return this.commandLine;
        }
        getName() : string {
          return this.name;
        }
        getType() : string {
          return this.type;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.alive) {
                          json.alive = this.alive;
                        }
                        if (this.outputChannel) {
                          json.outputChannel = this.outputChannel;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.pid) {
                          json.pid = this.pid;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.commandLine) {
                          json.commandLine = this.commandLine;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface GitUser {
      getEmail(): string;
      setEmail(arg0): void;
      withName(arg0): org.eclipse.che.api.git.shared.GitUser;
      withEmail(arg0): org.eclipse.che.api.git.shared.GitUser;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class GitUserImpl implements org.eclipse.che.api.git.shared.GitUser {

        name : string;
        email : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.email) {
                this.email = __jsonObject.email;
              }
            }

    } 

        getEmail() : string {
          return this.email;
        }
        setEmail(email : string) : void {
          this.email = email;
        }
        withName(name : string) : org.eclipse.che.api.git.shared.GitUser {
          this.name = name;
          return this;
        }
        withEmail(email : string) : org.eclipse.che.api.git.shared.GitUser {
          this.email = email;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.email) {
                          json.email = this.email;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface StepOverActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class StepOverActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface ProjectImporterData {
      getConfiguration(): Map<string,string>;
      setConfiguration(arg0): void;
      setImporters(arg0): void;
      withConfiguration(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterData;
      withImporters(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterData;
      getImporters(): Array<org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class ProjectImporterDataImpl implements org.eclipse.che.api.project.shared.dto.ProjectImporterData {

        importers : Array<org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor>;
        configuration : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.importers = new Array<org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor>();
            if (__jsonObject) {
              if (__jsonObject.importers) {
                  __jsonObject.importers.forEach((item) => {
                  this.importers.push(new org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptorImpl(item));
                  });
              }
            }
            this.configuration = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.configuration) {
                let tmp : Array<any> = Object.keys(__jsonObject.configuration);
                tmp.forEach((key) => {
                  this.configuration.set(key, __jsonObject.configuration[key]);
                 });
              }
            }

    } 

        getConfiguration() : Map<string,string> {
          return this.configuration;
        }
        setConfiguration(configuration : Map<string,string>) : void {
          this.configuration = configuration;
        }
        setImporters(importers : Array<org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor>) : void {
          this.importers = importers;
        }
        withConfiguration(configuration : Map<string,string>) : org.eclipse.che.api.project.shared.dto.ProjectImporterData {
          this.configuration = configuration;
          return this;
        }
        withImporters(importers : Array<org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor>) : org.eclipse.che.api.project.shared.dto.ProjectImporterData {
          this.importers = importers;
          return this;
        }
        getImporters() : Array<org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor> {
          return this.importers;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.importers) {
                            let listArray = [];
                            this.importers.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptorImpl).toJson());
                            json.importers = listArray;
                            });
                        }
                        if (this.configuration) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.configuration.entries()) {
                            tmpMap[key] = value;
                           }
                          json.configuration = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface AddRequest {
      getFilePattern(): Array<string>;
      setFilePattern(arg0): void;
      withFilePattern(arg0): org.eclipse.che.api.git.shared.AddRequest;
      isUpdate(): boolean;
      setUpdate(arg0): void;
      withUpdate(arg0): org.eclipse.che.api.git.shared.AddRequest;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class AddRequestImpl implements org.eclipse.che.api.git.shared.AddRequest {

        filePattern : Array<string>;
        update : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.filePattern = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.filePattern) {
                  __jsonObject.filePattern.forEach((item) => {
                  this.filePattern.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.update) {
                this.update = __jsonObject.update;
              }
            }

    } 

        getFilePattern() : Array<string> {
          return this.filePattern;
        }
        setFilePattern(filePattern : Array<string>) : void {
          this.filePattern = filePattern;
        }
        withFilePattern(filePattern : Array<string>) : org.eclipse.che.api.git.shared.AddRequest {
          this.filePattern = filePattern;
          return this;
        }
        isUpdate() : boolean {
          return this.update;
        }
        setUpdate(update : boolean) : void {
          this.update = update;
        }
        withUpdate(update : boolean) : org.eclipse.che.api.git.shared.AddRequest {
          this.update = update;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.filePattern) {
                            let listArray = [];
                            this.filePattern.forEach((item) => {
                            listArray.push(item);
                            json.filePattern = listArray;
                            });
                        }
                        if (this.update) {
                          json.update = this.update;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface ProjectProblemDto {
      setMessage(arg0): void;
      getCode(): number;
      setCode(arg0): void;
      withCode(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
      withMessage(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
      getMessage(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class ProjectProblemDtoImpl implements org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto {

        code : number;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.code) {
                this.code = __jsonObject.code;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        setMessage(message : string) : void {
          this.message = message;
        }
        getCode() : number {
          return this.code;
        }
        setCode(code : number) : void {
          this.code = code;
        }
        withCode(code : number) : org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto {
          this.code = code;
          return this;
        }
        withMessage(message : string) : org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto {
          this.message = message;
          return this;
        }
        getMessage() : string {
          return this.message;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.code) {
                          json.code = this.code;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface ActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.ActionDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class ActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.ActionDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.ActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface FactoryDto {
      setId(arg0): void;
      getWorkspace(): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      withName(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      withLinks(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      withId(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      setWorkspace(arg0): void;
      withWorkspace(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      getPolicies(): org.eclipse.che.api.factory.shared.dto.PoliciesDto;
      setPolicies(arg0): void;
      withPolicies(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      getV(): string;
      setV(arg0): void;
      withV(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      getCreator(): org.eclipse.che.api.factory.shared.dto.AuthorDto;
      setCreator(arg0): void;
      withCreator(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      getButton(): org.eclipse.che.api.factory.shared.dto.ButtonDto;
      setButton(arg0): void;
      withButton(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      getIde(): org.eclipse.che.api.factory.shared.dto.IdeDto;
      setIde(arg0): void;
      withIde(arg0): org.eclipse.che.api.factory.shared.dto.FactoryDto;
      getName(): string;
      setName(arg0): void;
      getId(): string;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class FactoryDtoImpl implements org.eclipse.che.api.factory.shared.dto.FactoryDto {

        button : org.eclipse.che.api.factory.shared.dto.ButtonDto;
        workspace : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
        creator : org.eclipse.che.api.factory.shared.dto.AuthorDto;
        v : string;
        name : string;
        policies : org.eclipse.che.api.factory.shared.dto.PoliciesDto;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        id : string;
        ide : org.eclipse.che.api.factory.shared.dto.IdeDto;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.button) {
                this.button = new org.eclipse.che.api.factory.shared.dto.ButtonDtoImpl(__jsonObject.button);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspace) {
                this.workspace = new org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl(__jsonObject.workspace);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.creator) {
                this.creator = new org.eclipse.che.api.factory.shared.dto.AuthorDtoImpl(__jsonObject.creator);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.v) {
                this.v = __jsonObject.v;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.policies) {
                this.policies = new org.eclipse.che.api.factory.shared.dto.PoliciesDtoImpl(__jsonObject.policies);
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.ide) {
                this.ide = new org.eclipse.che.api.factory.shared.dto.IdeDtoImpl(__jsonObject.ide);
              }
            }

    } 

        setId(id : string) : void {
          this.id = id;
        }
        getWorkspace() : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          return this.workspace;
        }
        withName(name : string) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.name = name;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.links = links;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.id = id;
          return this;
        }
        setWorkspace(workspace : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto) : void {
          this.workspace = workspace;
        }
        withWorkspace(workspace : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.workspace = workspace;
          return this;
        }
        getPolicies() : org.eclipse.che.api.factory.shared.dto.PoliciesDto {
          return this.policies;
        }
        setPolicies(policies : org.eclipse.che.api.factory.shared.dto.PoliciesDto) : void {
          this.policies = policies;
        }
        withPolicies(policies : org.eclipse.che.api.factory.shared.dto.PoliciesDto) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.policies = policies;
          return this;
        }
        getV() : string {
          return this.v;
        }
        setV(v : string) : void {
          this.v = v;
        }
        withV(v : string) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.v = v;
          return this;
        }
        getCreator() : org.eclipse.che.api.factory.shared.dto.AuthorDto {
          return this.creator;
        }
        setCreator(creator : org.eclipse.che.api.factory.shared.dto.AuthorDto) : void {
          this.creator = creator;
        }
        withCreator(creator : org.eclipse.che.api.factory.shared.dto.AuthorDto) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.creator = creator;
          return this;
        }
        getButton() : org.eclipse.che.api.factory.shared.dto.ButtonDto {
          return this.button;
        }
        setButton(button : org.eclipse.che.api.factory.shared.dto.ButtonDto) : void {
          this.button = button;
        }
        withButton(button : org.eclipse.che.api.factory.shared.dto.ButtonDto) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.button = button;
          return this;
        }
        getIde() : org.eclipse.che.api.factory.shared.dto.IdeDto {
          return this.ide;
        }
        setIde(ide : org.eclipse.che.api.factory.shared.dto.IdeDto) : void {
          this.ide = ide;
        }
        withIde(ide : org.eclipse.che.api.factory.shared.dto.IdeDto) : org.eclipse.che.api.factory.shared.dto.FactoryDto {
          this.ide = ide;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getId() : string {
          return this.id;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.button) {
                          json.button = (this.button as org.eclipse.che.api.factory.shared.dto.ButtonDtoImpl).toJson();
                        }
                        if (this.workspace) {
                          json.workspace = (this.workspace as org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl).toJson();
                        }
                        if (this.creator) {
                          json.creator = (this.creator as org.eclipse.che.api.factory.shared.dto.AuthorDtoImpl).toJson();
                        }
                        if (this.v) {
                          json.v = this.v;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.policies) {
                          json.policies = (this.policies as org.eclipse.che.api.factory.shared.dto.PoliciesDtoImpl).toJson();
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.ide) {
                          json.ide = (this.ide as org.eclipse.che.api.factory.shared.dto.IdeDtoImpl).toJson();
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface Hyperlinks {
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;
      withLinks(arg0): org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class HyperlinksImpl implements org.eclipse.che.api.core.rest.shared.dto.Hyperlinks {

        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }

    } 

        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.core.rest.shared.dto.Hyperlinks {
          this.links = links;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface MergeResult {
      getConflicts(): Array<string>;
      setConflicts(arg0): void;
      withConflicts(arg0): org.eclipse.che.api.git.shared.MergeResult;
      getFailed(): Array<string>;
      setFailed(arg0): void;
      withFailed(arg0): org.eclipse.che.api.git.shared.MergeResult;
      getNewHead(): string;
      setNewHead(arg0): void;
      withNewHead(arg0): org.eclipse.che.api.git.shared.MergeResult;
      getMergeStatus(): string;
      setMergeStatus(arg0): void;
      withMergeStatus(arg0): org.eclipse.che.api.git.shared.MergeResult;
      getMergedCommits(): Array<string>;
      setMergedCommits(arg0): void;
      withMergedCommits(arg0): org.eclipse.che.api.git.shared.MergeResult;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class MergeResultImpl implements org.eclipse.che.api.git.shared.MergeResult {

        newHead : string;
        mergedCommits : Array<string>;
        conflicts : Array<string>;
        failed : Array<string>;
        mergeStatus : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.newHead) {
                this.newHead = __jsonObject.newHead;
              }
            }
            this.mergedCommits = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.mergedCommits) {
                  __jsonObject.mergedCommits.forEach((item) => {
                  this.mergedCommits.push(item);
                  });
              }
            }
            this.conflicts = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.conflicts) {
                  __jsonObject.conflicts.forEach((item) => {
                  this.conflicts.push(item);
                  });
              }
            }
            this.failed = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.failed) {
                  __jsonObject.failed.forEach((item) => {
                  this.failed.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.mergeStatus) {
                this.mergeStatus = __jsonObject.mergeStatus;
              }
            }

    } 

        getConflicts() : Array<string> {
          return this.conflicts;
        }
        setConflicts(conflicts : Array<string>) : void {
          this.conflicts = conflicts;
        }
        withConflicts(conflicts : Array<string>) : org.eclipse.che.api.git.shared.MergeResult {
          this.conflicts = conflicts;
          return this;
        }
        getFailed() : Array<string> {
          return this.failed;
        }
        setFailed(failed : Array<string>) : void {
          this.failed = failed;
        }
        withFailed(failed : Array<string>) : org.eclipse.che.api.git.shared.MergeResult {
          this.failed = failed;
          return this;
        }
        getNewHead() : string {
          return this.newHead;
        }
        setNewHead(newHead : string) : void {
          this.newHead = newHead;
        }
        withNewHead(newHead : string) : org.eclipse.che.api.git.shared.MergeResult {
          this.newHead = newHead;
          return this;
        }
        getMergeStatus() : string {
          return this.mergeStatus;
        }
        setMergeStatus(mergeStatus : string) : void {
          this.mergeStatus = mergeStatus;
        }
        withMergeStatus(mergeStatus : string) : org.eclipse.che.api.git.shared.MergeResult {
          this.mergeStatus = mergeStatus;
          return this;
        }
        getMergedCommits() : Array<string> {
          return this.mergedCommits;
        }
        setMergedCommits(mergedCommits : Array<string>) : void {
          this.mergedCommits = mergedCommits;
        }
        withMergedCommits(mergedCommits : Array<string>) : org.eclipse.che.api.git.shared.MergeResult {
          this.mergedCommits = mergedCommits;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.newHead) {
                          json.newHead = this.newHead;
                        }
                        if (this.mergedCommits) {
                            let listArray = [];
                            this.mergedCommits.forEach((item) => {
                            listArray.push(item);
                            json.mergedCommits = listArray;
                            });
                        }
                        if (this.conflicts) {
                            let listArray = [];
                            this.conflicts.forEach((item) => {
                            listArray.push(item);
                            json.conflicts = listArray;
                            });
                        }
                        if (this.failed) {
                            let listArray = [];
                            this.failed.forEach((item) => {
                            listArray.push(item);
                            json.failed = listArray;
                            });
                        }
                        if (this.mergeStatus) {
                          json.mergeStatus = this.mergeStatus;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface DebuggerInfoDto {
      getVersion(): string;
      setVersion(arg0): void;
      setFile(arg0): void;
      setHost(arg0): void;
      setPort(arg0): void;
      getPid(): number;
      setPid(arg0): void;
      withName(arg0): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      withVersion(arg0): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      withPort(arg0): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      withPid(arg0): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      withFile(arg0): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      withHost(arg0): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      getName(): string;
      setName(arg0): void;
      getFile(): string;
      getHost(): string;
      getPort(): number;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class DebuggerInfoDtoImpl implements org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {

        file : string;
        port : number;
        host : string;
        name : string;
        pid : number;
        version : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.file) {
                this.file = __jsonObject.file;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.port) {
                this.port = __jsonObject.port;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.host) {
                this.host = __jsonObject.host;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.pid) {
                this.pid = __jsonObject.pid;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.version) {
                this.version = __jsonObject.version;
              }
            }

    } 

        getVersion() : string {
          return this.version;
        }
        setVersion(version : string) : void {
          this.version = version;
        }
        setFile(file : string) : void {
          this.file = file;
        }
        setHost(host : string) : void {
          this.host = host;
        }
        setPort(port : number) : void {
          this.port = port;
        }
        getPid() : number {
          return this.pid;
        }
        setPid(pid : number) : void {
          this.pid = pid;
        }
        withName(name : string) : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          this.name = name;
          return this;
        }
        withVersion(version : string) : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          this.version = version;
          return this;
        }
        withPort(port : number) : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          this.port = port;
          return this;
        }
        withPid(pid : number) : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          this.pid = pid;
          return this;
        }
        withFile(file : string) : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          this.file = file;
          return this;
        }
        withHost(host : string) : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          this.host = host;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getFile() : string {
          return this.file;
        }
        getHost() : string {
          return this.host;
        }
        getPort() : number {
          return this.port;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.file) {
                          json.file = this.file;
                        }
                        if (this.port) {
                          json.port = this.port;
                        }
                        if (this.host) {
                          json.host = this.host;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.pid) {
                          json.pid = this.pid;
                        }
                        if (this.version) {
                          json.version = this.version;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface StepIntoActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class StepIntoActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export interface JsonRpcError {
      getData(): string;
      getCode(): number;
      withData(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError;
      withCode(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError;
      withMessage(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError;
      getMessage(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export class JsonRpcErrorImpl implements org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError {

        code : number;
        data : string;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.code) {
                this.code = __jsonObject.code;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.data) {
                this.data = __jsonObject.data;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        getData() : string {
          return this.data;
        }
        getCode() : number {
          return this.code;
        }
        withData(data : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError {
          this.data = data;
          return this;
        }
        withCode(code : number) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError {
          this.code = code;
          return this;
        }
        withMessage(message : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError {
          this.message = message;
          return this;
        }
        getMessage() : string {
          return this.message;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.code) {
                          json.code = this.code;
                        }
                        if (this.data) {
                          json.data = this.data;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface ProjectConfigDto {
      getProblems(): Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>;
      getDescription(): string;
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      getSource(): org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
      withSource(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      setSource(arg0): void;
      setProblems(arg0): void;
      setPath(arg0): void;
      withName(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;
      withLinks(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      withPath(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      getMixins(): Array<string>;
      setMixins(arg0): void;
      withMixins(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      withProblems(arg0): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      getName(): string;
      setName(arg0): void;
      getType(): string;
      getPath(): string;
      getAttributes(): Map<string,Array<string>>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class ProjectConfigDtoImpl implements org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {

        path : string;
        mixins : Array<string>;
        name : string;
        description : string;
        attributes : Map<string,Array<string>>;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        source : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
        type : string;
        problems : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            this.mixins = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.mixins) {
                  __jsonObject.mixins.forEach((item) => {
                  this.mixins.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.attributes = new Map<string,Array<string>>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.source) {
                this.source = new org.eclipse.che.api.workspace.shared.dto.SourceStorageDtoImpl(__jsonObject.source);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            this.problems = new Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>();
            if (__jsonObject) {
              if (__jsonObject.problems) {
                  __jsonObject.problems.forEach((item) => {
                  this.problems.push(new org.eclipse.che.api.workspace.shared.dto.ProjectProblemDtoImpl(item));
                  });
              }
            }

    } 

        getProblems() : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto> {
          return this.problems;
        }
        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.description = description;
          return this;
        }
        getSource() : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto {
          return this.source;
        }
        withSource(source : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.source = source;
          return this;
        }
        setSource(source : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto) : void {
          this.source = source;
        }
        setProblems(problems : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>) : void {
          this.problems = problems;
        }
        setPath(path : string) : void {
          this.path = path;
        }
        withName(name : string) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.name = name;
          return this;
        }
        setAttributes(attributes : Map<string,Array<string>>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,Array<string>>) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.attributes = attributes;
          return this;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.links = links;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.path = path;
          return this;
        }
        getMixins() : Array<string> {
          return this.mixins;
        }
        setMixins(mixins : Array<string>) : void {
          this.mixins = mixins;
        }
        withMixins(mixins : Array<string>) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.mixins = mixins;
          return this;
        }
        withProblems(problems : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>) : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          this.problems = problems;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getType() : string {
          return this.type;
        }
        getPath() : string {
          return this.path;
        }
        getAttributes() : Map<string,Array<string>> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.mixins) {
                            let listArray = [];
                            this.mixins.forEach((item) => {
                            listArray.push(item);
                            json.mixins = listArray;
                            });
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.source) {
                          json.source = (this.source as org.eclipse.che.api.workspace.shared.dto.SourceStorageDtoImpl).toJson();
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.problems) {
                            let listArray = [];
                            this.problems.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.workspace.shared.dto.ProjectProblemDtoImpl).toJson());
                            json.problems = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RepoInfo {
      withRemoteUri(arg0): org.eclipse.che.api.git.shared.RepoInfo;
      getRemoteUri(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RepoInfoImpl implements org.eclipse.che.api.git.shared.RepoInfo {

        remoteUri : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.remoteUri) {
                this.remoteUri = __jsonObject.remoteUri;
              }
            }

    } 

        withRemoteUri(remoteUri : string) : org.eclipse.che.api.git.shared.RepoInfo {
          this.remoteUri = remoteUri;
          return this;
        }
        getRemoteUri() : string {
          return this.remoteUri;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.remoteUri) {
                          json.remoteUri = this.remoteUri;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface SourceEstimation {
      withType(arg0): org.eclipse.che.api.project.shared.dto.SourceEstimation;
      withAttributes(arg0): org.eclipse.che.api.project.shared.dto.SourceEstimation;
      withMatched(arg0): org.eclipse.che.api.project.shared.dto.SourceEstimation;
      isMatched(): boolean;
      getType(): string;
      getAttributes(): Map<string,Array<string>>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class SourceEstimationImpl implements org.eclipse.che.api.project.shared.dto.SourceEstimation {

        attributes : Map<string,Array<string>>;
        matched : boolean;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.attributes = new Map<string,Array<string>>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.matched) {
                this.matched = __jsonObject.matched;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.project.shared.dto.SourceEstimation {
          this.type = type;
          return this;
        }
        withAttributes(attributes : Map<string,Array<string>>) : org.eclipse.che.api.project.shared.dto.SourceEstimation {
          this.attributes = attributes;
          return this;
        }
        withMatched(matched : boolean) : org.eclipse.che.api.project.shared.dto.SourceEstimation {
          this.matched = matched;
          return this;
        }
        isMatched() : boolean {
          return this.matched;
        }
        getType() : string {
          return this.type;
        }
        getAttributes() : Map<string,Array<string>> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.matched) {
                          json.matched = this.matched;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.agent.shared.dto {

  export interface AgentDto {
      getVersion(): string;
      getServers(): Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>;
      setServers(arg0): void;
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      setVersion(arg0): void;
      getDependencies(): Array<string>;
      setId(arg0): void;
      setDependencies(arg0): void;
      withName(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      withDependencies(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      withId(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      withVersion(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      setScript(arg0): void;
      withScript(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      withProperties(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      withServers(arg0): org.eclipse.che.api.agent.shared.dto.AgentDto;
      getName(): string;
      getProperties(): Map<string,string>;
      setProperties(arg0): void;
      setName(arg0): void;
      getId(): string;
      getScript(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.agent.shared.dto {

  export class AgentDtoImpl implements org.eclipse.che.api.agent.shared.dto.AgentDto {

        servers : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>;
        name : string;
        description : string;
        id : string;
        version : string;
        script : string;
        properties : Map<string,string>;
        dependencies : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.servers = new Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>();
            if (__jsonObject) {
              if (__jsonObject.servers) {
                let tmp : Array<any> = Object.keys(__jsonObject.servers);
                tmp.forEach((key) => {
                  this.servers.set(key, new org.eclipse.che.api.workspace.shared.dto.ServerConf2DtoImpl(__jsonObject.servers[key]));
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.version) {
                this.version = __jsonObject.version;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.script) {
                this.script = __jsonObject.script;
              }
            }
            this.properties = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.properties) {
                let tmp : Array<any> = Object.keys(__jsonObject.properties);
                tmp.forEach((key) => {
                  this.properties.set(key, __jsonObject.properties[key]);
                 });
              }
            }
            this.dependencies = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.dependencies) {
                  __jsonObject.dependencies.forEach((item) => {
                  this.dependencies.push(item);
                  });
              }
            }

    } 

        getVersion() : string {
          return this.version;
        }
        getServers() : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto> {
          return this.servers;
        }
        setServers(servers : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>) : void {
          this.servers = servers;
        }
        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.description = description;
          return this;
        }
        setVersion(version : string) : void {
          this.version = version;
        }
        getDependencies() : Array<string> {
          return this.dependencies;
        }
        setId(id : string) : void {
          this.id = id;
        }
        setDependencies(dependencies : Array<string>) : void {
          this.dependencies = dependencies;
        }
        withName(name : string) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.name = name;
          return this;
        }
        withDependencies(dependencies : Array<string>) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.dependencies = dependencies;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.id = id;
          return this;
        }
        withVersion(version : string) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.version = version;
          return this;
        }
        setScript(script : string) : void {
          this.script = script;
        }
        withScript(script : string) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.script = script;
          return this;
        }
        withProperties(properties : Map<string,string>) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.properties = properties;
          return this;
        }
        withServers(servers : Map<string,org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto>) : org.eclipse.che.api.agent.shared.dto.AgentDto {
          this.servers = servers;
          return this;
        }
        getName() : string {
          return this.name;
        }
        getProperties() : Map<string,string> {
          return this.properties;
        }
        setProperties(properties : Map<string,string>) : void {
          this.properties = properties;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getId() : string {
          return this.id;
        }
        getScript() : string {
          return this.script;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.servers) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.servers.entries()) {
                            tmpMap[key] = (value as org.eclipse.che.api.workspace.shared.dto.ServerConf2DtoImpl).toJson();
                           }
                          json.servers = tmpMap;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.version) {
                          json.version = this.version;
                        }
                        if (this.script) {
                          json.script = this.script;
                        }
                        if (this.properties) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.properties.entries()) {
                            tmpMap[key] = value;
                           }
                          json.properties = tmpMap;
                        }
                        if (this.dependencies) {
                            let listArray = [];
                            this.dependencies.forEach((item) => {
                            listArray.push(item);
                            json.dependencies = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface ProjectImporterDescriptor {
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
      setId(arg0): void;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
      withId(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
      getCategory(): string;
      setCategory(arg0): void;
      withCategory(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
      isInternal(): boolean;
      setInternal(arg0): void;
      withInternal(arg0): org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
      getId(): string;
      getAttributes(): Map<string,string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class ProjectImporterDescriptorImpl implements org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor {

        internal : boolean;
        description : string;
        attributes : Map<string,string>;
        id : string;
        category : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.internal) {
                this.internal = __jsonObject.internal;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.category) {
                this.category = __jsonObject.category;
              }
            }

    } 

        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor {
          this.description = description;
          return this;
        }
        setId(id : string) : void {
          this.id = id;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor {
          this.attributes = attributes;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor {
          this.id = id;
          return this;
        }
        getCategory() : string {
          return this.category;
        }
        setCategory(category : string) : void {
          this.category = category;
        }
        withCategory(category : string) : org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor {
          this.category = category;
          return this;
        }
        isInternal() : boolean {
          return this.internal;
        }
        setInternal(internal : boolean) : void {
          this.internal = internal;
        }
        withInternal(internal : boolean) : org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor {
          this.internal = internal;
          return this;
        }
        getId() : string {
          return this.id;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.internal) {
                          json.internal = this.internal;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.category) {
                          json.category = this.category;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineSourceDto {
      withType(arg0): org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
      setType(arg0): void;
      setLocation(arg0): void;
      setContent(arg0): void;
      withContent(arg0): org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
      withLocation(arg0): org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
      getLocation(): string;
      getType(): string;
      getContent(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineSourceDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineSourceDto {

        location : string;
        type : string;
        content : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.location) {
                this.location = __jsonObject.location;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.content) {
                this.content = __jsonObject.content;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.machine.shared.dto.MachineSourceDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setLocation(location : string) : void {
          this.location = location;
        }
        setContent(content : string) : void {
          this.content = content;
        }
        withContent(content : string) : org.eclipse.che.api.machine.shared.dto.MachineSourceDto {
          this.content = content;
          return this;
        }
        withLocation(location : string) : org.eclipse.che.api.machine.shared.dto.MachineSourceDto {
          this.location = location;
          return this;
        }
        getLocation() : string {
          return this.location;
        }
        getType() : string {
          return this.type;
        }
        getContent() : string {
          return this.content;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.location) {
                          json.location = this.location;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.content) {
                          json.content = this.content;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface EnvironmentDto {
      setRecipe(arg0): void;
      withRecipe(arg0): org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
      getMachines(): Map<string,org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto>;
      setMachines(arg0): void;
      withMachines(arg0): org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
      getRecipe(): org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class EnvironmentDtoImpl implements org.eclipse.che.api.workspace.shared.dto.EnvironmentDto {

        recipe : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
        machines : Map<string,org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.recipe) {
                this.recipe = new org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDtoImpl(__jsonObject.recipe);
              }
            }
            this.machines = new Map<string,org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto>();
            if (__jsonObject) {
              if (__jsonObject.machines) {
                let tmp : Array<any> = Object.keys(__jsonObject.machines);
                tmp.forEach((key) => {
                  this.machines.set(key, new org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDtoImpl(__jsonObject.machines[key]));
                 });
              }
            }

    } 

        setRecipe(recipe : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto) : void {
          this.recipe = recipe;
        }
        withRecipe(recipe : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto) : org.eclipse.che.api.workspace.shared.dto.EnvironmentDto {
          this.recipe = recipe;
          return this;
        }
        getMachines() : Map<string,org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto> {
          return this.machines;
        }
        setMachines(machines : Map<string,org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto>) : void {
          this.machines = machines;
        }
        withMachines(machines : Map<string,org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto>) : org.eclipse.che.api.workspace.shared.dto.EnvironmentDto {
          this.machines = machines;
          return this;
        }
        getRecipe() : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto {
          return this.recipe;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.recipe) {
                          json.recipe = (this.recipe as org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDtoImpl).toJson();
                        }
                        if (this.machines) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.machines.entries()) {
                            tmpMap[key] = (value as org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDtoImpl).toJson();
                           }
                          json.machines = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Log {
      getCommits(): Array<org.eclipse.che.api.git.shared.Revision>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class LogImpl implements org.eclipse.che.api.git.shared.Log {

        commits : Array<org.eclipse.che.api.git.shared.Revision>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.commits = new Array<org.eclipse.che.api.git.shared.Revision>();
            if (__jsonObject) {
              if (__jsonObject.commits) {
                  __jsonObject.commits.forEach((item) => {
                  this.commits.push(new org.eclipse.che.api.git.shared.RevisionImpl(item));
                  });
              }
            }

    } 

        getCommits() : Array<org.eclipse.che.api.git.shared.Revision> {
          return this.commits;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commits) {
                            let listArray = [];
                            this.commits.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.git.shared.RevisionImpl).toJson());
                            json.commits = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export interface DisconnectEventDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export class DisconnectEventDtoImpl implements org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export interface SuspendEventDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
      setType(arg0): void;
      setLocation(arg0): void;
      withLocation(arg0): org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
      getLocation(): org.eclipse.che.api.debug.shared.dto.LocationDto;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export class SuspendEventDtoImpl implements org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto {

        location : org.eclipse.che.api.debug.shared.dto.LocationDto;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.location) {
                this.location = new org.eclipse.che.api.debug.shared.dto.LocationDtoImpl(__jsonObject.location);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setLocation(location : org.eclipse.che.api.debug.shared.dto.LocationDto) : void {
          this.location = location;
        }
        withLocation(location : org.eclipse.che.api.debug.shared.dto.LocationDto) : org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto {
          this.location = location;
          return this;
        }
        getLocation() : org.eclipse.che.api.debug.shared.dto.LocationDto {
          return this.location;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.location) {
                          json.location = (this.location as org.eclipse.che.api.debug.shared.dto.LocationDtoImpl).toJson();
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface BreakpointDto {
      isEnabled(): boolean;
      setEnabled(arg0): void;
      setLocation(arg0): void;
      withLocation(arg0): org.eclipse.che.api.debug.shared.dto.BreakpointDto;
      setCondition(arg0): void;
      withCondition(arg0): org.eclipse.che.api.debug.shared.dto.BreakpointDto;
      withEnabled(arg0): org.eclipse.che.api.debug.shared.dto.BreakpointDto;
      getCondition(): string;
      getLocation(): org.eclipse.che.api.debug.shared.dto.LocationDto;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class BreakpointDtoImpl implements org.eclipse.che.api.debug.shared.dto.BreakpointDto {

        condition : string;
        location : org.eclipse.che.api.debug.shared.dto.LocationDto;
        enabled : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.condition) {
                this.condition = __jsonObject.condition;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.location) {
                this.location = new org.eclipse.che.api.debug.shared.dto.LocationDtoImpl(__jsonObject.location);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.enabled) {
                this.enabled = __jsonObject.enabled;
              }
            }

    } 

        isEnabled() : boolean {
          return this.enabled;
        }
        setEnabled(enabled : boolean) : void {
          this.enabled = enabled;
        }
        setLocation(location : org.eclipse.che.api.debug.shared.dto.LocationDto) : void {
          this.location = location;
        }
        withLocation(location : org.eclipse.che.api.debug.shared.dto.LocationDto) : org.eclipse.che.api.debug.shared.dto.BreakpointDto {
          this.location = location;
          return this;
        }
        setCondition(condition : string) : void {
          this.condition = condition;
        }
        withCondition(condition : string) : org.eclipse.che.api.debug.shared.dto.BreakpointDto {
          this.condition = condition;
          return this;
        }
        withEnabled(enabled : boolean) : org.eclipse.che.api.debug.shared.dto.BreakpointDto {
          this.enabled = enabled;
          return this;
        }
        getCondition() : string {
          return this.condition;
        }
        getLocation() : org.eclipse.che.api.debug.shared.dto.LocationDto {
          return this.location;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.condition) {
                          json.condition = this.condition;
                        }
                        if (this.location) {
                          json.location = (this.location as org.eclipse.che.api.debug.shared.dto.LocationDtoImpl).toJson();
                        }
                        if (this.enabled) {
                          json.enabled = this.enabled;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.user.shared.dto {

  export interface ProfileDto {
      getEmail(): string;
      getUserId(): string;
      setUserId(arg0): void;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.user.shared.dto.ProfileDto;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;
      withLinks(arg0): org.eclipse.che.api.user.shared.dto.ProfileDto;
      withUserId(arg0): org.eclipse.che.api.user.shared.dto.ProfileDto;
      withEmail(arg0): org.eclipse.che.api.user.shared.dto.ProfileDto;
      getAttributes(): Map<string,string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.user.shared.dto {

  export class ProfileDtoImpl implements org.eclipse.che.api.user.shared.dto.ProfileDto {

        attributes : Map<string,string>;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        userId : string;
        email : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.userId) {
                this.userId = __jsonObject.userId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.email) {
                this.email = __jsonObject.email;
              }
            }

    } 

        getEmail() : string {
          return this.email;
        }
        getUserId() : string {
          return this.userId;
        }
        setUserId(userId : string) : void {
          this.userId = userId;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.user.shared.dto.ProfileDto {
          this.attributes = attributes;
          return this;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.user.shared.dto.ProfileDto {
          this.links = links;
          return this;
        }
        withUserId(userId : string) : org.eclipse.che.api.user.shared.dto.ProfileDto {
          this.userId = userId;
          return this;
        }
        withEmail(email : string) : org.eclipse.che.api.user.shared.dto.ProfileDto {
          this.email = email;
          return this;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.userId) {
                          json.userId = this.userId;
                        }
                        if (this.email) {
                          json.email = this.email;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface WorkspaceRuntimeDto {
      withLinks(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
      withActiveEnv(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
      getDevMachine(): org.eclipse.che.api.machine.shared.dto.MachineDto;
      setDevMachine(arg0): void;
      withDevMachine(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
      getMachines(): Array<org.eclipse.che.api.machine.shared.dto.MachineDto>;
      setMachines(arg0): void;
      withMachines(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
      setRootFolder(arg0): void;
      withRootFolder(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
      setActiveEnv(arg0): void;
      getActiveEnv(): string;
      getRootFolder(): string;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class WorkspaceRuntimeDtoImpl implements org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {

        devMachine : org.eclipse.che.api.machine.shared.dto.MachineDto;
        rootFolder : string;
        activeEnv : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        machines : Array<org.eclipse.che.api.machine.shared.dto.MachineDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.devMachine) {
                this.devMachine = new org.eclipse.che.api.machine.shared.dto.MachineDtoImpl(__jsonObject.devMachine);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.rootFolder) {
                this.rootFolder = __jsonObject.rootFolder;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.activeEnv) {
                this.activeEnv = __jsonObject.activeEnv;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            this.machines = new Array<org.eclipse.che.api.machine.shared.dto.MachineDto>();
            if (__jsonObject) {
              if (__jsonObject.machines) {
                  __jsonObject.machines.forEach((item) => {
                  this.machines.push(new org.eclipse.che.api.machine.shared.dto.MachineDtoImpl(item));
                  });
              }
            }

    } 

        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {
          this.links = links;
          return this;
        }
        withActiveEnv(activeEnv : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {
          this.activeEnv = activeEnv;
          return this;
        }
        getDevMachine() : org.eclipse.che.api.machine.shared.dto.MachineDto {
          return this.devMachine;
        }
        setDevMachine(devMachine : org.eclipse.che.api.machine.shared.dto.MachineDto) : void {
          this.devMachine = devMachine;
        }
        withDevMachine(devMachine : org.eclipse.che.api.machine.shared.dto.MachineDto) : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {
          this.devMachine = devMachine;
          return this;
        }
        getMachines() : Array<org.eclipse.che.api.machine.shared.dto.MachineDto> {
          return this.machines;
        }
        setMachines(machines : Array<org.eclipse.che.api.machine.shared.dto.MachineDto>) : void {
          this.machines = machines;
        }
        withMachines(machines : Array<org.eclipse.che.api.machine.shared.dto.MachineDto>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {
          this.machines = machines;
          return this;
        }
        setRootFolder(rootFolder : string) : void {
          this.rootFolder = rootFolder;
        }
        withRootFolder(rootFolder : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {
          this.rootFolder = rootFolder;
          return this;
        }
        setActiveEnv(activeEnv : string) : void {
          this.activeEnv = activeEnv;
        }
        getActiveEnv() : string {
          return this.activeEnv;
        }
        getRootFolder() : string {
          return this.rootFolder;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.devMachine) {
                          json.devMachine = (this.devMachine as org.eclipse.che.api.machine.shared.dto.MachineDtoImpl).toJson();
                        }
                        if (this.rootFolder) {
                          json.rootFolder = this.rootFolder;
                        }
                        if (this.activeEnv) {
                          json.activeEnv = this.activeEnv;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.machines) {
                            let listArray = [];
                            this.machines.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.machine.shared.dto.MachineDtoImpl).toJson());
                            json.machines = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface OnAppLoadedDto {
      setActions(arg0): void;
      withActions(arg0): org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto;
      getActions(): Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class OnAppLoadedDtoImpl implements org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto {

        actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.actions = new Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>();
            if (__jsonObject) {
              if (__jsonObject.actions) {
                  __jsonObject.actions.forEach((item) => {
                  this.actions.push(new org.eclipse.che.api.factory.shared.dto.IdeActionDtoImpl(item));
                  });
              }
            }

    } 

        setActions(actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>) : void {
          this.actions = actions;
        }
        withActions(actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>) : org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto {
          this.actions = actions;
          return this;
        }
        getActions() : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto> {
          return this.actions;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.actions) {
                            let listArray = [];
                            this.actions.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.factory.shared.dto.IdeActionDtoImpl).toJson());
                            json.actions = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.user.shared.dto {

  export interface UserDto {
      setId(arg0): void;
      setPassword(arg0): void;
      getAliases(): Array<string>;
      setAliases(arg0): void;
      getPassword(): string;
      getEmail(): string;
      setEmail(arg0): void;
      withName(arg0): org.eclipse.che.api.user.shared.dto.UserDto;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;
      withLinks(arg0): org.eclipse.che.api.user.shared.dto.UserDto;
      withEmail(arg0): org.eclipse.che.api.user.shared.dto.UserDto;
      withId(arg0): org.eclipse.che.api.user.shared.dto.UserDto;
      withAliases(arg0): org.eclipse.che.api.user.shared.dto.UserDto;
      withPassword(arg0): org.eclipse.che.api.user.shared.dto.UserDto;
      getName(): string;
      setName(arg0): void;
      getId(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.user.shared.dto {

  export class UserDtoImpl implements org.eclipse.che.api.user.shared.dto.UserDto {

        password : string;
        aliases : Array<string>;
        name : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        id : string;
        email : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.password) {
                this.password = __jsonObject.password;
              }
            }
            this.aliases = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.aliases) {
                  __jsonObject.aliases.forEach((item) => {
                  this.aliases.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.email) {
                this.email = __jsonObject.email;
              }
            }

    } 

        setId(id : string) : void {
          this.id = id;
        }
        setPassword(password : string) : void {
          this.password = password;
        }
        getAliases() : Array<string> {
          return this.aliases;
        }
        setAliases(aliases : Array<string>) : void {
          this.aliases = aliases;
        }
        getPassword() : string {
          return this.password;
        }
        getEmail() : string {
          return this.email;
        }
        setEmail(email : string) : void {
          this.email = email;
        }
        withName(name : string) : org.eclipse.che.api.user.shared.dto.UserDto {
          this.name = name;
          return this;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.user.shared.dto.UserDto {
          this.links = links;
          return this;
        }
        withEmail(email : string) : org.eclipse.che.api.user.shared.dto.UserDto {
          this.email = email;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.user.shared.dto.UserDto {
          this.id = id;
          return this;
        }
        withAliases(aliases : Array<string>) : org.eclipse.che.api.user.shared.dto.UserDto {
          this.aliases = aliases;
          return this;
        }
        withPassword(password : string) : org.eclipse.che.api.user.shared.dto.UserDto {
          this.password = password;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getId() : string {
          return this.id;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.password) {
                          json.password = this.password;
                        }
                        if (this.aliases) {
                            let listArray = [];
                            this.aliases.forEach((item) => {
                            listArray.push(item);
                            json.aliases = listArray;
                            });
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.email) {
                          json.email = this.email;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface AuthorDto {
      setCreated(arg0): void;
      getEmail(): string;
      setEmail(arg0): void;
      withName(arg0): org.eclipse.che.api.factory.shared.dto.AuthorDto;
      getUserId(): string;
      setUserId(arg0): void;
      withUserId(arg0): org.eclipse.che.api.factory.shared.dto.AuthorDto;
      withEmail(arg0): org.eclipse.che.api.factory.shared.dto.AuthorDto;
      getCreated(): number;
      withCreated(arg0): org.eclipse.che.api.factory.shared.dto.AuthorDto;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class AuthorDtoImpl implements org.eclipse.che.api.factory.shared.dto.AuthorDto {

        created : number;
        name : string;
        userId : string;
        email : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.created) {
                this.created = __jsonObject.created;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.userId) {
                this.userId = __jsonObject.userId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.email) {
                this.email = __jsonObject.email;
              }
            }

    } 

        setCreated(created : number) : void {
          this.created = created;
        }
        getEmail() : string {
          return this.email;
        }
        setEmail(email : string) : void {
          this.email = email;
        }
        withName(name : string) : org.eclipse.che.api.factory.shared.dto.AuthorDto {
          this.name = name;
          return this;
        }
        getUserId() : string {
          return this.userId;
        }
        setUserId(userId : string) : void {
          this.userId = userId;
        }
        withUserId(userId : string) : org.eclipse.che.api.factory.shared.dto.AuthorDto {
          this.userId = userId;
          return this;
        }
        withEmail(email : string) : org.eclipse.che.api.factory.shared.dto.AuthorDto {
          this.email = email;
          return this;
        }
        getCreated() : number {
          return this.created;
        }
        withCreated(created : number) : org.eclipse.che.api.factory.shared.dto.AuthorDto {
          this.created = created;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.created) {
                          json.created = this.created;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.userId) {
                          json.userId = this.userId;
                        }
                        if (this.email) {
                          json.email = this.email;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface DebugSessionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
      setType(arg0): void;
      setId(arg0): void;
      withId(arg0): org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
      getDebuggerInfo(): org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
      withDebuggerInfo(arg0): org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
      setDebuggerInfo(arg0): void;
      getId(): string;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class DebugSessionDtoImpl implements org.eclipse.che.api.debug.shared.dto.DebugSessionDto {

        id : string;
        type : string;
        debuggerInfo : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.debuggerInfo) {
                this.debuggerInfo = new org.eclipse.che.api.debug.shared.dto.DebuggerInfoDtoImpl(__jsonObject.debuggerInfo);
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.DebugSessionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setId(id : string) : void {
          this.id = id;
        }
        withId(id : string) : org.eclipse.che.api.debug.shared.dto.DebugSessionDto {
          this.id = id;
          return this;
        }
        getDebuggerInfo() : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto {
          return this.debuggerInfo;
        }
        withDebuggerInfo(debuggerInfo : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto) : org.eclipse.che.api.debug.shared.dto.DebugSessionDto {
          this.debuggerInfo = debuggerInfo;
          return this;
        }
        setDebuggerInfo(debuggerInfo : org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto) : void {
          this.debuggerInfo = debuggerInfo;
        }
        getId() : string {
          return this.id;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.debuggerInfo) {
                          json.debuggerInfo = (this.debuggerInfo as org.eclipse.che.api.debug.shared.dto.DebuggerInfoDtoImpl).toJson();
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface ButtonDto {
      withType(arg0): org.eclipse.che.api.factory.shared.dto.ButtonDto;
      setType(arg0): void;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.factory.shared.dto.ButtonDto;
      getType(): string;
      getAttributes(): org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class ButtonDtoImpl implements org.eclipse.che.api.factory.shared.dto.ButtonDto {

        attributes : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                this.attributes = new org.eclipse.che.api.factory.shared.dto.ButtonAttributesDtoImpl(__jsonObject.attributes);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.factory.shared.dto.ButtonDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setAttributes(attributes : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto) : org.eclipse.che.api.factory.shared.dto.ButtonDto {
          this.attributes = attributes;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getAttributes() : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.attributes) {
                          json.attributes = (this.attributes as org.eclipse.che.api.factory.shared.dto.ButtonAttributesDtoImpl).toJson();
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface ServerConfDto {
      setProtocol(arg0): void;
      setPort(arg0): void;
      setPath(arg0): void;
      withRef(arg0): org.eclipse.che.api.machine.shared.dto.ServerConfDto;
      withPort(arg0): org.eclipse.che.api.machine.shared.dto.ServerConfDto;
      withProtocol(arg0): org.eclipse.che.api.machine.shared.dto.ServerConfDto;
      withPath(arg0): org.eclipse.che.api.machine.shared.dto.ServerConfDto;
      setRef(arg0): void;
      getPath(): string;
      getProtocol(): string;
      getPort(): string;
      getRef(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class ServerConfDtoImpl implements org.eclipse.che.api.machine.shared.dto.ServerConfDto {

        path : string;
        protocol : string;
        ref : string;
        port : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.protocol) {
                this.protocol = __jsonObject.protocol;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.ref) {
                this.ref = __jsonObject.ref;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.port) {
                this.port = __jsonObject.port;
              }
            }

    } 

        setProtocol(protocol : string) : void {
          this.protocol = protocol;
        }
        setPort(port : string) : void {
          this.port = port;
        }
        setPath(path : string) : void {
          this.path = path;
        }
        withRef(ref : string) : org.eclipse.che.api.machine.shared.dto.ServerConfDto {
          this.ref = ref;
          return this;
        }
        withPort(port : string) : org.eclipse.che.api.machine.shared.dto.ServerConfDto {
          this.port = port;
          return this;
        }
        withProtocol(protocol : string) : org.eclipse.che.api.machine.shared.dto.ServerConfDto {
          this.protocol = protocol;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.machine.shared.dto.ServerConfDto {
          this.path = path;
          return this;
        }
        setRef(ref : string) : void {
          this.ref = ref;
        }
        getPath() : string {
          return this.path;
        }
        getProtocol() : string {
          return this.protocol;
        }
        getPort() : string {
          return this.port;
        }
        getRef() : string {
          return this.ref;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.protocol) {
                          json.protocol = this.protocol;
                        }
                        if (this.ref) {
                          json.ref = this.ref;
                        }
                        if (this.port) {
                          json.port = this.port;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface FetchRequest {
      setTimeout(arg0): void;
      getTimeout(): number;
      setPassword(arg0): void;
      getUsername(): string;
      getPassword(): string;
      setUsername(arg0): void;
      setRemote(arg0): void;
      withRemote(arg0): org.eclipse.che.api.git.shared.FetchRequest;
      withPassword(arg0): org.eclipse.che.api.git.shared.FetchRequest;
      getRefSpec(): Array<string>;
      setRefSpec(arg0): void;
      withRefSpec(arg0): org.eclipse.che.api.git.shared.FetchRequest;
      getRemote(): string;
      withTimeout(arg0): org.eclipse.che.api.git.shared.FetchRequest;
      withUsername(arg0): org.eclipse.che.api.git.shared.FetchRequest;
      setRemoveDeletedRefs(arg0): void;
      withRemoveDeletedRefs(arg0): org.eclipse.che.api.git.shared.FetchRequest;
      isRemoveDeletedRefs(): boolean;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class FetchRequestImpl implements org.eclipse.che.api.git.shared.FetchRequest {

        password : string;
        removeDeletedRefs : boolean;
        refSpec : Array<string>;
        remote : string;
        timeout : number;
        username : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.password) {
                this.password = __jsonObject.password;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.removeDeletedRefs) {
                this.removeDeletedRefs = __jsonObject.removeDeletedRefs;
              }
            }
            this.refSpec = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.refSpec) {
                  __jsonObject.refSpec.forEach((item) => {
                  this.refSpec.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.remote) {
                this.remote = __jsonObject.remote;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.timeout) {
                this.timeout = __jsonObject.timeout;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.username) {
                this.username = __jsonObject.username;
              }
            }

    } 

        setTimeout(timeout : number) : void {
          this.timeout = timeout;
        }
        getTimeout() : number {
          return this.timeout;
        }
        setPassword(password : string) : void {
          this.password = password;
        }
        getUsername() : string {
          return this.username;
        }
        getPassword() : string {
          return this.password;
        }
        setUsername(username : string) : void {
          this.username = username;
        }
        setRemote(remote : string) : void {
          this.remote = remote;
        }
        withRemote(remote : string) : org.eclipse.che.api.git.shared.FetchRequest {
          this.remote = remote;
          return this;
        }
        withPassword(password : string) : org.eclipse.che.api.git.shared.FetchRequest {
          this.password = password;
          return this;
        }
        getRefSpec() : Array<string> {
          return this.refSpec;
        }
        setRefSpec(refSpec : Array<string>) : void {
          this.refSpec = refSpec;
        }
        withRefSpec(refSpec : Array<string>) : org.eclipse.che.api.git.shared.FetchRequest {
          this.refSpec = refSpec;
          return this;
        }
        getRemote() : string {
          return this.remote;
        }
        withTimeout(timeout : number) : org.eclipse.che.api.git.shared.FetchRequest {
          this.timeout = timeout;
          return this;
        }
        withUsername(username : string) : org.eclipse.che.api.git.shared.FetchRequest {
          this.username = username;
          return this;
        }
        setRemoveDeletedRefs(removeDeletedRefs : boolean) : void {
          this.removeDeletedRefs = removeDeletedRefs;
        }
        withRemoveDeletedRefs(removeDeletedRefs : boolean) : org.eclipse.che.api.git.shared.FetchRequest {
          this.removeDeletedRefs = removeDeletedRefs;
          return this;
        }
        isRemoveDeletedRefs() : boolean {
          return this.removeDeletedRefs;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.password) {
                          json.password = this.password;
                        }
                        if (this.removeDeletedRefs) {
                          json.removeDeletedRefs = this.removeDeletedRefs;
                        }
                        if (this.refSpec) {
                            let listArray = [];
                            this.refSpec.forEach((item) => {
                            listArray.push(item);
                            json.refSpec = listArray;
                            });
                        }
                        if (this.remote) {
                          json.remote = this.remote;
                        }
                        if (this.timeout) {
                          json.timeout = this.timeout;
                        }
                        if (this.username) {
                          json.username = this.username;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export interface RecipeUpdate {
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
      setId(arg0): void;
      setTags(arg0): void;
      withName(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
      withId(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
      setScript(arg0): void;
      withScript(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
      withTags(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
      setName(arg0): void;
      getDescription(): string;
      getTags(): Array<string>;
      getCreator(): string;
      getName(): string;
      getId(): string;
      getType(): string;
      getScript(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export class RecipeUpdateImpl implements org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {

        creator : string;
        name : string;
        description : string;
        id : string;
        type : string;
        script : string;
        tags : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.creator) {
                this.creator = __jsonObject.creator;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.script) {
                this.script = __jsonObject.script;
              }
            }
            this.tags = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.tags) {
                  __jsonObject.tags.forEach((item) => {
                  this.tags.push(item);
                  });
              }
            }

    } 

        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {
          this.description = description;
          return this;
        }
        setId(id : string) : void {
          this.id = id;
        }
        setTags(tags : Array<string>) : void {
          this.tags = tags;
        }
        withName(name : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {
          this.name = name;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {
          this.id = id;
          return this;
        }
        setScript(script : string) : void {
          this.script = script;
        }
        withScript(script : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {
          this.script = script;
          return this;
        }
        withTags(tags : Array<string>) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate {
          this.tags = tags;
          return this;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getDescription() : string {
          return this.description;
        }
        getTags() : Array<string> {
          return this.tags;
        }
        getCreator() : string {
          return this.creator;
        }
        getName() : string {
          return this.name;
        }
        getId() : string {
          return this.id;
        }
        getType() : string {
          return this.type;
        }
        getScript() : string {
          return this.script;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.creator) {
                          json.creator = this.creator;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.script) {
                          json.script = this.script;
                        }
                        if (this.tags) {
                            let listArray = [];
                            this.tags.forEach((item) => {
                            listArray.push(item);
                            json.tags = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface ValueDto {
      getString(): string;
      getList(): Array<string>;
      withList(arg0): org.eclipse.che.api.project.shared.dto.ValueDto;
      isEmpty(): boolean;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class ValueDtoImpl implements org.eclipse.che.api.project.shared.dto.ValueDto {

        string : string;
        list : Array<string>;
        empty : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.string) {
                this.string = __jsonObject.string;
              }
            }
            this.list = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.list) {
                  __jsonObject.list.forEach((item) => {
                  this.list.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.empty) {
                this.empty = __jsonObject.empty;
              }
            }

    } 

        getString() : string {
          return this.string;
        }
        getList() : Array<string> {
          return this.list;
        }
        withList(list : Array<string>) : org.eclipse.che.api.project.shared.dto.ValueDto {
          this.list = list;
          return this;
        }
        isEmpty() : boolean {
          return this.empty;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.string) {
                          json.string = this.string;
                        }
                        if (this.list) {
                            let listArray = [];
                            this.list.forEach((item) => {
                            listArray.push(item);
                            json.list = listArray;
                            });
                        }
                        if (this.empty) {
                          json.empty = this.empty;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface CloneRequest {
      setRecursive(arg0): void;
      isRecursive(): boolean;
      setTimeout(arg0): void;
      getTimeout(): number;
      setPassword(arg0): void;
      getUsername(): string;
      getPassword(): string;
      setUsername(arg0): void;
      withPassword(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      withRemoteUri(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      getWorkingDir(): string;
      getRemoteUri(): string;
      withTimeout(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      withUsername(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      getBranchesToFetch(): Array<string>;
      withBranchesToFetch(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      withWorkingDir(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      getRemoteName(): string;
      setRemoteName(arg0): void;
      withRemoteName(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      withRecursive(arg0): org.eclipse.che.api.git.shared.CloneRequest;
      setRemoteUri(arg0): void;
      setBranchesToFetch(arg0): void;
      setWorkingDir(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class CloneRequestImpl implements org.eclipse.che.api.git.shared.CloneRequest {

        password : string;
        remoteUri : string;
        branchesToFetch : Array<string>;
        workingDir : string;
        recursive : boolean;
        timeout : number;
        username : string;
        remoteName : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.password) {
                this.password = __jsonObject.password;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.remoteUri) {
                this.remoteUri = __jsonObject.remoteUri;
              }
            }
            this.branchesToFetch = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.branchesToFetch) {
                  __jsonObject.branchesToFetch.forEach((item) => {
                  this.branchesToFetch.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workingDir) {
                this.workingDir = __jsonObject.workingDir;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.recursive) {
                this.recursive = __jsonObject.recursive;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.timeout) {
                this.timeout = __jsonObject.timeout;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.username) {
                this.username = __jsonObject.username;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.remoteName) {
                this.remoteName = __jsonObject.remoteName;
              }
            }

    } 

        setRecursive(recursive : boolean) : void {
          this.recursive = recursive;
        }
        isRecursive() : boolean {
          return this.recursive;
        }
        setTimeout(timeout : number) : void {
          this.timeout = timeout;
        }
        getTimeout() : number {
          return this.timeout;
        }
        setPassword(password : string) : void {
          this.password = password;
        }
        getUsername() : string {
          return this.username;
        }
        getPassword() : string {
          return this.password;
        }
        setUsername(username : string) : void {
          this.username = username;
        }
        withPassword(password : string) : org.eclipse.che.api.git.shared.CloneRequest {
          this.password = password;
          return this;
        }
        withRemoteUri(remoteUri : string) : org.eclipse.che.api.git.shared.CloneRequest {
          this.remoteUri = remoteUri;
          return this;
        }
        getWorkingDir() : string {
          return this.workingDir;
        }
        getRemoteUri() : string {
          return this.remoteUri;
        }
        withTimeout(timeout : number) : org.eclipse.che.api.git.shared.CloneRequest {
          this.timeout = timeout;
          return this;
        }
        withUsername(username : string) : org.eclipse.che.api.git.shared.CloneRequest {
          this.username = username;
          return this;
        }
        getBranchesToFetch() : Array<string> {
          return this.branchesToFetch;
        }
        withBranchesToFetch(branchesToFetch : Array<string>) : org.eclipse.che.api.git.shared.CloneRequest {
          this.branchesToFetch = branchesToFetch;
          return this;
        }
        withWorkingDir(workingDir : string) : org.eclipse.che.api.git.shared.CloneRequest {
          this.workingDir = workingDir;
          return this;
        }
        getRemoteName() : string {
          return this.remoteName;
        }
        setRemoteName(remoteName : string) : void {
          this.remoteName = remoteName;
        }
        withRemoteName(remoteName : string) : org.eclipse.che.api.git.shared.CloneRequest {
          this.remoteName = remoteName;
          return this;
        }
        withRecursive(recursive : boolean) : org.eclipse.che.api.git.shared.CloneRequest {
          this.recursive = recursive;
          return this;
        }
        setRemoteUri(remoteUri : string) : void {
          this.remoteUri = remoteUri;
        }
        setBranchesToFetch(branchesToFetch : Array<string>) : void {
          this.branchesToFetch = branchesToFetch;
        }
        setWorkingDir(workingDir : string) : void {
          this.workingDir = workingDir;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.password) {
                          json.password = this.password;
                        }
                        if (this.remoteUri) {
                          json.remoteUri = this.remoteUri;
                        }
                        if (this.branchesToFetch) {
                            let listArray = [];
                            this.branchesToFetch.forEach((item) => {
                            listArray.push(item);
                            json.branchesToFetch = listArray;
                            });
                        }
                        if (this.workingDir) {
                          json.workingDir = this.workingDir;
                        }
                        if (this.recursive) {
                          json.recursive = this.recursive;
                        }
                        if (this.timeout) {
                          json.timeout = this.timeout;
                        }
                        if (this.username) {
                          json.username = this.username;
                        }
                        if (this.remoteName) {
                          json.remoteName = this.remoteName;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface StackFrameDumpDto {
      setFields(arg0): void;
      getVariables(): Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
      withFields(arg0): org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
      setVariables(arg0): void;
      withVariables(arg0): org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
      getFields(): Array<org.eclipse.che.api.debug.shared.dto.FieldDto>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class StackFrameDumpDtoImpl implements org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto {

        variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
        fields : Array<org.eclipse.che.api.debug.shared.dto.FieldDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.variables = new Array<org.eclipse.che.api.debug.shared.dto.VariableDto>();
            if (__jsonObject) {
              if (__jsonObject.variables) {
                  __jsonObject.variables.forEach((item) => {
                  this.variables.push(new org.eclipse.che.api.debug.shared.dto.VariableDtoImpl(item));
                  });
              }
            }
            this.fields = new Array<org.eclipse.che.api.debug.shared.dto.FieldDto>();
            if (__jsonObject) {
              if (__jsonObject.fields) {
                  __jsonObject.fields.forEach((item) => {
                  this.fields.push(new org.eclipse.che.api.debug.shared.dto.FieldDtoImpl(item));
                  });
              }
            }

    } 

        setFields(fields : Array<org.eclipse.che.api.debug.shared.dto.FieldDto>) : void {
          this.fields = fields;
        }
        getVariables() : Array<org.eclipse.che.api.debug.shared.dto.VariableDto> {
          return this.variables;
        }
        withFields(fields : Array<org.eclipse.che.api.debug.shared.dto.FieldDto>) : org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto {
          this.fields = fields;
          return this;
        }
        setVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : void {
          this.variables = variables;
        }
        withVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto {
          this.variables = variables;
          return this;
        }
        getFields() : Array<org.eclipse.che.api.debug.shared.dto.FieldDto> {
          return this.fields;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.variables) {
                            let listArray = [];
                            this.variables.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.debug.shared.dto.VariableDtoImpl).toJson());
                            json.variables = listArray;
                            });
                        }
                        if (this.fields) {
                            let listArray = [];
                            this.fields.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.debug.shared.dto.FieldDtoImpl).toJson());
                            json.fields = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface ButtonAttributesDto {
      setColor(arg0): void;
      getColor(): string;
      withColor(arg0): org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;
      getCounter(): boolean;
      setCounter(arg0): void;
      withCounter(arg0): org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;
      getLogo(): string;
      setLogo(arg0): void;
      withLogo(arg0): org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;
      getStyle(): string;
      setStyle(arg0): void;
      withStyle(arg0): org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class ButtonAttributesDtoImpl implements org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto {

        color : string;
        logo : string;
        style : string;
        counter : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.color) {
                this.color = __jsonObject.color;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.logo) {
                this.logo = __jsonObject.logo;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.style) {
                this.style = __jsonObject.style;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.counter) {
                this.counter = __jsonObject.counter;
              }
            }

    } 

        setColor(color : string) : void {
          this.color = color;
        }
        getColor() : string {
          return this.color;
        }
        withColor(color : string) : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto {
          this.color = color;
          return this;
        }
        getCounter() : boolean {
          return this.counter;
        }
        setCounter(counter : boolean) : void {
          this.counter = counter;
        }
        withCounter(counter : boolean) : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto {
          this.counter = counter;
          return this;
        }
        getLogo() : string {
          return this.logo;
        }
        setLogo(logo : string) : void {
          this.logo = logo;
        }
        withLogo(logo : string) : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto {
          this.logo = logo;
          return this;
        }
        getStyle() : string {
          return this.style;
        }
        setStyle(style : string) : void {
          this.style = style;
        }
        withStyle(style : string) : org.eclipse.che.api.factory.shared.dto.ButtonAttributesDto {
          this.style = style;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.color) {
                          json.color = this.color;
                        }
                        if (this.logo) {
                          json.logo = this.logo;
                        }
                        if (this.style) {
                          json.style = this.style;
                        }
                        if (this.counter) {
                          json.counter = this.counter;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export interface DebuggerEventDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.event.DebuggerEventDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export class DebuggerEventDtoImpl implements org.eclipse.che.api.debug.shared.dto.event.DebuggerEventDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.event.DebuggerEventDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export interface NewRecipe {
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
      setTags(arg0): void;
      withName(arg0): org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
      setScript(arg0): void;
      withScript(arg0): org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
      withTags(arg0): org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
      setName(arg0): void;
      getDescription(): string;
      getTags(): Array<string>;
      getCreator(): string;
      getName(): string;
      getId(): string;
      getType(): string;
      getScript(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export class NewRecipeImpl implements org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe {

        creator : string;
        name : string;
        description : string;
        id : string;
        type : string;
        script : string;
        tags : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.creator) {
                this.creator = __jsonObject.creator;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.script) {
                this.script = __jsonObject.script;
              }
            }
            this.tags = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.tags) {
                  __jsonObject.tags.forEach((item) => {
                  this.tags.push(item);
                  });
              }
            }

    } 

        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe {
          this.description = description;
          return this;
        }
        setTags(tags : Array<string>) : void {
          this.tags = tags;
        }
        withName(name : string) : org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe {
          this.name = name;
          return this;
        }
        setScript(script : string) : void {
          this.script = script;
        }
        withScript(script : string) : org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe {
          this.script = script;
          return this;
        }
        withTags(tags : Array<string>) : org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe {
          this.tags = tags;
          return this;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getDescription() : string {
          return this.description;
        }
        getTags() : Array<string> {
          return this.tags;
        }
        getCreator() : string {
          return this.creator;
        }
        getName() : string {
          return this.name;
        }
        getId() : string {
          return this.id;
        }
        getType() : string {
          return this.type;
        }
        getScript() : string {
          return this.script;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.creator) {
                          json.creator = this.creator;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.script) {
                          json.script = this.script;
                        }
                        if (this.tags) {
                            let listArray = [];
                            this.tags.forEach((item) => {
                            listArray.push(item);
                            json.tags = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface EnvironmentRecipeDto {
      getContentType(): string;
      setContentType(arg0): void;
      withType(arg0): org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
      setType(arg0): void;
      setLocation(arg0): void;
      setContent(arg0): void;
      withContent(arg0): org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
      withContentType(arg0): org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
      withLocation(arg0): org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
      getLocation(): string;
      getType(): string;
      getContent(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class EnvironmentRecipeDtoImpl implements org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto {

        location : string;
        type : string;
        contentType : string;
        content : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.location) {
                this.location = __jsonObject.location;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.contentType) {
                this.contentType = __jsonObject.contentType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.content) {
                this.content = __jsonObject.content;
              }
            }

    } 

        getContentType() : string {
          return this.contentType;
        }
        setContentType(contentType : string) : void {
          this.contentType = contentType;
        }
        withType(type : string) : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setLocation(location : string) : void {
          this.location = location;
        }
        setContent(content : string) : void {
          this.content = content;
        }
        withContent(content : string) : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto {
          this.content = content;
          return this;
        }
        withContentType(contentType : string) : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto {
          this.contentType = contentType;
          return this;
        }
        withLocation(location : string) : org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto {
          this.location = location;
          return this;
        }
        getLocation() : string {
          return this.location;
        }
        getType() : string {
          return this.type;
        }
        getContent() : string {
          return this.content;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.location) {
                          json.location = this.location;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.contentType) {
                          json.contentType = this.contentType;
                        }
                        if (this.content) {
                          json.content = this.content;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineRuntimeInfoDto {
      getServers(): Map<string,org.eclipse.che.api.machine.shared.dto.ServerDto>;
      withProperties(arg0): org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
      withServers(arg0): org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
      withEnvVariables(arg0): org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
      getEnvVariables(): Map<string,string>;
      getProperties(): Map<string,string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineRuntimeInfoDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto {

        servers : Map<string,org.eclipse.che.api.machine.shared.dto.ServerDto>;
        envVariables : Map<string,string>;
        properties : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.servers = new Map<string,org.eclipse.che.api.machine.shared.dto.ServerDto>();
            if (__jsonObject) {
              if (__jsonObject.servers) {
                let tmp : Array<any> = Object.keys(__jsonObject.servers);
                tmp.forEach((key) => {
                  this.servers.set(key, new org.eclipse.che.api.machine.shared.dto.ServerDtoImpl(__jsonObject.servers[key]));
                 });
              }
            }
            this.envVariables = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.envVariables) {
                let tmp : Array<any> = Object.keys(__jsonObject.envVariables);
                tmp.forEach((key) => {
                  this.envVariables.set(key, __jsonObject.envVariables[key]);
                 });
              }
            }
            this.properties = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.properties) {
                let tmp : Array<any> = Object.keys(__jsonObject.properties);
                tmp.forEach((key) => {
                  this.properties.set(key, __jsonObject.properties[key]);
                 });
              }
            }

    } 

        getServers() : Map<string,org.eclipse.che.api.machine.shared.dto.ServerDto> {
          return this.servers;
        }
        withProperties(properties : Map<string,string>) : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto {
          this.properties = properties;
          return this;
        }
        withServers(servers : Map<string,org.eclipse.che.api.machine.shared.dto.ServerDto>) : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto {
          this.servers = servers;
          return this;
        }
        withEnvVariables(envVariables : Map<string,string>) : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto {
          this.envVariables = envVariables;
          return this;
        }
        getEnvVariables() : Map<string,string> {
          return this.envVariables;
        }
        getProperties() : Map<string,string> {
          return this.properties;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.servers) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.servers.entries()) {
                            tmpMap[key] = (value as org.eclipse.che.api.machine.shared.dto.ServerDtoImpl).toJson();
                           }
                          json.servers = tmpMap;
                        }
                        if (this.envVariables) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.envVariables.entries()) {
                            tmpMap[key] = value;
                           }
                          json.envVariables = tmpMap;
                        }
                        if (this.properties) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.properties.entries()) {
                            tmpMap[key] = value;
                           }
                          json.properties = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineLogMessageDto {
      setContent(arg0): void;
      withContent(arg0): org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto;
      withMachineName(arg0): org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto;
      setMachineName(arg0): void;
      getMachineName(): string;
      getContent(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineLogMessageDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto {

        content : string;
        machineName : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.content) {
                this.content = __jsonObject.content;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.machineName) {
                this.machineName = __jsonObject.machineName;
              }
            }

    } 

        setContent(content : string) : void {
          this.content = content;
        }
        withContent(content : string) : org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto {
          this.content = content;
          return this;
        }
        withMachineName(machineName : string) : org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto {
          this.machineName = machineName;
          return this;
        }
        setMachineName(machineName : string) : void {
          this.machineName = machineName;
        }
        getMachineName() : string {
          return this.machineName;
        }
        getContent() : string {
          return this.content;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.content) {
                          json.content = this.content;
                        }
                        if (this.machineName) {
                          json.machineName = this.machineName;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface LogResponse {
      getTextLog(): string;
      getCommits(): Array<org.eclipse.che.api.git.shared.Revision>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class LogResponseImpl implements org.eclipse.che.api.git.shared.LogResponse {

        commits : Array<org.eclipse.che.api.git.shared.Revision>;
        textLog : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.commits = new Array<org.eclipse.che.api.git.shared.Revision>();
            if (__jsonObject) {
              if (__jsonObject.commits) {
                  __jsonObject.commits.forEach((item) => {
                  this.commits.push(new org.eclipse.che.api.git.shared.RevisionImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.textLog) {
                this.textLog = __jsonObject.textLog;
              }
            }

    } 

        getTextLog() : string {
          return this.textLog;
        }
        getCommits() : Array<org.eclipse.che.api.git.shared.Revision> {
          return this.commits;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commits) {
                            let listArray = [];
                            this.commits.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.git.shared.RevisionImpl).toJson());
                            json.commits = listArray;
                            });
                        }
                        if (this.textLog) {
                          json.textLog = this.textLog;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface ResumeActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class ResumeActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.ssh.shared.dto {

  export interface GenerateSshPairRequest {
      getService(): string;
      withName(arg0): org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
      setService(arg0): void;
      withService(arg0): org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.ssh.shared.dto {

  export class GenerateSshPairRequestImpl implements org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest {

        service : string;
        name : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.service) {
                this.service = __jsonObject.service;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }

    } 

        getService() : string {
          return this.service;
        }
        withName(name : string) : org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest {
          this.name = name;
          return this;
        }
        setService(service : string) : void {
          this.service = service;
        }
        withService(service : string) : org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest {
          this.service = service;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.service) {
                          json.service = this.service;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface WorkspaceDto {
      setId(arg0): void;
      getConfig(): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      setStatus(arg0): void;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      withLinks(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      withStatus(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      withId(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      withConfig(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      withRuntime(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      setNamespace(arg0): void;
      withNamespace(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      setRuntime(arg0): void;
      setTemporary(arg0): void;
      withTemporary(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
      setConfig(arg0): void;
      getRuntime(): org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
      getNamespace(): string;
      getStatus(): string;
      isTemporary(): boolean;
      getId(): string;
      getAttributes(): Map<string,string>;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class WorkspaceDtoImpl implements org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {

        temporary : boolean;
        namespace : string;
        runtime : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
        attributes : Map<string,string>;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        id : string;
        config : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
        status : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.temporary) {
                this.temporary = __jsonObject.temporary;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.namespace) {
                this.namespace = __jsonObject.namespace;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.runtime) {
                this.runtime = new org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDtoImpl(__jsonObject.runtime);
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.config) {
                this.config = new org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl(__jsonObject.config);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.status) {
                this.status = __jsonObject.status;
              }
            }

    } 

        setId(id : string) : void {
          this.id = id;
        }
        getConfig() : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          return this.config;
        }
        setStatus(status : string) : void {
          this.status = status;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.attributes = attributes;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.links = links;
          return this;
        }
        withStatus(status : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.status = status;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.id = id;
          return this;
        }
        withConfig(config : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.config = config;
          return this;
        }
        withRuntime(runtime : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.runtime = runtime;
          return this;
        }
        setNamespace(namespace : string) : void {
          this.namespace = namespace;
        }
        withNamespace(namespace : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.namespace = namespace;
          return this;
        }
        setRuntime(runtime : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto) : void {
          this.runtime = runtime;
        }
        setTemporary(temporary : boolean) : void {
          this.temporary = temporary;
        }
        withTemporary(temporary : boolean) : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto {
          this.temporary = temporary;
          return this;
        }
        setConfig(config : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto) : void {
          this.config = config;
        }
        getRuntime() : org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto {
          return this.runtime;
        }
        getNamespace() : string {
          return this.namespace;
        }
        getStatus() : string {
          return this.status;
        }
        isTemporary() : boolean {
          return this.temporary;
        }
        getId() : string {
          return this.id;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.temporary) {
                          json.temporary = this.temporary;
                        }
                        if (this.namespace) {
                          json.namespace = this.namespace;
                        }
                        if (this.runtime) {
                          json.runtime = (this.runtime as org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDtoImpl).toJson();
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.config) {
                          json.config = (this.config as org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl).toJson();
                        }
                        if (this.status) {
                          json.status = this.status;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export interface RecipeDescriptor {
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      setId(arg0): void;
      setTags(arg0): void;
      withName(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      withId(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      setScript(arg0): void;
      withScript(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      setCreator(arg0): void;
      withCreator(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      withTags(arg0): org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
      setName(arg0): void;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;
      withLinks(arg0): org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
      getDescription(): string;
      getTags(): Array<string>;
      getCreator(): string;
      getName(): string;
      getId(): string;
      getType(): string;
      getScript(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export class RecipeDescriptorImpl implements org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {

        creator : string;
        name : string;
        description : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        id : string;
        type : string;
        script : string;
        tags : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.creator) {
                this.creator = __jsonObject.creator;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.script) {
                this.script = __jsonObject.script;
              }
            }
            this.tags = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.tags) {
                  __jsonObject.tags.forEach((item) => {
                  this.tags.push(item);
                  });
              }
            }

    } 

        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.description = description;
          return this;
        }
        setId(id : string) : void {
          this.id = id;
        }
        setTags(tags : Array<string>) : void {
          this.tags = tags;
        }
        withName(name : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.name = name;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.id = id;
          return this;
        }
        setScript(script : string) : void {
          this.script = script;
        }
        withScript(script : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.script = script;
          return this;
        }
        setCreator(creator : string) : void {
          this.creator = creator;
        }
        withCreator(creator : string) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.creator = creator;
          return this;
        }
        withTags(tags : Array<string>) : org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor {
          this.tags = tags;
          return this;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.core.rest.shared.dto.Hyperlinks {
          this.links = links;
          return this;
        }
        getDescription() : string {
          return this.description;
        }
        getTags() : Array<string> {
          return this.tags;
        }
        getCreator() : string {
          return this.creator;
        }
        getName() : string {
          return this.name;
        }
        getId() : string {
          return this.id;
        }
        getType() : string {
          return this.type;
        }
        getScript() : string {
          return this.script;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.creator) {
                          json.creator = this.creator;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.script) {
                          json.script = this.script;
                        }
                        if (this.tags) {
                            let listArray = [];
                            this.tags.forEach((item) => {
                            listArray.push(item);
                            json.tags = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface ApiInfo {
      getSpecificationTitle(): string;
      getSpecificationVersion(): string;
      getSpecificationVendor(): string;
      getImplementationVersion(): string;
      getImplementationVendor(): string;
      getScmRevision(): string;
      setScmRevision(arg0): void;
      withScmRevision(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      withSpecificationVendor(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      setSpecificationVendor(arg0): void;
      withImplementationVendor(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      setImplementationVendor(arg0): void;
      withSpecificationTitle(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      setSpecificationTitle(arg0): void;
      withSpecificationVersion(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      setSpecificationVersion(arg0): void;
      withImplementationVersion(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      setImplementationVersion(arg0): void;
      getIdeVersion(): string;
      withIdeVersion(arg0): org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
      setIdeVersion(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class ApiInfoImpl implements org.eclipse.che.api.core.rest.shared.dto.ApiInfo {

        specificationVendor : string;
        ideVersion : string;
        specificationTitle : string;
        implementationVersion : string;
        implementationVendor : string;
        scmRevision : string;
        specificationVersion : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.specificationVendor) {
                this.specificationVendor = __jsonObject.specificationVendor;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.ideVersion) {
                this.ideVersion = __jsonObject.ideVersion;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.specificationTitle) {
                this.specificationTitle = __jsonObject.specificationTitle;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.implementationVersion) {
                this.implementationVersion = __jsonObject.implementationVersion;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.implementationVendor) {
                this.implementationVendor = __jsonObject.implementationVendor;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.scmRevision) {
                this.scmRevision = __jsonObject.scmRevision;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.specificationVersion) {
                this.specificationVersion = __jsonObject.specificationVersion;
              }
            }

    } 

        getSpecificationTitle() : string {
          return this.specificationTitle;
        }
        getSpecificationVersion() : string {
          return this.specificationVersion;
        }
        getSpecificationVendor() : string {
          return this.specificationVendor;
        }
        getImplementationVersion() : string {
          return this.implementationVersion;
        }
        getImplementationVendor() : string {
          return this.implementationVendor;
        }
        getScmRevision() : string {
          return this.scmRevision;
        }
        setScmRevision(scmRevision : string) : void {
          this.scmRevision = scmRevision;
        }
        withScmRevision(scmRevision : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.scmRevision = scmRevision;
          return this;
        }
        withSpecificationVendor(specificationVendor : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.specificationVendor = specificationVendor;
          return this;
        }
        setSpecificationVendor(specificationVendor : string) : void {
          this.specificationVendor = specificationVendor;
        }
        withImplementationVendor(implementationVendor : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.implementationVendor = implementationVendor;
          return this;
        }
        setImplementationVendor(implementationVendor : string) : void {
          this.implementationVendor = implementationVendor;
        }
        withSpecificationTitle(specificationTitle : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.specificationTitle = specificationTitle;
          return this;
        }
        setSpecificationTitle(specificationTitle : string) : void {
          this.specificationTitle = specificationTitle;
        }
        withSpecificationVersion(specificationVersion : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.specificationVersion = specificationVersion;
          return this;
        }
        setSpecificationVersion(specificationVersion : string) : void {
          this.specificationVersion = specificationVersion;
        }
        withImplementationVersion(implementationVersion : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.implementationVersion = implementationVersion;
          return this;
        }
        setImplementationVersion(implementationVersion : string) : void {
          this.implementationVersion = implementationVersion;
        }
        getIdeVersion() : string {
          return this.ideVersion;
        }
        withIdeVersion(ideVersion : string) : org.eclipse.che.api.core.rest.shared.dto.ApiInfo {
          this.ideVersion = ideVersion;
          return this;
        }
        setIdeVersion(ideVersion : string) : void {
          this.ideVersion = ideVersion;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.specificationVendor) {
                          json.specificationVendor = this.specificationVendor;
                        }
                        if (this.ideVersion) {
                          json.ideVersion = this.ideVersion;
                        }
                        if (this.specificationTitle) {
                          json.specificationTitle = this.specificationTitle;
                        }
                        if (this.implementationVersion) {
                          json.implementationVersion = this.implementationVersion;
                        }
                        if (this.implementationVendor) {
                          json.implementationVendor = this.implementationVendor;
                        }
                        if (this.scmRevision) {
                          json.scmRevision = this.scmRevision;
                        }
                        if (this.specificationVersion) {
                          json.specificationVersion = this.specificationVersion;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface WsAgentHealthStateDto {
      getReason(): string;
      getCode(): number;
      setCode(arg0): void;
      withCode(arg0): org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
      withWorkspaceStatus(arg0): org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
      getWorkspaceStatus(): string;
      setReason(arg0): void;
      withReason(arg0): org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
      setWorkspaceStatus(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class WsAgentHealthStateDtoImpl implements org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto {

        reason : string;
        code : number;
        workspaceStatus : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.reason) {
                this.reason = __jsonObject.reason;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.code) {
                this.code = __jsonObject.code;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceStatus) {
                this.workspaceStatus = __jsonObject.workspaceStatus;
              }
            }

    } 

        getReason() : string {
          return this.reason;
        }
        getCode() : number {
          return this.code;
        }
        setCode(code : number) : void {
          this.code = code;
        }
        withCode(code : number) : org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto {
          this.code = code;
          return this;
        }
        withWorkspaceStatus(workspaceStatus : string) : org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto {
          this.workspaceStatus = workspaceStatus;
          return this;
        }
        getWorkspaceStatus() : string {
          return this.workspaceStatus;
        }
        setReason(reason : string) : void {
          this.reason = reason;
        }
        withReason(reason : string) : org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto {
          this.reason = reason;
          return this;
        }
        setWorkspaceStatus(workspaceStatus : string) : void {
          this.workspaceStatus = workspaceStatus;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.reason) {
                          json.reason = this.reason;
                        }
                        if (this.code) {
                          json.code = this.code;
                        }
                        if (this.workspaceStatus) {
                          json.workspaceStatus = this.workspaceStatus;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export interface SuspendActionDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
      setType(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.action {

  export class SuspendActionDtoImpl implements org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto {

        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export interface VfsWatchEvent {
      withType(arg0): org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent;
      withPath(arg0): org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent;
      withFile(arg0): org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent;
      getType(): string;
      getPath(): string;
      isFile(): boolean;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export class VfsWatchEventImpl implements org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent {

        path : string;
        file : boolean;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.file) {
                this.file = __jsonObject.file;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent {
          this.type = type;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent {
          this.path = path;
          return this;
        }
        withFile(file : boolean) : org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent {
          this.file = file;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getPath() : string {
          return this.path;
        }
        isFile() : boolean {
          return this.file;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.file) {
                          json.file = this.file;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.templates.shared.dto {

  export interface ProjectTemplateDescriptor {
      getProblems(): Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>;
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      getSource(): org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
      withSource(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      setSource(arg0): void;
      setProblems(arg0): void;
      setPath(arg0): void;
      getTags(): Array<string>;
      setTags(arg0): void;
      withName(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;
      withLinks(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      withPath(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      withDisplayName(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      getProjectType(): string;
      setProjectType(arg0): void;
      withProjectType(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      getMixins(): Array<string>;
      setMixins(arg0): void;
      withMixins(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      withProblems(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      setDisplayName(arg0): void;
      getCategory(): string;
      setCategory(arg0): void;
      withCategory(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      getCommands(): Array<org.eclipse.che.api.machine.shared.dto.CommandDto>;
      setCommands(arg0): void;
      withCommands(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      withTags(arg0): org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
      getName(): string;
      setName(arg0): void;
      getPath(): string;
      getAttributes(): Map<string,Array<string>>;
      getDisplayName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.templates.shared.dto {

  export class ProjectTemplateDescriptorImpl implements org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {

        displayName : string;
        projectType : string;
        description : string;
        source : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
        tags : Array<string>;
        path : string;
        mixins : Array<string>;
        name : string;
        attributes : Map<string,Array<string>>;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        category : string;
        commands : Array<org.eclipse.che.api.machine.shared.dto.CommandDto>;
        problems : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.displayName) {
                this.displayName = __jsonObject.displayName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.projectType) {
                this.projectType = __jsonObject.projectType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.source) {
                this.source = new org.eclipse.che.api.workspace.shared.dto.SourceStorageDtoImpl(__jsonObject.source);
              }
            }
            this.tags = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.tags) {
                  __jsonObject.tags.forEach((item) => {
                  this.tags.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            this.mixins = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.mixins) {
                  __jsonObject.mixins.forEach((item) => {
                  this.mixins.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.attributes = new Map<string,Array<string>>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.category) {
                this.category = __jsonObject.category;
              }
            }
            this.commands = new Array<org.eclipse.che.api.machine.shared.dto.CommandDto>();
            if (__jsonObject) {
              if (__jsonObject.commands) {
                  __jsonObject.commands.forEach((item) => {
                  this.commands.push(new org.eclipse.che.api.machine.shared.dto.CommandDtoImpl(item));
                  });
              }
            }
            this.problems = new Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>();
            if (__jsonObject) {
              if (__jsonObject.problems) {
                  __jsonObject.problems.forEach((item) => {
                  this.problems.push(new org.eclipse.che.api.workspace.shared.dto.ProjectProblemDtoImpl(item));
                  });
              }
            }

    } 

        getProblems() : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto> {
          return this.problems;
        }
        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.description = description;
          return this;
        }
        getSource() : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto {
          return this.source;
        }
        withSource(source : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.source = source;
          return this;
        }
        setSource(source : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto) : void {
          this.source = source;
        }
        setProblems(problems : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>) : void {
          this.problems = problems;
        }
        setPath(path : string) : void {
          this.path = path;
        }
        getTags() : Array<string> {
          return this.tags;
        }
        setTags(tags : Array<string>) : void {
          this.tags = tags;
        }
        withName(name : string) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.name = name;
          return this;
        }
        setAttributes(attributes : Map<string,Array<string>>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,Array<string>>) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.attributes = attributes;
          return this;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.links = links;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.path = path;
          return this;
        }
        withDisplayName(displayName : string) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.displayName = displayName;
          return this;
        }
        getProjectType() : string {
          return this.projectType;
        }
        setProjectType(projectType : string) : void {
          this.projectType = projectType;
        }
        withProjectType(projectType : string) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.projectType = projectType;
          return this;
        }
        getMixins() : Array<string> {
          return this.mixins;
        }
        setMixins(mixins : Array<string>) : void {
          this.mixins = mixins;
        }
        withMixins(mixins : Array<string>) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.mixins = mixins;
          return this;
        }
        withProblems(problems : Array<org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto>) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.problems = problems;
          return this;
        }
        setDisplayName(displayName : string) : void {
          this.displayName = displayName;
        }
        getCategory() : string {
          return this.category;
        }
        setCategory(category : string) : void {
          this.category = category;
        }
        withCategory(category : string) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.category = category;
          return this;
        }
        getCommands() : Array<org.eclipse.che.api.machine.shared.dto.CommandDto> {
          return this.commands;
        }
        setCommands(commands : Array<org.eclipse.che.api.machine.shared.dto.CommandDto>) : void {
          this.commands = commands;
        }
        withCommands(commands : Array<org.eclipse.che.api.machine.shared.dto.CommandDto>) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.commands = commands;
          return this;
        }
        withTags(tags : Array<string>) : org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor {
          this.tags = tags;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getPath() : string {
          return this.path;
        }
        getAttributes() : Map<string,Array<string>> {
          return this.attributes;
        }
        getDisplayName() : string {
          return this.displayName;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.displayName) {
                          json.displayName = this.displayName;
                        }
                        if (this.projectType) {
                          json.projectType = this.projectType;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.source) {
                          json.source = (this.source as org.eclipse.che.api.workspace.shared.dto.SourceStorageDtoImpl).toJson();
                        }
                        if (this.tags) {
                            let listArray = [];
                            this.tags.forEach((item) => {
                            listArray.push(item);
                            json.tags = listArray;
                            });
                        }
                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.mixins) {
                            let listArray = [];
                            this.mixins.forEach((item) => {
                            listArray.push(item);
                            json.mixins = listArray;
                            });
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.category) {
                          json.category = this.category;
                        }
                        if (this.commands) {
                            let listArray = [];
                            this.commands.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.machine.shared.dto.CommandDtoImpl).toJson());
                            json.commands = listArray;
                            });
                        }
                        if (this.problems) {
                            let listArray = [];
                            this.problems.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.workspace.shared.dto.ProjectProblemDtoImpl).toJson());
                            json.problems = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export interface VfsFileStatusUpdateDto {
      withType(arg0): org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
      withHashCode(arg0): org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
      withPath(arg0): org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
      getType(): string;
      getPath(): string;
      getHashCode(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export class VfsFileStatusUpdateDtoImpl implements org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto {

        path : string;
        hashCode : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.hashCode) {
                this.hashCode = __jsonObject.hashCode;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto {
          this.type = type;
          return this;
        }
        withHashCode(hashCode : string) : org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto {
          this.hashCode = hashCode;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto {
          this.path = path;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getPath() : string {
          return this.path;
        }
        getHashCode() : string {
          return this.hashCode;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.hashCode) {
                          json.hashCode = this.hashCode;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export interface BreakpointActivatedEventDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
      setType(arg0): void;
      getBreakpoint(): org.eclipse.che.api.debug.shared.dto.BreakpointDto;
      setBreakpoint(arg0): void;
      withBreakpoint(arg0): org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto.event {

  export class BreakpointActivatedEventDtoImpl implements org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto {

        type : string;
        breakpoint : org.eclipse.che.api.debug.shared.dto.BreakpointDto;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.breakpoint) {
                this.breakpoint = new org.eclipse.che.api.debug.shared.dto.BreakpointDtoImpl(__jsonObject.breakpoint);
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        getBreakpoint() : org.eclipse.che.api.debug.shared.dto.BreakpointDto {
          return this.breakpoint;
        }
        setBreakpoint(breakpoint : org.eclipse.che.api.debug.shared.dto.BreakpointDto) : void {
          this.breakpoint = breakpoint;
        }
        withBreakpoint(breakpoint : org.eclipse.che.api.debug.shared.dto.BreakpointDto) : org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto {
          this.breakpoint = breakpoint;
          return this;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.breakpoint) {
                          json.breakpoint = (this.breakpoint as org.eclipse.che.api.debug.shared.dto.BreakpointDtoImpl).toJson();
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.agent.shared.dto {

  export interface AgentKeyDto {
      getVersion(): string;
      setVersion(arg0): void;
      withName(arg0): org.eclipse.che.api.agent.shared.dto.AgentKeyDto;
      withVersion(arg0): org.eclipse.che.api.agent.shared.dto.AgentKeyDto;
      getName(): string;
      setName(arg0): void;
      getId(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.agent.shared.dto {

  export class AgentKeyDtoImpl implements org.eclipse.che.api.agent.shared.dto.AgentKeyDto {

        name : string;
        id : string;
        version : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.version) {
                this.version = __jsonObject.version;
              }
            }

    } 

        getVersion() : string {
          return this.version;
        }
        setVersion(version : string) : void {
          this.version = version;
        }
        withName(name : string) : org.eclipse.che.api.agent.shared.dto.AgentKeyDto {
          this.name = name;
          return this;
        }
        withVersion(version : string) : org.eclipse.che.api.agent.shared.dto.AgentKeyDto {
          this.version = version;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getId() : string {
          return this.id;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.version) {
                          json.version = this.version;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export interface FileTrackingOperationDto {
      withType(arg0): org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
      getOldPath(): string;
      withPath(arg0): org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
      withOldPath(arg0): org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
      getType(): string;
      getPath(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export class FileTrackingOperationDtoImpl implements org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto {

        path : string;
        oldPath : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.oldPath) {
                this.oldPath = __jsonObject.oldPath;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto {
          this.type = type;
          return this;
        }
        getOldPath() : string {
          return this.oldPath;
        }
        withPath(path : string) : org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto {
          this.path = path;
          return this;
        }
        withOldPath(oldPath : string) : org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto {
          this.oldPath = oldPath;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getPath() : string {
          return this.path;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.oldPath) {
                          json.oldPath = this.oldPath;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RmRequest {
      getItems(): Array<string>;
      setItems(arg0): void;
      setCached(arg0): void;
      withCached(arg0): org.eclipse.che.api.git.shared.RmRequest;
      withItems(arg0): org.eclipse.che.api.git.shared.RmRequest;
      isCached(): boolean;
      isRecursively(): boolean;
      setRecursively(arg0): void;
      withRecursively(arg0): org.eclipse.che.api.git.shared.RmRequest;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RmRequestImpl implements org.eclipse.che.api.git.shared.RmRequest {

        cached : boolean;
        recursively : boolean;
        items : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.cached) {
                this.cached = __jsonObject.cached;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.recursively) {
                this.recursively = __jsonObject.recursively;
              }
            }
            this.items = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.items) {
                  __jsonObject.items.forEach((item) => {
                  this.items.push(item);
                  });
              }
            }

    } 

        getItems() : Array<string> {
          return this.items;
        }
        setItems(items : Array<string>) : void {
          this.items = items;
        }
        setCached(cached : boolean) : void {
          this.cached = cached;
        }
        withCached(cached : boolean) : org.eclipse.che.api.git.shared.RmRequest {
          this.cached = cached;
          return this;
        }
        withItems(items : Array<string>) : org.eclipse.che.api.git.shared.RmRequest {
          this.items = items;
          return this;
        }
        isCached() : boolean {
          return this.cached;
        }
        isRecursively() : boolean {
          return this.recursively;
        }
        setRecursively(recursively : boolean) : void {
          this.recursively = recursively;
        }
        withRecursively(recursively : boolean) : org.eclipse.che.api.git.shared.RmRequest {
          this.recursively = recursively;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.cached) {
                          json.cached = this.cached;
                        }
                        if (this.recursively) {
                          json.recursively = this.recursively;
                        }
                        if (this.items) {
                            let listArray = [];
                            this.items.forEach((item) => {
                            listArray.push(item);
                            json.items = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface OnProjectsLoadedDto {
      setActions(arg0): void;
      withActions(arg0): org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto;
      getActions(): Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class OnProjectsLoadedDtoImpl implements org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto {

        actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.actions = new Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>();
            if (__jsonObject) {
              if (__jsonObject.actions) {
                  __jsonObject.actions.forEach((item) => {
                  this.actions.push(new org.eclipse.che.api.factory.shared.dto.IdeActionDtoImpl(item));
                  });
              }
            }

    } 

        setActions(actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>) : void {
          this.actions = actions;
        }
        withActions(actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>) : org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto {
          this.actions = actions;
          return this;
        }
        getActions() : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto> {
          return this.actions;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.actions) {
                            let listArray = [];
                            this.actions.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.factory.shared.dto.IdeActionDtoImpl).toJson());
                            json.actions = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Commiters {
      getCommiters(): Array<org.eclipse.che.api.git.shared.GitUser>;
      withCommiters(arg0): org.eclipse.che.api.git.shared.Commiters;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class CommitersImpl implements org.eclipse.che.api.git.shared.Commiters {

        commiters : Array<org.eclipse.che.api.git.shared.GitUser>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.commiters = new Array<org.eclipse.che.api.git.shared.GitUser>();
            if (__jsonObject) {
              if (__jsonObject.commiters) {
                  __jsonObject.commiters.forEach((item) => {
                  this.commiters.push(new org.eclipse.che.api.git.shared.GitUserImpl(item));
                  });
              }
            }

    } 

        getCommiters() : Array<org.eclipse.che.api.git.shared.GitUser> {
          return this.commiters;
        }
        withCommiters(commiters : Array<org.eclipse.che.api.git.shared.GitUser>) : org.eclipse.che.api.git.shared.Commiters {
          this.commiters = commiters;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commiters) {
                            let listArray = [];
                            this.commiters.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.git.shared.GitUserImpl).toJson());
                            json.commiters = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface BranchCreateRequest {
      withName(arg0): org.eclipse.che.api.git.shared.BranchCreateRequest;
      getStartPoint(): string;
      setStartPoint(arg0): void;
      withStartPoint(arg0): org.eclipse.che.api.git.shared.BranchCreateRequest;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class BranchCreateRequestImpl implements org.eclipse.che.api.git.shared.BranchCreateRequest {

        name : string;
        startPoint : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.startPoint) {
                this.startPoint = __jsonObject.startPoint;
              }
            }

    } 

        withName(name : string) : org.eclipse.che.api.git.shared.BranchCreateRequest {
          this.name = name;
          return this;
        }
        getStartPoint() : string {
          return this.startPoint;
        }
        setStartPoint(startPoint : string) : void {
          this.startPoint = startPoint;
        }
        withStartPoint(startPoint : string) : org.eclipse.che.api.git.shared.BranchCreateRequest {
          this.startPoint = startPoint;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.startPoint) {
                          json.startPoint = this.startPoint;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface Link {
      setParameters(arg0): void;
      setMethod(arg0): void;
      getHref(): string;
      setRequestBody(arg0): void;
      withMethod(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      withParameters(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      setHref(arg0): void;
      withHref(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      withRequestBody(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      withRel(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      setRel(arg0): void;
      getProduces(): string;
      withProduces(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      setProduces(arg0): void;
      getConsumes(): string;
      withConsumes(arg0): org.eclipse.che.api.core.rest.shared.dto.Link;
      setConsumes(arg0): void;
      getRequestBody(): org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor;
      getRel(): string;
      getMethod(): string;
      getParameters(): Array<org.eclipse.che.api.core.rest.shared.dto.LinkParameter>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class LinkImpl implements org.eclipse.che.api.core.rest.shared.dto.Link {

        method : string;
        requestBody : org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor;
        rel : string;
        produces : string;
        href : string;
        parameters : Array<org.eclipse.che.api.core.rest.shared.dto.LinkParameter>;
        consumes : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.method) {
                this.method = __jsonObject.method;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.requestBody) {
                this.requestBody = new org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptorImpl(__jsonObject.requestBody);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.rel) {
                this.rel = __jsonObject.rel;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.produces) {
                this.produces = __jsonObject.produces;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.href) {
                this.href = __jsonObject.href;
              }
            }
            this.parameters = new Array<org.eclipse.che.api.core.rest.shared.dto.LinkParameter>();
            if (__jsonObject) {
              if (__jsonObject.parameters) {
                  __jsonObject.parameters.forEach((item) => {
                  this.parameters.push(new org.eclipse.che.api.core.rest.shared.dto.LinkParameterImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.consumes) {
                this.consumes = __jsonObject.consumes;
              }
            }

    } 

        setParameters(parameters : Array<org.eclipse.che.api.core.rest.shared.dto.LinkParameter>) : void {
          this.parameters = parameters;
        }
        setMethod(method : string) : void {
          this.method = method;
        }
        getHref() : string {
          return this.href;
        }
        setRequestBody(requestBody : org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor) : void {
          this.requestBody = requestBody;
        }
        withMethod(method : string) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.method = method;
          return this;
        }
        withParameters(parameters : Array<org.eclipse.che.api.core.rest.shared.dto.LinkParameter>) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.parameters = parameters;
          return this;
        }
        setHref(href : string) : void {
          this.href = href;
        }
        withHref(href : string) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.href = href;
          return this;
        }
        withRequestBody(requestBody : org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.requestBody = requestBody;
          return this;
        }
        withRel(rel : string) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.rel = rel;
          return this;
        }
        setRel(rel : string) : void {
          this.rel = rel;
        }
        getProduces() : string {
          return this.produces;
        }
        withProduces(produces : string) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.produces = produces;
          return this;
        }
        setProduces(produces : string) : void {
          this.produces = produces;
        }
        getConsumes() : string {
          return this.consumes;
        }
        withConsumes(consumes : string) : org.eclipse.che.api.core.rest.shared.dto.Link {
          this.consumes = consumes;
          return this;
        }
        setConsumes(consumes : string) : void {
          this.consumes = consumes;
        }
        getRequestBody() : org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor {
          return this.requestBody;
        }
        getRel() : string {
          return this.rel;
        }
        getMethod() : string {
          return this.method;
        }
        getParameters() : Array<org.eclipse.che.api.core.rest.shared.dto.LinkParameter> {
          return this.parameters;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.method) {
                          json.method = this.method;
                        }
                        if (this.requestBody) {
                          json.requestBody = (this.requestBody as org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptorImpl).toJson();
                        }
                        if (this.rel) {
                          json.rel = this.rel;
                        }
                        if (this.produces) {
                          json.produces = this.produces;
                        }
                        if (this.href) {
                          json.href = this.href;
                        }
                        if (this.parameters) {
                            let listArray = [];
                            this.parameters.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkParameterImpl).toJson());
                            json.parameters = listArray;
                            });
                        }
                        if (this.consumes) {
                          json.consumes = this.consumes;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface ExtendedError {
      getErrorCode(): number;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
      withMessage(arg0): org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
      withErrorCode(arg0): org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
      setErrorCode(arg0): void;
      getAttributes(): Map<string,string>;
      setMessage(arg0): void;
      getMessage(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class ExtendedErrorImpl implements org.eclipse.che.api.core.rest.shared.dto.ExtendedError {

        errorCode : number;
        attributes : Map<string,string>;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.errorCode) {
                this.errorCode = __jsonObject.errorCode;
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        getErrorCode() : number {
          return this.errorCode;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.core.rest.shared.dto.ExtendedError {
          this.attributes = attributes;
          return this;
        }
        withMessage(message : string) : org.eclipse.che.api.core.rest.shared.dto.ExtendedError {
          this.message = message;
          return this;
        }
        withErrorCode(errorCode : number) : org.eclipse.che.api.core.rest.shared.dto.ExtendedError {
          this.errorCode = errorCode;
          return this;
        }
        setErrorCode(errorCode : number) : void {
          this.errorCode = errorCode;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
        setMessage(message : string) : void {
          this.message = message;
        }
        getMessage() : string {
          return this.message;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.errorCode) {
                          json.errorCode = this.errorCode;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export interface MachineRecipe {
      withType(arg0): org.eclipse.che.api.machine.shared.dto.recipe.MachineRecipe;
      setType(arg0): void;
      setScript(arg0): void;
      withScript(arg0): org.eclipse.che.api.machine.shared.dto.recipe.MachineRecipe;
      getType(): string;
      getScript(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.recipe {

  export class MachineRecipeImpl implements org.eclipse.che.api.machine.shared.dto.recipe.MachineRecipe {

        type : string;
        script : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.script) {
                this.script = __jsonObject.script;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.machine.shared.dto.recipe.MachineRecipe {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setScript(script : string) : void {
          this.script = script;
        }
        withScript(script : string) : org.eclipse.che.api.machine.shared.dto.recipe.MachineRecipe {
          this.script = script;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getScript() : string {
          return this.script;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.script) {
                          json.script = this.script;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Revision {
      setId(arg0): void;
      setMessage(arg0): void;
      getBranches(): Array<org.eclipse.che.api.git.shared.Branch>;
      setBranches(arg0): void;
      isFake(): boolean;
      setFake(arg0): void;
      withAuthor(arg0): org.eclipse.che.api.git.shared.Revision;
      withBranches(arg0): org.eclipse.che.api.git.shared.Revision;
      getDiffCommitFile(): Array<org.eclipse.che.api.git.shared.DiffCommitFile>;
      setDiffCommitFile(arg0): void;
      withDiffCommitFile(arg0): org.eclipse.che.api.git.shared.Revision;
      getCommitParent(): Array<string>;
      setCommitParent(arg0): void;
      withCommitParent(arg0): org.eclipse.che.api.git.shared.Revision;
      getBranch(): string;
      setBranch(arg0): void;
      withBranch(arg0): org.eclipse.che.api.git.shared.Revision;
      getCommitTime(): number;
      setCommitTime(arg0): void;
      withCommitTime(arg0): org.eclipse.che.api.git.shared.Revision;
      getCommitter(): org.eclipse.che.api.git.shared.GitUser;
      setCommitter(arg0): void;
      withCommitter(arg0): org.eclipse.che.api.git.shared.Revision;
      withId(arg0): org.eclipse.che.api.git.shared.Revision;
      withMessage(arg0): org.eclipse.che.api.git.shared.Revision;
      getAuthor(): org.eclipse.che.api.git.shared.GitUser;
      setAuthor(arg0): void;
      getMessage(): string;
      getId(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RevisionImpl implements org.eclipse.che.api.git.shared.Revision {

        commitParent : Array<string>;
        committer : org.eclipse.che.api.git.shared.GitUser;
        commitTime : number;
        author : org.eclipse.che.api.git.shared.GitUser;
        fake : boolean;
        id : string;
        message : string;
        branches : Array<org.eclipse.che.api.git.shared.Branch>;
        diffCommitFile : Array<org.eclipse.che.api.git.shared.DiffCommitFile>;
        branch : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.commitParent = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.commitParent) {
                  __jsonObject.commitParent.forEach((item) => {
                  this.commitParent.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.committer) {
                this.committer = new org.eclipse.che.api.git.shared.GitUserImpl(__jsonObject.committer);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.commitTime) {
                this.commitTime = __jsonObject.commitTime;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.author) {
                this.author = new org.eclipse.che.api.git.shared.GitUserImpl(__jsonObject.author);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.fake) {
                this.fake = __jsonObject.fake;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }
            this.branches = new Array<org.eclipse.che.api.git.shared.Branch>();
            if (__jsonObject) {
              if (__jsonObject.branches) {
                  __jsonObject.branches.forEach((item) => {
                  this.branches.push(new org.eclipse.che.api.git.shared.BranchImpl(item));
                  });
              }
            }
            this.diffCommitFile = new Array<org.eclipse.che.api.git.shared.DiffCommitFile>();
            if (__jsonObject) {
              if (__jsonObject.diffCommitFile) {
                  __jsonObject.diffCommitFile.forEach((item) => {
                  this.diffCommitFile.push(new org.eclipse.che.api.git.shared.DiffCommitFileImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.branch) {
                this.branch = __jsonObject.branch;
              }
            }

    } 

        setId(id : string) : void {
          this.id = id;
        }
        setMessage(message : string) : void {
          this.message = message;
        }
        getBranches() : Array<org.eclipse.che.api.git.shared.Branch> {
          return this.branches;
        }
        setBranches(branches : Array<org.eclipse.che.api.git.shared.Branch>) : void {
          this.branches = branches;
        }
        isFake() : boolean {
          return this.fake;
        }
        setFake(fake : boolean) : void {
          this.fake = fake;
        }
        withAuthor(author : org.eclipse.che.api.git.shared.GitUser) : org.eclipse.che.api.git.shared.Revision {
          this.author = author;
          return this;
        }
        withBranches(branches : Array<org.eclipse.che.api.git.shared.Branch>) : org.eclipse.che.api.git.shared.Revision {
          this.branches = branches;
          return this;
        }
        getDiffCommitFile() : Array<org.eclipse.che.api.git.shared.DiffCommitFile> {
          return this.diffCommitFile;
        }
        setDiffCommitFile(diffCommitFile : Array<org.eclipse.che.api.git.shared.DiffCommitFile>) : void {
          this.diffCommitFile = diffCommitFile;
        }
        withDiffCommitFile(diffCommitFile : Array<org.eclipse.che.api.git.shared.DiffCommitFile>) : org.eclipse.che.api.git.shared.Revision {
          this.diffCommitFile = diffCommitFile;
          return this;
        }
        getCommitParent() : Array<string> {
          return this.commitParent;
        }
        setCommitParent(commitParent : Array<string>) : void {
          this.commitParent = commitParent;
        }
        withCommitParent(commitParent : Array<string>) : org.eclipse.che.api.git.shared.Revision {
          this.commitParent = commitParent;
          return this;
        }
        getBranch() : string {
          return this.branch;
        }
        setBranch(branch : string) : void {
          this.branch = branch;
        }
        withBranch(branch : string) : org.eclipse.che.api.git.shared.Revision {
          this.branch = branch;
          return this;
        }
        getCommitTime() : number {
          return this.commitTime;
        }
        setCommitTime(commitTime : number) : void {
          this.commitTime = commitTime;
        }
        withCommitTime(commitTime : number) : org.eclipse.che.api.git.shared.Revision {
          this.commitTime = commitTime;
          return this;
        }
        getCommitter() : org.eclipse.che.api.git.shared.GitUser {
          return this.committer;
        }
        setCommitter(committer : org.eclipse.che.api.git.shared.GitUser) : void {
          this.committer = committer;
        }
        withCommitter(committer : org.eclipse.che.api.git.shared.GitUser) : org.eclipse.che.api.git.shared.Revision {
          this.committer = committer;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.git.shared.Revision {
          this.id = id;
          return this;
        }
        withMessage(message : string) : org.eclipse.che.api.git.shared.Revision {
          this.message = message;
          return this;
        }
        getAuthor() : org.eclipse.che.api.git.shared.GitUser {
          return this.author;
        }
        setAuthor(author : org.eclipse.che.api.git.shared.GitUser) : void {
          this.author = author;
        }
        getMessage() : string {
          return this.message;
        }
        getId() : string {
          return this.id;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commitParent) {
                            let listArray = [];
                            this.commitParent.forEach((item) => {
                            listArray.push(item);
                            json.commitParent = listArray;
                            });
                        }
                        if (this.committer) {
                          json.committer = (this.committer as org.eclipse.che.api.git.shared.GitUserImpl).toJson();
                        }
                        if (this.commitTime) {
                          json.commitTime = this.commitTime;
                        }
                        if (this.author) {
                          json.author = (this.author as org.eclipse.che.api.git.shared.GitUserImpl).toJson();
                        }
                        if (this.fake) {
                          json.fake = this.fake;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }
                        if (this.branches) {
                            let listArray = [];
                            this.branches.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.git.shared.BranchImpl).toJson());
                            json.branches = listArray;
                            });
                        }
                        if (this.diffCommitFile) {
                            let listArray = [];
                            this.diffCommitFile.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.git.shared.DiffCommitFileImpl).toJson());
                            json.diffCommitFile = listArray;
                            });
                        }
                        if (this.branch) {
                          json.branch = this.branch;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface GeneratorDescription {
      getOptions(): Map<string,string>;
      setOptions(arg0): void;
      withOptions(arg0): org.eclipse.che.api.project.shared.dto.GeneratorDescription;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class GeneratorDescriptionImpl implements org.eclipse.che.api.project.shared.dto.GeneratorDescription {

        options : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.options = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.options) {
                let tmp : Array<any> = Object.keys(__jsonObject.options);
                tmp.forEach((key) => {
                  this.options.set(key, __jsonObject.options[key]);
                 });
              }
            }

    } 

        getOptions() : Map<string,string> {
          return this.options;
        }
        setOptions(options : Map<string,string>) : void {
          this.options = options;
        }
        withOptions(options : Map<string,string>) : org.eclipse.che.api.project.shared.dto.GeneratorDescription {
          this.options = options;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.options) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.options.entries()) {
                            tmpMap[key] = value;
                           }
                          json.options = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export interface ProjectTreeStatusUpdateDto {
      withType(arg0): org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto;
      withPath(arg0): org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto;
      getType(): string;
      getPath(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export class ProjectTreeStatusUpdateDtoImpl implements org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto {

        path : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto {
          this.type = type;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.project.shared.dto.event.ProjectTreeStatusUpdateDto {
          this.path = path;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getPath() : string {
          return this.path;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface CheckoutRequest {
      getFiles(): Array<string>;
      withName(arg0): org.eclipse.che.api.git.shared.CheckoutRequest;
      setFiles(arg0): void;
      withFiles(arg0): org.eclipse.che.api.git.shared.CheckoutRequest;
      getStartPoint(): string;
      setStartPoint(arg0): void;
      withStartPoint(arg0): org.eclipse.che.api.git.shared.CheckoutRequest;
      isCreateNew(): boolean;
      isNoTrack(): boolean;
      withNoTrack(arg0): org.eclipse.che.api.git.shared.CheckoutRequest;
      setCreateNew(arg0): void;
      withCreateNew(arg0): org.eclipse.che.api.git.shared.CheckoutRequest;
      getTrackBranch(): string;
      setTrackBranch(arg0): void;
      withTrackBranch(arg0): org.eclipse.che.api.git.shared.CheckoutRequest;
      setNoTrack(arg0): void;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class CheckoutRequestImpl implements org.eclipse.che.api.git.shared.CheckoutRequest {

        noTrack : boolean;
        name : string;
        startPoint : string;
        files : Array<string>;
        trackBranch : string;
        createNew : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.noTrack) {
                this.noTrack = __jsonObject.noTrack;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.startPoint) {
                this.startPoint = __jsonObject.startPoint;
              }
            }
            this.files = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.files) {
                  __jsonObject.files.forEach((item) => {
                  this.files.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.trackBranch) {
                this.trackBranch = __jsonObject.trackBranch;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.createNew) {
                this.createNew = __jsonObject.createNew;
              }
            }

    } 

        getFiles() : Array<string> {
          return this.files;
        }
        withName(name : string) : org.eclipse.che.api.git.shared.CheckoutRequest {
          this.name = name;
          return this;
        }
        setFiles(files : Array<string>) : void {
          this.files = files;
        }
        withFiles(files : Array<string>) : org.eclipse.che.api.git.shared.CheckoutRequest {
          this.files = files;
          return this;
        }
        getStartPoint() : string {
          return this.startPoint;
        }
        setStartPoint(startPoint : string) : void {
          this.startPoint = startPoint;
        }
        withStartPoint(startPoint : string) : org.eclipse.che.api.git.shared.CheckoutRequest {
          this.startPoint = startPoint;
          return this;
        }
        isCreateNew() : boolean {
          return this.createNew;
        }
        isNoTrack() : boolean {
          return this.noTrack;
        }
        withNoTrack(noTrack : boolean) : org.eclipse.che.api.git.shared.CheckoutRequest {
          this.noTrack = noTrack;
          return this;
        }
        setCreateNew(createNew : boolean) : void {
          this.createNew = createNew;
        }
        withCreateNew(createNew : boolean) : org.eclipse.che.api.git.shared.CheckoutRequest {
          this.createNew = createNew;
          return this;
        }
        getTrackBranch() : string {
          return this.trackBranch;
        }
        setTrackBranch(trackBranch : string) : void {
          this.trackBranch = trackBranch;
        }
        withTrackBranch(trackBranch : string) : org.eclipse.che.api.git.shared.CheckoutRequest {
          this.trackBranch = trackBranch;
          return this;
        }
        setNoTrack(noTrack : boolean) : void {
          this.noTrack = noTrack;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.noTrack) {
                          json.noTrack = this.noTrack;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.startPoint) {
                          json.startPoint = this.startPoint;
                        }
                        if (this.files) {
                            let listArray = [];
                            this.files.forEach((item) => {
                            listArray.push(item);
                            json.files = listArray;
                            });
                        }
                        if (this.trackBranch) {
                          json.trackBranch = this.trackBranch;
                        }
                        if (this.createNew) {
                          json.createNew = this.createNew;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface CopyOptions {
      setOverWrite(arg0): void;
      getOverWrite(): boolean;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class CopyOptionsImpl implements org.eclipse.che.api.project.shared.dto.CopyOptions {

        name : string;
        overWrite : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.overWrite) {
                this.overWrite = __jsonObject.overWrite;
              }
            }

    } 

        setOverWrite(overWrite : boolean) : void {
          this.overWrite = overWrite;
        }
        getOverWrite() : boolean {
          return this.overWrite;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.overWrite) {
                          json.overWrite = this.overWrite;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface ItemReference {
      setContentLength(arg0): void;
      getContentLength(): number;
      getProject(): string;
      withType(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      setType(arg0): void;
      setPath(arg0): void;
      withName(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      withLinks(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      withPath(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      getModified(): number;
      setModified(arg0): void;
      withProject(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      withModified(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      getProjectConfig(): org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
      withContentLength(arg0): org.eclipse.che.api.project.shared.dto.ItemReference;
      setProjectConfig(arg0): void;
      getName(): string;
      setName(arg0): void;
      getType(): string;
      getPath(): string;
      getAttributes(): Map<string,string>;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class ItemReferenceImpl implements org.eclipse.che.api.project.shared.dto.ItemReference {

        path : string;
        projectConfig : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
        name : string;
        project : string;
        modified : number;
        contentLength : number;
        attributes : Map<string,string>;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.projectConfig) {
                this.projectConfig = new org.eclipse.che.api.workspace.shared.dto.ProjectConfigDtoImpl(__jsonObject.projectConfig);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.project) {
                this.project = __jsonObject.project;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.modified) {
                this.modified = __jsonObject.modified;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.contentLength) {
                this.contentLength = __jsonObject.contentLength;
              }
            }
            this.attributes = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        setContentLength(contentLength : number) : void {
          this.contentLength = contentLength;
        }
        getContentLength() : number {
          return this.contentLength;
        }
        getProject() : string {
          return this.project;
        }
        withType(type : string) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setPath(path : string) : void {
          this.path = path;
        }
        withName(name : string) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.name = name;
          return this;
        }
        setAttributes(attributes : Map<string,string>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,string>) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.attributes = attributes;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.links = links;
          return this;
        }
        withPath(path : string) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.path = path;
          return this;
        }
        getModified() : number {
          return this.modified;
        }
        setModified(modified : number) : void {
          this.modified = modified;
        }
        withProject(project : string) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.project = project;
          return this;
        }
        withModified(modified : number) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.modified = modified;
          return this;
        }
        getProjectConfig() : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto {
          return this.projectConfig;
        }
        withContentLength(contentLength : number) : org.eclipse.che.api.project.shared.dto.ItemReference {
          this.contentLength = contentLength;
          return this;
        }
        setProjectConfig(projectConfig : org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto) : void {
          this.projectConfig = projectConfig;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getType() : string {
          return this.type;
        }
        getPath() : string {
          return this.path;
        }
        getAttributes() : Map<string,string> {
          return this.attributes;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.projectConfig) {
                          json.projectConfig = (this.projectConfig as org.eclipse.che.api.workspace.shared.dto.ProjectConfigDtoImpl).toJson();
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.project) {
                          json.project = this.project;
                        }
                        if (this.modified) {
                          json.modified = this.modified;
                        }
                        if (this.contentLength) {
                          json.contentLength = this.contentLength;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface FieldDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      setType(arg0): void;
      withName(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      getVariables(): Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
      setVariables(arg0): void;
      withVariables(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      setIsFinal(arg0): void;
      withValue(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      withIsFinal(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      isIsStatic(): boolean;
      setIsStatic(arg0): void;
      withIsStatic(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      isIsTransient(): boolean;
      setIsTransient(arg0): void;
      withIsTransient(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      isIsVolatile(): boolean;
      setIsVolatile(arg0): void;
      withIsVolatile(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      isExistInformation(): boolean;
      setExistInformation(arg0): void;
      withExistInformation(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      getVariablePath(): org.eclipse.che.api.debug.shared.dto.VariablePathDto;
      setVariablePath(arg0): void;
      withVariablePath(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      setPrimitive(arg0): void;
      withPrimitive(arg0): org.eclipse.che.api.debug.shared.dto.FieldDto;
      isIsFinal(): boolean;
      isPrimitive(): boolean;
      getName(): string;
      getValue(): string;
      setName(arg0): void;
      setValue(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class FieldDtoImpl implements org.eclipse.che.api.debug.shared.dto.FieldDto {

        variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
        isStatic : boolean;
        variablePath : org.eclipse.che.api.debug.shared.dto.VariablePathDto;
        primitive : boolean;
        name : string;
        isVolatile : boolean;
        isFinal : boolean;
        type : string;
        isTransient : boolean;
        existInformation : boolean;
        value : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.variables = new Array<org.eclipse.che.api.debug.shared.dto.VariableDto>();
            if (__jsonObject) {
              if (__jsonObject.variables) {
                  __jsonObject.variables.forEach((item) => {
                  this.variables.push(new org.eclipse.che.api.debug.shared.dto.VariableDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.isStatic) {
                this.isStatic = __jsonObject.isStatic;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.variablePath) {
                this.variablePath = new org.eclipse.che.api.debug.shared.dto.VariablePathDtoImpl(__jsonObject.variablePath);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.primitive) {
                this.primitive = __jsonObject.primitive;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.isVolatile) {
                this.isVolatile = __jsonObject.isVolatile;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.isFinal) {
                this.isFinal = __jsonObject.isFinal;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.isTransient) {
                this.isTransient = __jsonObject.isTransient;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.existInformation) {
                this.existInformation = __jsonObject.existInformation;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.value) {
                this.value = __jsonObject.value;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withName(name : string) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.name = name;
          return this;
        }
        getVariables() : Array<org.eclipse.che.api.debug.shared.dto.VariableDto> {
          return this.variables;
        }
        setVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : void {
          this.variables = variables;
        }
        withVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.variables = variables;
          return this;
        }
        setIsFinal(isFinal : boolean) : void {
          this.isFinal = isFinal;
        }
        withValue(value : string) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.value = value;
          return this;
        }
        withIsFinal(isFinal : boolean) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.isFinal = isFinal;
          return this;
        }
        isIsStatic() : boolean {
          return this.isStatic;
        }
        setIsStatic(isStatic : boolean) : void {
          this.isStatic = isStatic;
        }
        withIsStatic(isStatic : boolean) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.isStatic = isStatic;
          return this;
        }
        isIsTransient() : boolean {
          return this.isTransient;
        }
        setIsTransient(isTransient : boolean) : void {
          this.isTransient = isTransient;
        }
        withIsTransient(isTransient : boolean) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.isTransient = isTransient;
          return this;
        }
        isIsVolatile() : boolean {
          return this.isVolatile;
        }
        setIsVolatile(isVolatile : boolean) : void {
          this.isVolatile = isVolatile;
        }
        withIsVolatile(isVolatile : boolean) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.isVolatile = isVolatile;
          return this;
        }
        isExistInformation() : boolean {
          return this.existInformation;
        }
        setExistInformation(existInformation : boolean) : void {
          this.existInformation = existInformation;
        }
        withExistInformation(existInformation : boolean) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.existInformation = existInformation;
          return this;
        }
        getVariablePath() : org.eclipse.che.api.debug.shared.dto.VariablePathDto {
          return this.variablePath;
        }
        setVariablePath(variablePath : org.eclipse.che.api.debug.shared.dto.VariablePathDto) : void {
          this.variablePath = variablePath;
        }
        withVariablePath(variablePath : org.eclipse.che.api.debug.shared.dto.VariablePathDto) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.variablePath = variablePath;
          return this;
        }
        setPrimitive(primitive : boolean) : void {
          this.primitive = primitive;
        }
        withPrimitive(primitive : boolean) : org.eclipse.che.api.debug.shared.dto.FieldDto {
          this.primitive = primitive;
          return this;
        }
        isIsFinal() : boolean {
          return this.isFinal;
        }
        isPrimitive() : boolean {
          return this.primitive;
        }
        getName() : string {
          return this.name;
        }
        getValue() : string {
          return this.value;
        }
        setName(name : string) : void {
          this.name = name;
        }
        setValue(value : string) : void {
          this.value = value;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.variables) {
                            let listArray = [];
                            this.variables.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.debug.shared.dto.VariableDtoImpl).toJson());
                            json.variables = listArray;
                            });
                        }
                        if (this.isStatic) {
                          json.isStatic = this.isStatic;
                        }
                        if (this.variablePath) {
                          json.variablePath = (this.variablePath as org.eclipse.che.api.debug.shared.dto.VariablePathDtoImpl).toJson();
                        }
                        if (this.primitive) {
                          json.primitive = this.primitive;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.isVolatile) {
                          json.isVolatile = this.isVolatile;
                        }
                        if (this.isFinal) {
                          json.isFinal = this.isFinal;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.isTransient) {
                          json.isTransient = this.isTransient;
                        }
                        if (this.existInformation) {
                          json.existInformation = this.existInformation;
                        }
                        if (this.value) {
                          json.value = this.value;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface TagCreateRequest {
      setMessage(arg0): void;
      setCommit(arg0): void;
      getCommit(): string;
      isForce(): boolean;
      setForce(arg0): void;
      getName(): string;
      getMessage(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class TagCreateRequestImpl implements org.eclipse.che.api.git.shared.TagCreateRequest {

        commit : string;
        name : string;
        force : boolean;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.commit) {
                this.commit = __jsonObject.commit;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.force) {
                this.force = __jsonObject.force;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        setMessage(message : string) : void {
          this.message = message;
        }
        setCommit(commit : string) : void {
          this.commit = commit;
        }
        getCommit() : string {
          return this.commit;
        }
        isForce() : boolean {
          return this.force;
        }
        setForce(force : boolean) : void {
          this.force = force;
        }
        getName() : string {
          return this.name;
        }
        getMessage() : string {
          return this.message;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commit) {
                          json.commit = this.commit;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.force) {
                          json.force = this.force;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Branch {
      isActive(): boolean;
      withName(arg0): org.eclipse.che.api.git.shared.Branch;
      withRemote(arg0): org.eclipse.che.api.git.shared.Branch;
      withDisplayName(arg0): org.eclipse.che.api.git.shared.Branch;
      isRemote(): boolean;
      withActive(arg0): org.eclipse.che.api.git.shared.Branch;
      getName(): string;
      getDisplayName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class BranchImpl implements org.eclipse.che.api.git.shared.Branch {

        displayName : string;
        name : string;
        active : boolean;
        remote : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.displayName) {
                this.displayName = __jsonObject.displayName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.active) {
                this.active = __jsonObject.active;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.remote) {
                this.remote = __jsonObject.remote;
              }
            }

    } 

        isActive() : boolean {
          return this.active;
        }
        withName(name : string) : org.eclipse.che.api.git.shared.Branch {
          this.name = name;
          return this;
        }
        withRemote(remote : boolean) : org.eclipse.che.api.git.shared.Branch {
          this.remote = remote;
          return this;
        }
        withDisplayName(displayName : string) : org.eclipse.che.api.git.shared.Branch {
          this.displayName = displayName;
          return this;
        }
        isRemote() : boolean {
          return this.remote;
        }
        withActive(active : boolean) : org.eclipse.che.api.git.shared.Branch {
          this.active = active;
          return this;
        }
        getName() : string {
          return this.name;
        }
        getDisplayName() : string {
          return this.displayName;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.displayName) {
                          json.displayName = this.displayName;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.active) {
                          json.active = this.active;
                        }
                        if (this.remote) {
                          json.remote = this.remote;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface MoveRequest {
      getSource(): string;
      setSource(arg0): void;
      getTarget(): string;
      setTarget(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class MoveRequestImpl implements org.eclipse.che.api.git.shared.MoveRequest {

        source : string;
        target : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.source) {
                this.source = __jsonObject.source;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.target) {
                this.target = __jsonObject.target;
              }
            }

    } 

        getSource() : string {
          return this.source;
        }
        setSource(source : string) : void {
          this.source = source;
        }
        getTarget() : string {
          return this.target;
        }
        setTarget(target : string) : void {
          this.target = target;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.source) {
                          json.source = this.source;
                        }
                        if (this.target) {
                          json.target = this.target;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export interface PomModifiedEventDto {
      withPath(arg0): org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto;
      getPath(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto.event {

  export class PomModifiedEventDtoImpl implements org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto {

        path : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }

    } 

        withPath(path : string) : org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto {
          this.path = path;
          return this;
        }
        getPath() : string {
          return this.path;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export interface JsonRpcResponse {
      withResult(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
      getError(): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError;
      getJsonrpc(): string;
      withJsonrpc(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
      withId(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
      withError(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
      getId(): number;
      getResult(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export class JsonRpcResponseImpl implements org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse {

        result : string;
        id : number;
        error : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError;
        jsonrpc : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.result) {
                this.result = __jsonObject.result;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.error) {
                this.error = new org.eclipse.che.api.core.jsonrpc.shared.JsonRpcErrorImpl(__jsonObject.error);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.jsonrpc) {
                this.jsonrpc = __jsonObject.jsonrpc;
              }
            }

    } 

        withResult(result : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse {
          this.result = result;
          return this;
        }
        getError() : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError {
          return this.error;
        }
        getJsonrpc() : string {
          return this.jsonrpc;
        }
        withJsonrpc(jsonrpc : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse {
          this.jsonrpc = jsonrpc;
          return this;
        }
        withId(id : number) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse {
          this.id = id;
          return this;
        }
        withError(error : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcError) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse {
          this.error = error;
          return this;
        }
        getId() : number {
          return this.id;
        }
        getResult() : string {
          return this.result;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.result) {
                          json.result = this.result;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.error) {
                          json.error = (this.error as org.eclipse.che.api.core.jsonrpc.shared.JsonRpcErrorImpl).toJson();
                        }
                        if (this.jsonrpc) {
                          json.jsonrpc = this.jsonrpc;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface PullResponse {
      getCommandOutput(): string;
      setCommandOutput(arg0): void;
      withCommandOutput(arg0): org.eclipse.che.api.git.shared.PullResponse;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class PullResponseImpl implements org.eclipse.che.api.git.shared.PullResponse {

        commandOutput : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.commandOutput) {
                this.commandOutput = __jsonObject.commandOutput;
              }
            }

    } 

        getCommandOutput() : string {
          return this.commandOutput;
        }
        setCommandOutput(commandOutput : string) : void {
          this.commandOutput = commandOutput;
        }
        withCommandOutput(commandOutput : string) : org.eclipse.che.api.git.shared.PullResponse {
          this.commandOutput = commandOutput;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commandOutput) {
                          json.commandOutput = this.commandOutput;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface TreeElement {
      setNode(arg0): void;
      getChildren(): Array<org.eclipse.che.api.project.shared.dto.TreeElement>;
      setChildren(arg0): void;
      withChildren(arg0): org.eclipse.che.api.project.shared.dto.TreeElement;
      withNode(arg0): org.eclipse.che.api.project.shared.dto.TreeElement;
      getNode(): org.eclipse.che.api.project.shared.dto.ItemReference;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class TreeElementImpl implements org.eclipse.che.api.project.shared.dto.TreeElement {

        node : org.eclipse.che.api.project.shared.dto.ItemReference;
        children : Array<org.eclipse.che.api.project.shared.dto.TreeElement>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.node) {
                this.node = new org.eclipse.che.api.project.shared.dto.ItemReferenceImpl(__jsonObject.node);
              }
            }
            this.children = new Array<org.eclipse.che.api.project.shared.dto.TreeElement>();
            if (__jsonObject) {
              if (__jsonObject.children) {
                  __jsonObject.children.forEach((item) => {
                  this.children.push(new org.eclipse.che.api.project.shared.dto.TreeElementImpl(item));
                  });
              }
            }

    } 

        setNode(node : org.eclipse.che.api.project.shared.dto.ItemReference) : void {
          this.node = node;
        }
        getChildren() : Array<org.eclipse.che.api.project.shared.dto.TreeElement> {
          return this.children;
        }
        setChildren(children : Array<org.eclipse.che.api.project.shared.dto.TreeElement>) : void {
          this.children = children;
        }
        withChildren(children : Array<org.eclipse.che.api.project.shared.dto.TreeElement>) : org.eclipse.che.api.project.shared.dto.TreeElement {
          this.children = children;
          return this;
        }
        withNode(node : org.eclipse.che.api.project.shared.dto.ItemReference) : org.eclipse.che.api.project.shared.dto.TreeElement {
          this.node = node;
          return this;
        }
        getNode() : org.eclipse.che.api.project.shared.dto.ItemReference {
          return this.node;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.node) {
                          json.node = (this.node as org.eclipse.che.api.project.shared.dto.ItemReferenceImpl).toJson();
                        }
                        if (this.children) {
                            let listArray = [];
                            this.children.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.project.shared.dto.TreeElementImpl).toJson());
                            json.children = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface AttributeDto {
      isRequired(): boolean;
      getDescription(): string;
      withDescription(arg0): org.eclipse.che.api.project.shared.dto.AttributeDto;
      withName(arg0): org.eclipse.che.api.project.shared.dto.AttributeDto;
      withRequired(arg0): org.eclipse.che.api.project.shared.dto.AttributeDto;
      withValue(arg0): org.eclipse.che.api.project.shared.dto.AttributeDto;
      isVariable(): boolean;
      withVariable(arg0): org.eclipse.che.api.project.shared.dto.AttributeDto;
      getName(): string;
      getValue(): org.eclipse.che.api.project.shared.dto.ValueDto;
      getProjectType(): string;
      getId(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class AttributeDtoImpl implements org.eclipse.che.api.project.shared.dto.AttributeDto {

        name : string;
        variable : boolean;
        projectType : string;
        description : string;
        id : string;
        value : org.eclipse.che.api.project.shared.dto.ValueDto;
        required : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.variable) {
                this.variable = __jsonObject.variable;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.projectType) {
                this.projectType = __jsonObject.projectType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.value) {
                this.value = new org.eclipse.che.api.project.shared.dto.ValueDtoImpl(__jsonObject.value);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.required) {
                this.required = __jsonObject.required;
              }
            }

    } 

        isRequired() : boolean {
          return this.required;
        }
        getDescription() : string {
          return this.description;
        }
        withDescription(description : string) : org.eclipse.che.api.project.shared.dto.AttributeDto {
          this.description = description;
          return this;
        }
        withName(name : string) : org.eclipse.che.api.project.shared.dto.AttributeDto {
          this.name = name;
          return this;
        }
        withRequired(required : boolean) : org.eclipse.che.api.project.shared.dto.AttributeDto {
          this.required = required;
          return this;
        }
        withValue(value : org.eclipse.che.api.project.shared.dto.ValueDto) : org.eclipse.che.api.project.shared.dto.AttributeDto {
          this.value = value;
          return this;
        }
        isVariable() : boolean {
          return this.variable;
        }
        withVariable(variable : boolean) : org.eclipse.che.api.project.shared.dto.AttributeDto {
          this.variable = variable;
          return this;
        }
        getName() : string {
          return this.name;
        }
        getValue() : org.eclipse.che.api.project.shared.dto.ValueDto {
          return this.value;
        }
        getProjectType() : string {
          return this.projectType;
        }
        getId() : string {
          return this.id;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.variable) {
                          json.variable = this.variable;
                        }
                        if (this.projectType) {
                          json.projectType = this.projectType;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.value) {
                          json.value = (this.value as org.eclipse.che.api.project.shared.dto.ValueDtoImpl).toJson();
                        }
                        if (this.required) {
                          json.required = this.required;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface IndexFile {
      setPath(arg0): void;
      withPath(arg0): org.eclipse.che.api.git.shared.IndexFile;
      isIndexed(): boolean;
      setIndexed(arg0): void;
      withIndexed(arg0): org.eclipse.che.api.git.shared.IndexFile;
      getPath(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class IndexFileImpl implements org.eclipse.che.api.git.shared.IndexFile {

        path : string;
        indexed : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.path) {
                this.path = __jsonObject.path;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.indexed) {
                this.indexed = __jsonObject.indexed;
              }
            }

    } 

        setPath(path : string) : void {
          this.path = path;
        }
        withPath(path : string) : org.eclipse.che.api.git.shared.IndexFile {
          this.path = path;
          return this;
        }
        isIndexed() : boolean {
          return this.indexed;
        }
        setIndexed(indexed : boolean) : void {
          this.indexed = indexed;
        }
        withIndexed(indexed : boolean) : org.eclipse.che.api.git.shared.IndexFile {
          this.indexed = indexed;
          return this;
        }
        getPath() : string {
          return this.path;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                          json.path = this.path;
                        }
                        if (this.indexed) {
                          json.indexed = this.indexed;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface DiffCommitFile {
      getOldPath(): string;
      setChangeType(arg0): void;
      withChangeType(arg0): org.eclipse.che.api.git.shared.DiffCommitFile;
      setOldPath(arg0): void;
      getNewPath(): string;
      getChangeType(): string;
      setNewPath(arg0): void;
      withNewPath(arg0): org.eclipse.che.api.git.shared.DiffCommitFile;
      withOldPath(arg0): org.eclipse.che.api.git.shared.DiffCommitFile;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class DiffCommitFileImpl implements org.eclipse.che.api.git.shared.DiffCommitFile {

        changeType : string;
        oldPath : string;
        newPath : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.changeType) {
                this.changeType = __jsonObject.changeType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.oldPath) {
                this.oldPath = __jsonObject.oldPath;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.newPath) {
                this.newPath = __jsonObject.newPath;
              }
            }

    } 

        getOldPath() : string {
          return this.oldPath;
        }
        setChangeType(changeType : string) : void {
          this.changeType = changeType;
        }
        withChangeType(changeType : string) : org.eclipse.che.api.git.shared.DiffCommitFile {
          this.changeType = changeType;
          return this;
        }
        setOldPath(oldPath : string) : void {
          this.oldPath = oldPath;
        }
        getNewPath() : string {
          return this.newPath;
        }
        getChangeType() : string {
          return this.changeType;
        }
        setNewPath(newPath : string) : void {
          this.newPath = newPath;
        }
        withNewPath(newPath : string) : org.eclipse.che.api.git.shared.DiffCommitFile {
          this.newPath = newPath;
          return this;
        }
        withOldPath(oldPath : string) : org.eclipse.che.api.git.shared.DiffCommitFile {
          this.oldPath = oldPath;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.changeType) {
                          json.changeType = this.changeType;
                        }
                        if (this.oldPath) {
                          json.oldPath = this.oldPath;
                        }
                        if (this.newPath) {
                          json.newPath = this.newPath;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface SimpleValueDto {
      getVariables(): Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
      setVariables(arg0): void;
      withVariables(arg0): org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
      withValue(arg0): org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
      getValue(): string;
      setValue(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class SimpleValueDtoImpl implements org.eclipse.che.api.debug.shared.dto.SimpleValueDto {

        variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
        value : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.variables = new Array<org.eclipse.che.api.debug.shared.dto.VariableDto>();
            if (__jsonObject) {
              if (__jsonObject.variables) {
                  __jsonObject.variables.forEach((item) => {
                  this.variables.push(new org.eclipse.che.api.debug.shared.dto.VariableDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.value) {
                this.value = __jsonObject.value;
              }
            }

    } 

        getVariables() : Array<org.eclipse.che.api.debug.shared.dto.VariableDto> {
          return this.variables;
        }
        setVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : void {
          this.variables = variables;
        }
        withVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : org.eclipse.che.api.debug.shared.dto.SimpleValueDto {
          this.variables = variables;
          return this;
        }
        withValue(value : string) : org.eclipse.che.api.debug.shared.dto.SimpleValueDto {
          this.value = value;
          return this;
        }
        getValue() : string {
          return this.value;
        }
        setValue(value : string) : void {
          this.value = value;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.variables) {
                            let listArray = [];
                            this.variables.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.debug.shared.dto.VariableDtoImpl).toJson());
                            json.variables = listArray;
                            });
                        }
                        if (this.value) {
                          json.value = this.value;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface SnapshotDto {
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      setId(arg0): void;
      withLinks(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      withId(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      setWorkspaceId(arg0): void;
      withWorkspaceId(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      withDev(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      setDev(arg0): void;
      withMachineName(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      setMachineName(arg0): void;
      withCreationDate(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      withEnvName(arg0): org.eclipse.che.api.machine.shared.dto.SnapshotDto;
      setEnvName(arg0): void;
      setCreationDate(arg0): void;
      getDescription(): string;
      getWorkspaceId(): string;
      isDev(): boolean;
      getMachineName(): string;
      getCreationDate(): number;
      getEnvName(): string;
      getId(): string;
      getType(): string;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class SnapshotDtoImpl implements org.eclipse.che.api.machine.shared.dto.SnapshotDto {

        dev : boolean;
        envName : string;
        description : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        id : string;
        type : string;
        creationDate : number;
        machineName : string;
        workspaceId : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.dev) {
                this.dev = __jsonObject.dev;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.envName) {
                this.envName = __jsonObject.envName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.creationDate) {
                this.creationDate = __jsonObject.creationDate;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.machineName) {
                this.machineName = __jsonObject.machineName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceId) {
                this.workspaceId = __jsonObject.workspaceId;
              }
            }

    } 

        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.description = description;
          return this;
        }
        setId(id : string) : void {
          this.id = id;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.links = links;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.id = id;
          return this;
        }
        setWorkspaceId(workspaceId : string) : void {
          this.workspaceId = workspaceId;
        }
        withWorkspaceId(workspaceId : string) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.workspaceId = workspaceId;
          return this;
        }
        withDev(dev : boolean) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.dev = dev;
          return this;
        }
        setDev(dev : boolean) : void {
          this.dev = dev;
        }
        withMachineName(machineName : string) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.machineName = machineName;
          return this;
        }
        setMachineName(machineName : string) : void {
          this.machineName = machineName;
        }
        withCreationDate(creationDate : number) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.creationDate = creationDate;
          return this;
        }
        withEnvName(envName : string) : org.eclipse.che.api.machine.shared.dto.SnapshotDto {
          this.envName = envName;
          return this;
        }
        setEnvName(envName : string) : void {
          this.envName = envName;
        }
        setCreationDate(creationDate : number) : void {
          this.creationDate = creationDate;
        }
        getDescription() : string {
          return this.description;
        }
        getWorkspaceId() : string {
          return this.workspaceId;
        }
        isDev() : boolean {
          return this.dev;
        }
        getMachineName() : string {
          return this.machineName;
        }
        getCreationDate() : number {
          return this.creationDate;
        }
        getEnvName() : string {
          return this.envName;
        }
        getId() : string {
          return this.id;
        }
        getType() : string {
          return this.type;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.dev) {
                          json.dev = this.dev;
                        }
                        if (this.envName) {
                          json.envName = this.envName;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.creationDate) {
                          json.creationDate = this.creationDate;
                        }
                        if (this.machineName) {
                          json.machineName = this.machineName;
                        }
                        if (this.workspaceId) {
                          json.workspaceId = this.workspaceId;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface ProjectUpdate {
      getDescription(): string;
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      setAttributes(arg0): void;
      withAttributes(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      setRecipe(arg0): void;
      withRecipe(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      getMixins(): Array<string>;
      setMixins(arg0): void;
      withMixins(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      getRecipe(): string;
      setVisibility(arg0): void;
      withVisibility(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      getVisibility(): string;
      getContentRoot(): string;
      setContentRoot(arg0): void;
      withContentRoot(arg0): org.eclipse.che.api.project.shared.dto.ProjectUpdate;
      getType(): string;
      getAttributes(): Map<string,Array<string>>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class ProjectUpdateImpl implements org.eclipse.che.api.project.shared.dto.ProjectUpdate {

        mixins : Array<string>;
        visibility : string;
        contentRoot : string;
        recipe : string;
        description : string;
        attributes : Map<string,Array<string>>;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.mixins = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.mixins) {
                  __jsonObject.mixins.forEach((item) => {
                  this.mixins.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.visibility) {
                this.visibility = __jsonObject.visibility;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.contentRoot) {
                this.contentRoot = __jsonObject.contentRoot;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.recipe) {
                this.recipe = __jsonObject.recipe;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.attributes = new Map<string,Array<string>>();
            if (__jsonObject) {
              if (__jsonObject.attributes) {
                let tmp : Array<any> = Object.keys(__jsonObject.attributes);
                tmp.forEach((key) => {
                  this.attributes.set(key, __jsonObject.attributes[key]);
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.description = description;
          return this;
        }
        setAttributes(attributes : Map<string,Array<string>>) : void {
          this.attributes = attributes;
        }
        withAttributes(attributes : Map<string,Array<string>>) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.attributes = attributes;
          return this;
        }
        setRecipe(recipe : string) : void {
          this.recipe = recipe;
        }
        withRecipe(recipe : string) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.recipe = recipe;
          return this;
        }
        getMixins() : Array<string> {
          return this.mixins;
        }
        setMixins(mixins : Array<string>) : void {
          this.mixins = mixins;
        }
        withMixins(mixins : Array<string>) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.mixins = mixins;
          return this;
        }
        getRecipe() : string {
          return this.recipe;
        }
        setVisibility(visibility : string) : void {
          this.visibility = visibility;
        }
        withVisibility(visibility : string) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.visibility = visibility;
          return this;
        }
        getVisibility() : string {
          return this.visibility;
        }
        getContentRoot() : string {
          return this.contentRoot;
        }
        setContentRoot(contentRoot : string) : void {
          this.contentRoot = contentRoot;
        }
        withContentRoot(contentRoot : string) : org.eclipse.che.api.project.shared.dto.ProjectUpdate {
          this.contentRoot = contentRoot;
          return this;
        }
        getType() : string {
          return this.type;
        }
        getAttributes() : Map<string,Array<string>> {
          return this.attributes;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.mixins) {
                            let listArray = [];
                            this.mixins.forEach((item) => {
                            listArray.push(item);
                            json.mixins = listArray;
                            });
                        }
                        if (this.visibility) {
                          json.visibility = this.visibility;
                        }
                        if (this.contentRoot) {
                          json.contentRoot = this.contentRoot;
                        }
                        if (this.recipe) {
                          json.recipe = this.recipe;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.attributes) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.attributes.entries()) {
                            tmpMap[key] = value;
                           }
                          json.attributes = tmpMap;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export interface AttributeDescriptor {
      getValues(): Array<string>;
      setRequired(arg0): void;
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.project.shared.dto.AttributeDescriptor;
      setVariable(arg0): void;
      withName(arg0): org.eclipse.che.api.project.shared.dto.AttributeDescriptor;
      withRequired(arg0): org.eclipse.che.api.project.shared.dto.AttributeDescriptor;
      withVariable(arg0): org.eclipse.che.api.project.shared.dto.AttributeDescriptor;
      getRequired(): boolean;
      setValues(arg0): void;
      withValues(arg0): org.eclipse.che.api.project.shared.dto.AttributeDescriptor;
      getVariable(): boolean;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.project.shared.dto {

  export class AttributeDescriptorImpl implements org.eclipse.che.api.project.shared.dto.AttributeDescriptor {

        values : Array<string>;
        variable : boolean;
        name : string;
        description : string;
        required : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.values = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.values) {
                  __jsonObject.values.forEach((item) => {
                  this.values.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.variable) {
                this.variable = __jsonObject.variable;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.required) {
                this.required = __jsonObject.required;
              }
            }

    } 

        getValues() : Array<string> {
          return this.values;
        }
        setRequired(required : boolean) : void {
          this.required = required;
        }
        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.project.shared.dto.AttributeDescriptor {
          this.description = description;
          return this;
        }
        setVariable(variable : boolean) : void {
          this.variable = variable;
        }
        withName(name : string) : org.eclipse.che.api.project.shared.dto.AttributeDescriptor {
          this.name = name;
          return this;
        }
        withRequired(required : boolean) : org.eclipse.che.api.project.shared.dto.AttributeDescriptor {
          this.required = required;
          return this;
        }
        withVariable(variable : boolean) : org.eclipse.che.api.project.shared.dto.AttributeDescriptor {
          this.variable = variable;
          return this;
        }
        getRequired() : boolean {
          return this.required;
        }
        setValues(values : Array<string>) : void {
          this.values = values;
        }
        withValues(values : Array<string>) : org.eclipse.che.api.project.shared.dto.AttributeDescriptor {
          this.values = values;
          return this;
        }
        getVariable() : boolean {
          return this.variable;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.values) {
                            let listArray = [];
                            this.values.forEach((item) => {
                            listArray.push(item);
                            json.values = listArray;
                            });
                        }
                        if (this.variable) {
                          json.variable = this.variable;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.required) {
                          json.required = this.required;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface ResetRequest {
      setType(arg0): void;
      setCommit(arg0): void;
      withCommit(arg0): org.eclipse.che.api.git.shared.ResetRequest;
      getFilePattern(): Array<string>;
      setFilePattern(arg0): void;
      withFilePattern(arg0): org.eclipse.che.api.git.shared.ResetRequest;
      getCommit(): string;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class ResetRequestImpl implements org.eclipse.che.api.git.shared.ResetRequest {

        filePattern : Array<string>;
        commit : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.filePattern = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.filePattern) {
                  __jsonObject.filePattern.forEach((item) => {
                  this.filePattern.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.commit) {
                this.commit = __jsonObject.commit;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        setType(type : string) : void {
          this.type = type;
        }
        setCommit(commit : string) : void {
          this.commit = commit;
        }
        withCommit(commit : string) : org.eclipse.che.api.git.shared.ResetRequest {
          this.commit = commit;
          return this;
        }
        getFilePattern() : Array<string> {
          return this.filePattern;
        }
        setFilePattern(filePattern : Array<string>) : void {
          this.filePattern = filePattern;
        }
        withFilePattern(filePattern : Array<string>) : org.eclipse.che.api.git.shared.ResetRequest {
          this.filePattern = filePattern;
          return this;
        }
        getCommit() : string {
          return this.commit;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.filePattern) {
                            let listArray = [];
                            this.filePattern.forEach((item) => {
                            listArray.push(item);
                            json.filePattern = listArray;
                            });
                        }
                        if (this.commit) {
                          json.commit = this.commit;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.stack {

  export interface StackSourceDto {
      withType(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
      setType(arg0): void;
      withOrigin(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
      setOrigin(arg0): void;
      getOrigin(): string;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.stack {

  export class StackSourceDtoImpl implements org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto {

        origin : string;
        type : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.origin) {
                this.origin = __jsonObject.origin;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withOrigin(origin : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto {
          this.origin = origin;
          return this;
        }
        setOrigin(origin : string) : void {
          this.origin = origin;
        }
        getOrigin() : string {
          return this.origin;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.origin) {
                          json.origin = this.origin;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface GitCheckoutEvent {
      getWorkspaceId(): string;
      setWorkspaceId(arg0): void;
      withWorkspaceId(arg0): org.eclipse.che.api.git.shared.GitCheckoutEvent;
      getBranchRef(): string;
      setBranchRef(arg0): void;
      withBranchRef(arg0): org.eclipse.che.api.git.shared.GitCheckoutEvent;
      getProjectName(): string;
      setProjectName(arg0): void;
      withProjectName(arg0): org.eclipse.che.api.git.shared.GitCheckoutEvent;
      setCheckoutOnly(arg0): void;
      withCheckoutOnly(arg0): org.eclipse.che.api.git.shared.GitCheckoutEvent;
      isCheckoutOnly(): boolean;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class GitCheckoutEventImpl implements org.eclipse.che.api.git.shared.GitCheckoutEvent {

        branchRef : string;
        projectName : string;
        checkoutOnly : boolean;
        workspaceId : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.branchRef) {
                this.branchRef = __jsonObject.branchRef;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.projectName) {
                this.projectName = __jsonObject.projectName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.checkoutOnly) {
                this.checkoutOnly = __jsonObject.checkoutOnly;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceId) {
                this.workspaceId = __jsonObject.workspaceId;
              }
            }

    } 

        getWorkspaceId() : string {
          return this.workspaceId;
        }
        setWorkspaceId(workspaceId : string) : void {
          this.workspaceId = workspaceId;
        }
        withWorkspaceId(workspaceId : string) : org.eclipse.che.api.git.shared.GitCheckoutEvent {
          this.workspaceId = workspaceId;
          return this;
        }
        getBranchRef() : string {
          return this.branchRef;
        }
        setBranchRef(branchRef : string) : void {
          this.branchRef = branchRef;
        }
        withBranchRef(branchRef : string) : org.eclipse.che.api.git.shared.GitCheckoutEvent {
          this.branchRef = branchRef;
          return this;
        }
        getProjectName() : string {
          return this.projectName;
        }
        setProjectName(projectName : string) : void {
          this.projectName = projectName;
        }
        withProjectName(projectName : string) : org.eclipse.che.api.git.shared.GitCheckoutEvent {
          this.projectName = projectName;
          return this;
        }
        setCheckoutOnly(checkoutOnly : boolean) : void {
          this.checkoutOnly = checkoutOnly;
        }
        withCheckoutOnly(checkoutOnly : boolean) : org.eclipse.che.api.git.shared.GitCheckoutEvent {
          this.checkoutOnly = checkoutOnly;
          return this;
        }
        isCheckoutOnly() : boolean {
          return this.checkoutOnly;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.branchRef) {
                          json.branchRef = this.branchRef;
                        }
                        if (this.projectName) {
                          json.projectName = this.projectName;
                        }
                        if (this.checkoutOnly) {
                          json.checkoutOnly = this.checkoutOnly;
                        }
                        if (this.workspaceId) {
                          json.workspaceId = this.workspaceId;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RemoteReference {
      setReferenceName(arg0): void;
      withReferenceName(arg0): org.eclipse.che.api.git.shared.RemoteReference;
      setCommitId(arg0): void;
      withCommitId(arg0): org.eclipse.che.api.git.shared.RemoteReference;
      getReferenceName(): string;
      getCommitId(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RemoteReferenceImpl implements org.eclipse.che.api.git.shared.RemoteReference {

        commitId : string;
        referenceName : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.commitId) {
                this.commitId = __jsonObject.commitId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.referenceName) {
                this.referenceName = __jsonObject.referenceName;
              }
            }

    } 

        setReferenceName(referenceName : string) : void {
          this.referenceName = referenceName;
        }
        withReferenceName(referenceName : string) : org.eclipse.che.api.git.shared.RemoteReference {
          this.referenceName = referenceName;
          return this;
        }
        setCommitId(commitId : string) : void {
          this.commitId = commitId;
        }
        withCommitId(commitId : string) : org.eclipse.che.api.git.shared.RemoteReference {
          this.commitId = commitId;
          return this;
        }
        getReferenceName() : string {
          return this.referenceName;
        }
        getCommitId() : string {
          return this.commitId;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commitId) {
                          json.commitId = this.commitId;
                        }
                        if (this.referenceName) {
                          json.referenceName = this.referenceName;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.event {

  export interface WorkspaceStatusEvent {
      getEventType(): string;
      getError(): string;
      setEventType(arg0): void;
      withError(arg0): org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
      withEventType(arg0): org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
      getWorkspaceId(): string;
      setWorkspaceId(arg0): void;
      withWorkspaceId(arg0): org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
      setError(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.event {

  export class WorkspaceStatusEventImpl implements org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent {

        eventType : string;
        error : string;
        workspaceId : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.eventType) {
                this.eventType = __jsonObject.eventType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.error) {
                this.error = __jsonObject.error;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceId) {
                this.workspaceId = __jsonObject.workspaceId;
              }
            }

    } 

        getEventType() : string {
          return this.eventType;
        }
        getError() : string {
          return this.error;
        }
        setEventType(eventType : string) : void {
          this.eventType = eventType;
        }
        withError(error : string) : org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent {
          this.error = error;
          return this;
        }
        withEventType(eventType : string) : org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent {
          this.eventType = eventType;
          return this;
        }
        getWorkspaceId() : string {
          return this.workspaceId;
        }
        setWorkspaceId(workspaceId : string) : void {
          this.workspaceId = workspaceId;
        }
        withWorkspaceId(workspaceId : string) : org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent {
          this.workspaceId = workspaceId;
          return this;
        }
        setError(error : string) : void {
          this.error = error;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.eventType) {
                          json.eventType = this.eventType;
                        }
                        if (this.error) {
                          json.error = this.error;
                        }
                        if (this.workspaceId) {
                          json.workspaceId = this.workspaceId;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineLimitsDto {
      withRam(arg0): org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;
      getRam(): number;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineLimitsDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineLimitsDto {

        ram : number;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.ram) {
                this.ram = __jsonObject.ram;
              }
            }

    } 

        withRam(ram : number) : org.eclipse.che.api.machine.shared.dto.MachineLimitsDto {
          this.ram = ram;
          return this;
        }
        getRam() : number {
          return this.ram;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.ram) {
                          json.ram = this.ram;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface ConfigRequest {
      getConfigEntries(): Map<string,string>;
      withConfigEntries(arg0): org.eclipse.che.api.git.shared.ConfigRequest;
      setConfigEntries(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class ConfigRequestImpl implements org.eclipse.che.api.git.shared.ConfigRequest {

        configEntries : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.configEntries = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.configEntries) {
                let tmp : Array<any> = Object.keys(__jsonObject.configEntries);
                tmp.forEach((key) => {
                  this.configEntries.set(key, __jsonObject.configEntries[key]);
                 });
              }
            }

    } 

        getConfigEntries() : Map<string,string> {
          return this.configEntries;
        }
        withConfigEntries(configEntries : Map<string,string>) : org.eclipse.che.api.git.shared.ConfigRequest {
          this.configEntries = configEntries;
          return this;
        }
        setConfigEntries(configEntries : Map<string,string>) : void {
          this.configEntries = configEntries;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.configEntries) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.configEntries.entries()) {
                            tmpMap[key] = value;
                           }
                          json.configEntries = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RemoteAddRequest {
      getUrl(): string;
      setUrl(arg0): void;
      getBranches(): Array<string>;
      setBranches(arg0): void;
      withName(arg0): org.eclipse.che.api.git.shared.RemoteAddRequest;
      withUrl(arg0): org.eclipse.che.api.git.shared.RemoteAddRequest;
      getName(): string;
      setName(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RemoteAddRequestImpl implements org.eclipse.che.api.git.shared.RemoteAddRequest {

        name : string;
        branches : Array<string>;
        url : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.branches = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.branches) {
                  __jsonObject.branches.forEach((item) => {
                  this.branches.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.url) {
                this.url = __jsonObject.url;
              }
            }

    } 

        getUrl() : string {
          return this.url;
        }
        setUrl(url : string) : void {
          this.url = url;
        }
        getBranches() : Array<string> {
          return this.branches;
        }
        setBranches(branches : Array<string>) : void {
          this.branches = branches;
        }
        withName(name : string) : org.eclipse.che.api.git.shared.RemoteAddRequest {
          this.name = name;
          return this;
        }
        withUrl(url : string) : org.eclipse.che.api.git.shared.RemoteAddRequest {
          this.url = url;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.branches) {
                            let listArray = [];
                            this.branches.forEach((item) => {
                            listArray.push(item);
                            json.branches = listArray;
                            });
                        }
                        if (this.url) {
                          json.url = this.url;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface Tag {
      withName(arg0): org.eclipse.che.api.git.shared.Tag;
      getName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class TagImpl implements org.eclipse.che.api.git.shared.Tag {

        name : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }

    } 

        withName(name : string) : org.eclipse.che.api.git.shared.Tag {
          this.name = name;
          return this;
        }
        getName() : string {
          return this.name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface WorkspaceConfigDto {
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      getProjects(): Array<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto>;
      setProjects(arg0): void;
      withName(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      withLinks(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      getCommands(): Array<org.eclipse.che.api.machine.shared.dto.CommandDto>;
      setCommands(arg0): void;
      withCommands(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      getEnvironments(): Map<string,org.eclipse.che.api.workspace.shared.dto.EnvironmentDto>;
      setEnvironments(arg0): void;
      withEnvironments(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      withProjects(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      getDefaultEnv(): string;
      setDefaultEnv(arg0): void;
      withDefaultEnv(arg0): org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
      getName(): string;
      setName(arg0): void;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class WorkspaceConfigDtoImpl implements org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {

        projects : Array<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto>;
        environments : Map<string,org.eclipse.che.api.workspace.shared.dto.EnvironmentDto>;
        name : string;
        description : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        commands : Array<org.eclipse.che.api.machine.shared.dto.CommandDto>;
        defaultEnv : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.projects = new Array<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto>();
            if (__jsonObject) {
              if (__jsonObject.projects) {
                  __jsonObject.projects.forEach((item) => {
                  this.projects.push(new org.eclipse.che.api.workspace.shared.dto.ProjectConfigDtoImpl(item));
                  });
              }
            }
            this.environments = new Map<string,org.eclipse.che.api.workspace.shared.dto.EnvironmentDto>();
            if (__jsonObject) {
              if (__jsonObject.environments) {
                let tmp : Array<any> = Object.keys(__jsonObject.environments);
                tmp.forEach((key) => {
                  this.environments.set(key, new org.eclipse.che.api.workspace.shared.dto.EnvironmentDtoImpl(__jsonObject.environments[key]));
                 });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            this.commands = new Array<org.eclipse.che.api.machine.shared.dto.CommandDto>();
            if (__jsonObject) {
              if (__jsonObject.commands) {
                  __jsonObject.commands.forEach((item) => {
                  this.commands.push(new org.eclipse.che.api.machine.shared.dto.CommandDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.defaultEnv) {
                this.defaultEnv = __jsonObject.defaultEnv;
              }
            }

    } 

        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.description = description;
          return this;
        }
        getProjects() : Array<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto> {
          return this.projects;
        }
        setProjects(projects : Array<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto>) : void {
          this.projects = projects;
        }
        withName(name : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.name = name;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.links = links;
          return this;
        }
        getCommands() : Array<org.eclipse.che.api.machine.shared.dto.CommandDto> {
          return this.commands;
        }
        setCommands(commands : Array<org.eclipse.che.api.machine.shared.dto.CommandDto>) : void {
          this.commands = commands;
        }
        withCommands(commands : Array<org.eclipse.che.api.machine.shared.dto.CommandDto>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.commands = commands;
          return this;
        }
        getEnvironments() : Map<string,org.eclipse.che.api.workspace.shared.dto.EnvironmentDto> {
          return this.environments;
        }
        setEnvironments(environments : Map<string,org.eclipse.che.api.workspace.shared.dto.EnvironmentDto>) : void {
          this.environments = environments;
        }
        withEnvironments(environments : Map<string,org.eclipse.che.api.workspace.shared.dto.EnvironmentDto>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.environments = environments;
          return this;
        }
        withProjects(projects : Array<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto>) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.projects = projects;
          return this;
        }
        getDefaultEnv() : string {
          return this.defaultEnv;
        }
        setDefaultEnv(defaultEnv : string) : void {
          this.defaultEnv = defaultEnv;
        }
        withDefaultEnv(defaultEnv : string) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
          this.defaultEnv = defaultEnv;
          return this;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.projects) {
                            let listArray = [];
                            this.projects.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.workspace.shared.dto.ProjectConfigDtoImpl).toJson());
                            json.projects = listArray;
                            });
                        }
                        if (this.environments) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.environments.entries()) {
                            tmpMap[key] = (value as org.eclipse.che.api.workspace.shared.dto.EnvironmentDtoImpl).toJson();
                           }
                          json.environments = tmpMap;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.commands) {
                            let listArray = [];
                            this.commands.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.machine.shared.dto.CommandDtoImpl).toJson());
                            json.commands = listArray;
                            });
                        }
                        if (this.defaultEnv) {
                          json.defaultEnv = this.defaultEnv;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RebaseRequest {
      getBranch(): string;
      setBranch(arg0): void;
      setOperation(arg0): void;
      getOperation(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RebaseRequestImpl implements org.eclipse.che.api.git.shared.RebaseRequest {

        branch : string;
        operation : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.branch) {
                this.branch = __jsonObject.branch;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.operation) {
                this.operation = __jsonObject.operation;
              }
            }

    } 

        getBranch() : string {
          return this.branch;
        }
        setBranch(branch : string) : void {
          this.branch = branch;
        }
        setOperation(operation : string) : void {
          this.operation = operation;
        }
        getOperation() : string {
          return this.operation;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.branch) {
                          json.branch = this.branch;
                        }
                        if (this.operation) {
                          json.operation = this.operation;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.stack {

  export interface StackComponentDto {
      setVersion(arg0): void;
      withName(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
      withVersion(arg0): org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
      setName(arg0): void;
      getVersion(): string;
      getName(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto.stack {

  export class StackComponentDtoImpl implements org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto {

        name : string;
        version : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.version) {
                this.version = __jsonObject.version;
              }
            }

    } 

        setVersion(version : string) : void {
          this.version = version;
        }
        withName(name : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto {
          this.name = name;
          return this;
        }
        withVersion(version : string) : org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto {
          this.version = version;
          return this;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getVersion() : string {
          return this.version;
        }
        getName() : string {
          return this.name;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.version) {
                          json.version = this.version;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface ServiceDescriptor {
      getVersion(): string;
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor;
      setVersion(arg0): void;
      getHref(): string;
      withLinks(arg0): org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor;
      withVersion(arg0): org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor;
      setHref(arg0): void;
      withHref(arg0): org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class ServiceDescriptorImpl implements org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor {

        description : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        href : string;
        version : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.href) {
                this.href = __jsonObject.href;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.version) {
                this.version = __jsonObject.version;
              }
            }

    } 

        getVersion() : string {
          return this.version;
        }
        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor {
          this.description = description;
          return this;
        }
        setVersion(version : string) : void {
          this.version = version;
        }
        getHref() : string {
          return this.href;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor {
          this.links = links;
          return this;
        }
        withVersion(version : string) : org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor {
          this.version = version;
          return this;
        }
        setHref(href : string) : void {
          this.href = href;
        }
        withHref(href : string) : org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor {
          this.href = href;
          return this;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.href) {
                          json.href = this.href;
                        }
                        if (this.version) {
                          json.version = this.version;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface NewSnapshotDescriptor {
      getDescription(): string;
      setDescription(arg0): void;
      withDescription(arg0): org.eclipse.che.api.machine.shared.dto.NewSnapshotDescriptor;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class NewSnapshotDescriptorImpl implements org.eclipse.che.api.machine.shared.dto.NewSnapshotDescriptor {

        description : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }

    } 

        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withDescription(description : string) : org.eclipse.che.api.machine.shared.dto.NewSnapshotDescriptor {
          this.description = description;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.description) {
                          json.description = this.description;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export interface JsonRpcObject {
      withType(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
      withMessage(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
      getMessage(): string;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export class JsonRpcObjectImpl implements org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject {

        type : string;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject {
          this.type = type;
          return this;
        }
        withMessage(message : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject {
          this.message = message;
          return this;
        }
        getMessage() : string {
          return this.message;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface RebaseResponse {
      getStatus(): string;
      setStatus(arg0): void;
      withStatus(arg0): org.eclipse.che.api.git.shared.RebaseResponse;
      getConflicts(): Array<string>;
      setConflicts(arg0): void;
      withConflicts(arg0): org.eclipse.che.api.git.shared.RebaseResponse;
      getFailed(): Array<string>;
      setFailed(arg0): void;
      withFailed(arg0): org.eclipse.che.api.git.shared.RebaseResponse;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class RebaseResponseImpl implements org.eclipse.che.api.git.shared.RebaseResponse {

        conflicts : Array<string>;
        failed : Array<string>;
        status : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.conflicts = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.conflicts) {
                  __jsonObject.conflicts.forEach((item) => {
                  this.conflicts.push(item);
                  });
              }
            }
            this.failed = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.failed) {
                  __jsonObject.failed.forEach((item) => {
                  this.failed.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.status) {
                this.status = __jsonObject.status;
              }
            }

    } 

        getStatus() : string {
          return this.status;
        }
        setStatus(status : string) : void {
          this.status = status;
        }
        withStatus(status : string) : org.eclipse.che.api.git.shared.RebaseResponse {
          this.status = status;
          return this;
        }
        getConflicts() : Array<string> {
          return this.conflicts;
        }
        setConflicts(conflicts : Array<string>) : void {
          this.conflicts = conflicts;
        }
        withConflicts(conflicts : Array<string>) : org.eclipse.che.api.git.shared.RebaseResponse {
          this.conflicts = conflicts;
          return this;
        }
        getFailed() : Array<string> {
          return this.failed;
        }
        setFailed(failed : Array<string>) : void {
          this.failed = failed;
        }
        withFailed(failed : Array<string>) : org.eclipse.che.api.git.shared.RebaseResponse {
          this.failed = failed;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.conflicts) {
                            let listArray = [];
                            this.conflicts.forEach((item) => {
                            listArray.push(item);
                            json.conflicts = listArray;
                            });
                        }
                        if (this.failed) {
                            let listArray = [];
                            this.failed.forEach((item) => {
                            listArray.push(item);
                            json.failed = listArray;
                            });
                        }
                        if (this.status) {
                          json.status = this.status;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface LocationDto {
      setLineNumber(arg0): void;
      withTarget(arg0): org.eclipse.che.api.debug.shared.dto.LocationDto;
      withLineNumber(arg0): org.eclipse.che.api.debug.shared.dto.LocationDto;
      setExternalResource(arg0): void;
      setResourcePath(arg0): void;
      withResourcePath(arg0): org.eclipse.che.api.debug.shared.dto.LocationDto;
      withExternalResource(arg0): org.eclipse.che.api.debug.shared.dto.LocationDto;
      setExternalResourceId(arg0): void;
      withExternalResourceId(arg0): org.eclipse.che.api.debug.shared.dto.LocationDto;
      setResourceProjectPath(arg0): void;
      withResourceProjectPath(arg0): org.eclipse.che.api.debug.shared.dto.LocationDto;
      setTarget(arg0): void;
      getResourcePath(): string;
      isExternalResource(): boolean;
      getExternalResourceId(): number;
      getResourceProjectPath(): string;
      getTarget(): string;
      getLineNumber(): number;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class LocationDtoImpl implements org.eclipse.che.api.debug.shared.dto.LocationDto {

        externalResourceId : number;
        resourcePath : string;
        externalResource : boolean;
        resourceProjectPath : string;
        lineNumber : number;
        target : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.externalResourceId) {
                this.externalResourceId = __jsonObject.externalResourceId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.resourcePath) {
                this.resourcePath = __jsonObject.resourcePath;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.externalResource) {
                this.externalResource = __jsonObject.externalResource;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.resourceProjectPath) {
                this.resourceProjectPath = __jsonObject.resourceProjectPath;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.lineNumber) {
                this.lineNumber = __jsonObject.lineNumber;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.target) {
                this.target = __jsonObject.target;
              }
            }

    } 

        setLineNumber(lineNumber : number) : void {
          this.lineNumber = lineNumber;
        }
        withTarget(target : string) : org.eclipse.che.api.debug.shared.dto.LocationDto {
          this.target = target;
          return this;
        }
        withLineNumber(lineNumber : number) : org.eclipse.che.api.debug.shared.dto.LocationDto {
          this.lineNumber = lineNumber;
          return this;
        }
        setExternalResource(externalResource : boolean) : void {
          this.externalResource = externalResource;
        }
        setResourcePath(resourcePath : string) : void {
          this.resourcePath = resourcePath;
        }
        withResourcePath(resourcePath : string) : org.eclipse.che.api.debug.shared.dto.LocationDto {
          this.resourcePath = resourcePath;
          return this;
        }
        withExternalResource(externalResource : boolean) : org.eclipse.che.api.debug.shared.dto.LocationDto {
          this.externalResource = externalResource;
          return this;
        }
        setExternalResourceId(externalResourceId : number) : void {
          this.externalResourceId = externalResourceId;
        }
        withExternalResourceId(externalResourceId : number) : org.eclipse.che.api.debug.shared.dto.LocationDto {
          this.externalResourceId = externalResourceId;
          return this;
        }
        setResourceProjectPath(resourceProjectPath : string) : void {
          this.resourceProjectPath = resourceProjectPath;
        }
        withResourceProjectPath(resourceProjectPath : string) : org.eclipse.che.api.debug.shared.dto.LocationDto {
          this.resourceProjectPath = resourceProjectPath;
          return this;
        }
        setTarget(target : string) : void {
          this.target = target;
        }
        getResourcePath() : string {
          return this.resourcePath;
        }
        isExternalResource() : boolean {
          return this.externalResource;
        }
        getExternalResourceId() : number {
          return this.externalResourceId;
        }
        getResourceProjectPath() : string {
          return this.resourceProjectPath;
        }
        getTarget() : string {
          return this.target;
        }
        getLineNumber() : number {
          return this.lineNumber;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.externalResourceId) {
                          json.externalResourceId = this.externalResourceId;
                        }
                        if (this.resourcePath) {
                          json.resourcePath = this.resourcePath;
                        }
                        if (this.externalResource) {
                          json.externalResource = this.externalResource;
                        }
                        if (this.resourceProjectPath) {
                          json.resourceProjectPath = this.resourceProjectPath;
                        }
                        if (this.lineNumber) {
                          json.lineNumber = this.lineNumber;
                        }
                        if (this.target) {
                          json.target = this.target;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export interface JsonRpcRequest {
      getParams(): string;
      withMethod(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
      withParams(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
      getJsonrpc(): string;
      withJsonrpc(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
      withId(arg0): org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
      getMethod(): string;
      getId(): number;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.jsonrpc.shared {

  export class JsonRpcRequestImpl implements org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest {

        method : string;
        id : number;
        params : string;
        jsonrpc : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.method) {
                this.method = __jsonObject.method;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.params) {
                this.params = __jsonObject.params;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.jsonrpc) {
                this.jsonrpc = __jsonObject.jsonrpc;
              }
            }

    } 

        getParams() : string {
          return this.params;
        }
        withMethod(method : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest {
          this.method = method;
          return this;
        }
        withParams(params : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest {
          this.params = params;
          return this;
        }
        getJsonrpc() : string {
          return this.jsonrpc;
        }
        withJsonrpc(jsonrpc : string) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest {
          this.jsonrpc = jsonrpc;
          return this;
        }
        withId(id : number) : org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest {
          this.id = id;
          return this;
        }
        getMethod() : string {
          return this.method;
        }
        getId() : number {
          return this.id;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.method) {
                          json.method = this.method;
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.params) {
                          json.params = this.params;
                        }
                        if (this.jsonrpc) {
                          json.jsonrpc = this.jsonrpc;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface OnAppClosedDto {
      setActions(arg0): void;
      withActions(arg0): org.eclipse.che.api.factory.shared.dto.OnAppClosedDto;
      getActions(): Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class OnAppClosedDtoImpl implements org.eclipse.che.api.factory.shared.dto.OnAppClosedDto {

        actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.actions = new Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>();
            if (__jsonObject) {
              if (__jsonObject.actions) {
                  __jsonObject.actions.forEach((item) => {
                  this.actions.push(new org.eclipse.che.api.factory.shared.dto.IdeActionDtoImpl(item));
                  });
              }
            }

    } 

        setActions(actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>) : void {
          this.actions = actions;
        }
        withActions(actions : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto>) : org.eclipse.che.api.factory.shared.dto.OnAppClosedDto {
          this.actions = actions;
          return this;
        }
        getActions() : Array<org.eclipse.che.api.factory.shared.dto.IdeActionDto> {
          return this.actions;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.actions) {
                            let listArray = [];
                            this.actions.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.factory.shared.dto.IdeActionDtoImpl).toJson());
                            json.actions = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface IdeDto {
      withOnAppClosed(arg0): org.eclipse.che.api.factory.shared.dto.IdeDto;
      setOnAppLoaded(arg0): void;
      withOnAppLoaded(arg0): org.eclipse.che.api.factory.shared.dto.IdeDto;
      getOnAppClosed(): org.eclipse.che.api.factory.shared.dto.OnAppClosedDto;
      getOnAppLoaded(): org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto;
      setOnAppClosed(arg0): void;
      getOnProjectsLoaded(): org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto;
      setOnProjectsLoaded(arg0): void;
      withOnProjectsLoaded(arg0): org.eclipse.che.api.factory.shared.dto.IdeDto;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class IdeDtoImpl implements org.eclipse.che.api.factory.shared.dto.IdeDto {

        onAppLoaded : org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto;
        onProjectsLoaded : org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto;
        onAppClosed : org.eclipse.che.api.factory.shared.dto.OnAppClosedDto;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.onAppLoaded) {
                this.onAppLoaded = new org.eclipse.che.api.factory.shared.dto.OnAppLoadedDtoImpl(__jsonObject.onAppLoaded);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.onProjectsLoaded) {
                this.onProjectsLoaded = new org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDtoImpl(__jsonObject.onProjectsLoaded);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.onAppClosed) {
                this.onAppClosed = new org.eclipse.che.api.factory.shared.dto.OnAppClosedDtoImpl(__jsonObject.onAppClosed);
              }
            }

    } 

        withOnAppClosed(onAppClosed : org.eclipse.che.api.factory.shared.dto.OnAppClosedDto) : org.eclipse.che.api.factory.shared.dto.IdeDto {
          this.onAppClosed = onAppClosed;
          return this;
        }
        setOnAppLoaded(onAppLoaded : org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto) : void {
          this.onAppLoaded = onAppLoaded;
        }
        withOnAppLoaded(onAppLoaded : org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto) : org.eclipse.che.api.factory.shared.dto.IdeDto {
          this.onAppLoaded = onAppLoaded;
          return this;
        }
        getOnAppClosed() : org.eclipse.che.api.factory.shared.dto.OnAppClosedDto {
          return this.onAppClosed;
        }
        getOnAppLoaded() : org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto {
          return this.onAppLoaded;
        }
        setOnAppClosed(onAppClosed : org.eclipse.che.api.factory.shared.dto.OnAppClosedDto) : void {
          this.onAppClosed = onAppClosed;
        }
        getOnProjectsLoaded() : org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto {
          return this.onProjectsLoaded;
        }
        setOnProjectsLoaded(onProjectsLoaded : org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto) : void {
          this.onProjectsLoaded = onProjectsLoaded;
        }
        withOnProjectsLoaded(onProjectsLoaded : org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto) : org.eclipse.che.api.factory.shared.dto.IdeDto {
          this.onProjectsLoaded = onProjectsLoaded;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.onAppLoaded) {
                          json.onAppLoaded = (this.onAppLoaded as org.eclipse.che.api.factory.shared.dto.OnAppLoadedDtoImpl).toJson();
                        }
                        if (this.onProjectsLoaded) {
                          json.onProjectsLoaded = (this.onProjectsLoaded as org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDtoImpl).toJson();
                        }
                        if (this.onAppClosed) {
                          json.onAppClosed = (this.onAppClosed as org.eclipse.che.api.factory.shared.dto.OnAppClosedDtoImpl).toJson();
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface MachineDto {
      getConfig(): org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      withLinks(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withStatus(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withId(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withWorkspaceId(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withConfig(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withEnvName(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withOwner(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      withRuntime(arg0): org.eclipse.che.api.machine.shared.dto.MachineDto;
      getRuntime(): org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
      getStatus(): string;
      getWorkspaceId(): string;
      getEnvName(): string;
      getId(): string;
      getOwner(): string;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class MachineDtoImpl implements org.eclipse.che.api.machine.shared.dto.MachineDto {

        owner : string;
        envName : string;
        runtime : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        id : string;
        config : org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
        status : string;
        workspaceId : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.owner) {
                this.owner = __jsonObject.owner;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.envName) {
                this.envName = __jsonObject.envName;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.runtime) {
                this.runtime = new org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDtoImpl(__jsonObject.runtime);
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.config) {
                this.config = new org.eclipse.che.api.machine.shared.dto.MachineConfigDtoImpl(__jsonObject.config);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.status) {
                this.status = __jsonObject.status;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.workspaceId) {
                this.workspaceId = __jsonObject.workspaceId;
              }
            }

    } 

        getConfig() : org.eclipse.che.api.machine.shared.dto.MachineConfigDto {
          return this.config;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.links = links;
          return this;
        }
        withStatus(status : string) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.status = status;
          return this;
        }
        withId(id : string) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.id = id;
          return this;
        }
        withWorkspaceId(workspaceId : string) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.workspaceId = workspaceId;
          return this;
        }
        withConfig(config : org.eclipse.che.api.machine.shared.dto.MachineConfigDto) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.config = config;
          return this;
        }
        withEnvName(envName : string) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.envName = envName;
          return this;
        }
        withOwner(owner : string) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.owner = owner;
          return this;
        }
        withRuntime(runtime : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto) : org.eclipse.che.api.machine.shared.dto.MachineDto {
          this.runtime = runtime;
          return this;
        }
        getRuntime() : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto {
          return this.runtime;
        }
        getStatus() : string {
          return this.status;
        }
        getWorkspaceId() : string {
          return this.workspaceId;
        }
        getEnvName() : string {
          return this.envName;
        }
        getId() : string {
          return this.id;
        }
        getOwner() : string {
          return this.owner;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.owner) {
                          json.owner = this.owner;
                        }
                        if (this.envName) {
                          json.envName = this.envName;
                        }
                        if (this.runtime) {
                          json.runtime = (this.runtime as org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDtoImpl).toJson();
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.config) {
                          json.config = (this.config as org.eclipse.che.api.machine.shared.dto.MachineConfigDtoImpl).toJson();
                        }
                        if (this.status) {
                          json.status = this.status;
                        }
                        if (this.workspaceId) {
                          json.workspaceId = this.workspaceId;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface MergeRequest {
      setCommit(arg0): void;
      withCommit(arg0): org.eclipse.che.api.git.shared.MergeRequest;
      getCommit(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class MergeRequestImpl implements org.eclipse.che.api.git.shared.MergeRequest {

        commit : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.commit) {
                this.commit = __jsonObject.commit;
              }
            }

    } 

        setCommit(commit : string) : void {
          this.commit = commit;
        }
        withCommit(commit : string) : org.eclipse.che.api.git.shared.MergeRequest {
          this.commit = commit;
          return this;
        }
        getCommit() : string {
          return this.commit;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commit) {
                          json.commit = this.commit;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.event {

  export interface MachineProcessEvent {
      getEventType(): string;
      getError(): string;
      setEventType(arg0): void;
      withError(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
      withEventType(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
      getProcessId(): number;
      getMachineId(): string;
      setMachineId(arg0): void;
      withMachineId(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
      setProcessId(arg0): void;
      withProcessId(arg0): org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
      setError(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto.event {

  export class MachineProcessEventImpl implements org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent {

        machineId : string;
        processId : number;
        eventType : string;
        error : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.machineId) {
                this.machineId = __jsonObject.machineId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.processId) {
                this.processId = __jsonObject.processId;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.eventType) {
                this.eventType = __jsonObject.eventType;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.error) {
                this.error = __jsonObject.error;
              }
            }

    } 

        getEventType() : string {
          return this.eventType;
        }
        getError() : string {
          return this.error;
        }
        setEventType(eventType : string) : void {
          this.eventType = eventType;
        }
        withError(error : string) : org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent {
          this.error = error;
          return this;
        }
        withEventType(eventType : string) : org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent {
          this.eventType = eventType;
          return this;
        }
        getProcessId() : number {
          return this.processId;
        }
        getMachineId() : string {
          return this.machineId;
        }
        setMachineId(machineId : string) : void {
          this.machineId = machineId;
        }
        withMachineId(machineId : string) : org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent {
          this.machineId = machineId;
          return this;
        }
        setProcessId(processId : number) : void {
          this.processId = processId;
        }
        withProcessId(processId : number) : org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent {
          this.processId = processId;
          return this;
        }
        setError(error : string) : void {
          this.error = error;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.machineId) {
                          json.machineId = this.machineId;
                        }
                        if (this.processId) {
                          json.processId = this.processId;
                        }
                        if (this.eventType) {
                          json.eventType = this.eventType;
                        }
                        if (this.error) {
                          json.error = this.error;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.ssh.shared.dto {

  export interface SshPairDto {
      setPrivateKey(arg0): void;
      getService(): string;
      getPrivateKey(): string;
      withName(arg0): org.eclipse.che.api.ssh.shared.dto.SshPairDto;
      withPublicKey(arg0): org.eclipse.che.api.ssh.shared.dto.SshPairDto;
      withPrivateKey(arg0): org.eclipse.che.api.ssh.shared.dto.SshPairDto;
      withLinks(arg0): org.eclipse.che.api.ssh.shared.dto.SshPairDto;
      setService(arg0): void;
      withService(arg0): org.eclipse.che.api.ssh.shared.dto.SshPairDto;
      setPublicKey(arg0): void;
      getName(): string;
      setName(arg0): void;
      getPublicKey(): string;
      getLinks(): Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
      setLinks(arg0): void;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.ssh.shared.dto {

  export class SshPairDtoImpl implements org.eclipse.che.api.ssh.shared.dto.SshPairDto {

        privateKey : string;
        service : string;
        name : string;
        links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>;
        publicKey : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.privateKey) {
                this.privateKey = __jsonObject.privateKey;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.service) {
                this.service = __jsonObject.service;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            this.links = new Array<org.eclipse.che.api.core.rest.shared.dto.Link>();
            if (__jsonObject) {
              if (__jsonObject.links) {
                  __jsonObject.links.forEach((item) => {
                  this.links.push(new org.eclipse.che.api.core.rest.shared.dto.LinkImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.publicKey) {
                this.publicKey = __jsonObject.publicKey;
              }
            }

    } 

        setPrivateKey(privateKey : string) : void {
          this.privateKey = privateKey;
        }
        getService() : string {
          return this.service;
        }
        getPrivateKey() : string {
          return this.privateKey;
        }
        withName(name : string) : org.eclipse.che.api.ssh.shared.dto.SshPairDto {
          this.name = name;
          return this;
        }
        withPublicKey(publicKey : string) : org.eclipse.che.api.ssh.shared.dto.SshPairDto {
          this.publicKey = publicKey;
          return this;
        }
        withPrivateKey(privateKey : string) : org.eclipse.che.api.ssh.shared.dto.SshPairDto {
          this.privateKey = privateKey;
          return this;
        }
        withLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : org.eclipse.che.api.ssh.shared.dto.SshPairDto {
          this.links = links;
          return this;
        }
        setService(service : string) : void {
          this.service = service;
        }
        withService(service : string) : org.eclipse.che.api.ssh.shared.dto.SshPairDto {
          this.service = service;
          return this;
        }
        setPublicKey(publicKey : string) : void {
          this.publicKey = publicKey;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getPublicKey() : string {
          return this.publicKey;
        }
        getLinks() : Array<org.eclipse.che.api.core.rest.shared.dto.Link> {
          return this.links;
        }
        setLinks(links : Array<org.eclipse.che.api.core.rest.shared.dto.Link>) : void {
          this.links = links;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.privateKey) {
                          json.privateKey = this.privateKey;
                        }
                        if (this.service) {
                          json.service = this.service;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.links) {
                            let listArray = [];
                            this.links.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.core.rest.shared.dto.LinkImpl).toJson());
                            json.links = listArray;
                            });
                        }
                        if (this.publicKey) {
                          json.publicKey = this.publicKey;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface VariableDto {
      withType(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      setType(arg0): void;
      withName(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      getVariables(): Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
      setVariables(arg0): void;
      withVariables(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      withValue(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      isExistInformation(): boolean;
      setExistInformation(arg0): void;
      withExistInformation(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      getVariablePath(): org.eclipse.che.api.debug.shared.dto.VariablePathDto;
      setVariablePath(arg0): void;
      withVariablePath(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      setPrimitive(arg0): void;
      withPrimitive(arg0): org.eclipse.che.api.debug.shared.dto.VariableDto;
      isPrimitive(): boolean;
      getName(): string;
      getValue(): string;
      setName(arg0): void;
      setValue(arg0): void;
      getType(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class VariableDtoImpl implements org.eclipse.che.api.debug.shared.dto.VariableDto {

        variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>;
        variablePath : org.eclipse.che.api.debug.shared.dto.VariablePathDto;
        primitive : boolean;
        name : string;
        type : string;
        existInformation : boolean;
        value : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.variables = new Array<org.eclipse.che.api.debug.shared.dto.VariableDto>();
            if (__jsonObject) {
              if (__jsonObject.variables) {
                  __jsonObject.variables.forEach((item) => {
                  this.variables.push(new org.eclipse.che.api.debug.shared.dto.VariableDtoImpl(item));
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.variablePath) {
                this.variablePath = new org.eclipse.che.api.debug.shared.dto.VariablePathDtoImpl(__jsonObject.variablePath);
              }
            }
            if (__jsonObject) {
              if (__jsonObject.primitive) {
                this.primitive = __jsonObject.primitive;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.existInformation) {
                this.existInformation = __jsonObject.existInformation;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.value) {
                this.value = __jsonObject.value;
              }
            }

    } 

        withType(type : string) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withName(name : string) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.name = name;
          return this;
        }
        getVariables() : Array<org.eclipse.che.api.debug.shared.dto.VariableDto> {
          return this.variables;
        }
        setVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : void {
          this.variables = variables;
        }
        withVariables(variables : Array<org.eclipse.che.api.debug.shared.dto.VariableDto>) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.variables = variables;
          return this;
        }
        withValue(value : string) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.value = value;
          return this;
        }
        isExistInformation() : boolean {
          return this.existInformation;
        }
        setExistInformation(existInformation : boolean) : void {
          this.existInformation = existInformation;
        }
        withExistInformation(existInformation : boolean) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.existInformation = existInformation;
          return this;
        }
        getVariablePath() : org.eclipse.che.api.debug.shared.dto.VariablePathDto {
          return this.variablePath;
        }
        setVariablePath(variablePath : org.eclipse.che.api.debug.shared.dto.VariablePathDto) : void {
          this.variablePath = variablePath;
        }
        withVariablePath(variablePath : org.eclipse.che.api.debug.shared.dto.VariablePathDto) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.variablePath = variablePath;
          return this;
        }
        setPrimitive(primitive : boolean) : void {
          this.primitive = primitive;
        }
        withPrimitive(primitive : boolean) : org.eclipse.che.api.debug.shared.dto.VariableDto {
          this.primitive = primitive;
          return this;
        }
        isPrimitive() : boolean {
          return this.primitive;
        }
        getName() : string {
          return this.name;
        }
        getValue() : string {
          return this.value;
        }
        setName(name : string) : void {
          this.name = name;
        }
        setValue(value : string) : void {
          this.value = value;
        }
        getType() : string {
          return this.type;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.variables) {
                            let listArray = [];
                            this.variables.forEach((item) => {
                            listArray.push((item as org.eclipse.che.api.debug.shared.dto.VariableDtoImpl).toJson());
                            json.variables = listArray;
                            });
                        }
                        if (this.variablePath) {
                          json.variablePath = (this.variablePath as org.eclipse.che.api.debug.shared.dto.VariablePathDtoImpl).toJson();
                        }
                        if (this.primitive) {
                          json.primitive = this.primitive;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.existInformation) {
                          json.existInformation = this.existInformation;
                        }
                        if (this.value) {
                          json.value = this.value;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface PushResponse {
      getCommandOutput(): string;
      withUpdates(arg0): org.eclipse.che.api.git.shared.PushResponse;
      setUpdates(arg0): void;
      getUpdates(): Array<Map<string,string>>;
      setCommandOutput(arg0): void;
      withCommandOutput(arg0): org.eclipse.che.api.git.shared.PushResponse;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class PushResponseImpl implements org.eclipse.che.api.git.shared.PushResponse {

        commandOutput : string;
        updates : Array<Map<string,string>>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.commandOutput) {
                this.commandOutput = __jsonObject.commandOutput;
              }
            }
            this.updates = new Array<Map<string,string>>();
            if (__jsonObject) {
              if (__jsonObject.updates) {
                  __jsonObject.updates.forEach((item) => {
                  this.updates.push(item);
                  });
              }
            }

    } 

        getCommandOutput() : string {
          return this.commandOutput;
        }
        withUpdates(updates : Array<Map<string,string>>) : org.eclipse.che.api.git.shared.PushResponse {
          this.updates = updates;
          return this;
        }
        setUpdates(updates : Array<Map<string,string>>) : void {
          this.updates = updates;
        }
        getUpdates() : Array<Map<string,string>> {
          return this.updates;
        }
        setCommandOutput(commandOutput : string) : void {
          this.commandOutput = commandOutput;
        }
        withCommandOutput(commandOutput : string) : org.eclipse.che.api.git.shared.PushResponse {
          this.commandOutput = commandOutput;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.commandOutput) {
                          json.commandOutput = this.commandOutput;
                        }
                        if (this.updates) {
                            let listArray = [];
                            this.updates.forEach((item) => {
                            listArray.push(item);
                            json.updates = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export interface ServerDto {
      getUrl(): string;
      setUrl(arg0): void;
      setProtocol(arg0): void;
      setAddress(arg0): void;
      withProperties(arg0): org.eclipse.che.api.machine.shared.dto.ServerDto;
      withRef(arg0): org.eclipse.che.api.machine.shared.dto.ServerDto;
      withProtocol(arg0): org.eclipse.che.api.machine.shared.dto.ServerDto;
      setRef(arg0): void;
      withUrl(arg0): org.eclipse.che.api.machine.shared.dto.ServerDto;
      withAddress(arg0): org.eclipse.che.api.machine.shared.dto.ServerDto;
      getAddress(): string;
      getProperties(): org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;
      setProperties(arg0): void;
      getProtocol(): string;
      getRef(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.machine.shared.dto {

  export class ServerDtoImpl implements org.eclipse.che.api.machine.shared.dto.ServerDto {

        protocol : string;
        ref : string;
        address : string;
        url : string;
        properties : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.protocol) {
                this.protocol = __jsonObject.protocol;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.ref) {
                this.ref = __jsonObject.ref;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.address) {
                this.address = __jsonObject.address;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.url) {
                this.url = __jsonObject.url;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.properties) {
                this.properties = new org.eclipse.che.api.machine.shared.dto.ServerPropertiesDtoImpl(__jsonObject.properties);
              }
            }

    } 

        getUrl() : string {
          return this.url;
        }
        setUrl(url : string) : void {
          this.url = url;
        }
        setProtocol(protocol : string) : void {
          this.protocol = protocol;
        }
        setAddress(address : string) : void {
          this.address = address;
        }
        withProperties(properties : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto) : org.eclipse.che.api.machine.shared.dto.ServerDto {
          this.properties = properties;
          return this;
        }
        withRef(ref : string) : org.eclipse.che.api.machine.shared.dto.ServerDto {
          this.ref = ref;
          return this;
        }
        withProtocol(protocol : string) : org.eclipse.che.api.machine.shared.dto.ServerDto {
          this.protocol = protocol;
          return this;
        }
        setRef(ref : string) : void {
          this.ref = ref;
        }
        withUrl(url : string) : org.eclipse.che.api.machine.shared.dto.ServerDto {
          this.url = url;
          return this;
        }
        withAddress(address : string) : org.eclipse.che.api.machine.shared.dto.ServerDto {
          this.address = address;
          return this;
        }
        getAddress() : string {
          return this.address;
        }
        getProperties() : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto {
          return this.properties;
        }
        setProperties(properties : org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto) : void {
          this.properties = properties;
        }
        getProtocol() : string {
          return this.protocol;
        }
        getRef() : string {
          return this.ref;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.protocol) {
                          json.protocol = this.protocol;
                        }
                        if (this.ref) {
                          json.ref = this.ref;
                        }
                        if (this.address) {
                          json.address = this.address;
                        }
                        if (this.url) {
                          json.url = this.url;
                        }
                        if (this.properties) {
                          json.properties = (this.properties as org.eclipse.che.api.machine.shared.dto.ServerPropertiesDtoImpl).toJson();
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.websocket.shared {

  export interface WebSocketTransmission {
      withMessage(arg0): org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
      withProtocol(arg0): org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
      getMessage(): string;
      getProtocol(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.websocket.shared {

  export class WebSocketTransmissionImpl implements org.eclipse.che.api.core.websocket.shared.WebSocketTransmission {

        protocol : string;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.protocol) {
                this.protocol = __jsonObject.protocol;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        withMessage(message : string) : org.eclipse.che.api.core.websocket.shared.WebSocketTransmission {
          this.message = message;
          return this;
        }
        withProtocol(protocol : string) : org.eclipse.che.api.core.websocket.shared.WebSocketTransmission {
          this.protocol = protocol;
          return this;
        }
        getMessage() : string {
          return this.message;
        }
        getProtocol() : string {
          return this.protocol;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.protocol) {
                          json.protocol = this.protocol;
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface SourceStorageDto {
      setParameters(arg0): void;
      withType(arg0): org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
      setType(arg0): void;
      setLocation(arg0): void;
      withParameters(arg0): org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
      withLocation(arg0): org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
      getLocation(): string;
      getType(): string;
      getParameters(): Map<string,string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class SourceStorageDtoImpl implements org.eclipse.che.api.workspace.shared.dto.SourceStorageDto {

        location : string;
        type : string;
        parameters : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.location) {
                this.location = __jsonObject.location;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            this.parameters = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.parameters) {
                let tmp : Array<any> = Object.keys(__jsonObject.parameters);
                tmp.forEach((key) => {
                  this.parameters.set(key, __jsonObject.parameters[key]);
                 });
              }
            }

    } 

        setParameters(parameters : Map<string,string>) : void {
          this.parameters = parameters;
        }
        withType(type : string) : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        setLocation(location : string) : void {
          this.location = location;
        }
        withParameters(parameters : Map<string,string>) : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto {
          this.parameters = parameters;
          return this;
        }
        withLocation(location : string) : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto {
          this.location = location;
          return this;
        }
        getLocation() : string {
          return this.location;
        }
        getType() : string {
          return this.type;
        }
        getParameters() : Map<string,string> {
          return this.parameters;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.location) {
                          json.location = this.location;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.parameters) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.parameters.entries()) {
                            tmpMap[key] = value;
                           }
                          json.parameters = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface ConflictExceptionError {
      setMessage(arg0): void;
      withMessage(arg0): org.eclipse.che.api.git.shared.ConflictExceptionError;
      getConflictingPaths(): Array<string>;
      withConflictingPaths(arg0): org.eclipse.che.api.git.shared.ConflictExceptionError;
      setConflictingPaths(arg0): void;
      getMessage(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class ConflictExceptionErrorImpl implements org.eclipse.che.api.git.shared.ConflictExceptionError {

        conflictingPaths : Array<string>;
        message : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.conflictingPaths = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.conflictingPaths) {
                  __jsonObject.conflictingPaths.forEach((item) => {
                  this.conflictingPaths.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.message) {
                this.message = __jsonObject.message;
              }
            }

    } 

        setMessage(message : string) : void {
          this.message = message;
        }
        withMessage(message : string) : org.eclipse.che.api.git.shared.ConflictExceptionError {
          this.message = message;
          return this;
        }
        getConflictingPaths() : Array<string> {
          return this.conflictingPaths;
        }
        withConflictingPaths(conflictingPaths : Array<string>) : org.eclipse.che.api.git.shared.ConflictExceptionError {
          this.conflictingPaths = conflictingPaths;
          return this;
        }
        setConflictingPaths(conflictingPaths : Array<string>) : void {
          this.conflictingPaths = conflictingPaths;
        }
        getMessage() : string {
          return this.message;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.conflictingPaths) {
                            let listArray = [];
                            this.conflictingPaths.forEach((item) => {
                            listArray.push(item);
                            json.conflictingPaths = listArray;
                            });
                        }
                        if (this.message) {
                          json.message = this.message;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export interface IdeActionDto {
      setId(arg0): void;
      withId(arg0): org.eclipse.che.api.factory.shared.dto.IdeActionDto;
      withProperties(arg0): org.eclipse.che.api.factory.shared.dto.IdeActionDto;
      getProperties(): Map<string,string>;
      setProperties(arg0): void;
      getId(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.factory.shared.dto {

  export class IdeActionDtoImpl implements org.eclipse.che.api.factory.shared.dto.IdeActionDto {

        id : string;
        properties : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.id) {
                this.id = __jsonObject.id;
              }
            }
            this.properties = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.properties) {
                let tmp : Array<any> = Object.keys(__jsonObject.properties);
                tmp.forEach((key) => {
                  this.properties.set(key, __jsonObject.properties[key]);
                 });
              }
            }

    } 

        setId(id : string) : void {
          this.id = id;
        }
        withId(id : string) : org.eclipse.che.api.factory.shared.dto.IdeActionDto {
          this.id = id;
          return this;
        }
        withProperties(properties : Map<string,string>) : org.eclipse.che.api.factory.shared.dto.IdeActionDto {
          this.properties = properties;
          return this;
        }
        getProperties() : Map<string,string> {
          return this.properties;
        }
        setProperties(properties : Map<string,string>) : void {
          this.properties = properties;
        }
        getId() : string {
          return this.id;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.id) {
                          json.id = this.id;
                        }
                        if (this.properties) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.properties.entries()) {
                            tmpMap[key] = value;
                           }
                          json.properties = tmpMap;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export interface LinkParameter {
      isRequired(): boolean;
      setRequired(arg0): void;
      getDescription(): string;
      setDescription(arg0): void;
      withType(arg0): org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
      setType(arg0): void;
      withDescription(arg0): org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
      setDefaultValue(arg0): void;
      withName(arg0): org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
      withDefaultValue(arg0): org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
      withRequired(arg0): org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
      getValid(): Array<string>;
      withValid(arg0): org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
      setValid(arg0): void;
      getName(): string;
      setName(arg0): void;
      getType(): string;
      getDefaultValue(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.core.rest.shared.dto {

  export class LinkParameterImpl implements org.eclipse.che.api.core.rest.shared.dto.LinkParameter {

        valid : Array<string>;
        defaultValue : string;
        name : string;
        description : string;
        type : string;
        required : boolean;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.valid = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.valid) {
                  __jsonObject.valid.forEach((item) => {
                  this.valid.push(item);
                  });
              }
            }
            if (__jsonObject) {
              if (__jsonObject.defaultValue) {
                this.defaultValue = __jsonObject.defaultValue;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.name) {
                this.name = __jsonObject.name;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.description) {
                this.description = __jsonObject.description;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.type) {
                this.type = __jsonObject.type;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.required) {
                this.required = __jsonObject.required;
              }
            }

    } 

        isRequired() : boolean {
          return this.required;
        }
        setRequired(required : boolean) : void {
          this.required = required;
        }
        getDescription() : string {
          return this.description;
        }
        setDescription(description : string) : void {
          this.description = description;
        }
        withType(type : string) : org.eclipse.che.api.core.rest.shared.dto.LinkParameter {
          this.type = type;
          return this;
        }
        setType(type : string) : void {
          this.type = type;
        }
        withDescription(description : string) : org.eclipse.che.api.core.rest.shared.dto.LinkParameter {
          this.description = description;
          return this;
        }
        setDefaultValue(defaultValue : string) : void {
          this.defaultValue = defaultValue;
        }
        withName(name : string) : org.eclipse.che.api.core.rest.shared.dto.LinkParameter {
          this.name = name;
          return this;
        }
        withDefaultValue(defaultValue : string) : org.eclipse.che.api.core.rest.shared.dto.LinkParameter {
          this.defaultValue = defaultValue;
          return this;
        }
        withRequired(required : boolean) : org.eclipse.che.api.core.rest.shared.dto.LinkParameter {
          this.required = required;
          return this;
        }
        getValid() : Array<string> {
          return this.valid;
        }
        withValid(valid : Array<string>) : org.eclipse.che.api.core.rest.shared.dto.LinkParameter {
          this.valid = valid;
          return this;
        }
        setValid(valid : Array<string>) : void {
          this.valid = valid;
        }
        getName() : string {
          return this.name;
        }
        setName(name : string) : void {
          this.name = name;
        }
        getType() : string {
          return this.type;
        }
        getDefaultValue() : string {
          return this.defaultValue;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.valid) {
                            let listArray = [];
                            this.valid.forEach((item) => {
                            listArray.push(item);
                            json.valid = listArray;
                            });
                        }
                        if (this.defaultValue) {
                          json.defaultValue = this.defaultValue;
                        }
                        if (this.name) {
                          json.name = this.name;
                        }
                        if (this.description) {
                          json.description = this.description;
                        }
                        if (this.type) {
                          json.type = this.type;
                        }
                        if (this.required) {
                          json.required = this.required;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export interface VariablePathDto {
      setPath(arg0): void;
      withPath(arg0): org.eclipse.che.api.debug.shared.dto.VariablePathDto;
      getPath(): Array<string>;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.debug.shared.dto {

  export class VariablePathDtoImpl implements org.eclipse.che.api.debug.shared.dto.VariablePathDto {

        path : Array<string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            this.path = new Array<string>();
            if (__jsonObject) {
              if (__jsonObject.path) {
                  __jsonObject.path.forEach((item) => {
                  this.path.push(item);
                  });
              }
            }

    } 

        setPath(path : Array<string>) : void {
          this.path = path;
        }
        withPath(path : Array<string>) : org.eclipse.che.api.debug.shared.dto.VariablePathDto {
          this.path = path;
          return this;
        }
        getPath() : Array<string> {
          return this.path;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.path) {
                            let listArray = [];
                            this.path.forEach((item) => {
                            listArray.push(item);
                            json.path = listArray;
                            });
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.git.shared {

  export interface PullRequest {
      setTimeout(arg0): void;
      getTimeout(): number;
      setPassword(arg0): void;
      getUsername(): string;
      getPassword(): string;
      setUsername(arg0): void;
      setRemote(arg0): void;
      withRemote(arg0): org.eclipse.che.api.git.shared.PullRequest;
      withPassword(arg0): org.eclipse.che.api.git.shared.PullRequest;
      getRefSpec(): string;
      setRefSpec(arg0): void;
      withRefSpec(arg0): org.eclipse.che.api.git.shared.PullRequest;
      getRemote(): string;
      withTimeout(arg0): org.eclipse.che.api.git.shared.PullRequest;
      withUsername(arg0): org.eclipse.che.api.git.shared.PullRequest;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.git.shared {

  export class PullRequestImpl implements org.eclipse.che.api.git.shared.PullRequest {

        password : string;
        refSpec : string;
        remote : string;
        timeout : number;
        username : string;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.password) {
                this.password = __jsonObject.password;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.refSpec) {
                this.refSpec = __jsonObject.refSpec;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.remote) {
                this.remote = __jsonObject.remote;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.timeout) {
                this.timeout = __jsonObject.timeout;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.username) {
                this.username = __jsonObject.username;
              }
            }

    } 

        setTimeout(timeout : number) : void {
          this.timeout = timeout;
        }
        getTimeout() : number {
          return this.timeout;
        }
        setPassword(password : string) : void {
          this.password = password;
        }
        getUsername() : string {
          return this.username;
        }
        getPassword() : string {
          return this.password;
        }
        setUsername(username : string) : void {
          this.username = username;
        }
        setRemote(remote : string) : void {
          this.remote = remote;
        }
        withRemote(remote : string) : org.eclipse.che.api.git.shared.PullRequest {
          this.remote = remote;
          return this;
        }
        withPassword(password : string) : org.eclipse.che.api.git.shared.PullRequest {
          this.password = password;
          return this;
        }
        getRefSpec() : string {
          return this.refSpec;
        }
        setRefSpec(refSpec : string) : void {
          this.refSpec = refSpec;
        }
        withRefSpec(refSpec : string) : org.eclipse.che.api.git.shared.PullRequest {
          this.refSpec = refSpec;
          return this;
        }
        getRemote() : string {
          return this.remote;
        }
        withTimeout(timeout : number) : org.eclipse.che.api.git.shared.PullRequest {
          this.timeout = timeout;
          return this;
        }
        withUsername(username : string) : org.eclipse.che.api.git.shared.PullRequest {
          this.username = username;
          return this;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.password) {
                          json.password = this.password;
                        }
                        if (this.refSpec) {
                          json.refSpec = this.refSpec;
                        }
                        if (this.remote) {
                          json.remote = this.remote;
                        }
                        if (this.timeout) {
                          json.timeout = this.timeout;
                        }
                        if (this.username) {
                          json.username = this.username;
                        }

        return json;
      }
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export interface ServerConf2Dto {
      setProtocol(arg0): void;
      setPort(arg0): void;
      withProperties(arg0): org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto;
      withPort(arg0): org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto;
      withProtocol(arg0): org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto;
      getProperties(): Map<string,string>;
      setProperties(arg0): void;
      getProtocol(): string;
      getPort(): string;

  toJson() : any;
  } 
} 


export module org.eclipse.che.api.workspace.shared.dto {

  export class ServerConf2DtoImpl implements org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto {

        protocol : string;
        port : string;
        properties : Map<string,string>;

    __jsonObject : any;

    constructor(__jsonObject?: any) {
      this.__jsonObject = __jsonObject;
            if (__jsonObject) {
              if (__jsonObject.protocol) {
                this.protocol = __jsonObject.protocol;
              }
            }
            if (__jsonObject) {
              if (__jsonObject.port) {
                this.port = __jsonObject.port;
              }
            }
            this.properties = new Map<string,string>();
            if (__jsonObject) {
              if (__jsonObject.properties) {
                let tmp : Array<any> = Object.keys(__jsonObject.properties);
                tmp.forEach((key) => {
                  this.properties.set(key, __jsonObject.properties[key]);
                 });
              }
            }

    } 

        setProtocol(protocol : string) : void {
          this.protocol = protocol;
        }
        setPort(port : string) : void {
          this.port = port;
        }
        withProperties(properties : Map<string,string>) : org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto {
          this.properties = properties;
          return this;
        }
        withPort(port : string) : org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto {
          this.port = port;
          return this;
        }
        withProtocol(protocol : string) : org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto {
          this.protocol = protocol;
          return this;
        }
        getProperties() : Map<string,string> {
          return this.properties;
        }
        setProperties(properties : Map<string,string>) : void {
          this.properties = properties;
        }
        getProtocol() : string {
          return this.protocol;
        }
        getPort() : string {
          return this.port;
        }
 
      toJson() : any {
        let json : any = {};

                        if (this.protocol) {
                          json.protocol = this.protocol;
                        }
                        if (this.port) {
                          json.port = this.port;
                        }
                        if (this.properties) {
                          let tmpMap : any = {};
                          for (const [key, value] of this.properties.entries()) {
                            tmpMap[key] = value;
                           }
                          json.properties = tmpMap;
                        }

        return json;
      }
  } 
} 
