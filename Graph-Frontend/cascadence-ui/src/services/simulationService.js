import { api } from "../api/apiClient";

export const simulationService = {
  runCascade: (failedNode) =>
    api.post("/simulation/cascade", null, { params: { failedNode } }),

  runTemporalCascade: (failedNode) =>
    api.post("/simulation/temporal-cascade", null, { params: { failedNode } }),
};
