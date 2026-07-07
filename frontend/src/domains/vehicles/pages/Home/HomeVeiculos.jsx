import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import "../../../../App.css";
import imagemMockada from "../../../../assets/mockImagemSala.jpg";
import "../../../../pages/AdminPages/Admin.css";
import { getVeiculos } from "../../../../services/api";
import "./Home.css";

const FILE_SERVER_URL = "http://localhost:8088/api/file-server/v1/files";

function HomeVeiculos() {
  const [veiculos, setVeiculos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const termoBusca = searchParams.get("busca") || "";
  const navigate = useNavigate();

  useEffect(() => {
    async function carregarVeiculos() {
      try {
        const response = await getVeiculos();
        setVeiculos(response.data);
      } catch (error) {
        console.error("Erro ao carregar veículos:", error);
        alert("Não conseguimos carregar os veículos no momento.");
      } finally {
        setLoading(false);
      }
    }
    carregarVeiculos();
  }, []);

  if (loading)
    return (
      <div className="p-10 text-center text-xl font-bold">
        Carregando veículos...
      </div>
    );

  const veiculosFiltrados = veiculos.filter((veiculo) => {
    const busca = termoBusca.toLowerCase();
    return (
      veiculo.nome.toLowerCase().includes(busca) ||
      veiculo.placa.toLowerCase().includes(busca) ||
      veiculo.modelo.toLowerCase().includes(busca)
    );
  });

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Explorar veículos</h1>
        </div>

        <section className="rooms-grid">
          {veiculosFiltrados.length === 0 ? (
            <p>Nenhum veículo encontrado.</p>
          ) : (
            veiculosFiltrados.map((veiculo) => {
              const isManutencao = veiculo.status === "MANUTENCAO";

              return (
                <div
                  key={veiculo.id}
                  className={`sala-card ${isManutencao ? "sala-manutencao" : ""}`}
                >
                  <div className="sala-image-container">
                    <span className="sala-type-tag">{veiculo.marca}</span>
                    <img
                      src={
                        veiculo.imagens && veiculo.imagens.length > 0
                          ? `${FILE_SERVER_URL}/${veiculo.imagens[0]}`
                          : imagemMockada
                      }
                      alt={veiculo.nome}
                      className="sala-image"
                    />

                    {isManutencao && (
                      <div className="manutencao-overlay">
                        <span className="material-icons manutencao-icon">
                          construction
                        </span>
                        <span className="manutencao-text">INTERDITADO</span>
                      </div>
                    )}
                  </div>

                  <div className="sala-content">
                    <div className="sala-header">
                      <h3 className="sala-title">{veiculo.nome}</h3>
                      <div className="sala-capacity">
                        <span className="material-icons capacity-icon">
                          directions_car
                        </span>
                        <span>{veiculo.placa}</span>
                      </div>
                    </div>

                    <p className="sala-location">
                      {veiculo.marca} {veiculo.modelo}
                    </p>

                    <div className="sala-features">
                      <span className="feature-tag">{veiculo.cor}</span>
                    </div>

                    <button
                      className="btn-primary"
                      onClick={() =>
                        !isManutencao &&
                        navigate("/veiculos/criar-reserva/" + veiculo.id)
                      }
                      disabled={isManutencao}
                    >
                      {isManutencao ? (
                        <>
                          <span className="material-icons btn-icon">block</span>
                          Em Manutenção
                        </>
                      ) : (
                        <>
                          Reservar Veículo{" "}
                          <span className="material-icons btn-icon">
                            arrow_forward
                          </span>
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

export default HomeVeiculos;
