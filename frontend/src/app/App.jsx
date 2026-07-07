import { Navigate, Route, Routes } from "react-router-dom";
import AcessoNegado from "../pages/AcessoNegado/AcessoNegado";
import Admin from "../pages/AdminPages/Admin";
import CadastroSala from "../pages/AdminPages/CadastroSala";
import EditarSala from "../pages/AdminPages/EditarSala";
import RegrasAvaliacao from "../pages/AdminPages/RegrasAvaliacao";
import RegrasTrustScore from "../pages/AdminPages/RegrasTrustScore";
import CheckinReserva from "../pages/CheckinCheckout/CheckinReserva";
import CheckoutReserva from "../pages/CheckinCheckout/CheckoutReserva";
import Home from "../pages/Home/Home";
import Login from "../pages/Login/Login";
import Perfil from "../pages/Perfil/Perfil";
import Reserva from "../pages/Reserva/Reserva";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./contexts/AuthContext";

import AdminVeiculo from "../instancia_veiculos/pages/AdminPages/Admin";
// import CadastroVeiculo from "./instancia_veiculos/pages/AdminPages/CadastroVeiculo";
// import EditarVeiculo from "./instancia_veiculos/pages/AdminPages/EditarVeiculo";

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
                <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
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

            {/* Rotas Exclusivas de ADMIN — instância Sala */}
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

            {/* Rotas Exclusivas de ADMIN — instância Veículo */}
            <Route
              path="/admin/veiculos"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <AdminVeiculo />
                </ProtectedRoute>
              }
            />
            {/* <Route
              path="/cadastrar-veiculo"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <CadastroVeiculo />
                </ProtectedRoute>
              }
            />
            <Route
              path="/editar-veiculo/:id"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <EditarVeiculo />
                </ProtectedRoute>
              }
            /> */}

            {/* Rotas Exclusivas de ADMIN — compartilhadas entre instâncias */}
            <Route
              path="/regras-avaliacao"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <RegrasAvaliacao />
                </ProtectedRoute>
              }
            />
            <Route
              path="/regras-trust-score"
              element={
                <ProtectedRoute allowedRoles={["ADMIN"]}>
                  <RegrasTrustScore />
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
