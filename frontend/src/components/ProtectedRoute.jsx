import { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "../contexts/AuthContext";

function ProtectedRoute({ children, allowedRoles }) {
  const { user, loadingAuth } = useContext(AuthContext);

  if (loadingAuth) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '50px' }}>
        <h2>Sincronizando sessão...</h2>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.perfil)) {
    return <Navigate to="/acesso-negado" replace />;
  }

  return children;
}

export default ProtectedRoute;