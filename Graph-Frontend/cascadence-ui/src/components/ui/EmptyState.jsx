export default function EmptyState({ icon, title, message, action }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-border bg-bg-card p-12 text-center animate-in">
      {icon && <span className="text-4xl mb-3 opacity-40">{icon}</span>}
      {title && <p className="text-sm font-semibold text-text-secondary">{title}</p>}
      {message && <p className="mt-1 text-xs text-text-muted max-w-xs">{message}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
