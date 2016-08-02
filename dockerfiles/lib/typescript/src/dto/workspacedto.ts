export class WorkspaceDto {

    id: string;
    content: any;


    constructor(workspaceObject: any) {
        this.content = workspaceObject;
    }

    getId() : string {
        return this.content.id;
    }

    getName() : string {
        return this.content.config.name;
    }
    getContent() : any {
        return this.content;
    }
}