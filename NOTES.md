# Notes

**Assumptions**: Query matching is case-insensitive substring search over non-archived tasks; `pageSize` is clamped between 1 and 100 with default 10.

### 1. Summary of Changes
- **SQL / Repository**: Parenthesized `(LOWER(title) LIKE :term OR LOWER(description) LIKE :term)` in `TaskRepository` and reference SQL files; added deterministic `id DESC` secondary sorting.
- **Oracle PL/SQL**: Aligned operator precedence, sanitized null/negative pagination inputs, and added stable tie-breaker sorting.
- **Backend Controller**: Added status validation returning HTTP 400 on invalid enums, clamped `page >= 1` and `1 <= pageSize <= 100`, removed artificial `Thread.sleep` blocking, and added MockMvc test suite.
- **Frontend**: Added 300ms search debounce, auto-reset page to 1 upon filter changes, added `AbortController` cancellation to prevent race conditions, and ensured `loading` clears on error.

### 2. What I Chose Not to Change and Why
- **In-Memory Slicing (`subList`)**: Kept database-to-controller contract intact rather than rewriting repository signatures to Spring Data `Pageable`, preserving surgical scope.
- **H2 Console in `application.properties`**: Retained to allow reviewers to inspect in-memory database state directly.
- **Third-Party Libraries**: Avoided adding Lodash, Axios, or React Query, solving debouncing and cancellation natively.

### 3. Biggest Remaining Risk
In-memory pagination (`allResults.subList`) requires loading all matching rows into Java heap memory before slicing. Under large tables (10,000+ rows) or high traffic, this will cause memory pressure and latency. It should be migrated to database-level SQL `LIMIT`/`OFFSET` (`Pageable`).

### 4. Tools/AI Used and How
Used Antigravity AI to audit code against the exercise checklist and draft initial patch candidates.
- *Accepted*: Parenthesized SQL logic, `AbortController` cleanup in `useEffect`, and MockMvc integration tests.
- *Overridden/Rejected*: AI suggested refactoring repository return types to Spring Data `Page<Task>`, which I rejected to keep diffs minimal and avoid breaking query contracts. [EDIT: Mention any specific prompt or personal insight you had while reviewing].
