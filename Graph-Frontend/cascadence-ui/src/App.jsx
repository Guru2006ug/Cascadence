import { BrowserRouter, Routes, Route } from "react-router-dom";
import Navbar from "./layout/Navbar";
import Sidebar from "./layout/Sidebar";
import ErrorBoundary from "./components/ui/ErrorBoundary";
import Dashboard from "./pages/Dashboard";
import GraphModel from "./pages/GraphModel";
import Simulation from "./pages/Simulation";
import RiskAnalysis from "./pages/RiskAnalysis";
import Architecture from "./pages/Architecture";
import Health from "./pages/Health";
import Recommendations from "./pages/Recommendations";
import Scenario from "./pages/Scenario";
import Optimization from "./pages/Optimization";
import NotFound from "./pages/NotFound";

function App() {
  return (
    <BrowserRouter>
      <div className="flex h-screen flex-col overflow-hidden">
        <Navbar />
        <div className="flex flex-1 overflow-hidden">
          <Sidebar />
          <main className="flex-1 overflow-y-auto bg-bg-primary">
            <ErrorBoundary>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/graph" element={<GraphModel />} />
                <Route path="/simulation" element={<Simulation />} />
                <Route path="/risk" element={<RiskAnalysis />} />
                <Route path="/architecture" element={<Architecture />} />
                <Route path="/health" element={<Health />} />
                <Route path="/recommendations" element={<Recommendations />} />
                <Route path="/scenario" element={<Scenario />} />
                <Route path="/optimization" element={<Optimization />} />
                <Route path="*" element={<NotFound />} />
              </Routes>
            </ErrorBoundary>
          </main>
        </div>
      </div>
    </BrowserRouter>
  );
}

export default App;
