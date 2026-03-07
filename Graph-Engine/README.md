# Graph-Based Probabilistic Distributed Resilience & Risk Simulation Engine

A **3-tier** Spring Boot backend that models microservice architectures as directed weighted graphs and provides failure simulation, risk analysis, architecture scoring, and intelligent recommendations — all powered by classic graph algorithms and Monte Carlo methods.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Project Structure](#project-structure)
5. [Tier Breakdown](#tier-breakdown)
   - [Tier 1 — Core Engine](#tier-1--core-engine)
   - [Tier 2 — Risk & Intelligence](#tier-2--risk--intelligence)
   - [Tier 3 — Advanced Enhancements](#tier-3--advanced-enhancements)
6. [Performance Upgrades](#performance-upgrades)
   - [Upgrade 1 — Temporal Failure Simulation](#upgrade-1--temporal-failure-simulation)
   - [Upgrade 2 — Parallel Monte Carlo](#upgrade-2--parallel-monte-carlo)
   - [Upgrade 3 — Confidence Intervals](#upgrade-3--confidence-intervals)
   - [Upgrade 4 — Weighted Service Impact](#upgrade-4--weighted-service-impact)
   - [Upgrade 5 — Correlated Failures](#upgrade-5--correlated-failures)
7. [Algorithms Used](#algorithms-used)
8. [API Reference](#api-reference)
   - [Graph Management](#1-graph-management)
   - [Simulation](#2-simulation)
   - [Recovery](#3-recovery)
   - [Optimization](#4-optimization)
   - [Risk Analysis](#5-risk-analysis)
   - [Architecture](#6-architecture)
   - [Health](#7-health)
   - [Recommendations](#8-recommendations)
9. [Sample Graph Payload](#sample-graph-payload)
10. [How to Run](#how-to-run)
11. [Testing Summary](#testing-summary)
12. [File Inventory](#file-inventory)
13. [Design Principles](#design-principles)
14. [What's Next](#whats-next)

---

## Project Overview

This engine allows you to:

- **Model** a microservice ecosystem as a directed weighted graph (nodes = services, edges = dependencies)
- **Simulate** cascading failures using deterministic BFS, probabilistic Monte Carlo, and **time-based temporal simulation**
- **Plan** safe recovery orders using topological sorting with cycle detection
- **Optimize** infrastructure costs via Dijkstra's shortest paths, DSU clustering, and Kruskal's MST
- **Assess Risk** by detecting critical nodes, computing resilience scores, running sensitivity analysis, and what-if comparisons — now with **95% confidence intervals** and **weighted service impact**
- **Model Correlated Failures** — define correlation groups where services share infrastructure and fail together
- **Score Architectures** with a 5-metric composite scoring system (0-100 with A-F grades)
- **Monitor Health** in real-time with node state management, live dashboards, and recovery plans
- **Get Recommendations** — auto-generated, prioritized improvement suggestions

---

## Tech Stack

| Component        | Technology                      |
|------------------|---------------------------------|
| **Language**     | Java 21 (LTS)                   |
| **Framework**    | Spring Boot 4.0.3               |
| **Build Tool**   | Maven (with Maven Wrapper)      |
| **Dependencies** | `spring-boot-starter-webmvc`, `lombok`, `devtools` |
| **Database**     | None — fully in-memory graph    |
| **API Style**    | RESTful JSON                    |
| **Port**         | 8080 (default)                  |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      REST Controllers                          │
│  GraphController │ RiskController │ ArchitectureController     │
│  HealthController │ RecommendationController                   │
├─────────────────────────────────────────────────────────────────┤
│                      Service Layer                             │
│  GraphService │ SimulationService │ RecoveryService            │
│  OptimizationService │ RiskAnalysisService                     │
│  ArchitectureService │ HealthService │ RecommendationService   │
├─────────────────────────────────────────────────────────────────┤
│                     Algorithm Layer                            │
│  DFSUtil │ TopoSortUtil │ DijkstraUtil │ DSU │ MSTUtil         │
│  MonteCarloUtil │ TemporalCascadeUtil                          │
├─────────────────────────────────────────────────────────────────┤
│                      Model Layer                               │
│  Graph │ ServiceNode │ Edge │ ServiceState │ CorrelationGroup  │
└─────────────────────────────────────────────────────────────────┘
```

**Request → Controller → Service → Algorithm → Model → Response**

- **Controllers** handle HTTP, validate input, delegate to services
- **Services** contain business logic, orchestrate algorithms
- **Algorithms** are pure graph computation utilities (stateless `@Component` beans)
- **Models** are data holders — the `Graph` class is the core in-memory data structure

---

## Project Structure

```
Graph-Engine/
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
└── src/main/java/com/klu/backend/
    ├── GraphEngineApplication.java          # Spring Boot entry point
    │
    ├── model/
    │   ├── ServiceState.java                # Enum: HEALTHY, FAILED, RECOVERING
    │   ├── ServiceNode.java                 # Node: id, restartCost, state, recoveryTime, importanceWeight
    │   ├── Edge.java                        # Edge: from, to, failureProbability, infraCost, propagationDelay
    │   ├── Graph.java                       # Core graph: adjacency lists, reverse adj, inDegree, correlationGroups
    │   └── CorrelationGroup.java            # Correlated failure group: groupId, nodeIds, correlationFactor
    │
    ├── algorithm/
    │   ├── DFSUtil.java                     # BFS cascade propagation on reverse graph (+ weighted impact)
    │   ├── TopoSortUtil.java                # Cycle detection + Kahn's topological sort
    │   ├── DijkstraUtil.java                # Min-cost recovery paths (priority queue)
    │   ├── DSU.java                         # Disjoint Set Union (path compression + rank)
    │   ├── MSTUtil.java                     # Kruskal's Minimum Spanning Tree
    │   ├── MonteCarloUtil.java              # Parallel probabilistic BFS with CIs + correlated failures
    │   └── TemporalCascadeUtil.java         # Time-based cascade with priority queue + propagation delays
    │
    ├── service/
    │   ├── GraphService.java                # Graph CRUD, bulk load, state retrieval
    │   ├── SimulationService.java           # Deterministic cascade simulation
    │   ├── RecoveryService.java             # Recovery plans, cycle checks
    │   ├── OptimizationService.java         # Dijkstra, DSU clusters, MST
    │   ├── RiskAnalysisService.java         # Monte Carlo, critical nodes, resilience, sensitivity, what-if
    │   ├── ArchitectureService.java         # 5-metric composite scoring, architecture comparison
    │   ├── HealthService.java               # Node state management, live dashboard, recovery
    │   └── RecommendationService.java       # Auto-generated prioritized improvement suggestions
    │
    ├── controller/
    │   ├── GraphController.java             # T1: /api/graph, /api/simulation, /api/recovery, /api/optimization
    │   ├── RiskController.java              # T2: /api/risk
    │   ├── ArchitectureController.java      # T3: /api/architecture
    │   ├── HealthController.java            # T3: /api/health
    │   └── RecommendationController.java    # T3: /api/recommendations
    │
    ├── dto/
    │   ├── request/
    │   │   ├── AddServiceRequest.java       # { id, restartCost, recoveryTime?, importanceWeight? }
    │   │   ├── AddDependencyRequest.java    # { from, to, failureProbability, infraCost, propagationDelay? }
    │   │   ├── GraphLoadRequest.java        # { services[], dependencies[] } (with optional new fields)
    │   │   ├── WhatIfRequest.java           # { addEdges[], removeEdges[], updateProbabilities[] }
    │   │   ├── NodeStateRequest.java        # { state }
    │   │   └── AddCorrelationGroupRequest.java  # { groupId, nodeIds[], correlationFactor }
    │   │
    │   └── response/
    │       ├── GraphStateResponse.java      # Full graph snapshot (includes new fields)
    │       ├── CascadeResult.java           # Cascade chain + failure count + weightedImpactScore
    │       ├── TemporalCascadeResult.java   # Time-based cascade timeline + temporal metrics
    │       ├── RecoveryResult.java          # Restart order + cycle info + critical chain
    │       ├── DijkstraResult.java          # Min-cost paths from source
    │       ├── ClusterResult.java           # Independent service clusters
    │       ├── MSTResult.java               # Minimum spanning tree edges + total cost
    │       ├── MonteCarloResult.java        # Hit counts + probabilities + CIs + weighted impact
    │       ├── CriticalNodeResult.java      # Nodes ranked by failure impact + weighted scores
    │       ├── ResilienceReport.java        # System resilience score + fragility metrics
    │       ├── SensitivityResult.java       # Edges ranked by hardening impact
    │       ├── WhatIfResult.java            # Before/After resilience comparison
    │       ├── ArchitectureScoreResult.java # Composite score + grade + sub-metrics
    │       ├── ArchitectureComparisonResult.java  # Current vs. proposed scoring
    │       ├── HealthDashboard.java         # Live system health overview
    │       ├── LiveRecoveryPlan.java        # Step-by-step recovery for failed nodes
    │       └── RecommendationResult.java    # Categorized improvement suggestions
    │
    ├── exception/
    │   ├── GraphValidationException.java    # Custom RuntimeException
    │   └── GlobalExceptionHandler.java      # @RestControllerAdvice → 400 JSON responses
    │
    └── config/
        └── WebConfig.java                   # CORS enabled for /api/** (all origins)
```

**Total: 52 Java source files**

---

## Tier Breakdown

### Tier 1 — Core Engine

**27 files | 17 endpoints | Fully tested**

The foundational layer that provides:

| Feature                   | Description                                                                      |
|---------------------------|----------------------------------------------------------------------------------|
| **Graph CRUD**            | Add/remove services & dependencies, update costs, bulk load, clear               |
| **Cascade Simulation**    | BFS on reverse adjacency list — deterministic failure propagation                |
| **Recovery Planning**     | Topological sort with cycle detection — safe restart order & critical chain       |
| **Dijkstra Optimization** | Minimum-cost recovery paths via priority queue on reverse graph                  |
| **Cluster Detection**     | Disjoint Set Union (path compression + union by rank) finds independent groups   |
| **MST**                   | Kruskal's algorithm — minimum-cost infrastructure backbone                       |

---

### Tier 2 — Risk & Intelligence

**9 files | 5 endpoints | Fully tested | Zero T1 modifications**

Adds probabilistic analysis on top of T1:

| Feature                   | Description                                                                          |
|---------------------------|--------------------------------------------------------------------------------------|
| **Monte Carlo Simulation** | Probabilistic BFS using `ThreadLocalRandom` — runs N iterations, aggregates hit counts & probabilities |
| **Critical Node Detection** | Fails each node one-by-one via Monte Carlo, ranks by average cascade impact         |
| **Resilience Report**      | System-wide fragility score (0-1), resilience percentage, worst-case analysis        |
| **Sensitivity Analysis**   | Tests every edge — ranks by how much hardening (halving probability) reduces system risk |
| **What-If Engine**         | Apply temporary modifications (add/remove edges, update probabilities), compare before vs. after resilience, then rollback |

---

### Tier 3 — Advanced Enhancements

**12 files | 6 endpoints | Fully tested | Zero T1/T2 modifications**

Adds architecture intelligence, health monitoring, and automated recommendations:

| Feature                        | Description                                                                                |
|--------------------------------|--------------------------------------------------------------------------------------------|
| **Architecture Score**         | Composite 0-100 score from 5 weighted metrics — Resilience (30%), Redundancy (20%), Coupling (20%), Depth (15%), SPOF (15%). Letter grade (A/B/C/D/F) |
| **Architecture Comparison**    | Score current architecture vs. a proposed design. The proposed graph is built temporarily — live graph never modified |
| **Node State Management**      | Transition nodes between `HEALTHY`, `FAILED`, `RECOVERING` states                          |
| **Health Dashboard**           | Live overview: node counts by state, failed/recovering lists, cascade risk zones, estimated recovery cost |
| **Live Recovery Plan**         | Step-by-step restart instructions with correct dependency ordering, individual + cumulative costs, prerequisites |
| **Recommendations Engine**     | Auto-generates prioritized suggestions in 5 categories: SPOF elimination (HIGH), Edge hardening (HIGH), Redundancy gaps (MEDIUM), Cluster isolation (MEDIUM), Cost optimization (LOW) |

---

## Performance Upgrades

**4 new files + 16 modified files | 3 new endpoints | All backward-compatible**

Five performance and simulation accuracy upgrades were added without altering the core system design. All new fields have sensible defaults — existing JSON payloads continue to work unchanged.

### Upgrade 1 — Temporal Failure Simulation

**Problem:** The original cascade simulation treated all failures as instantaneous. Real systems have propagation delays.

**Solution:** New `TemporalCascadeUtil` uses a **priority queue** (min-heap) ordered by failure time. Each edge has a `propagationDelay` — when a node fails, downstream failures are scheduled at `currentTime + delay`. The simulation processes events chronologically.

**New Fields:**
- `ServiceNode.recoveryTime` — time in seconds for the node to recover (default: 0.0)
- `Edge.propagationDelay` — time in seconds for failure to propagate along this edge (default: 0.0)

**New Endpoint:** `POST /api/simulation/temporal-cascade?failedNode=X`

**New Metrics:**
| Metric | Description |
|--------|-------------|
| `cascadeDuration` | Total time from first to last failure event |
| `timeToSystemCollapse` | Time when ≥50% of nodes have failed (-1 if never reached) |
| `peakFailureMoment` | Timestamp of maximum concurrent failures |
| `peakConcurrentFailures` | Highest number of simultaneous failures |
| `weightedImpactScore` | Impact weighted by node importance |

**Timeline Example:**
```json
{
  "timeline": [
    { "nodeId": "notification", "failureTime": 0.0, "cascadeLevel": 0, "triggeredBy": null },
    { "nodeId": "payment",      "failureTime": 1.0, "cascadeLevel": 1, "triggeredBy": "notification" },
    { "nodeId": "order-svc",    "failureTime": 4.0, "cascadeLevel": 2, "triggeredBy": "payment" }
  ],
  "cascadeDuration": 4.0,
  "timeToSystemCollapse": -1.0
}
```

---

### Upgrade 2 — Parallel Monte Carlo

**Problem:** Running 10,000+ Monte Carlo iterations on a single thread is slow for large graphs.

**Solution:** `MonteCarloUtil` now uses `ExecutorService.newFixedThreadPool()` with `Runtime.getRuntime().availableProcessors()` threads. Iterations are split into batches across threads. Results are aggregated using `ConcurrentHashMap<String, AtomicLong>` for lock-free accumulation.

**Impact:** Near-linear speedup on multi-core machines. 10,000 iterations complete in milliseconds.

---

### Upgrade 3 — Confidence Intervals

**Problem:** Monte Carlo probabilities are point estimates — how confident can we be in a 0.37 failure probability?

**Solution:** After aggregating simulation results, **95% confidence intervals** are computed for each node's failure probability using the normal approximation:

$$CI = p \pm 1.96 \times \sqrt{\frac{p(1-p)}{n}}$$

Where $p$ is the estimated probability and $n$ is the iteration count.

**New Field in `MonteCarloResult`:**
```json
"confidenceIntervals": {
  "gateway":      [0.0889, 0.0946, 0.1003],
  "order-svc":    [0.3670, 0.3765, 0.3860],
  "payment":      [0.5892, 0.5988, 0.6084],
  "notification": [1.0000, 1.0000, 1.0000]
}
```
Each array is `[lower_bound, point_estimate, upper_bound]`.

---

### Upgrade 4 — Weighted Service Impact

**Problem:** The original cascade impact treated all nodes equally. In reality, a payment gateway failing is far worse than a logging service failing.

**Solution:** Each `ServiceNode` now has an `importanceWeight` (default: 1.0). Impact scores are computed as:

$$\text{weightedImpact} = \frac{\sum \text{affectedWeight}}{\sum \text{totalWeight}}$$

**New Fields:**
- `ServiceNode.importanceWeight` — criticality weight (e.g., gateway=5.0, notification=1.0)
- `CascadeResult.weightedImpactScore` — weighted cascade impact
- `CriticalNodeResult.NodeImpact.weightedImpactScore` — weighted node ranking
- `MonteCarloResult.weightedExpectedImpact` — expected weighted impact per simulation

**Example Comparison:**
| Metric | payment (wt=5.0) | inventory (wt=2.0) |
|--------|-------------------|---------------------|
| Unweighted impact (3/7) | 0.4286 | 0.4286 |
| **Weighted impact** | **0.5909** | **0.4545** |

Same affected count, but payment's cascade hits higher-weight nodes.

---

### Upgrade 5 — Correlated Failures

**Problem:** The original Monte Carlo assumed independent failures. Real systems have shared infrastructure — when a database rack fails, all services on that rack crash together.

**Solution:** New `CorrelationGroup` model — services can be grouped with a `correlationFactor` [0.0, 1.0]. During Monte Carlo simulation, when a node fails and belongs to a correlation group, each other group member probabilistically co-fails based on the factor.

**New Endpoints:**
- `POST /api/graph/correlation-groups` — Add a correlation group
- `DELETE /api/graph/correlation-groups/{groupId}` — Remove a correlation group

**Dramatic Before/After (payment-cluster: payment, order-svc, inventory @ 0.8):**

| Metric | Without Correlation | With Correlation |
|--------|---------------------|------------------|
| inventory failure prob | 0.0000 | **0.9471** |
| order-svc failure prob | 0.4895 | **0.9724** |
| gateway failure prob | 0.1254 | **0.2452** |
| Weighted Impact | 0.3225 | **0.5017** |

---

## Algorithms Used

| Algorithm               | Class                 | Purpose                                              | Complexity         |
|-------------------------|-----------------------|------------------------------------------------------|--------------------|
| **BFS (Reverse Graph)** | `DFSUtil`             | Deterministic cascade failure propagation + weighted impact | O(V + E)     |
| **DFS (3-Color)**       | `TopoSortUtil`        | Cycle detection in dependency graph                  | O(V + E)           |
| **Kahn's Algorithm**    | `TopoSortUtil`        | Topological sort for safe restart ordering           | O(V + E)           |
| **Longest Path (DAG)**  | `TopoSortUtil`        | Critical chain computation                           | O(V + E)           |
| **Dijkstra's Algorithm**| `DijkstraUtil`        | Min-cost recovery paths (priority queue)             | O((V+E) log V)     |
| **DSU (Union-Find)**    | `DSU`                 | Independent cluster detection                        | O(α(V)) ≈ O(1)    |
| **Kruskal's MST**       | `MSTUtil`             | Minimum-cost infrastructure backbone                 | O(E log E)         |
| **Parallel Monte Carlo**| `MonteCarloUtil`      | Multi-threaded probabilistic cascade with CIs + correlation | O(iterations × (V+E) / threads) |
| **Temporal Cascade**    | `TemporalCascadeUtil` | Time-based PQ cascade with propagation delays        | O((V+E) log V)     |

---

## API Reference

**Base URL:** `http://localhost:8080`

### 1. Graph Management

| Method   | Endpoint                          | Description                                   | Body / Params                                          |
|----------|-----------------------------------|-----------------------------------------------|--------------------------------------------------------|
| `GET`    | `/api/graph`                      | Get full graph state                          | —                                                      |
| `POST`   | `/api/graph/services`             | Add a service node                            | `{ "id": "svc-a", "restartCost": 5.0 }`               |
| `DELETE` | `/api/graph/services/{id}`        | Remove a service and its edges                | Path: `id`                                             |
| `POST`   | `/api/graph/dependencies`         | Add a dependency edge                         | `{ "from": "a", "to": "b", "failureProbability": 0.3, "infraCost": 2.0 }` |
| `DELETE` | `/api/graph/dependencies`         | Remove a dependency edge                      | Query: `from`, `to`                                    |
| `PUT`    | `/api/graph/services/{id}/cost`   | Update restart cost of a service              | Query: `cost`                                          |
| `POST`   | `/api/graph/load`                 | Bulk-load entire graph (clears existing)      | `GraphLoadRequest` JSON (see [sample](#sample-graph-payload)) |
| `DELETE` | `/api/graph`                      | Clear the entire graph                        | —                                                      |
| `POST`   | `/api/graph/correlation-groups`   | Add a correlated failure group                | `{ "groupId": "...", "nodeIds": [...], "correlationFactor": 0.8 }` |
| `DELETE` | `/api/graph/correlation-groups/{groupId}` | Remove a correlation group            | Path: `groupId`                                        |

### 2. Simulation

| Method   | Endpoint                                | Description                                        | Params                |
|----------|-----------------------------------------|----------------------------------------------------|-----------------------|
| `POST`   | `/api/simulation/cascade`               | Simulate cascading failure from a node             | Query: `failedNode`   |
| `POST`   | `/api/simulation/temporal-cascade`      | Time-based cascade with propagation delays         | Query: `failedNode`   |

### 3. Recovery

| Method   | Endpoint                          | Description                                   | Params |
|----------|-----------------------------------|-----------------------------------------------|--------|
| `GET`    | `/api/recovery/plan`              | Get safe recovery plan (topo sort + critical chain) | — |
| `GET`    | `/api/recovery/cycle-check`       | Check if dependency graph has a cycle         | —      |

### 4. Optimization

| Method   | Endpoint                          | Description                                   | Params             |
|----------|-----------------------------------|-----------------------------------------------|--------------------|
| `GET`    | `/api/optimization/dijkstra`      | Min-cost recovery paths from source           | Query: `source`    |
| `GET`    | `/api/optimization/clusters`      | Detect independent service clusters           | —                  |
| `GET`    | `/api/optimization/mst`           | Compute minimum spanning tree                 | —                  |

### 5. Risk Analysis

| Method   | Endpoint                          | Description                                       | Params / Body                                         |
|----------|-----------------------------------|---------------------------------------------------|-------------------------------------------------------|
| `POST`   | `/api/risk/monte-carlo`           | Probabilistic cascade (parallel + CIs + weighted) | Query: `failedNode`, `iterations` (default: 1000)     |
| `GET`    | `/api/risk/critical-nodes`        | Rank nodes by failure impact                      | —                                                     |
| `GET`    | `/api/risk/resilience`            | System resilience & fragility score               | Query: `iterations` (default: 1000)                   |
| `GET`    | `/api/risk/sensitivity`           | Rank edges by hardening priority                  | Query: `iterations` (default: 500)                    |
| `POST`   | `/api/risk/what-if`              | Before/After architecture comparison               | `WhatIfRequest` JSON body                             |

### 6. Architecture

| Method   | Endpoint                          | Description                                   | Body                    |
|----------|-----------------------------------|-----------------------------------------------|-------------------------|
| `GET`    | `/api/architecture/score`         | Composite architecture health score (0-100)   | —                       |
| `POST`   | `/api/architecture/compare`       | Compare current vs. proposed architecture     | `GraphLoadRequest` JSON |

### 7. Health

| Method   | Endpoint                          | Description                                   | Body / Params                   |
|----------|-----------------------------------|-----------------------------------------------|---------------------------------|
| `PUT`    | `/api/health/node/{id}/state`     | Change node health state                      | `{ "state": "FAILED" }`        |
| `GET`    | `/api/health/dashboard`           | Live health overview                          | —                               |
| `POST`   | `/api/health/recovery-plan`       | Recovery plan for currently failed nodes      | —                               |

### 8. Recommendations

| Method   | Endpoint                          | Description                                        |
|----------|-----------------------------------|----------------------------------------------------|
| `GET`    | `/api/recommendations`            | Prioritized architecture improvement suggestions   |

**Total: 31 REST endpoints**

---

## Sample Graph Payload

Use this with `POST /api/graph/load` to load a 7-node microservice architecture:

```json
{
  "services": [
    { "id": "gateway",      "restartCost": 10.0, "recoveryTime": 5.0, "importanceWeight": 5.0 },
    { "id": "auth",         "restartCost": 5.0,  "recoveryTime": 3.0, "importanceWeight": 4.0 },
    { "id": "user-svc",     "restartCost": 3.0,  "recoveryTime": 2.0, "importanceWeight": 2.0 },
    { "id": "order-svc",    "restartCost": 7.0,  "recoveryTime": 4.0, "importanceWeight": 3.0 },
    { "id": "payment",      "restartCost": 8.0,  "recoveryTime": 6.0, "importanceWeight": 5.0 },
    { "id": "inventory",    "restartCost": 4.0,  "recoveryTime": 2.0, "importanceWeight": 2.0 },
    { "id": "notification", "restartCost": 2.0,  "recoveryTime": 1.0, "importanceWeight": 1.0 }
  ],
  "dependencies": [
    { "from": "gateway",   "to": "auth",         "failureProbability": 0.3,  "infraCost": 2.0, "propagationDelay": 2.0 },
    { "from": "gateway",   "to": "user-svc",     "failureProbability": 0.2,  "infraCost": 3.0, "propagationDelay": 1.5 },
    { "from": "gateway",   "to": "order-svc",    "failureProbability": 0.25, "infraCost": 4.0, "propagationDelay": 1.0 },
    { "from": "auth",      "to": "user-svc",     "failureProbability": 0.4,  "infraCost": 1.5, "propagationDelay": 0.5 },
    { "from": "order-svc", "to": "payment",      "failureProbability": 0.5,  "infraCost": 5.0, "propagationDelay": 3.0 },
    { "from": "order-svc", "to": "inventory",    "failureProbability": 0.35, "infraCost": 2.5, "propagationDelay": 2.0 },
    { "from": "payment",   "to": "notification", "failureProbability": 0.6,  "infraCost": 1.0, "propagationDelay": 1.0 },
    { "from": "inventory", "to": "notification", "failureProbability": 0.3,  "infraCost": 1.0, "propagationDelay": 0.5 }
  ]
}
```

> **Note:** `recoveryTime`, `importanceWeight`, and `propagationDelay` are optional — omitting them defaults to `0.0`, `1.0`, and `0.0` respectively. Existing payloads without these fields continue to work unchanged.

### Sample What-If Request

Use with `POST /api/risk/what-if`:

```json
{
  "addEdges": [
    { "from": "gateway", "to": "payment", "failureProbability": 0.1, "infraCost": 3.0 }
  ],
  "removeEdges": [
    { "from": "payment", "to": "notification" }
  ],
  "updateProbabilities": [
    { "from": "order-svc", "to": "payment", "failureProbability": 0.2 }
  ]
}
```

---

## How to Run

### Prerequisites

- **Java 21** (LTS) installed
- No database required (fully in-memory)

### Steps

```bash
# 1. Clone / navigate to the project
cd Graph-Engine

# 2. Build (uses Maven Wrapper — no Maven installation needed)
./mvnw.cmd compile        # Windows
./mvnw compile            # macOS/Linux

# 3. Run the application
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # macOS/Linux

# 4. Server starts on http://localhost:8080
```

### Quick Smoke Test

```bash
# Load the sample graph
curl -X POST http://localhost:8080/api/graph/load \
  -H "Content-Type: application/json" \
  -d '{"services":[{"id":"A","restartCost":5},{"id":"B","restartCost":3},{"id":"C","restartCost":2}],"dependencies":[{"from":"A","to":"B","failureProbability":0.4,"infraCost":2},{"from":"B","to":"C","failureProbability":0.5,"infraCost":1}]}'

# Simulate a cascade
curl -X POST "http://localhost:8080/api/simulation/cascade?failedNode=A"

# Temporal cascade (time-based)
curl -X POST "http://localhost:8080/api/simulation/temporal-cascade?failedNode=A"

# Monte Carlo with confidence intervals
curl -X POST "http://localhost:8080/api/risk/monte-carlo?failedNode=C&iterations=5000"

# Add a correlation group
curl -X POST http://localhost:8080/api/graph/correlation-groups \
  -H "Content-Type: application/json" \
  -d '{"groupId":"shared-rack","nodeIds":["A","B"],"correlationFactor":0.9}'

# Check architecture score
curl http://localhost:8080/api/architecture/score

# Get recommendations
curl http://localhost:8080/api/recommendations
```

---

## Testing Summary

All 3 tiers have been manually tested end-to-end with comprehensive curl tests.

### Tier 1 — 25 Test Cases ✅

| # | Test                                   | Result  |
|---|----------------------------------------|---------|
| 1 | GET empty graph                        | ✅ Pass |
| 2 | Add service node                       | ✅ Pass |
| 3 | Add dependency edge                    | ✅ Pass |
| 4 | Bulk load 7-node graph                 | ✅ Pass |
| 5 | GET full graph state                   | ✅ Pass |
| 6 | Cascade simulation (gateway)           | ✅ Pass |
| 7 | Cascade simulation (leaf node)         | ✅ Pass |
| 8 | Recovery plan                          | ✅ Pass |
| 9 | Cycle check (no cycle)                 | ✅ Pass |
| 10 | Dijkstra (auth)                       | ✅ Pass |
| 11 | Dijkstra (gateway)                    | ✅ Pass |
| 12 | Cluster detection                     | ✅ Pass |
| 13 | MST computation                       | ✅ Pass |
| 14 | Update restart cost                   | ✅ Pass |
| 15 | Remove dependency                     | ✅ Pass |
| 16 | Add dependency back                   | ✅ Pass |
| 17 | Remove service                        | ✅ Pass |
| 18 | Graph state after removal             | ✅ Pass |
| 19 | Re-add removed service                | ✅ Pass |
| 20 | Cascade from non-existent node (400)  | ✅ Pass |
| 21 | Dijkstra from non-existent node (400) | ✅ Pass |
| 22 | Duplicate add service (400)           | ✅ Pass |
| 23 | Remove non-existent service (400)     | ✅ Pass |
| 24 | Clear graph                           | ✅ Pass |
| 25 | Verify empty after clear              | ✅ Pass |

### Tier 2 — 9 Test Cases ✅

| # | Test                                   | Result  |
|---|----------------------------------------|---------|
| 1 | Load graph for T2 tests               | ✅ Pass |
| 2 | Monte Carlo simulation (1000 iters)   | ✅ Pass |
| 3 | Critical node detection                | ✅ Pass |
| 4 | Resilience report                      | ✅ Pass |
| 5 | Sensitivity analysis                   | ✅ Pass |
| 6 | What-If engine                         | ✅ Pass |
| 7 | Monte Carlo (non-existent node — 400) | ✅ Pass |
| 8 | What-If (empty request)               | ✅ Pass |
| 9 | T1 regression (cascade still works)   | ✅ Pass |

### Tier 3 — 12 Test Cases ✅

| # | Test                                       | Result  |
|---|--------------------------------------------|---------|
| 1 | Architecture Score (49.3/100 Grade D)      | ✅ Pass |
| 2 | Architecture Comparison (D→C, +4.8pts)     | ✅ Pass |
| 3 | Set node state to FAILED                   | ✅ Pass |
| 4 | Set node state to RECOVERING               | ✅ Pass |
| 5 | Health Dashboard (71.4% healthy, zones)    | ✅ Pass |
| 6 | Live Recovery Plan (2-step topo-sorted)    | ✅ Pass |
| 7 | Recommendations (15 suggestions, 5 cats)   | ✅ Pass |
| 8 | Invalid state error (400)                  | ✅ Pass |
| 9 | Non-existent node state change (400)       | ✅ Pass |
| 10 | T1 regression — cascade still works       | ✅ Pass |
| 11 | T2 regression — Monte Carlo still works   | ✅ Pass |
| 12 | T2 regression — resilience still works    | ✅ Pass |

**Total: 46/46 tests passed across all tiers**

### Performance Upgrades — 14 Test Cases ✅

| # | Test                                                       | Result  |
|---|------------------------------------------------------------|---------|
| 1 | Load graph with new fields (recoveryTime, importanceWeight, propagationDelay) | ✅ Pass |
| 2 | GET graph state shows all new fields                       | ✅ Pass |
| 3 | Backward-compatible add service (no new fields)            | ✅ Pass |
| 4 | Backward-compatible add dependency (no new fields)         | ✅ Pass |
| 5 | Temporal cascade — single node (gateway)                   | ✅ Pass |
| 6 | Temporal cascade — multi-level timeline (notification)     | ✅ Pass |
| 7 | Temporal cascade — probabilistic variation (5 runs)        | ✅ Pass |
| 8 | Monte Carlo — parallel execution (10K iterations)          | ✅ Pass |
| 9 | Monte Carlo — confidence intervals [lower, p, upper]       | ✅ Pass |
| 10 | Monte Carlo — weightedExpectedImpact field                | ✅ Pass |
| 11 | Weighted cascade — weightedImpactScore in cascade result  | ✅ Pass |
| 12 | Critical nodes — weightedImpactScore per node             | ✅ Pass |
| 13 | Correlation group — add/remove + Monte Carlo before/after | ✅ Pass |
| 14 | Full T1/T2/T3 regression after all upgrades               | ✅ Pass |

**Grand Total: 60/60 tests passed (46 original + 14 upgrade tests)**

---

## File Inventory

| Layer        | Count | Files                                                               |
|--------------|-------|---------------------------------------------------------------------|
| **Model**    | 5     | `ServiceState`, `ServiceNode`, `Edge`, `Graph`, `CorrelationGroup`  |
| **Algorithm**| 7     | `DFSUtil`, `TopoSortUtil`, `DijkstraUtil`, `DSU`, `MSTUtil`, `MonteCarloUtil`, `TemporalCascadeUtil` |
| **Service**  | 8     | `GraphService`, `SimulationService`, `RecoveryService`, `OptimizationService`, `RiskAnalysisService`, `ArchitectureService`, `HealthService`, `RecommendationService` |
| **Controller**| 5    | `GraphController`, `RiskController`, `ArchitectureController`, `HealthController`, `RecommendationController` |
| **DTO Request** | 6  | `AddServiceRequest`, `AddDependencyRequest`, `GraphLoadRequest`, `WhatIfRequest`, `NodeStateRequest`, `AddCorrelationGroupRequest` |
| **DTO Response** | 17 | `GraphStateResponse`, `CascadeResult`, `TemporalCascadeResult`, `RecoveryResult`, `DijkstraResult`, `ClusterResult`, `MSTResult`, `MonteCarloResult`, `CriticalNodeResult`, `ResilienceReport`, `SensitivityResult`, `WhatIfResult`, `ArchitectureScoreResult`, `ArchitectureComparisonResult`, `HealthDashboard`, `LiveRecoveryPlan`, `RecommendationResult` |
| **Exception**| 2     | `GraphValidationException`, `GlobalExceptionHandler`                |
| **Config**   | 1     | `WebConfig`                                                         |
| **Main**     | 1     | `GraphEngineApplication`                                            |
| **Total**    | **52**| All compile with zero errors                                        |

---

## Design Principles

| Principle                    | Implementation                                                            |
|------------------------------|---------------------------------------------------------------------------|
| **Additive-Only Tiers**      | Each tier adds new files without modifying lower-tier code                |
| **Layered Architecture**     | Controller → Service → Algorithm → Model (strict one-way dependency)     |
| **In-Memory Graph**          | Single `Graph` instance held by `GraphService` — no database, no ORM     |
| **Synchronized Thread-Safety** | All `Graph` mutations are `synchronized` methods                       |
| **Stateless REST**           | No session state — all state is in the graph; API is fully idempotent    |
| **Immutable DTOs**           | All request/response DTOs are Java `record` types (immutable by design)  |
| **Global Error Handling**    | `@RestControllerAdvice` catches `GraphValidationException` → 400 JSON    |
| **CORS Enabled**             | `WebConfig` allows all origins on `/api/**` for frontend integration     |
| **No Over-Engineering**      | No database, no caching, no message queues — minimal dependencies only   |
| **Algorithm Separation**     | Each algorithm is its own `@Component` — testable, swappable, reusable   |
| **Backward Compatibility**   | All new DTO fields use `Double` wrappers with null-safe compact constructors — existing JSON payloads work unchanged |
| **Lock-Free Concurrency**    | Parallel Monte Carlo uses `ConcurrentHashMap` + `AtomicLong` for safe accumulation without locks |

---

## What's Next

Potential next steps to extend this engine:

| Option                  | Description                                                                |
|-------------------------|----------------------------------------------------------------------------|
| **React Frontend**      | Graph visualization, dashboards, health monitoring, score gauges           |
| **Postman Collection**  | Organized collection of all 31 endpoints for demo/testing                  |
| **Unit Tests**          | JUnit 5 test classes for all services and controllers                      |
| **API Documentation**   | Swagger/OpenAPI specs for interactive docs                                 |
| **Docker**              | Containerize with Dockerfile + docker-compose for deployment               |

---

## License

This project is developed as an academic/research simulation engine.

---

*Built with Spring Boot 4.0.3 • Java 21 • 52 source files • 31 REST endpoints • 9 graph algorithms • 3 tiers + 5 performance upgrades*
