import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 animate-in">
      <span className="text-7xl font-bold text-text-muted">404</span>
      <p className="text-base text-text-secondary">Page not found</p>
      <Link to="/" className="btn-primary no-underline">
        Back to Dashboard
      </Link>
    </div>
  );
}
