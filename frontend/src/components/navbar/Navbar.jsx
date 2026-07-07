import { useContext } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import SSBLogo from "../../assets/SSBLogo.png";
import { AuthContext } from "../../contexts/AuthContext";
import "./Navbar.css";

function Navbar() {
  const { user, logout } = useContext(AuthContext);

  const [searchParams, setSearchParams] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();

  const termoBusca = searchParams.get("busca") || "";

  const pathSegments = location.pathname.split("/").filter(Boolean);
  const moduloAtual = pathSegments[0] || "salas";

  const basePath = `/${moduloAtual}`;
  const homePath = `${basePath}/home`;
  const adminPath = `${basePath}/admin`;
  const perfilPath = `${basePath}/perfil`;

  const handleBuscaChange = (e) => {
    const valorDigitado = e.target.value;

    if (location.pathname !== adminPath && location.pathname !== homePath) {
      const rotaDestino = user?.perfil === "ADMIN" ? adminPath : homePath;
      navigate(`${rotaDestino}?busca=${valorDigitado}`);
      return;
    }

    if (valorDigitado) {
      setSearchParams({ busca: valorDigitado });
    } else {
      setSearchParams({});
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const getPlaceholder = () => {
    if (moduloAtual === "veiculos") return "Pesquise um veículo";
    if (moduloAtual === "equipamentos") return "Pesquise um equipamento";
    return "Pesquise uma sala";
  };

  return (
    <header className="global-navbar">
      <div
        className="navbar-logo-container clickable"
        onClick={() => {
          navigate(user?.perfil === "ADMIN" ? adminPath : homePath);
        }}
      >
        <img className="navbar-logo" src={SSBLogo} alt="SSB Logo" />
      </div>

      <div className="navbar-search">
        <span className="material-icons search-icon">search</span>
        <input
          type="text"
          placeholder={getPlaceholder()}
          value={termoBusca}
          onChange={handleBuscaChange}
        />
      </div>

      <div className="navbar-actions">
        <div
          className="navbar-user clickable"
          onClick={() => navigate(perfilPath)}
        >
          <span className="user-icon material-icons">account_circle</span>
          <span className="user-role">
            {user?.perfil === "ADMIN" ? "Admin" : "User"}
          </span>
        </div>
        <div className="navbar-divider"></div>

        <button
          className="navbar-logout-btn clickable"
          onClick={handleLogout}
          title="Sair do sistema"
        >
          <span className="material-icons">logout</span>
        </button>
      </div>
    </header>
  );
}

export default Navbar;
