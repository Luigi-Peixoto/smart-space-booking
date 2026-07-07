import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./contexts/AuthContext";
import AcessoNegado from "./pages/AcessoNegado/AcessoNegado";
import Login from "./pages/Login/Login";

// --- DOMÍNIO: SALAS ---
import AdminSalas from "./pages/AdminPages/Admin";
import CadastroSala from "./pages/AdminPages/CadastroSala";
import EditarSala from "./pages/AdminPages/EditarSala";
import RegrasAvaliacaoSala from "./pages/AdminPages/RegrasAvaliacao";
import RegrasTrustScoreSala from "./pages/AdminPages/RegrasTrustScore";
import CheckinReservaSala from "./pages/CheckinCheckout/CheckinReserva";
import CheckoutReservaSala from "./pages/CheckinCheckout/CheckoutReserva";
import HomeSalas from "./pages/Home/Home";
import PerfilSala from "./pages/Perfil/Perfil";
import ReservaSala from "./pages/Reserva/Reserva";

// --- DOMÍNIO: VEÍCULOS ---
import AdminVeiculos from "./domains/vehicles/pages/AdminPages/Admin";
import CadastroVeiculo from "./domains/vehicles/pages/AdminPages/CadastroVeiculo";
import EditarVeiculo from "./domains/vehicles/pages/AdminPages/EditarVeiculo";
import RegrasAvaliacaoVeiculo from "./domains/vehicles/pages/AdminPages/RegrasAvaliacaoVeiculo";
import RegrasTrustScoreVeiculo from "./domains/vehicles/pages/AdminPages/RegrasTrustScoreVeiculo";
import CheckinReservaVeiculo from "./domains/vehicles/pages/CheckinCheckout/CheckinReservaVeiculo";
import CheckoutReservaVeiculo from "./domains/vehicles/pages/CheckinCheckout/CheckoutReservaVeiculo";
import HomeVeiculos from "./domains/vehicles/pages/Home/HomeVeiculos";
import PerfilVeiculo from "./domains/vehicles/pages/Perfil/PerfilVeiculo";
import ReservaVeiculo from "./domains/vehicles/pages/Reserva/ReservaVeiculo";

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
            {/* =========================================
                MÓDULO: SALAS (Prefixo /salas)
                ========================================= */}
            <Route path="salas">
              <Route
                path="home"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <HomeSalas />
                  </ProtectedRoute>
                }
              />
              <Route
                path="perfil"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <PerfilSala />
                  </ProtectedRoute>
                }
              />
              <Route
                path="criar-reserva/:id"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <ReservaSala />
                  </ProtectedRoute>
                }
              />
              <Route
                path="checkin/:id"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <CheckinReservaSala />
                  </ProtectedRoute>
                }
              />
              <Route
                path="checkout/:id"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <CheckoutReservaSala />
                  </ProtectedRoute>
                }
              />

              {/* Rotas de Admin de Salas */}
              <Route
                path="admin"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <AdminSalas />
                  </ProtectedRoute>
                }
              />
              <Route
                path="cadastrar-sala"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <CadastroSala />
                  </ProtectedRoute>
                }
              />
              <Route
                path="editar-sala/:id"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <EditarSala />
                  </ProtectedRoute>
                }
              />
              <Route
                path="regras-avaliacao"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <RegrasAvaliacaoSala />
                  </ProtectedRoute>
                }
              />
              <Route
                path="regras-trust-score"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <RegrasTrustScoreSala />
                  </ProtectedRoute>
                }
              />
            </Route>

            {/* =========================================
                MÓDULO: VEÍCULOS (Prefixo /veiculos)
                ========================================= */}
            <Route path="veiculos">
              <Route
                path="home"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <HomeVeiculos />
                  </ProtectedRoute>
                }
              />
              <Route
                path="perfil"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <PerfilVeiculo />
                  </ProtectedRoute>
                }
              />

              {/* Rota de Admin de Veículos */}
              <Route
                path="admin"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <AdminVeiculos />
                  </ProtectedRoute>
                }
              />
              <Route
                path="cadastrar-veiculo"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <CadastroVeiculo />
                  </ProtectedRoute>
                }
              />
              <Route
                path="editar-veiculo/:id"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <EditarVeiculo />
                  </ProtectedRoute>
                }
              />
              <Route
                path="regras-avaliacao"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <RegrasAvaliacaoVeiculo />
                  </ProtectedRoute>
                }
              />
              <Route
                path="regras-trust-score"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN"]}>
                    <RegrasTrustScoreVeiculo />
                  </ProtectedRoute>
                }
              />

              <Route
                path="checkin/:id"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <CheckinReservaVeiculo />
                  </ProtectedRoute>
                }
              />
              <Route
                path="checkout/:id"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <CheckoutReservaVeiculo />
                  </ProtectedRoute>
                }
              />

              <Route
                path="criar-reserva/:id"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
                    <ReservaVeiculo />
                  </ProtectedRoute>
                }
              />
            </Route>
          </Route>
        </Routes>
      </div>
    </AuthProvider>
  );
}

export default App;
