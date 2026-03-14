import { api } from "../api/apiClient";

export const recommendationService = {
  getRecommendations: () => api.get("/recommendations"),
};
