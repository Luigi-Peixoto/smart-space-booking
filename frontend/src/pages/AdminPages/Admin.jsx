import { useState, useEffect } from 'react';
import { AuthContext } from "../../contexts/AuthContext";
import { useSearchParams, useNavigate } from 'react-router-dom';
import { deletarSala, getSalas } from '../../services/api';
import './Admin.css';
import './RegrasAvaliacao.css';
import '../../App.css';
import imagemMockada from '../../assets/mockImagemSala.jpg';

const FILE_SERVER_URL = import.meta.env.VITE_API_FILES_URL;

function Admin() {
  const { user } = useContext(AuthContext);
  const [salas, setSalas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const termoBusca = searchParams.get('busca') || "";
  const navigate = useNavigate();

  useEffect(() => {
    async function carregarSalas() {
      try {
        const response = await getSalas();
        setSalas(response.data);
      } catch (error) {
        console.error("Erro ao carregar salas:", error);
        alert("Não conseguimos carregar as salas no momento.");
      } finally {
        setLoading(false);
      }
    }
    carregarSalas();
  }, []);

  const salasFiltradas = salas.filter((sala) => {
    const busca = termoBusca.toLowerCase();
    return (
      sala.nome.toLowerCase().includes(busca) ||
      sala.local.toLowerCase().includes(busca)
    );
  });

  const handleDelete = async (id) => {
    try {
      await deletarSala(id, user.id);
      alert("Sala removida com sucesso!");
      setSalas(prev => prev.filter(sala => sala.id !== id));
    } catch (error) {
      console.error("Erro ao deletar:", error);
      if (error.response?.status === 404) {
        setSalas(prev => prev.filter(sala => sala.id !== id));
      } else {
        alert("Erro ao excluir: verifique se a sala possui vínculos ativos.");
      }
    }
  };

  if (loading) return <div className="p-10 text-center text-xl font-bold">Carregando salas...</div>;

  return (
    <div className="admin-container">
      <main className="admin-main">

        <div className="page-header">
          <h1 className="page-title">Gerenciar Salas</h1>
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
            {/* Botão para navegar para as Regras de Avaliação */}
            <button
              className="btn-regras"
              onClick={() => navigate('/regras-avaliacao')}
            >
              <span className="material-icons">rule</span>
              Regras de Avaliação
            </button>
            <button className="btn-primary btn-addsala" onClick={() => navigate('/cadastrar-sala')}>
              <span className="material-icons">add</span>
              Nova Sala
            </button>
          </div>
        </div>

        <section className="rooms-grid">
          {salasFiltradas.length === 0 ? (
            <p>Nenhuma sala encontrada.</p>
          ) : (
            salasFiltradas.map(sala => (
              <div key={sala.id} className="room-card">
                <div className="room-card-main-content">
                  <div className="room-text-content">
                    <h3 className="room-title">{sala.nome}</h3>
                    <p className="room-info">
                      <span className="material-icons">place</span> {sala.local}
                    </p>
                    <p className="room-info">
                      <span className="material-icons">groups</span> {sala.capacidade} pessoas
                    </p>
                  </div>
                  <div className="room-image-container">
                    <img
                      src={`${FILE_SERVER_URL}/${sala.imagens[0]}` || imagemMockada}
                      alt={sala.nome}
                      className="room-card-img"
                    />
                  </div>
                </div>

                <div className="sala-features">
                  {sala.caracteristicas && sala.caracteristicas.map((feature, index) => (
                    <span key={index} className="feature-tag">{feature}</span>
                  ))}
                </div>

                <div className="room-card-footer">
                  <div className="room-actions">
                    <span className="material-icons action-icon" onClick={() => navigate('/editar-sala/' + sala.id)}>edit</span>
                    <span className="material-icons action-icon delete" onClick={() => handleDelete(sala.id)}>delete</span>
                  </div>
                  <span className={`status-label ${sala.status?.toLowerCase()}`}>
                    {sala.status}
                  </span>
                </div>
              </div>
            ))
          )}
        </section>
      </main>
    </div>
  );
}

export default Admin;