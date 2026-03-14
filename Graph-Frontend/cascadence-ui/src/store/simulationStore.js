import { create } from "zustand";
import { simulationService } from "../services/simulationService";
import { riskService } from "../services/riskService";

export const useSimulationStore = create((set) => ({
  cascadeResult: null,
  temporalResult: null,
  monteCarloResult: null,
  criticalNodes: null,
  resilience: null,
  sensitivity: null,
  whatIfResult: null,
  loading: false,
  error: null,

  runCascade: async (failedNode) => {
    set({ loading: true, error: null, cascadeResult: null });
    try {
      const res = await simulationService.runCascade(failedNode);
      set({ cascadeResult: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "Cascade simulation failed", loading: false });
    }
  },

  runTemporalCascade: async (failedNode) => {
    set({ loading: true, error: null, temporalResult: null });
    try {
      const res = await simulationService.runTemporalCascade(failedNode);
      set({ temporalResult: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "Temporal cascade failed", loading: false });
    }
  },

  runMonteCarlo: async (failedNode, iterations) => {
    set({ loading: true, error: null, monteCarloResult: null });
    try {
      const res = await riskService.runMonteCarlo(failedNode, iterations);
      set({ monteCarloResult: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "Monte Carlo simulation failed", loading: false });
    }
  },

  fetchCriticalNodes: async () => {
    set({ loading: true, error: null });
    try {
      const res = await riskService.getCriticalNodes();
      set({ criticalNodes: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to fetch critical nodes", loading: false });
    }
  },

  fetchResilience: async (iterations) => {
    set({ loading: true, error: null });
    try {
      const res = await riskService.getResilience(iterations);
      set({ resilience: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to fetch resilience", loading: false });
    }
  },

  fetchSensitivity: async (iterations) => {
    set({ loading: true, error: null });
    try {
      const res = await riskService.getSensitivity(iterations);
      set({ sensitivity: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "Failed to fetch sensitivity", loading: false });
    }
  },

  runWhatIf: async (data) => {
    set({ loading: true, error: null, whatIfResult: null });
    try {
      const res = await riskService.runWhatIf(data);
      set({ whatIfResult: res.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.error || "What-if analysis failed", loading: false });
    }
  },

  clearResults: () =>
    set({
      cascadeResult: null,
      temporalResult: null,
      monteCarloResult: null,
      whatIfResult: null,
      error: null,
    }),

  clearError: () => set({ error: null }),
}));
