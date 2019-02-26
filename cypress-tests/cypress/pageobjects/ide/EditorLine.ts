export class EditorLine{
    private linePixelsCoordinate: number;
    private lineText: string;

    constructor(linePixelsCoordinate: number, lineText: string){
        this.linePixelsCoordinate = linePixelsCoordinate;
        this.lineText = lineText;
    }
    
    getLinePixelsCoordinate(): number {
        return this.linePixelsCoordinate;
    }

    getLineText(): string {
        return this.lineText;
    }
}