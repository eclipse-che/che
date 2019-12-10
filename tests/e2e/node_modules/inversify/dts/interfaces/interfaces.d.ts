declare namespace interfaces {
    type BindingScope = "Singleton" | "Transient" | "Request";
    type BindingType = "ConstantValue" | "Constructor" | "DynamicValue" | "Factory" | "Function" | "Instance" | "Invalid" | "Provider";
    type TargetType = "ConstructorArgument" | "ClassProperty" | "Variable";
    interface BindingScopeEnum {
        Request: interfaces.BindingScope;
        Singleton: interfaces.BindingScope;
        Transient: interfaces.BindingScope;
    }
    interface BindingTypeEnum {
        ConstantValue: interfaces.BindingType;
        Constructor: interfaces.BindingType;
        DynamicValue: interfaces.BindingType;
        Factory: interfaces.BindingType;
        Function: interfaces.BindingType;
        Instance: interfaces.BindingType;
        Invalid: interfaces.BindingType;
        Provider: interfaces.BindingType;
    }
    interface TargetTypeEnum {
        ConstructorArgument: interfaces.TargetType;
        ClassProperty: interfaces.TargetType;
        Variable: interfaces.TargetType;
    }
    interface Newable<T> {
        new (...args: any[]): T;
    }
    interface Abstract<T> {
        prototype: T;
    }
    type ServiceIdentifier<T> = (string | symbol | Newable<T> | Abstract<T>);
    interface Clonable<T> {
        clone(): T;
    }
    interface Binding<T> extends Clonable<Binding<T>> {
        id: number;
        moduleId: string;
        activated: boolean;
        serviceIdentifier: ServiceIdentifier<T>;
        constraint: ConstraintFunction;
        dynamicValue: ((context: interfaces.Context) => T) | null;
        scope: BindingScope;
        type: BindingType;
        implementationType: Newable<T> | null;
        factory: FactoryCreator<any> | null;
        provider: ProviderCreator<any> | null;
        onActivation: ((context: interfaces.Context, injectable: T) => T) | null;
        cache: T | null;
    }
    type Factory<T> = (...args: any[]) => (((...args: any[]) => T) | T);
    type FactoryCreator<T> = (context: Context) => Factory<T>;
    type Provider<T> = (...args: any[]) => (((...args: any[]) => Promise<T>) | Promise<T>);
    type ProviderCreator<T> = (context: Context) => Provider<T>;
    interface NextArgs {
        avoidConstraints: boolean;
        contextInterceptor: ((contexts: Context) => Context);
        isMultiInject: boolean;
        targetType: TargetType;
        serviceIdentifier: interfaces.ServiceIdentifier<any>;
        key?: string | number | symbol;
        value?: any;
    }
    type Next = (args: NextArgs) => (any | any[]);
    type Middleware = (next: Next) => Next;
    type ContextInterceptor = (context: interfaces.Context) => interfaces.Context;
    interface Context {
        id: number;
        container: Container;
        plan: Plan;
        currentRequest: Request;
        addPlan(plan: Plan): void;
        setCurrentRequest(request: Request): void;
    }
    interface ReflectResult {
        [key: string]: Metadata[];
    }
    interface Metadata {
        key: string | number | symbol;
        value: any;
    }
    interface Plan {
        parentContext: Context;
        rootRequest: Request;
    }
    interface QueryableString {
        startsWith(searchString: string): boolean;
        endsWith(searchString: string): boolean;
        contains(searchString: string): boolean;
        equals(compareString: string): boolean;
        value(): string;
    }
    type ResolveRequestHandler = (request: interfaces.Request) => any;
    type RequestScope = Map<any, any> | null;
    interface Request {
        id: number;
        serviceIdentifier: ServiceIdentifier<any>;
        parentContext: Context;
        parentRequest: Request | null;
        childRequests: Request[];
        target: Target;
        bindings: Binding<any>[];
        requestScope: RequestScope;
        addChildRequest(serviceIdentifier: ServiceIdentifier<any>, bindings: (Binding<any> | Binding<any>[]), target: Target): Request;
    }
    interface Target {
        id: number;
        serviceIdentifier: ServiceIdentifier<any>;
        type: TargetType;
        name: QueryableString;
        metadata: Metadata[];
        getNamedTag(): interfaces.Metadata | null;
        getCustomTags(): interfaces.Metadata[] | null;
        hasTag(key: string | number | symbol): boolean;
        isArray(): boolean;
        matchesArray(name: interfaces.ServiceIdentifier<any>): boolean;
        isNamed(): boolean;
        isTagged(): boolean;
        isOptional(): boolean;
        matchesNamedTag(name: string): boolean;
        matchesTag(key: string | number | symbol): (value: any) => boolean;
    }
    interface ContainerOptions {
        autoBindInjectable?: boolean;
        defaultScope?: BindingScope;
        skipBaseClassChecks?: boolean;
    }
    interface Container {
        id: number;
        parent: Container | null;
        options: ContainerOptions;
        bind<T>(serviceIdentifier: ServiceIdentifier<T>): BindingToSyntax<T>;
        rebind<T>(serviceIdentifier: interfaces.ServiceIdentifier<T>): interfaces.BindingToSyntax<T>;
        unbind(serviceIdentifier: ServiceIdentifier<any>): void;
        unbindAll(): void;
        isBound(serviceIdentifier: ServiceIdentifier<any>): boolean;
        isBoundNamed(serviceIdentifier: ServiceIdentifier<any>, named: string | number | symbol): boolean;
        isBoundTagged(serviceIdentifier: ServiceIdentifier<any>, key: string | number | symbol, value: any): boolean;
        get<T>(serviceIdentifier: ServiceIdentifier<T>): T;
        getNamed<T>(serviceIdentifier: ServiceIdentifier<T>, named: string | number | symbol): T;
        getTagged<T>(serviceIdentifier: ServiceIdentifier<T>, key: string | number | symbol, value: any): T;
        getAll<T>(serviceIdentifier: ServiceIdentifier<T>): T[];
        resolve<T>(constructorFunction: interfaces.Newable<T>): T;
        load(...modules: ContainerModule[]): void;
        loadAsync(...modules: AsyncContainerModule[]): Promise<void>;
        unload(...modules: ContainerModule[]): void;
        applyCustomMetadataReader(metadataReader: MetadataReader): void;
        applyMiddleware(...middleware: Middleware[]): void;
        snapshot(): void;
        restore(): void;
        createChild(): Container;
    }
    type Bind = <T>(serviceIdentifier: ServiceIdentifier<T>) => BindingToSyntax<T>;
    type Rebind = <T>(serviceIdentifier: ServiceIdentifier<T>) => BindingToSyntax<T>;
    type Unbind = <T>(serviceIdentifier: ServiceIdentifier<T>) => void;
    type IsBound = <T>(serviceIdentifier: ServiceIdentifier<T>) => boolean;
    interface ContainerModule {
        id: number;
        registry: ContainerModuleCallBack;
    }
    interface AsyncContainerModule {
        id: number;
        registry: AsyncContainerModuleCallBack;
    }
    type ContainerModuleCallBack = (bind: interfaces.Bind, unbind: interfaces.Unbind, isBound: interfaces.IsBound, rebind: interfaces.Rebind) => void;
    type AsyncContainerModuleCallBack = (bind: interfaces.Bind, unbind: interfaces.Unbind, isBound: interfaces.IsBound, rebind: interfaces.Rebind) => Promise<void>;
    interface ContainerSnapshot {
        bindings: Lookup<Binding<any>>;
        middleware: Next | null;
    }
    interface Lookup<T> extends Clonable<Lookup<T>> {
        add(serviceIdentifier: ServiceIdentifier<any>, value: T): void;
        getMap(): Map<interfaces.ServiceIdentifier<any>, T[]>;
        get(serviceIdentifier: ServiceIdentifier<any>): T[];
        remove(serviceIdentifier: interfaces.ServiceIdentifier<any>): void;
        removeByCondition(condition: (item: T) => boolean): void;
        hasKey(serviceIdentifier: ServiceIdentifier<any>): boolean;
        clone(): Lookup<T>;
        traverse(func: (key: interfaces.ServiceIdentifier<any>, value: T[]) => void): void;
    }
    interface BindingOnSyntax<T> {
        onActivation(fn: (context: Context, injectable: T) => T): BindingWhenSyntax<T>;
    }
    interface BindingWhenSyntax<T> {
        when(constraint: (request: Request) => boolean): BindingOnSyntax<T>;
        whenTargetNamed(name: string | number | symbol): BindingOnSyntax<T>;
        whenTargetIsDefault(): BindingOnSyntax<T>;
        whenTargetTagged(tag: string | number | symbol, value: any): BindingOnSyntax<T>;
        whenInjectedInto(parent: (Function | string)): BindingOnSyntax<T>;
        whenParentNamed(name: string | number | symbol): BindingOnSyntax<T>;
        whenParentTagged(tag: string | number | symbol, value: any): BindingOnSyntax<T>;
        whenAnyAncestorIs(ancestor: (Function | string)): BindingOnSyntax<T>;
        whenNoAncestorIs(ancestor: (Function | string)): BindingOnSyntax<T>;
        whenAnyAncestorNamed(name: string | number | symbol): BindingOnSyntax<T>;
        whenAnyAncestorTagged(tag: string | number | symbol, value: any): BindingOnSyntax<T>;
        whenNoAncestorNamed(name: string | number | symbol): BindingOnSyntax<T>;
        whenNoAncestorTagged(tag: string | number | symbol, value: any): BindingOnSyntax<T>;
        whenAnyAncestorMatches(constraint: (request: Request) => boolean): BindingOnSyntax<T>;
        whenNoAncestorMatches(constraint: (request: Request) => boolean): BindingOnSyntax<T>;
    }
    interface BindingWhenOnSyntax<T> extends BindingWhenSyntax<T>, BindingOnSyntax<T> {
    }
    interface BindingInSyntax<T> {
        inSingletonScope(): BindingWhenOnSyntax<T>;
        inTransientScope(): BindingWhenOnSyntax<T>;
        inRequestScope(): BindingWhenOnSyntax<T>;
    }
    interface BindingInWhenOnSyntax<T> extends BindingInSyntax<T>, BindingWhenOnSyntax<T> {
    }
    interface BindingToSyntax<T> {
        to(constructor: {
            new (...args: any[]): T;
        }): BindingInWhenOnSyntax<T>;
        toSelf(): BindingInWhenOnSyntax<T>;
        toConstantValue(value: T): BindingWhenOnSyntax<T>;
        toDynamicValue(func: (context: Context) => T): BindingInWhenOnSyntax<T>;
        toConstructor<T2>(constructor: Newable<T2>): BindingWhenOnSyntax<T>;
        toFactory<T2>(factory: FactoryCreator<T2>): BindingWhenOnSyntax<T>;
        toFunction(func: T): BindingWhenOnSyntax<T>;
        toAutoFactory<T2>(serviceIdentifier: ServiceIdentifier<T2>): BindingWhenOnSyntax<T>;
        toProvider<T2>(provider: ProviderCreator<T2>): BindingWhenOnSyntax<T>;
        toService(service: ServiceIdentifier<T>): void;
    }
    interface ConstraintFunction extends Function {
        metaData?: Metadata;
        (request: Request | null): boolean;
    }
    interface MetadataReader {
        getConstructorMetadata(constructorFunc: Function): ConstructorMetadata;
        getPropertiesMetadata(constructorFunc: Function): MetadataMap;
    }
    interface MetadataMap {
        [propertyNameOrArgumentIndex: string]: Metadata[];
    }
    interface ConstructorMetadata {
        compilerGeneratedMetadata: Function[] | undefined;
        userGeneratedMetadata: MetadataMap;
    }
}
export { interfaces };
