/// <reference types="Cypress" />

export class TestWorkspace{
    private static readonly API_ENDPOINT: string = Cypress.env('api_endpoint');
    private static readonly WORKSPACE_API_URL: string = TestWorkspace.API_ENDPOINT + "workspace";

    private workspaceName: string = "";
    private workspaceId: string = "";
    private workspaceIdeUrl: string = "";

    constructor (workspaceName: string){

        this.workspaceName = workspaceName;
        this.createWorkspace(workspaceName);
    }

    private createWorkspace(workspaceName: string){
        it("Do workspace creation request", ()=> {
            cy.fixture('workspace/che-7-preview.json').then( workspaceJson => {

                workspaceJson.name = workspaceName;

                cy.request('POST', TestWorkspace.WORKSPACE_API_URL, workspaceJson);     
            }).then(response => {
                let responceData = response.body;
                this.workspaceId = responceData.id;
                this.workspaceIdeUrl = responceData.links.ide;
                })
            })
        
        this.startWorkspace();    
    };
        
    getName(): string{
        return this.workspaceName;
    }
    
    getId(): string{
        return this.workspaceId;
    }

    getIdeUrl(): string{
        return this.workspaceIdeUrl;
    }

    startWorkspace(){
        it(`Start the \" ${this.workspaceName} \" workspace`, ()=>{
            let workspaceApiUrl: string = `${TestWorkspace.API_ENDPOINT}workspace/${this.getId()}/runtime`;
            
            cy.request('POST', workspaceApiUrl);
        })
    }

    openWorkspaceIde(){
        it(`Open workspace IDE`, ()=>{
            cy.visit(this.workspaceIdeUrl);
        })
    }

    deleteWorkspace(){
        it(`Delete the \" ${this.workspaceName} \" workspace with id \" ${this.workspaceId} \"`, ()=>{
            let workspaceApiUrl: string = `${TestWorkspace.API_ENDPOINT}workspace/${this.getId()}`;
            
            cy.request('DELETE', workspaceApiUrl);
        })
    }

}