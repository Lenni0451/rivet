# Rivet Project Mandates

This document outlines the project-specific engineering standards, architectural patterns, and coding conventions for the Rivet UI library.

## Core Technical Stack
- **Language:** Java 17 (Targeted)
- **Build System:** Gradle (9.x+)
- **Graphics Backend:** Interchangeable by the user
- **Boilerplate Reduction:** Project Lombok (Required)

### Graphics Backend
The graphics backend is designed to be interchangeable, allowing users to choose their preferred rendering technology (e.g. OpenGL, AWT).
Currently, the library includes a default OpenGL backend, which uses the ThinGL library for abstracting OpenGL calls.
For reference, the ThinGL library is cloned into the `ThinGL` directory at the root of the project.
Do not modify the ThinGL under any circumstances, as it is a third-party library.

## Engineering Standards

### Coding Style & Conventions
- **Lombok Usage:**
    - Prefer `@Getter` and `@Setter` over manual implementations.
    - Use `@Accessors(fluent = true, chain = true)` for all models, components, and configuration classes to enable a fluent API style.
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
- **Layout System:** Layouts (e.g., `Flow`, `Grid`, `Absolute`) are separate from components and handle the positioning and sizing logic.
- **Rendering:** The render system emits a RenderList, which is handled by the graphics backend. The render method may be called in a separate thread, so it should not modify any state that is not thread-safe.
- **Theme System:** Styles are managed via `Theme` and `ThemeKey`. Components should query the current theme for colors, fonts, and other stylistic properties.
- **Input Handling:** Input events (Keyboard, Mouse) are propagated through the component tree, often starting from the `Rivet` root context.

## Development Workflow
1. **Research:** Map the existing component hierarchy or layout logic before making changes.
2. **Implementation:** Adhere strictly to the fluent API and Lombok patterns.
3. **Verification:**
    - Run `./gradlew clean build` to ensure the code compiles and is checkstyle compliant.

## Security & Integrity
- **Credential Protection:** Never commit API keys or secrets.
- **Source Control:** Do not stage or commit changes unless explicitly requested. Use descriptive, concise commit messages that explain the "why".
