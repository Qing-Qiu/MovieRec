# MovieRec Agent Notes

This file is for coding agents and vibe-coding sessions. It captures the local
runtime shape of this repo so future agents do not rediscover it the slow way.

## Workspace

- Repo root: `E:\IdeaProjects\MovieRec`
- Spring Boot backend: `backend-springboot`
- Flask music proxy: `backend-flask`
- Vue frontend: `frontend`

## Local Services

| Service | Directory | Port | Runtime |
| --- | --- | --- | --- |
| Spring Boot API | `backend-springboot` | `8080` | JDK at `C:\Users\Lenovo\.jdks\openjdk-19.0.1` |
| Vue dev server | `frontend` | `8081` | Node/npm from `C:\Program Files\nodejs` |
| Flask music proxy | `backend-flask` | `5000` | Conda env `D:\anaconda3\envs\MovieRec` |

Check listeners:

```powershell
netstat -ano | Select-String ':8080|:8081|:5000'
```

## Start Commands

Run these from PowerShell or `cmd.exe`. Long-running commands should normally be
started in their own terminal.

### Spring Boot API

```powershell
cd E:\IdeaProjects\MovieRec\backend-springboot
$env:JAVA_HOME='C:\Users\Lenovo\.jdks\openjdk-19.0.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

Notes:

- `pom.xml` declares `<java.version>17</java.version>`. The local JDK 19 has
  been verified to run the app, but JDK 17 is the project-matching baseline.
- `.\mvnw.cmd -DskipTests compile` has succeeded with the local setup.
- `mvn test` is not the same as starting the app. The test class connects to
  the local MySQL datasource and may fail or hang if MySQL is unavailable.

### Vue Frontend

```powershell
cd E:\IdeaProjects\MovieRec\frontend
npm run serve -- --port 8081
```

Notes:

- `npm run build` and `npm run lint` have succeeded.
- The dev server is configured with `historyApiFallback`. Browser navigation to
  history routes such as `/home` or `/auth/login` should return `index.html`.
  When testing with PowerShell, include a browser-like `Accept: text/html` header;
  requests with `Accept: application/json` correctly return 404.

### Flask Music Proxy

```powershell
cd E:\IdeaProjects\MovieRec\backend-flask
D:\anaconda3\envs\MovieRec\python.exe app.py
```

Notes:

- Do not create a local `.venv` for this project unless asked. A project-specific
  Conda env already exists at `D:\anaconda3\envs\MovieRec`.
- The Conda env has `flask-cors==6.0.2` installed.
- Plain `python` may resolve to base Anaconda and miss `flask_cors`.
- `conda info --envs` can fail in this desktop sandbox because Conda's CUDA
  virtual-package plugin hits `PermissionError`. Directly using
  `D:\anaconda3\envs\MovieRec\python.exe` works.

## Smoke Tests

Spring Boot:

```powershell
Invoke-WebRequest -UseBasicParsing `
  -Method Post `
  -Uri http://localhost:8080/movie/count2 `
  -ContentType 'application/json' `
  -Body '{"keyword":""}'
```

Expected: HTTP 200, body like `13965`.

Frontend:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8081/
```

Expected: HTTP 200, HTML length around `641` in dev mode.

History route check:

```powershell
Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8081/home `
  -Headers @{Accept='text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'}
```

Expected: HTTP 200 with `index.html`.

Flask:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:5000/search?key=test&pn=1'
```

Expected: HTTP 200 JSON. In the current environment this returned:

```json
{"total": 0, "list": []}
```

## Runtime Logs

Some agent-started service runs redirect logs to:

- `backend-springboot\backend-run.log`
- `backend-springboot\backend-run.err.log`
- `backend-flask\flask-run.log`
- `backend-flask\flask-run.err.log`
- `frontend\frontend-run.log`
- `frontend\frontend-run.err.log`

Treat these as local runtime artifacts, not source files.

## Known Review Findings

Do not lose these during future edits:

- `backend-flask\kw.py` uses `eval()` on external HTTP response data.
- `backend-flask\kw.py` builds a `python -c` script using user-controlled `rid`.
- `backend-springboot\src\main\java\com\example\douban\controller\ImageController.java`
  proxies arbitrary URLs and needs SSRF protection.
- MyBatis mapper XML files use `${limit}` / `${offset}` string substitution.
- Passwords are stored and compared in plaintext.
- Comment append trusts client-supplied user identity.
- `application.yml` hardcodes the MySQL root password.
- `AccountMapper.xml` uses `MAX(id)+1` for account IDs, which races under
  concurrent registration.
