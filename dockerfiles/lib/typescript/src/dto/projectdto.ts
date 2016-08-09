export class ProjectDto {

    content: any;


    constructor(projectObject: any) {
        this.content = projectObject;
    }

    getContent() : any {
        return this.content;
    }
}