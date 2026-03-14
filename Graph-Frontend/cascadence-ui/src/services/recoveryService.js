import { api } from "../api/apiClient";

export const recoveryService = {
  getRecoveryPlan: () => api.get("/recovery/plan"),

  checkCycle: () => api.get("/recovery/cycle-check"),
};
