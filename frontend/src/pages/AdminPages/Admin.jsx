import { useState, useEffect, useContext } from 'react';
import { AuthContext } from "../../contexts/AuthContext";
import { useSearchParams, useNavigate } from 'react-router-dom';
import { deletarSala, getSalas, getIncidentesPendentes, aprovarIncidente, rejeitarIncidente } from '../../services/api';
import './Admin.css';
import './RegrasAvaliacao.css';
import '../../App.css';
import imagemMockada from '../../assets/mockImagemSala.jpg';

const FILE_SERVER_URL = "http://localhost:8088/api/file-server/v1/files";

function Admin() {
  const { user } = useContext(AuthContext);
  const [salas, setSalas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const [incidentes, setIncidentes] = useState([]);
  const termoBusca = searchParams.get('busca') || "";
  const navigate = useNavigate();

  async function carregarIncidentes() {
    try {
      const response = await getIncidentesPendentes(user.id);
      setIncidentes(response.data);
    } catch (error) {
      console.error("Erro ao buscar incidentes:", error);
    } finally {
      setLoading(false);
    }
  }

  async function handleAprovar(incidenteId) {
    if (!window.confirm("Aprovar incidente? A sala será bloqueada para MANUTENCAO.")) return;
    try {
      await aprovarIncidente(incidenteId, user.id);
      alert("Incidente aprovado! Sala enviada para manutenção.");
      carregarIncidentes();
    } catch (error) {
      alert("Erro ao aprovar incidente.");
    }
  }

  async function handleRejeitar(incidenteId) {
    if (!window.confirm("Rejeitar incidente? Ele será arquivado e a sala continuará ativa.")) return;
    try {
      await rejeitarIncidente(incidenteId, user.id);
      alert("Incidente rejeitado e arquivado.");
      carregarIncidentes();
    } catch (error) {
      alert("Erro ao rejeitar incidente.");
    }
  }

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
    carregarIncidentes();
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
            {/* Botão para navegar para as Penalidades de TrustScore */}
            <button
              className="btn-regras"
              onClick={() => navigate('/regras-trust-score-evento')}
            >
              <span className="material-icons">shield</span>
              Penalidades de TrustScore
            </button>
            <button className="btn-primary btn-addsala" onClick={() => navigate('/cadastrar-sala')}>
              <span className="material-icons">add</span>
              Nova Sala
            </button>
          </div>
        </div>
        <section className="incidentes-section" style={{ marginTop: '30px', marginBottom: '40px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '15px' }}>
            <span className="material-icons" style={{ color: '#d9534f', fontSize: '28px' }}>report_problem</span>
            <h2 style={{ margin: 0, fontSize: '1.5rem', color: '#333' }}>Caixa de Entrada: Incidentes Reportados</h2>
          </div>
          
          {loading ? (
            <p style={{ color: '#666', fontStyle: 'italic' }}>Buscando relatórios de infraestrutura...</p>
          ) : incidentes.length === 0 ? (
            <div style={{ padding: '20px', background: '#dff0d8', color: '#3c763d', borderRadius: '8px', borderLeft: '5px solid #3c763d' }}>
              <strong>Tudo limpo!</strong> Nenhum incidente pendente no momento.
            </div>
          ) : (
            <div className="cards-grid" style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              {incidentes.map((incidente) => (
                <div 
                  key={incidente.id} 
                  className="incidente-card" 
                  style={{ 
                    border: '1px solid #e0e0e0', 
                    borderLeft: '5px solid #d9534f', // Destaque lateral de problema
                    padding: '20px', 
                    borderRadius: '8px', 
                    background: '#fff',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '15px' }}>
                    
                    {/* Bloco de Informações */}
                    <div style={{ flex: '1 1 300px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                        <span className="material-icons" style={{ fontSize: '18px', color: '#555' }}>meeting_room</span>
                        <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#222' }}>{incidente.recursoNome}</h3>
                      </div>
                      
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', color: '#666', fontSize: '14px' }}>
                        <span className="material-icons" style={{ fontSize: '16px' }}>person</span>
                        <span>
                          Reportado por: <strong>{incidente.usuarioNome}</strong>{" "}
                          ({incidente.usuarioEmail})
                        </span>
                      </div>

                      <div style={{ background: '#f9f9f9', padding: '12px', borderRadius: '6px', border: '1px solid #eee' }}>
                        <p style={{ margin: 0, fontStyle: 'italic', color: '#444' }}>
                          "{incidente.descricao}"
                        </p>
                      </div>
                      
                      <p style={{ margin: '10px 0 0 0', fontSize: '12px', color: '#999' }}>
                        📅 Data do reporte: {new Date(incidente.dataReporte).toLocaleString('pt-BR')}
                      </p>
                    </div>
                    
                    {/* Bloco de Ações */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', minWidth: '200px' }}>
                      <button 
                        onClick={() => handleAprovar(incidente.id)}
                        style={{ padding: '10px 15px', background: '#d9534f', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', transition: 'background 0.2s' }}
                        onMouseOver={(e) => e.currentTarget.style.background = '#c9302c'}
                        onMouseOut={(e) => e.currentTarget.style.background = '#d9534f'}
                      >
                        <span className="material-icons" style={{ fontSize: '18px' }}>lock</span>
                        Aprovar e Bloquear Sala
                      </button>

                      <button 
                        onClick={() => handleRejeitar(incidente.id)}
                        style={{ padding: '10px 15px', background: '#f5f5f5', color: '#333', border: '1px solid #ccc', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', transition: 'background 0.2s' }}
                        onMouseOver={(e) => e.currentTarget.style.background = '#e6e6e6'}
                        onMouseOut={(e) => e.currentTarget.style.background = '#f5f5f5'}
                      >
                        <span className="material-icons" style={{ fontSize: '18px' }}>close</span>
                        Rejeitar e Arquivar
                      </button>
                    </div>

                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

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