## Agent Guidance

### Mission
You are working in `Upstart Minimal`, a Kotlin/Compose Android app that demonstrates a lightweight MVI architecture with mocked networking. Your goals are to keep the loan form flow functional, maintain JSON-parsing correctness, and preserve test coverage whenever you modify behavior.

### Architectural Snapshot
1. **UI Layer (`LoanFormScreen.kt`)**
   - Jetpack Compose, Material 3 components.
   - Consumes a single `LoanFormState` from the ViewModel, and emits `LoanFormEvent` back.
   - Key widgets: dropdown, slider (snapped to 500 increments), text input, submit action.
2. **ViewModel (`LoanFormViewModel.kt`)**
   - Implements MVI: StateFlow for state, event dispatcher for intents.
   - Coordinates validation, slider snapping, and submission via `LoanRepository`.
   - Uses Kotlin coroutines and does not rely on DI frameworks; manual instantiation via factory.
3. **Data Layer (`LoanRepository.kt` + `LoanApi`)**
   - Repository serializes domain models to DTOs and back using kotlinx.serialization.
   - `LoanApi` interface allows swapping `MockLoanApi` for tests or real clients.
   - JSON parsing guarantees: all mocked responses are actual JSON strings processed by serialization.
4. **Tests**
   - Located in `app/src/test/java`.
   - Repository tests validate round-trip JSON parsing and business heuristics.
   - ViewModel tests ensure validation, slider snapping, and error handling remain intact.

### Working Agreements
- **Preserve Contracts**: The ViewModel emits `LoanFormState` with the same fields; UI expects them. Update both sides together.
- **Keep JSON Parsing**: Mock APIs must return parsable JSON strings. Avoid shortcutting by returning objects directly.
- **Avoid DI Frameworks**: Manual wiring (e.g., factories) is deliberate; do not introduce Dagger/Hilt/Koin without approval.
- **Tests Matter**: Add or update unit tests when touching business logic. Use `kotlinx-coroutines-test` for coroutine scenarios.

### Tips for Agents
- Search with `rg` for quick code navigation.
- Use `apply_patch` for targeted edits.
- Run `./gradlew test` after non-trivial changes; network access may require approval.

