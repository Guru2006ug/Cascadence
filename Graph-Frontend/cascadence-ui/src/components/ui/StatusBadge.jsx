export default function StatusBadge({ status, className = "" }) {
  const styles = {
    HEALTHY: "bg-success/15 text-success border-success/30",
    FAILED: "bg-danger/15 text-danger border-danger/30",
    RECOVERING: "bg-warning/15 text-warning border-warning/30",
    HIGH: "bg-danger/15 text-danger border-danger/30",
    MEDIUM: "bg-warning/15 text-warning border-warning/30",
    LOW: "bg-info/15 text-info border-info/30",
    CRITICAL: "bg-danger/15 text-danger border-danger/30",
  };

  return (
    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[10px] font-semibold ${styles[status] || "bg-bg-secondary text-text-muted border-border"} ${className}`}>
      {status}
    </span>
  );
}
