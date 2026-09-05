# Rivet Project Mandates

This document outlines the project-specific engineering standards, architectural patterns, and coding conventions for the Rivet UI library.

## Core Technical Stack
- **Language:** Java 17 (Targeted)
- **Build System:** Gradle (9.x+)
- **Project Structure:** Multi-module Gradle build:
    - Root (`rivet`): Core UI library in `src/main/java`
    - `backend-thingl`: ThinGL (OpenGL) shared rendering implementation and batching pipeline
    - `backend-thingl-glfw`: GLFW windowing & input integration for ThinGL
    - `backend-thingl-sdl`: SDL windowing & input integration for ThinGL
    - `backend-awt`: Java AWT / Swing backend and application runner
    - `examples`: Demo applications and manual UI validation test runners
- **Graphics Backend:** Interchangeable by the user (ThinGL OpenGL via GLFW/SDL, Java AWT)
- **Boilerplate Reduction:** Project Lombok (Required)

### Graphics Backends
The graphics backend is designed to be interchangeable, allowing users to choose their preferred rendering technology:
- **ThinGL (OpenGL):** Abstracted through `backend-thingl` with platform integrations in `backend-thingl-glfw` (GLFW) and `backend-thingl-sdl` (SDL).
- **AWT / Swing:** Implemented in `backend-awt` (`AWTBackend`, `AWTApplication`, `AWTRenderer`, `RivetCanvas`).
- **ThinGL Reference Directory:** For reference, the ThinGL library is cloned into the `ThinGL` directory at the project root. **Do not modify the `ThinGL` directory under any circumstances**, as it is a third-party library.

## Engineering Standards

### Coding Style & Conventions
- **Lombok Usage:**
    - Prefer `@Getter` and `@Setter` over manual implementations.
    - Use `@Accessors(fluent = true, chain = true, makeFinal = true)` for models/components where possible (widely used in core classes such as `Component`, `Container`, `Rivet`, and `Layer`).
    - Use `@RequiredArgsConstructor` for classes with final fields that should be initialized via constructor.
- **Fluent API:** All methods that modify internal state and don't return a value should return `this` to support chaining.
- **Explicit Field Access:** Always use `this.` when accessing class fields to differentiate them from local variables or parameters (enforced by Checkstyle `RequireThis`).
- **Finality:**
    - Mark all method parameters as `final`.
    - Prefer `final` fields for immutability where possible.
- **Naming Conventions:** Standard Java conventions (PascalCase for classes, camelCase for methods/variables). Fluent getters/setters omit the `get`/`set` prefix (e.g., `size()` instead of `getSize()` or `setSize()`).
- **Indentation:** 4 spaces for indentation. No tabs.
- **Java 17 Features:** Utilize modern Java features like records (where appropriate), pattern matching for `instanceof`, and text blocks for multi-line strings.

### Architectural Patterns
- **Component-Based UI:** Everything is a `Component`. The `Container` class is the primary way to group and layout components.
- **Layout System:** Layouts (e.g., `FlowLayout`, `ListLayout`, `FlexLayout`, `GridLayout`, `TileLayout`, `DockLayout`, `BorderLayout`, `AnchorLayout`, `AbsoluteLayout`, `FullSizeLayout`) are decoupled from components and manage positioning and sizing.
- **Layout Contract:** `Layout#layoutComponents` must provide bounds for every child; missing bounds trigger an `IllegalStateException` in `Container#computeLayout`.
- **Layer System:** Rendering and input propagation are layer-based via `LayerList`; use `LayerBucket` (`BASE`, `OVERLAY`, `TOOLTIP`, `DRAG`) when adding cross-cutting UI such as popups, dropdowns, overlays, and drag previews.
- **Rendering Pipeline:** Rendering is abstracted through `Renderer` (and deferred rendering via `RenderList` / `DeferredRenderer` or batched executors). The render pass may run on a separate render thread, so render methods must not mutate state in a thread-unsafe manner.
- **Theme & Parser System:** Visual styles are managed via `Theme`, `ThemeKey`, and `ThemeOption`, loaded dynamically with `ThemeLoader`. Value parsing is handled by `ParserRegistry` and modular `Parser` implementations.
- **Input & Drag-and-Drop:** Input events (keyboard, mouse cursor, mouse buttons, scroll) propagate through the component hierarchy. Drag and drop interactions are orchestrated through `DragAndDropManager`.

## Development Workflow
1. **Research:** Map the existing component hierarchy, backend abstraction, or layout logic before making changes.
2. **Implementation:** Adhere strictly to the fluent API, Lombok patterns, and `this.` field access conventions.
3. **Verification:**
    - Run `./gradlew clean build` (or `./gradlew build`) to ensure the entire project compiles and passes Checkstyle verification.
    - Demo and manual UI test runners are located in `examples/src/main/java/test/impl` (extending `TestBase`). Manual UI tests should never be triggered by the agent and should only ever be run by the user.

## Security & Integrity
- **Credential Protection:** Never commit API keys or secrets.
- **Source Control:** Do not stage or commit changes unless explicitly requested. Use descriptive, concise commit messages that explain the "why".
