import React, { type ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";

function ProtectedRoute({ children }: { children: ReactNode }) {
  const location = useLocation();
  const token = localStorage.getItem("collabmatch_access_token") || localStorage.getItem("collabmatch_token");

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}

export default ProtectedRoute;
