declare function tagged(metadataKey: string | number | symbol, metadataValue: any): (target: any, targetKey: string, index?: number | undefined) => void;
export { tagged };
