import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getSalas } from '../../services/api';
import '../AdminPages/Admin.css';
import '../../App.css';
import './Home.css';
import SSBLogo from '../../assets/SSBLogo.png';
import imagemMockada from '../../assets/mockImagemSala.jpg';

const FILE_SERVER_URL = "http://localhost:8088/api/file-server/v1/files";

function Home() {
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

        // console.log(sala.imagens);
      } catch (error) {
        console.error("Erro ao carregar salas:", error);
        alert("Não conseguimos carregar as salas no momento.");
      } finally {
        setLoading(false);
      }
    }
    carregarSalas();
  }, []);

  if (loading) return <div className="p-10 text-center text-xl font-bold">Carregando salas...</div>;

  const salasFiltradas = salas.filter((sala) => {
    const busca = termoBusca.toLowerCase();
    return (
      sala.nome.toLowerCase().includes(busca) || 
      sala.local.toLowerCase().includes(busca)
    );
  });

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Explorar salas</h1>
        </div>

        {/* <section className="rooms-grid">
          {salasFiltradas.length === 0 ? (
            <p>Nenhuma sala encontrada.</p>
          ) : (
            salasFiltradas.map(sala => (
              <div className="sala-card">
                <div className="sala-image-container">
                  <span className="sala-type-tag">{sala.tipoSala || sala.tipo}</span>
                  <img src={`${FILE_SERVER_URL}/${sala.imagens[0]}` || imagemMockada} alt={sala.nome} className="sala-image" />
                </div>

                <div className="sala-content">
                  <div className="sala-header">
                    <h3 className="sala-title">{sala.nome}</h3>
                    <div className="sala-capacity">
                      <span className="material-icons capacity-icon">groups</span>
                      <span>{String(sala.capacidade).padStart(2, '0')}</span>
                    </div>
                  </div>

                  <p className="sala-location">{sala.local}</p>

                  <div className="sala-features">
                    {sala.caracteristicas && sala.caracteristicas.map((feature, index) => (
                      <span key={index} className="feature-tag">{feature}</span>
                    ))}
                  </div>

                  <button className="btn-primary" onClick={() => navigate('/criar-reserva/' + sala.id)}>
                    Reservar Sala <span className="material-icons">arrow_forward</span>
                  </button>
                </div>
              </div>
            ))
          )}
        </section> */}
        <section className="rooms-grid">
          {salasFiltradas.length === 0 ? (
            <p>Nenhuma sala encontrada.</p>
          ) : (
            salasFiltradas.map((sala) => {
              // Verificamos se a sala está interditada
              const isManutencao = sala.status === 'MANUTENCAO';

              return (
                // Adicionamos a classe dinâmica 'sala-manutencao' no card se for verdade
                <div key={sala.id} className={`sala-card ${isManutencao ? 'sala-manutencao' : ''}`}>
                  
                  <div className="sala-image-container">
                    <span className="sala-type-tag">{sala.tipoSala || sala.tipo}</span>
                    <img 
                      src={sala.imagens && sala.imagens.length > 0 ? `${FILE_SERVER_URL}/${sala.imagens[0]}` : imagemMockada} 
                      alt={sala.nome} 
                      className="sala-image" 
                    />
                    
                    {/* A div da camada visual agora tem a própria classe css */}
                    {isManutencao && (
                      <div className="manutencao-overlay">
                        <span className="material-icons manutencao-icon">construction</span>
                        <span className="manutencao-text">INTERDITADA</span>
                      </div>
                    )}
                  </div>

                  <div className="sala-content">
                    <div className="sala-header">
                      <h3 className="sala-title">{sala.nome}</h3>
                      <div className="sala-capacity">
                        <span className="material-icons capacity-icon">groups</span>
                        <span>{String(sala.capacidade).padStart(2, '0')}</span>
                      </div>
                    </div>

                    <p className="sala-location">{sala.local}</p>

                    <div className="sala-features">
                      {sala.caracteristicas && sala.caracteristicas.map((feature, index) => (
                        <span key={index} className="feature-tag">{feature}</span>
                      ))}
                    </div>

                    {/* Botão disabled puro. O CSS cuida de deixar ele cinza! */}
                    <button 
                      className="btn-primary" 
                      onClick={() => !isManutencao && navigate('/criar-reserva/' + sala.id)}
                      disabled={isManutencao}
                    >
                      {isManutencao ? (
                        <>
                          <span className="material-icons btn-icon">block</span>
                          Em Manutenção
                        </>
                      ) : (
                        <>
                          Reservar Sala <span className="material-icons btn-icon">arrow_forward</span>
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