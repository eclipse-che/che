import { Container } from "inversify";
import { Driver } from "./driver/Driver";
import { TYPES, CLASSES } from "./types";
import { ChromeDriver } from "./driver/ChromeDriver";
import { DriverHelper } from "./utils/DriverHelper";
import { LoginPage } from "./pageobjects/login/LoginPage";
import { SingleUserLoginPage } from "./pageobjects/login/SingleUserLoginPage";
import { Dashboard } from "./pageobjects/dashboard/Dashboard";
import { Workspaces } from "./pageobjects/dashboard/Workspaces";
import { NewWorkspace } from "./pageobjects/dashboard/NewWorkspace";
import { WorkspaceDetails } from "./pageobjects/dashboard/workspace-details/WorkspaceDetails";
import { WorkspaceDetailsPlugins } from "./pageobjects/dashboard/workspace-details/WorkspaceDetailsPlugins";
import { Ide } from "./pageobjects/ide/Ide";
import { TestWorkspaceUtil } from "./utils/workspace/TestWorkspaceUtil";
import { ProjectTree } from "./pageobjects/ide/ProjectTree";

const e2eContainer = new Container();

e2eContainer.bind<Driver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
e2eContainer.bind<LoginPage>(TYPES.LoginPage).to(SingleUserLoginPage).inSingletonScope();

e2eContainer.bind<DriverHelper>(CLASSES.DriverHelper).to(DriverHelper).inSingletonScope();
e2eContainer.bind<Dashboard>(CLASSES.Dashboard).to(Dashboard).inSingletonScope();
e2eContainer.bind<Workspaces>(CLASSES.Workspaces).to(Workspaces).inSingletonScope();
e2eContainer.bind<NewWorkspace>(CLASSES.NewWorkspace).to(NewWorkspace).inSingletonScope();
e2eContainer.bind<WorkspaceDetails>(CLASSES.WorkspaceDetails).to(WorkspaceDetails).inSingletonScope();
e2eContainer.bind<WorkspaceDetailsPlugins>(CLASSES.WorkspaceDetailsPlugins).to(WorkspaceDetailsPlugins).inSingletonScope();
e2eContainer.bind<Ide>(CLASSES.Ide).to(Ide).inSingletonScope();
e2eContainer.bind<TestWorkspaceUtil>(CLASSES.TestWorkspaceUtil).to(TestWorkspaceUtil).inSingletonScope();
e2eContainer.bind<ProjectTree>(CLASSES.ProjectTree).to(ProjectTree).inSingletonScope();



export { e2eContainer }
