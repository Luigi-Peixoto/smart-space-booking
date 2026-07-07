import { useContext, useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import "../../../../App.css";
import imagemMockada from "../../../../assets/mockImagemSala.jpg";
import { AuthContext } from "../../../../contexts/AuthContext";
import {
  aprovarIncidente,
  deletarVeiculo,
  getIncidentesPendentes,
  getVeiculos,
  rejeitarIncidente,
} from "../../../../services/api";
import "./Admin.css";
import "./RegrasAvaliacao.css";

const FILE_SERVER_URL = "http://localhost:8088/api/file-server/v1/files";

function AdminVeiculo() {
  const { user } = useContext(AuthContext);
  const [veiculos, setVeiculos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const [incidentes, setIncidentes] = useState([]);
  const termoBusca = searchParams.get("busca") || "";
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
    if (
      !window.confirm(
        "Aprovar incidente? O veículo será bloqueado para MANUTENCAO.",
      )
    )
      return;
    try {
      await aprovarIncidente(incidenteId, user.id);
      alert("Incidente aprovado! Veículo enviado para manutenção.");
      carregarIncidentes();
    } catch (error) {
      alert("Erro ao aprovar incidente.");
    }
  }

  async function handleRejeitar(incidenteId) {
    if (
      !window.confirm(
        "Rejeitar incidente? Ele será arquivado e o veículo continuará ativo.",
      )
    )
      return;
    try {
      await rejeitarIncidente(incidenteId, user.id);
      alert("Incidente rejeitado e arquivado.");
      carregarIncidentes();
    } catch (error) {
      alert("Erro ao rejeitar incidente.");
    }
  }

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
    carregarIncidentes();
  }, []);

  const veiculosFiltrados = veiculos.filter((veiculo) => {
    const busca = termoBusca.toLowerCase();
    return (
      veiculo.nome.toLowerCase().includes(busca) ||
      veiculo.placa.toLowerCase().includes(busca) ||
      veiculo.modelo.toLowerCase().includes(busca)
    );
  });

  const handleDelete = async (id) => {
    try {
      await deletarVeiculo(id, user.id);
      alert("Veículo removido com sucesso!");
      setVeiculos((prev) => prev.filter((veiculo) => veiculo.id !== id));
    } catch (error) {
      console.error("Erro ao deletar:", error);
      if (error.response?.status === 404) {
        setVeiculos((prev) => prev.filter((veiculo) => veiculo.id !== id));
      } else {
        alert(
          "Erro ao excluir: verifique se o veículo possui vínculos ativos.",
        );
      }
    }
  };

  if (loading)
    return (
      <div className="p-10 text-center text-xl font-bold">
        Carregando veículos...
      </div>
    );

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Gerenciar Frota</h1>
          <div
            style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}
          >
            <button
              className="btn-regras"
              onClick={() => navigate("/veiculos/regras-avaliacao")}
            >
              <span className="material-icons">rule</span>
              Regras de Avaliação
            </button>
            <button
              className="btn-regras"
              onClick={() => navigate("/veiculos/regras-trust-score")}
            >
              <span className="material-icons">shield</span>
              Regras de TrustScore
            </button>
            <button
              className="btn-primary btn-addsala"
              onClick={() => navigate("/veiculos/cadastrar-veiculo")}
            >
              <span className="material-icons">add</span>
              Novo Veículo
            </button>
          </div>
        </div>

        <section
          className="incidentes-section"
          style={{ marginTop: "30px", marginBottom: "40px" }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "10px",
              marginBottom: "15px",
            }}
          >
            <span
              className="material-icons"
              style={{ color: "#d9534f", fontSize: "28px" }}
            >
              report_problem
            </span>
            <h2 style={{ margin: 0, fontSize: "1.5rem", color: "#333" }}>
              Caixa de Entrada: Incidentes Reportados
            </h2>
          </div>

          {loading ? (
            <p style={{ color: "#666", fontStyle: "italic" }}>
              Buscando relatórios de frota...
            </p>
          ) : incidentes.length === 0 ? (
            <div
              style={{
                padding: "20px",
                background: "#dff0d8",
                color: "#3c763d",
                borderRadius: "8px",
                borderLeft: "5px solid #3c763d",
              }}
            >
              <strong>Tudo limpo!</strong> Nenhum incidente pendente no momento.
            </div>
          ) : (
            <div
              className="cards-grid"
              style={{ display: "flex", flexDirection: "column", gap: "15px" }}
            >
              {incidentes.map((incidente) => (
                <div
                  key={incidente.id}
                  className="incidente-card"
                  style={{
                    border: "1px solid #e0e0e0",
                    borderLeft: "5px solid #d9534f",
                    padding: "20px",
                    borderRadius: "8px",
                    background: "#fff",
                    boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
                  }}
                >
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "flex-start",
                      flexWrap: "wrap",
                      gap: "15px",
                    }}
                  >
                    <div style={{ flex: "1 1 300px" }}>
                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "8px",
                          marginBottom: "8px",
                        }}
                      >
                        <span
                          className="material-icons"
                          style={{ fontSize: "18px", color: "#555" }}
                        >
                          directions_car
                        </span>
                        <h3
                          style={{
                            margin: 0,
                            fontSize: "1.1rem",
                            color: "#222",
                          }}
                        >
                          {incidente.recursoNome}
                        </h3>
                      </div>

                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "8px",
                          marginBottom: "12px",
                          color: "#666",
                          fontSize: "14px",
                        }}
                      >
                        <span
                          className="material-icons"
                          style={{ fontSize: "16px" }}
                        >
                          person
                        </span>
                        <span>
                          Reportado por:{" "}
                          <strong>{incidente.usuarioNome}</strong> (
                          {incidente.usuarioEmail})
                        </span>
                      </div>

                      <div
                        style={{
                          background: "#f9f9f9",
                          padding: "12px",
                          borderRadius: "6px",
                          border: "1px solid #eee",
                        }}
                      >
                        <p
                          style={{
                            margin: 0,
                            fontStyle: "italic",
                            color: "#444",
                          }}
                        >
                          "{incidente.descricao}"
                        </p>
                      </div>

                      <p
                        style={{
                          margin: "10px 0 0 0",
                          fontSize: "12px",
                          color: "#999",
                        }}
                      >
                        📅 Data do reporte:{" "}
                        {new Date(incidente.dataReporte).toLocaleString(
                          "pt-BR",
                        )}
                      </p>
                    </div>

                    <div
                      style={{
                        display: "flex",
                        flexDirection: "column",
                        gap: "10px",
                        minWidth: "200px",
                      }}
                    >
                      <button
                        onClick={() => handleAprovar(incidente.id)}
                        style={{
                          padding: "10px 15px",
                          background: "#d9534f",
                          color: "white",
                          border: "none",
                          borderRadius: "6px",
                          cursor: "pointer",
                          fontWeight: "bold",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: "5px",
                          transition: "background 0.2s",
                        }}
                        onMouseOver={(e) =>
                          (e.currentTarget.style.background = "#c9302c")
                        }
                        onMouseOut={(e) =>
                          (e.currentTarget.style.background = "#d9534f")
                        }
                      >
                        <span
                          className="material-icons"
                          style={{ fontSize: "18px" }}
                        >
                          lock
                        </span>
                        Aprovar e Bloquear Veículo
                      </button>

                      <button
                        onClick={() => handleRejeitar(incidente.id)}
                        style={{
                          padding: "10px 15px",
                          background: "#f5f5f5",
                          color: "#333",
                          border: "1px solid #ccc",
                          borderRadius: "6px",
                          cursor: "pointer",
                          fontWeight: "bold",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: "5px",
                          transition: "background 0.2s",
                        }}
                        onMouseOver={(e) =>
                          (e.currentTarget.style.background = "#e6e6e6")
                        }
                        onMouseOut={(e) =>
                          (e.currentTarget.style.background = "#f5f5f5")
                        }
                      >
                        <span
                          className="material-icons"
                          style={{ fontSize: "18px" }}
                        >
                          close
                        </span>
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
          {veiculosFiltrados.length === 0 ? (
            <p>Nenhum veículo encontrado.</p>
          ) : (
            veiculosFiltrados.map((veiculo) => (
              <div key={veiculo.id} className="room-card">
                <div className="room-card-main-content">
                  <div className="room-text-content">
                    <h3 className="room-title">{veiculo.nome}</h3>
                    <p className="room-info">
                      <span className="material-icons">directions_car</span>{" "}
                      {veiculo.marca} {veiculo.modelo}
                    </p>
                    <p className="room-info">
                      <span className="material-icons">badge</span>{" "}
                      {veiculo.placa}
                    </p>
                  </div>
                  <div className="room-image-container">
                    <img
                      src={
                        veiculo.imagens?.[0]
                          ? `${FILE_SERVER_URL}/${veiculo.imagens[0]}`
                          : imagemMockada
                      }
                      alt={veiculo.nome}
                      className="room-card-img"
                    />
                  </div>
                </div>

                <div className="sala-features">
                  <span className="feature-tag">{veiculo.cor}</span>
                  <span className="feature-tag">Chassi: {veiculo.chassi}</span>
                  <span className="feature-tag">
                    RENAVAM: {veiculo.renavam}
                  </span>
                </div>

                <div className="room-card-footer">
                  <div className="room-actions">
                    <span
                      className="material-icons action-icon"
                      onClick={() =>
                        navigate("/veiculos/editar-veiculo/" + veiculo.id)
                      }
                    >
                      edit
                    </span>
                    <span
                      className="material-icons action-icon delete"
                      onClick={() => handleDelete(veiculo.id)}
                    >
                      delete
                    </span>
                  </div>
                  <span
                    className={`status-label ${veiculo.status?.toLowerCase()}`}
                  >
                    {veiculo.status}
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

export default AdminVeiculo;
