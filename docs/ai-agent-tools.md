# MovieRec AI Agent Tool Draft

This draft defines the first set of safe, read-only tools for evolving the
movie assistant from a single RAG call into a tool-orchestrated agent.

Current orchestration entrypoint:

- `AiAgentPlannerService` creates the read-only tool plan.
- `AiRetrieveService` executes the plan and builds compact context.
- `AiToolService` owns the concrete database/chart tool implementations.

## Principles

- Tools should be small, deterministic, and easy to show in the UI.
- Read-only tools can run automatically.
- Write or destructive tools must require explicit user confirmation.
- Tool results should include enough source data for the model to cite what it
  used, but should stay compact enough for chat context.

## Tool Schema

Each tool follows this shape:

```json
{
  "toolName": "retrieveMovie",
  "riskLevel": "read",
  "input": {},
  "output": {
    "references": [],
    "contextText": "",
    "steps": []
  }
}
```

Risk levels:

- `read`: database or chart reads only; safe to auto-run.
- `confirm`: user-visible side effects; ask before running.
- `write`: database writes; require authenticated user and confirmation.
- `external`: crawlers or third-party requests; rate-limit and show source.

## Initial Read Tools

### retrieveMovie

- Input: `query`, `keyword`, `mode`, `limit`.
- Output: movie references with title, year, region, genre, score, summary,
  image, and route.
- Current implementation: `AiToolService.retrieveMovies`.

### retrievePerson

- Input: `query`, `keyword`, `limit`.
- Output: person references with name, role/birthday/birthplace, summary,
  image, and route.
- Current implementation: `AiToolService.retrievePersons`.

### retrieveMovieCast

- Input: `movieID`.
- Output: director, writer, and actor summary for the movie.
- Current implementation: `AiToolService.retrieveMovieCast`.

### retrievePersonWorks

- Input: `personID`.
- Output: representative movies sorted by popularity.
- Current implementation: `AiToolService.retrievePersonWorks`.

### retrieveComments

- Input: `movieID`, `limit`.
- Output: compact comment insight with sentiment keyword counts and sample
  comments.
- Current implementation: `AiToolService.retrieveComments`.

### retrieveCharts

- Input: `query`, `nickname`.
- Output: chart references, optional year-specific popular movie/type counts,
  and optional user profile summary.
- Current implementation: `AiToolService.retrieveCharts`.

## Later Tools

- `recommendMovies`: combine structured filters and user profile signals.
- `repairPoster`: try trusted image sources for broken poster URLs.
- `explainChart`: generate deterministic chart summaries before model rewrite.
- `appendComment`: write user comment; must be `write` and confirmed.
