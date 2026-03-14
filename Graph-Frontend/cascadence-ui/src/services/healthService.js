import { api } from "../api/apiClient";

export const healthService = {
  getDashboard: () => api.get("/health/dashboard"),

  updateNodeState: (id, state) =>
    api.put(`/health/node/${encodeURIComponent(id)}/state`, { state }),

  getRecoveryPlan: () => api.post("/health/recovery-plan"),
};
