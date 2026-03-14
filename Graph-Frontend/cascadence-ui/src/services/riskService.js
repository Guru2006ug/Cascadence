import { api } from "../api/apiClient";

export const riskService = {
  runMonteCarlo: (failedNode, iterations = 1000) =>
    api.post("/risk/monte-carlo", null, { params: { failedNode, iterations } }),

  getCriticalNodes: () => api.get("/risk/critical-nodes"),

  getResilience: (iterations = 1000) =>
    api.get("/risk/resilience", { params: { iterations } }),

  getSensitivity: (iterations = 500) =>
    api.get("/risk/sensitivity", { params: { iterations } }),

  runWhatIf: (data) => api.post("/risk/what-if", data),
};
