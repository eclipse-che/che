# Eclipse Che — Candidate Issues to Close as Obsolete

_Generated 2026-09-04 against the [eclipse-che/che](https://github.com/eclipse-che/che/issues) open issue tracker (212 open issues, excluding PRs)._

## How this list was built

The recommendations below are anchored to Eclipse Che components that have since been **removed or deprecated**. An issue is a strong "obsolete" candidate when it is scoped entirely to one of those components:

| Component | Status | Signal used |
|---|---|---|
| **Che-Theia** editor | Removed — replaced by **Che-Code** (Microsoft VS Code – Open Source) | label `area/editor/theia`, "che-theia"/"Theia" in title |
| **Che Plugin Registry** + `meta.yaml` / plugin-broker / sidecar plugin model | Deprecated in favor of **Open VSX** (see active tracking issue #23816) | label `area/plugin-registry` |
| **Che Devfile Registry (v1)** + built-in "stacks" | Superseded by Devfile 2.x + DevWorkspaces + user-provided samples | label `area/devfile-registry` |
| **Projector**-based JetBrains/Android Studio editors | Projector discontinued by JetBrains | title mention |
| Pre-DevWorkspace engine (`wsmaster` / workspace-controller CRDs) | Replaced by the **DevWorkspace Operator** | title mention |

Almost all of these are already labeled `lifecycle/frozen` (173 of 212 open issues carry it) and have had **no activity since 2020–2023**.

> ⚠️ These are recommendations for maintainer review, not an automated close list. A small "borderline" section flags items whose underlying concern may still apply to the current Che-Code editor and should be re-triaged rather than closed blindly.

> **Review status (2026-09-04):** Group 1 (Che-Theia) has been actioned — **28 of 30 issues are now closed**. Two remain open ([#18302](https://github.com/eclipse-che/che/issues/18302), [#17196](https://github.com/eclipse-che/che/issues/17196)). Groups 2–5 and the borderline set are still open and awaiting maintainer review.

---

## 1. Che-Theia editor (removed) — 30 issues — ✅ 28 closed (reviewed 2026-09-04), 2 still open

The Che-Theia editor no longer exists; the default editor is Che-Code. Issues describing Che-Theia behavior, its build/self-hosting, or its plugin/webview/welcome UI cannot be reproduced or actioned.

| # | Last activity | Title | Status |
|---|---|---|---|
| [#15976](https://github.com/eclipse-che/che/issues/15976) | 2020-09 | Move che-theia plugins to dedicate repository and optimize build flow | ✅ Closed |
| [#13965](https://github.com/eclipse-che/che/issues/13965) | 2020-10 | Not language/machine dependent keybinding information for quick fix | ✅ Closed |
| [#16754](https://github.com/eclipse-che/che/issues/16754) | 2020-10 | A che task should reuse the same terminal | ✅ Closed |
| [#17587](https://github.com/eclipse-che/che/issues/17587) | 2021-02 | Volume for Theia data directory missing (plugin settings permanence) | ✅ Closed |
| [#17629](https://github.com/eclipse-che/che/issues/17629) | 2021-03 | Add documentation link in Che Theia Help menu | ✅ Closed |
| [#18314](https://github.com/eclipse-che/che/issues/18314) | 2021-05 | Simplify clone notifications on che-theia | ✅ Closed |
| [#18556](https://github.com/eclipse-che/che/issues/18556) | 2021-06 | Better error message when Che API is not reachable from Che Theia | ✅ Closed |
| [#18598](https://github.com/eclipse-che/che/issues/18598) | 2021-06 | Logging to the console from a plug-in may cause cyclic messaging | ✅ Closed |
| [#18937](https://github.com/eclipse-che/che/issues/18937) | 2021-08 | Yarn-Linked Packages may not work in Che-Theia | ✅ Closed |
| [#20477](https://github.com/eclipse-che/che/issues/20477) | 2022-09 | Folder preference settings are ignored | ✅ Closed |
| [#16085](https://github.com/eclipse-che/che/issues/16085) | 2022-12 | LSP features does not work in another directory | ✅ Closed |
| [#18302](https://github.com/eclipse-che/che/issues/18302) | 2022-12 | Che-theia should notify/disable actions + inform user how to restart | 🔲 **Still open** |
| [#17196](https://github.com/eclipse-che/che/issues/17196) | 2022-12 | Plugins view eats characters in Filter Field when under load | 🔲 **Still open** |
| [#15808](https://github.com/eclipse-che/che/issues/15808) | 2022-12 | Command palette skips steps upon focus loss | ✅ Closed |
| [#15160](https://github.com/eclipse-che/che/issues/15160) | 2022-12 | Inconvenient self-hosting for external contributors | ✅ Closed |
| [#15007](https://github.com/eclipse-che/che/issues/15007) | 2022-12 | Git support cannot handle che-theia self-hosting structure | ✅ Closed |
| [#13249](https://github.com/eclipse-che/che/issues/13249) | 2022-12 | Make it possible to debug Theia plugins in sidecars | ✅ Closed |
| [#13729](https://github.com/eclipse-che/che/issues/13729) | 2022-12 | Include source map into theia image by default | ✅ Closed |
| [#18210](https://github.com/eclipse-che/che/issues/18210) | 2023-01 | Add instructions about how to run tasks in che-theia welcome page | ✅ Closed |
| [#16945](https://github.com/eclipse-che/che/issues/16945) | 2021-04 | Language server not starting correctly (VS Code globalstorage volume) | ✅ Closed |
| [#16269](https://github.com/eclipse-che/che/issues/16269) | 2021-02 | Allow git repository switch from the git menu (che-theia dialog) | ✅ Closed |
| [#17232](https://github.com/eclipse-che/che/issues/17232) | 2022-12 | Adapt Welcome plugin to new WebView plugin API | ✅ Closed |
| [#13666](https://github.com/eclipse-che/che/issues/13666) | 2021-02 | Workspace panel is not getting updated with newly added task | ✅ Closed |
| [#15566](https://github.com/eclipse-che/che/issues/15566) | 2022-12 | Plugin commands are not in workspace panel | ✅ Closed |
| [#14694](https://github.com/eclipse-che/che/issues/14694) | 2020-09 | Open terminal in new browser window | ✅ Closed |
| [#19942](https://github.com/eclipse-che/che/issues/19942) | 2021-12 | Display running workspace container logs in the che-theia output view | ✅ Closed |
| [#20282](https://github.com/eclipse-che/che/issues/20282) | 2022-12 | Custom Theia Editor Support in Eclipse Che | ✅ Closed |
| [#16852](https://github.com/eclipse-che/che/issues/16852) | 2021-12 | Rework che-theia build process to not use source linking | ✅ Closed |
| [#16663](https://github.com/eclipse-che/che/issues/16663) | 2021-06 | [WIP] Indispensable VS Code extensions (che-theia bundling) | ✅ Closed |
| [#17328](https://github.com/eclipse-che/che/issues/17328) | 2021-01 | Sync VS Code built-ins with upstream Theia automatically | ✅ Closed |

---

## 2. Che Plugin Registry + `meta.yaml` / plugin-broker / sidecar model (deprecated → Open VSX) — 20 issues (all still open)

The `meta.yaml` plugin format, plugin broker, and per-plugin sidecar images are gone; extensions are consumed from Open VSX. The plugin-registry repository is itself being archived (#23816 — **keep that one, it is the active tracking issue**).

| # | Last activity | Title |
|---|---|---|
| [#15336](https://github.com/eclipse-che/che/issues/15336) | 2020-06 | Create Che Plugin Registry starter |
| [#16185](https://github.com/eclipse-che/che/issues/16185) | 2020-08 | cloud-shell needs to be updated to 7.6.0 |
| [#16187](https://github.com/eclipse-che/che/issues/16187) | 2020-08 | theia-dev needs to be updated to 0.0.3 |
| [#16188](https://github.com/eclipse-che/che/issues/16188) | 2020-08 | code-server needs to be updated to 2.1698-vsc1.41.1 |
| [#15281](https://github.com/eclipse-che/che/issues/15281) | 2020-08 | Use Che Bot to provide factory link to PR in che-plugin-registry |
| [#16953](https://github.com/eclipse-che/che/issues/16953) | 2021-01 | Include MongoDB for VS Code in plugin registry |
| [#13555](https://github.com/eclipse-che/che/issues/13555) | 2021-01 | Yaml plugin should not need to run inside its own sidecar container |
| [#16784](https://github.com/eclipse-che/che/issues/16784) | 2021-01 | We have three java plugins and it's unclear which one should be used |
| [#13836](https://github.com/eclipse-che/che/issues/13836) | 2021-01 | Have a nice looking page in the default che plugin registry |
| [#16723](https://github.com/eclipse-che/che/issues/16723) | 2021-02 | Support Jupyter Notebooks natively in Che via Python plugin |
| [#17640](https://github.com/eclipse-che/che/issues/17640) | 2021-02 | README and CONTRIBUTE files in che-plugin-registry need improving |
| [#17737](https://github.com/eclipse-che/che/issues/17737) | 2021-02 | Plugins used in devfiles lead to duplicate images |
| [#17679](https://github.com/eclipse-che/che/issues/17679) | 2021-02 | Path to plugin not changed during --offline build due to missing tag |
| [#15376](https://github.com/eclipse-che/che/issues/15376) | 2021-04 | [meta.yaml / Che plug-ins]: Add dependencies section |
| [#18363](https://github.com/eclipse-che/che/issues/18363) | 2021-05 | Handle dependencies between sidecars in plugin-registry |
| [#18364](https://github.com/eclipse-che/che/issues/18364) | 2021-05 | Share build-time resources between sidecars |
| [#19051](https://github.com/eclipse-che/che/issues/19051) | 2021-09 | Publish rhel versions of che-plugin-registry image |
| [#15966](https://github.com/eclipse-che/che/issues/15966) | 2021-10 | Che plugins dependencies resolution |
| [#16258](https://github.com/eclipse-che/che/issues/16258) | 2021-01 | Allow multiple plugin registries to be added to Che |
| [#15272](https://github.com/eclipse-che/che/issues/15272) | 2023-01 | Use plug-ins of extension packs for all default stacks |

---

## 3. Che Devfile Registry (v1) + built-in "stacks" (superseded by Devfile 2.x / DevWorkspaces) — 8 issues — all still open

The v1 devfile registry and curated "stacks" model has been replaced by Devfile 2.x, DevWorkspaces, and user-supplied samples.

| # | Last activity | Title |
|---|---|---|
| [#15898](https://github.com/eclipse-che/che/issues/15898) | 2020-10 | Consider pinning non che-sample projects in devfile registry to a commit |
| [#14691](https://github.com/eclipse-che/che/issues/14691) | 2020-10 | Adding a devfile to devfile registry at runtime doesn't work |
| [#14596](https://github.com/eclipse-che/che/issues/14596) | 2021-02 | Create a devfile registry for all che-in-che flow |
| [#18145](https://github.com/eclipse-che/che/issues/18145) | 2021-05 | quarkus endpoints should use different port |
| [#17924](https://github.com/eclipse-che/che/issues/17924) | 2021-05 | Provide experimental "portable JDK" devfile |
| [#18587](https://github.com/eclipse-che/che/issues/18587) | 2022-02 | Get-started samples: add Quarkus-hibernate + postgres stack |
| [#17334](https://github.com/eclipse-che/che/issues/17334) | 2022-12 | More strict PR status checks on che-devfile-registry repository |
| [#15053](https://github.com/eclipse-che/che/issues/15053) | 2022-12 | Preview is not shown in default project for Python Django stack |

---

## 4. Projector-based editors (Projector discontinued by JetBrains) — 3 issues

| # | Last activity | Title |
|---|---|---|
| [#20007](https://github.com/eclipse-che/che/issues/20007) | 2022-12 | Provide Android Studio as a Che editor on DevSandbox (Projector) |
| [#19575](https://github.com/eclipse-che/che/issues/19575) | 2022-12 | Android Studio as Che editor using Projector |
| [#20242](https://github.com/eclipse-che/che/issues/20242) | 2023-03 | Enable multiarch support for building jetbrains-editor-images (Projector) |

---

## 5. Pre-DevWorkspace engine (`wsmaster` / old CRDs, replaced by DevWorkspace Operator) — 4 issues

| # | Last activity | Title |
|---|---|---|
| [#15425](https://github.com/eclipse-che/che/issues/15425) | 2022-12 | New DevWorkspace CRD (the DevWorkspace CRD has long shipped) |
| [#16791](https://github.com/eclipse-che/che/issues/16791) | 2021-07 | [workspace-controller] CRDs are invalid on kubernetes >= 1.18 |
| [#14183](https://github.com/eclipse-che/che/issues/14183) | 2022-06 | Document how to develop Che (wsmaster) in Che |
| [#13020](https://github.com/eclipse-che/che/issues/13020) | 2020-03 | Plugin broker should be able to send warning about plugin configuration |

---

## Borderline — re-triage against Che-Code before closing (not recommended for blind close)

These are labeled/worded around Che-Theia but describe behavior that **may still be valid in Che-Code**. Worth verifying against the current editor rather than closing outright:

- [#19673](https://github.com/eclipse-che/che/issues/19673) — Workspace startup should not override my workspace folders
- [#18114](https://github.com/eclipse-che/che/issues/18114) — Che should be able to deal with large git projects (shallow clone)
- [#18457](https://github.com/eclipse-che/che/issues/18457) — Can't generate Debug configuration in launch.json
- [#17730](https://github.com/eclipse-che/che/issues/17730) — FR: Add LibreOffice Online as editor to Che
- [#16047](https://github.com/eclipse-che/che/issues/16047) — Use digests instead of tags to reference images (may still apply to operator/che-code images)

## Explicitly excluded (still active / actionable — do NOT close)

- [#23816](https://github.com/eclipse-che/che/issues/23816) — Archive che-plugin-registry repository (the tracking issue that makes group 2 obsolete)
- [#23840](https://github.com/eclipse-che/che/issues/23840) — [UD] Devfile Creator and Workspace Loader with AI Agent (new, active)

---

### Summary

| Group | Count | Status | Confidence |
|---|---|---|---|
| 1. Che-Theia editor (removed) | 30 | ✅ 28 closed, 2 still open | High |
| 2. Plugin Registry / meta.yaml model (deprecated) | 20 | Open — awaiting review | High |
| 3. Devfile Registry v1 / stacks (superseded) | 8 | Open — awaiting review | High |
| 4. Projector editors (discontinued) | 3 | Open — awaiting review | High |
| 5. Pre-DevWorkspace engine | 4 | Open — awaiting review | High |
| **Total candidates** | **65** | **28 closed / 37 open** | |
| Borderline (re-triage) | 5 | Open | Medium |
