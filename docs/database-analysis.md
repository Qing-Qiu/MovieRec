# MovieRec Database Notes

This note records the local `douban` MySQL shape observed on 2026-04-19.
It is intended for future coding-agent sessions and performance work.

## Table Shape

The application-facing names below are mostly views:

| Name | Type | Backing data |
| --- | --- | --- |
| `movie` | view | joins `movies` and `movieinfo` |
| `persons` | view | projects `person` |
| `relationship` | view | projects `relationships` and maps role codes to Chinese labels |
| `comments` | view | projects `idcomment` |
| `userinfo` | view | projects `alluseid` |

Approximate row counts in the current local database:

| Dataset | Rows |
| --- | ---: |
| `movie` | 13,965 |
| `persons` | 127,856 |
| `relationship` | 471,229 |
| `comments` / `idcomment` | 4,164,917 |
| `usrrate_none` | 287,348 |
| `userinfo` | 354,061 |

Movie years range from 1911 to 2019, with 105 distinct years. The densest
movie years are 2009, 2008, 2011, 2010, 2007, and 2012. The most common regions
are the US, Japan, Hong Kong, mainland China, South Korea, the UK, and France.

Common genre membership counts:

| Genre | Count |
| --- | ---: |
| Drama | 7,945 |
| Comedy | 3,958 |
| Romance | 3,086 |
| Action | 2,410 |
| Thriller | 2,283 |
| Crime | 1,704 |
| Mystery | 1,224 |
| Animation | 1,013 |
| Sci-Fi | 907 |
| History | 521 |
| Music | 376 |

## Indexes Added Locally

These indexes were added to support existing read paths:

```sql
CREATE INDEX idx_usrrate_none_nickname_movid
    ON usrrate_none (nickname, movid);

CREATE INDEX idx_idcomment_nickname_movie
    ON idcomment (nickname, `电影编号`);

CREATE INDEX idx_relationships_movie_role_person
    ON relationships (movie_id, role, person_id);

CREATE INDEX idx_relationships_person_movie
    ON relationships (person_id, movie_id);

CREATE INDEX idx_alluseid_nickname
    ON alluseid (nickname);

CREATE INDEX idx_usrid_id
    ON usrid (id);
```

Measured local impact:

| Query path | Before | After |
| --- | ---: | ---: |
| `/chart/figure` for `test` | about 5 seconds | about 130 ms |
| `/person/relevant` for `1292052` | full scan of `relationships` | about 128 ms |
| `/chart/chart3` hot request | N type queries before refactor | about 128 ms |

## Redis Guidance

Redis is not the first fix for this project. The biggest observed latency came
from missing MySQL indexes and N+1 SQL patterns, and those are now much better.

Redis would be useful later if the app needs shared caching across multiple
backend processes or higher concurrent traffic. Good candidates:

- Static chart endpoints: `/chart/year`, `/chart/chart1`, `/chart/chart2`,
  `/chart/chart3`.
- Per-user figure endpoint: `/chart/figure`, keyed by nickname with a short TTL.
- Homepage recommendations: `/movie/recommend`, keyed by nickname with a short
  TTL and invalidation after rating/comment changes.
- Expensive external music proxy calls, after fixing the Flask security issues.

Avoid Redis as a blanket cache for comments unless there is a clear invalidation
strategy on comment append. Also avoid using Redis to compensate for unindexed
SQL; indexes should stay the first layer.
