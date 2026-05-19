import { createContext, useEffect, useState } from "react";
import { getUsuarioById } from "../services/api";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem("@SSB:user");
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [loadingAuth, setLoadingAuth] = useState(true);

  const refreshUser = async () => {
    if (user && user.id) {
      try {
        const response = await getUsuarioById(user.id);
        const usuarioAtualizado = response.data;
        setUser(usuarioAtualizado);
        localStorage.setItem("@SSB:user", JSON.stringify(usuarioAtualizado));
      } catch (error) {
        console.error("Erro ao sincronizar dados do usuário:", error);
      } finally {
        setLoadingAuth(false);
      }
    } else {
      setLoadingAuth(false);
    }
  };

  useEffect(() => {
    refreshUser();
  }, []);

  const login = (userData) => {
    setUser(userData);

    localStorage.setItem("@SSB:user", JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);

    localStorage.removeItem("@SSB:user");
  };

  return (
    <AuthContext.Provider
      value={{ user, setUser, login, logout, refreshUser, loadingAuth }}
    >
      {children}
    </AuthContext.Provider>
  );
};
