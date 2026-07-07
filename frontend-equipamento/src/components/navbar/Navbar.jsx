import { useContext } from "react";
import { useSearchParams, useLocation, useNavigate } from "react-router-dom";
import { AuthContext } from "../../contexts/AuthContext";
import SSBLogo from "../../assets/SSBLogo.png";
import "./Navbar.css";

function Navbar() {
  const { user, logout } = useContext(AuthContext);

  const [searchParams, setSearchParams] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();

  const termoBusca = searchParams.get("busca") || "";

  const handleBuscaChange = (e) => {
    const valorDigitado = e.target.value;

    if (location.pathname !== "/admin" && location.pathname !== "/home") {
      const rotaDestino = user?.perfil === "ADMIN" ? "/admin" : "/home";
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

  return (
    <header className="global-navbar">
      <div
        className="navbar-logo-container clickable"
        onClick={() => {user?.perfil === "ADMIN" ? navigate("/admin") : navigate("/home")}
          

        }
      >
        <img className="navbar-logo" src={SSBLogo} alt="Smart Item Booking" />
      </div>

      <div className="navbar-search">
        <span className="material-icons search-icon">search</span>
        <input
          type="text"
          placeholder="Pesquise um equipamento"
          value={termoBusca}
          onChange={handleBuscaChange}
        />
      </div>

      <div className="navbar-actions">
        <div
          className="navbar-user clickable"
          onClick={() => navigate("/perfil")}
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
