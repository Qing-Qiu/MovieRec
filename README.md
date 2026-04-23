# MovieRec

MovieRec 是一个电影推荐与电影资料探索系统。项目围绕电影搜索、个性化推荐、电影/影人详情、评论互动、数据图表解读、电影助手和音乐播放等场景展开，适合作为推荐系统、数据可视化与大模型应用结合的综合实践项目。

## 功能亮点

- 首页推荐：结合用户历史、影片评分、类型偏好和多样性策略生成推荐结果，并展示更面向用户的推荐理由。
- 电影中心：支持按关键词、类型、年份等条件浏览电影，包含简洁分页和电影卡片展示。
- 电影详情：展示电影海报、评分、剧情简介、演职员表、评论区和相关信息。
- 影人详情：展示影人资料、简介、代表作品和参与作品。
- 评论区：支持评论查看、发布和继续加载更多评论。
- 图表解读：将年度热片、类型趋势、类型结构等可视化结果整合到电影助手中，支持完整视图查看。
- 电影助手：提供智能问答、片库资料查询、推荐助手、图表解读四种模式，可结合本地电影数据和 Ollama 兼容模型回答问题。
- 音乐中心：通过 Flask 代理搜索歌曲、播放音乐并获取歌词。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vue Router、Ant Design Vue、ECharts、Axios、Markdown 渲染 |
| 电影后端 | Spring Boot 3、MyBatis、MySQL、Apache Mahout、Jieba/IK 分词 |
| 音乐代理 | Flask、Flask-CORS、Requests、gevent |
| 大模型接口 | OpenAI 兼容接口，默认连接本地 Ollama `qwen3:4b` |

## 项目结构

```text
MovieRec
├─ backend-springboot   # 电影、推荐、评论、图表、AI 助手 API
├─ backend-flask        # 音乐搜索、播放地址、歌词代理
├─ frontend             # Vue 前端应用
├─ docs                 # 模块规划和数据库分析文档
└─ AGENTS.md            # 面向 coding agent 的本地运行备忘
```

## 运行环境

推荐环境：

- JDK 17 或更高版本。项目 `pom.xml` 以 Java 17 为基线。
- Node.js 与 npm。
- Python/Conda 环境，安装 Flask 代理依赖。
- MySQL，本地数据库名默认为 `douban`。
- 可选：本地 Ollama，默认模型为 `qwen3:4b`。

当前本地验证过的环境：

| 服务 | 目录 | 端口 | 运行时 |
| --- | --- | --- | --- |
| Spring Boot API | `backend-springboot` | `8080` | `C:\Users\Lenovo\.jdks\openjdk-19.0.1` |
| Vue 前端 | `frontend` | `8081` | Node/npm |
| Flask 音乐代理 | `backend-flask` | `5000` | `D:\anaconda3\envs\MovieRec` |

## 快速启动

### 1. 启动 Spring Boot 后端

```powershell
cd E:\IdeaProjects\MovieRec\backend-springboot
$env:JAVA_HOME='C:\Users\Lenovo\.jdks\openjdk-19.0.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

后端默认读取：

- 服务端口：`8080`
- MySQL：`jdbc:mysql://localhost:3306/douban`
- 大模型接口：`http://localhost:11434/v1`
- 默认模型：`qwen3:4b`

如果数据库账号、密码或模型地址不同，请调整 `backend-springboot/src/main/resources/application.yml` 和 `backend-springboot/src/main/resources/application.properties`。

### 2. 启动 Vue 前端

```powershell
cd E:\IdeaProjects\MovieRec\frontend
npm install
npm run serve -- --port 8081
```

浏览器访问：

```text
http://localhost:8081/
```

常用页面：

- `/home`：首页推荐
- `/center`：电影中心
- `/model`：电影助手
- `/music`：音乐中心
- `/movie/:movie_id`：电影详情
- `/person/:person_id`：影人详情

### 3. 启动 Flask 音乐代理

```powershell
cd E:\IdeaProjects\MovieRec\backend-flask
D:\anaconda3\envs\MovieRec\python.exe app.py
```

音乐代理默认运行在：

```text
http://localhost:5000
```

主要接口：

- `/search?key=周杰伦&pn=1`
- `/mp3?rid=歌曲ID`
- `/lrc?rid=歌曲ID`

## 常用检查命令

检查端口占用：

```powershell
netstat -ano | Select-String ':8080|:8081|:5000'
```

后端编译：

```powershell
cd E:\IdeaProjects\MovieRec\backend-springboot
.\mvnw.cmd -DskipTests compile
```

前端检查：

```powershell
cd E:\IdeaProjects\MovieRec\frontend
npm run lint
npm run build
```

基础接口烟测：

```powershell
Invoke-WebRequest -UseBasicParsing `
  -Method Post `
  -Uri http://localhost:8080/movie/count2 `
  -ContentType 'application/json' `
  -Body '{"keyword":""}'
```

## 推荐与电影助手说明

### 推荐模块

推荐结果会综合多类信息：

- 用户已有评分/评论行为。
- 影片类型、地区、年份、评分和热度。
- Mahout 基于评分数据给出的相似口味结果。
- 后端二次重排，用于控制热门电影占比、提升多样性，并生成推荐理由。

前端会展示类似“相似口味”“口味相近”“高分佳片”“新鲜发现”的标签，让推荐理由更接近用户语言。

### 电影助手模块

电影助手目前分为四种模式：

- 智能问答：适合开放式电影问题。
- 片库资料：优先查询电影、影人、评论等本地资料。
- 推荐助手：把自然语言偏好转成推荐方向和推荐理由。
- 图表解读：结合可视化数据解释趋势和异常点。

默认连接 Ollama 的 OpenAI 兼容接口。如果本地没有运行 Ollama，普通电影资料查询仍可返回参考资料，但模型回答会不可用或降级。

## 文档

- `docs/model-module-plan.md`：电影助手模块规划。
- `docs/ai-agent-tools.md`：电影助手工具化设计草案。
- `docs/database-analysis.md`：数据库规模、索引和缓存建议。
- `AGENTS.md`：本地服务启动细节和 coding agent 注意事项。

## 已知限制与后续优化

当前项目仍有一些值得继续完善的方向：

- 登录注册仍需升级为安全的密码哈希与认证机制。
- 评论发布应从服务端认证上下文确认用户身份，避免信任客户端传入身份。
- 数据库配置应改为环境变量或 profile 管理，避免把本地凭据固化到仓库。
- 注册账号生成方式应避免 `MAX(id)+1` 的并发风险。
- 图片代理需要继续收紧 URL 白名单，降低 SSRF 风险。
- 前端生产构建包体积偏大，可以继续拆分图表、图标和电影助手相关依赖。
- 推荐系统可以进一步把数据库行为数据、Mahout 输入文件生成和推荐解释串成更完整的自动刷新流程。

## 许可

本仓库主要用于课程项目、学习实践和本地演示。若要公开部署，请先完成认证、凭据管理、外部代理安全和版权合规检查。
