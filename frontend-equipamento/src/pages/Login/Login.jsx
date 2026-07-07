import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import SSBLogo from "../../assets/SSBLogo.png";
import { AuthContext } from "../../contexts/AuthContext";
import { loginUsuario } from "../../services/api";
import "./Login.css";

function Login() {
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);
  const [email, setEmail] = useState("");
  const [nome, setNome] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!email.trim() || !nome.trim()) {
      alert("Por favor, preencha todos os campos!");
      return;
    }

    try {
      const response = await loginUsuario({ email, nome });
      const usuario = response.data;

      login(usuario);

      if (usuario.perfil === "ADMIN") {
        navigate("/admin");
      } else {
        navigate("/home");
      }
    } catch (error) {
      console.error("Erro no login:", error);
      alert(error.response?.data?.message || "Erro ao acessar o sistema.");
    }
  };

  return (
    <div className="login-container">
      <form className="login-box" onSubmit={handleLogin}>
        <img className="logo" src={SSBLogo} alt="Logo Smart Item Booking" />

        <h4 className="subtitle">
          Acesse sua conta para gerenciar seus espaços.
        </h4>
        <div className="input-group">
          <label htmlFor="nome">Nome Completo</label>
          <input
            id="nome"
            type="text"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            placeholder="Digite seu nome"
            required
          />
        </div>

        <div className="input-group">
          <label htmlFor="email">Email Corporativo</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Digite seu email"
            required
          />
        </div>

        <button type="submit" className="btn-primary">
          Entrar
        </button>
      </form>
    </div>
  );
}

export default Login;
