import { createContext, useEffect, useState } from "react";
import { getUsuarioById } from "../services/api";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem("@SIB:user");
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [loadingAuth, setLoadingAuth] = useState(true);

  const refreshUser = async () => {
    if (user && user.id) {
      try {
        const response = await getUsuarioById(user.id);
        const usuarioAtualizado = response.data;
        setUser(usuarioAtualizado);
        localStorage.setItem("@SIB:user", JSON.stringify(usuarioAtualizado));
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

    localStorage.setItem("@SIB:user", JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);

    localStorage.removeItem("@SIB:user");
  };

  return (
    <AuthContext.Provider
      value={{ user, setUser, login, logout, refreshUser, loadingAuth }}
    >
      {children}
    </AuthContext.Provider>
  );
};
