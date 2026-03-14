import { useState, useEffect, useRef } from "react";

export default function TemporalTimeline({ temporalResult }) {
  if (!temporalResult) return null;
  const [animIndex, setAnimIndex] = useState(-1);
  const [isPlaying, setIsPlaying] = useState(false);
  const timerRef = useRef(null);

  const {
    failedNode,
    timeline,
    cascadeDuration,
    timeToSystemCollapse,
    peakFailureMoment,
    peakConcurrentFailures,
    totalAffected,
    totalNodes,
    impactScore,
    weightedImpactScore,
  } = temporalResult;

  const play = () => {
    setAnimIndex(-1);
    setIsPlaying(true);
  };

  const stop = () => {
    setIsPlaying(false);
    if (timerRef.current) clearTimeout(timerRef.current);
  };

  useEffect(() => {
    if (!isPlaying || !timeline?.length) return;

    if (animIndex < timeline.length - 1) {
      const currentTime = animIndex >= 0 ? timeline[animIndex].failureTime : 0;
      const nextTime = timeline[animIndex + 1].failureTime;
      const delay = Math.max(200, (nextTime - currentTime) * 400);

      timerRef.current = setTimeout(() => {
        setAnimIndex((prev) => prev + 1);
      }, delay);
    } else {
      setIsPlaying(false);
    }

    return () => { if (timerRef.current) clearTimeout(timerRef.current); };
  }, [animIndex, isPlaying, timeline]);

  const impactPercent = (impactScore * 100).toFixed(1);

  return (
    <div className="flex flex-col gap-4">
      {/* Controls & Stats */}
      <div className="flex items-center gap-3">
        <button onClick={isPlaying ? stop : play} className="btn-primary !py-2 !px-4 !text-xs">
          {isPlaying ? "⏸ Pause" : "▶ Play Cascade"}
        </button>
        <button onClick={() => { stop(); setAnimIndex(timeline.length - 1); }} className="btn-ghost !py-2 !px-3 !text-xs !text-text-secondary !bg-bg-card !border-border">Skip to End</button>
        <button onClick={() => { stop(); setAnimIndex(-1); }} className="btn-ghost !py-2 !px-3 !text-xs !text-text-secondary !bg-bg-card !border-border">Reset</button>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-4 gap-3">
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-text-primary">{cascadeDuration?.toFixed(1)}s</p>
          <p className="text-[10px] text-text-muted">Duration</p>
        </div>
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-danger">
            {timeToSystemCollapse >= 0 ? `${timeToSystemCollapse.toFixed(1)}s` : "Never"}
          </p>
          <p className="text-[10px] text-text-muted">50% Collapse</p>
        </div>
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-warning">{peakConcurrentFailures}</p>
          <p className="text-[10px] text-text-muted">Peak Concurrent</p>
        </div>
        <div className="rounded-xl border border-border bg-bg-card p-3 text-center">
          <p className="text-lg font-bold text-accent">{impactPercent}%</p>
          <p className="text-[10px] text-text-muted">Impact</p>
        </div>
      </div>

      {/* Timeline */}
      <div className="card">
        <p className="text-xs font-semibold text-text-primary mb-3">Temporal Event Log</p>
        <div className="relative flex flex-col gap-0">
          {timeline?.map((event, idx) => {
            const isRevealed = animIndex >= idx;
            const isLatest = animIndex === idx;
            return (
              <div
                key={idx}
                className={`relative flex items-start gap-3 border-l-2 py-2 pl-4 transition-all duration-300 ${
                  isRevealed
                    ? isLatest
                      ? "border-danger bg-danger/5"
                      : "border-warning/50"
                    : "border-border opacity-30"
                }`}
              >
                {/* Time dot */}
                <div className={`absolute -left-[5px] top-3 h-2 w-2 rounded-full ${
                  isRevealed ? (isLatest ? "bg-danger animate-pulse" : "bg-warning") : "bg-border"
                }`} />

                {/* Content */}
                <span className="shrink-0 font-mono text-[10px] text-text-muted w-12">
                  t={event.failureTime?.toFixed(1)}s
                </span>
                <div className="flex-1">
                  <span className={`text-xs font-bold ${isRevealed ? "text-text-primary" : "text-text-muted"}`}>
                    {event.nodeId}
                  </span>
                  <span className="ml-2 text-[10px] text-text-muted">
                    L{event.cascadeLevel} {event.triggeredBy && `← ${event.triggeredBy}`}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
