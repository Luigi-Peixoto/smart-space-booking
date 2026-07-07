import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getEquipamentos } from '../../services/api';
import '../AdminPages/Admin.css';
import '../../App.css';
import './Home.css';
import imagemMockada from '../../assets/mockImagemSala.jpg';

const FILE_SERVER_URL = "http://localhost:8088/api/file-server/v1/files";

const TIPO_LABELS = {
  PROJETOR: "Projetor",
  NOTEBOOK: "Notebook",
  MICROFONE: "Microfone",
  CAMERA: "Câmera",
  OUTRO: "Outro",
};

function Home() {
  const [equipamentos, setEquipamentos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const termoBusca = searchParams.get('busca') || "";
  const navigate = useNavigate();

  useEffect(() => {
    async function carregarEquipamentos() {
      try {
        const response = await getEquipamentos();
        setEquipamentos(response.data);
      } catch (error) {
        console.error("Erro ao carregar equipamentos:", error);
        alert("Não conseguimos carregar os equipamentos no momento.");
      } finally {
        setLoading(false);
      }
    }
    carregarEquipamentos();
  }, []);

  if (loading) return <div className="p-10 text-center text-xl font-bold">Carregando equipamentos...</div>;

  const equipamentosFiltrados = equipamentos.filter((equipamento) => {
    const busca = termoBusca.toLowerCase();
    return (
      equipamento.nome.toLowerCase().includes(busca) ||
      equipamento.marca.toLowerCase().includes(busca) ||
      equipamento.modelo.toLowerCase().includes(busca)
    );
  });

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Explorar equipamentos</h1>
        </div>

        <section className="rooms-grid">
          {equipamentosFiltrados.length === 0 ? (
            <p>Nenhum equipamento encontrado.</p>
          ) : (
            equipamentosFiltrados.map((equipamento) => {
              // Verificamos se o equipamento está interditado
              const isManutencao = equipamento.status === 'MANUTENCAO';

              return (
                // Adicionamos a classe dinâmica 'sala-manutencao' no card se for verdade
                <div key={equipamento.id} className={`sala-card ${isManutencao ? 'sala-manutencao' : ''}`}>

                  <div className="sala-image-container">
                    <span className="sala-type-tag">{TIPO_LABELS[equipamento.tipo] || equipamento.tipo}</span>
                    <img
                      src={equipamento.imagens && equipamento.imagens.length > 0 ? `${FILE_SERVER_URL}/${equipamento.imagens[0]}` : imagemMockada}
                      alt={equipamento.nome}
                      className="sala-image"
                    />

                    {/* A div da camada visual agora tem a própria classe css */}
                    {isManutencao && (
                      <div className="manutencao-overlay">
                        <span className="material-icons manutencao-icon">construction</span>
                        <span className="manutencao-text">INTERDITADO</span>
                      </div>
                    )}
                  </div>

                  <div className="sala-content">
                    <div className="sala-header">
                      <h3 className="sala-title">{equipamento.nome}</h3>
                      {equipamento.subItens && equipamento.subItens.length > 0 && (
                        <div className="sala-capacity" title="Itens do kit">
                          <span className="material-icons capacity-icon">inventory_2</span>
                          <span>{String(equipamento.subItens.length).padStart(2, '0')}</span>
                        </div>
                      )}
                    </div>

                    <p className="sala-location">
                      {equipamento.marca} {equipamento.modelo} — Nº de série: {equipamento.numeroSerie}
                    </p>

                    <div className="sala-features">
                      {equipamento.subItens && equipamento.subItens.map((subItem) => (
                        <span key={subItem.id} className="feature-tag">{subItem.nome}</span>
                      ))}
                    </div>

                    {/* Botão disabled puro. O CSS cuida de deixar ele cinza! */}
                    <button
                      className="btn-primary"
                      onClick={() => !isManutencao && navigate('/criar-reserva/' + equipamento.id)}
                      disabled={isManutencao}
                    >
                      {isManutencao ? (
                        <>
                          <span className="material-icons btn-icon">block</span>
                          Em Manutenção
                        </>
                      ) : (
                        <>
                          Reservar Equipamento <span className="material-icons btn-icon">arrow_forward</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </section>
      </main>
    </div>
  );
}

export default Home;
