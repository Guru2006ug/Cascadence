import { api } from "../api/apiClient";

export const architectureService = {
  getScore: () => api.get("/architecture/score"),

  compareArchitecture: (proposedGraph) =>
    api.post("/architecture/compare", proposedGraph),
};
