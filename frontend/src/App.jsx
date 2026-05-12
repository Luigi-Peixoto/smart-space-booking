import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import Admin from "./pages/AdminPages/Admin";
import CadastroSala from "./pages/AdminPages/CadastroSala";
import EditarSala from "./pages/AdminPages/EditarSala";
import RegrasAvaliacao from "./pages/AdminPages/RegrasAvaliacao";
import Login from "./pages/Login/Login";
import Home from "./pages/Home/Home";
import Reserva from "./pages/Reserva/Reserva";
import ProtectedRoute from "./components/ProtectedRoute";
import AcessoNegado from "./pages/AcessoNegado/AcessoNegado";
import Perfil from "./pages/Perfil/Perfil";
import Layout from "./components/Layout";
import CheckinReserva from "./pages/CheckinCheckout/CheckinReserva";
import CheckoutReserva from "./pages/CheckinCheckout/CheckoutReserva";

function App() {
  return (
    <AuthProvider>
      <div className="app-container">
        <Routes>
          {/* Rotas Públicas */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/acesso-negado" element={<AcessoNegado />} />

          <Route element={<Layout />}>
            {/* Rotas exclusivas de USER e ADMIN */}
            <Route
              path="/home"
              element={
                <ProtectedRoute allowedRoles={['USER', 'ADMIN']}>
                  <Home />
                </ProtectedRoute>
              }
            />
            <Route
              path="/criar-reserva/:id"
              element={
                <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                  <Reserva />
                </ProtectedRoute>
              }
            />
            <Route
              path="/checkin/:id"
              element={
                <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                  <CheckinReserva />
                </ProtectedRoute>
              }
            />
            <Route
              path="/checkout/:id"
              element={
                <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                  <CheckoutReserva />
                </ProtectedRoute>
              }
            />
            <Route
              path="/perfil"
              element={
                <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                  <Perfil />
                </ProtectedRoute>
              }
            />

            {/* Rotas Exclusivas de ADMIN */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <Admin />
                </ProtectedRoute>
              }
            />
            <Route
              path="/cadastrar-sala"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <CadastroSala />
                </ProtectedRoute>
              }
            />
            <Route
              path="/editar-sala/:id"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <EditarSala />
                </ProtectedRoute>
              }
            />
            <Route
              path="/regras-avaliacao"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <RegrasAvaliacao />
                </ProtectedRoute>
              }
            />
          </Route>
        </Routes>
      </div>
    </AuthProvider>
  );
}

export default App;