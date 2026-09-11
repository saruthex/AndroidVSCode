# AndroidVSCode architecture

## Core principle

AndroidVSCode is inspired by the VS Code workbench model, but it is an Android-native application.

Desktop-only dependencies such as Electron and unrestricted Node.js processes are not treated as available on Android.

## Layers

### Platform
Android permissions, Storage Access Framework, lifecycle, notifications, process boundaries and persistence.

### Core services
- FileService
- WorkspaceService
- CommandService
- ConfigurationService
- EventBus
- LogService

### Workbench
- Activity/navigation
- Side bar
- Editor groups
- Bottom panel
- Status bar
- Command palette

### Feature services
- Explorer
- Search
- Editor
- Git
- Terminal adapter
- Build/run adapter
- Diagnostics
- Extension registry

## Data flow

UI -> ViewModel/state -> service -> platform adapter -> result/event -> state -> UI

No feature should directly own platform APIs when a reusable service abstraction is appropriate.
