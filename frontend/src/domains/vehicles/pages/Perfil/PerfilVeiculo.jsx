import { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../../../contexts/AuthContext";
import PainelAuditoria from "../../../../pages/Perfil/components/PainelAuditoria/PainelAuditoria";
import PainelHistoricoScore from "../../../../pages/Perfil/components/PainelHistoricoScore/PainelHistoricoScore";
import "../../../../pages/Perfil/Perfil.css";
import {
  cancelarReservaVeiculo,
  getReservasVeiculoUsuario,
  reportarIncidente,
} from "../../../../services/api";

function PerfilVeiculo() {
  const { user, refreshUser, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const [reservas, setReservas] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modalAberto, setModalAberto] = useState(false);
  const [reservaParaCancelar, setReservaParaCancelar] = useState(null);
  const [motivoCancelamento, setMotivoCancelamento] = useState("");

  const [modalIncidenteAberto, setModalIncidenteAberto] = useState(false);
  const [recursoParaIncidente, setRecursoParaIncidente] = useState(null);
  const [descricaoIncidente, setDescricaoIncidente] = useState("");

  const [painelScoreAberto, setPainelScoreAberto] = useState(false);
  const [painelAuditoriaReserva, setPainelAuditoriaReserva] = useState(null);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  useEffect(() => {
    carregarReservas();
  }, [user]);

  async function carregarReservas() {
    if (!user?.id) {
      setLoading(false);
      return;
    }
    try {
      // O backend já devolve só as reservas de veículo deste usuário.
      const response = await getReservasVeiculoUsuario(user.id);
      setReservas(response.data.map(formatar));
    } catch (error) {
      console.error("Erro ao buscar reservas:", error);
    } finally {
      setLoading(false);
    }
  }

  function formatar(r) {
    const dataInicio = new Date(r.inicioDateTime);
    const dataFim = new Date(r.fimDateTime);
    const agora = new Date();

    const dataFormatada = dataInicio
      .toLocaleDateString("pt-BR", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      })
      .replace(" de ", " ")
      .replace(".", "");

    const horaInicio = dataInicio.toLocaleTimeString("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    });
    const horaFim = dataFim.toLocaleTimeString("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    });

    let statusVisual = r.status;

    if (r.status === "CONFIRMADA") {
      if (agora < dataInicio) {
        statusVisual = "CANCELAR";
      } else if (agora >= dataInicio && agora <= dataFim) {
        statusVisual = "FAZER CHECK-IN";
      }
    } else if (r.status === "EM_ANDAMENTO") {
      statusVisual = "FAZER CHECK-OUT";
    } else if (r.status === "ENCERRADA") {
      statusVisual = "CONCLUÍDA";
    } else if (r.status === "CANCELADA") {
      statusVisual =
        r.motivoCancelamento === "NO_SHOW_AUTOMATICO"
          ? "EXPIRADA"
          : "CANCELADA";
    }

    return {
      id: r.id,
      recursoId: r.recursoId,
      veiculo: r.recursoNome ? r.recursoNome : `Veículo #${r.recursoId}`,
      data: dataFormatada,
      horario: `${horaInicio} - ${horaFim}`,
      status: r.status,
      statusVisual,
      dataInicio,
      dataFim,
    };
  }

  function getStatusClass(statusVisual) {
    switch (statusVisual) {
      case "FAZER CHECK-IN":
        return "status-checkin";
      case "FAZER CHECK-OUT":
        return "status-checkout";
      case "CANCELAR":
        return "status-cancelar";
      case "CONCLUÍDA":
        return "status-concluida";
      case "CANCELADA":
        return "status-cancelada";
      case "EXPIRADA":
        return "status-cancelada";
      default:
        return "";
    }
  }

  function abrirModalCancelamento(reserva) {
    setReservaParaCancelar(reserva);
    setMotivoCancelamento("");
    setModalAberto(true);
  }

  async function confirmarCancelamento() {
    if (!motivoCancelamento.trim()) {
      alert("Por favor, informe um motivo para o cancelamento.");
      return;
    }
    try {
      await cancelarReservaVeiculo(
        reservaParaCancelar.id,
        user.id,
        motivoCancelamento,
      );
      setModalAberto(false);
      await carregarReservas();
      await refreshUser();
    } catch (error) {
      console.error(error);
      alert("Erro ao cancelar a reserva. Verifique a conexão com o servidor.");
    }
  }

  function abrirModalIncidente(reserva) {
    setRecursoParaIncidente(reserva);
    setDescricaoIncidente("");
    setModalIncidenteAberto(true);
  }

  async function confirmarReporteIncidente() {
    if (!descricaoIncidente.trim()) {
      alert("Por favor, descreva o problema encontrado no veículo.");
      return;
    }
    try {
      await reportarIncidente(
        recursoParaIncidente.recursoId,
        descricaoIncidente,
        user.id,
      );
      alert("Problema reportado com sucesso! A administração foi notificada.");
      setModalIncidenteAberto(false);
    } catch (error) {
      console.error(error);
      alert("Erro ao reportar o problema. Verifique a conexão.");
    }
  }

  function handleAcao(reserva) {
    switch (reserva.statusVisual) {
      case "FAZER CHECK-IN":
        navigate(`/veiculos/checkin/${reserva.id}`);
        break;
      case "FAZER CHECK-OUT":
        navigate(`/veiculos/checkout/${reserva.id}`);
        break;
      case "CANCELAR":
        abrirModalCancelamento(reserva);
        break;
      default:
        break;
    }
  }

  const isAcaoAtiva = (statusVisual) =>
    ["FAZER CHECK-IN", "FAZER CHECK-OUT", "CANCELAR"].includes(statusVisual);

  const temAuditoria = (status) =>
    status === "EM_ANDAMENTO" || status === "ENCERRADA";

  return (
    <div className="perfil-container">
      <main className="perfil-content">
        <h1 className="page-title">Perfil</h1>

        <section className="user-card">
          <div className="user-details">
            <h2 className="user-name">
              {user?.nome || "Usuário Desconhecido"}
            </h2>
            <p className="user-email">{user?.email || "email@ssb-corp.com"}</p>
            <div className="trust-score-badge">
              <span className="score-label">TRUST SCORE:</span>
              <button
                className="score-value score-value--clicavel"
                onClick={() => setPainelScoreAberto(true)}
                title="Ver histórico"
              >
                ★ {user?.trustScore || 0}/100
              </button>
            </div>
          </div>
          <button className="logout-button" onClick={handleLogout}>
            🚪 Sair da Conta
          </button>
        </section>

        <section className="history-section">
          <div className="history-header">
            <h2>Minhas Reservas de Veículos</h2>
            <p>Gerencie suas utilizações da frota corporativa.</p>
          </div>

          {loading ? (
            <p style={{ color: "#666" }}>Carregando reservas...</p>
          ) : reservas.length === 0 ? (
            <p style={{ color: "#666" }}>
              Você ainda não possui reservas de veículos registradas.
            </p>
          ) : (
            <div className="reservations-list">
              {reservas.map((reserva) => (
                <div
                  key={reserva.id}
                  className={`reservation-card ${getStatusClass(reserva.statusVisual)}`}
                >
                  <div className="reservation-info">
                    <h3>{reserva.veiculo}</h3>
                    <div className="reservation-datetime">
                      <span>📅 {reserva.data}</span>
                      <span>🕒 {reserva.horario}</span>
                    </div>

                    <div className="reservation-links">
                      {reserva.status === "CONFIRMADA" &&
                        reserva.statusVisual !== "CANCELAR" && (
                          <button
                            onClick={() => abrirModalCancelamento(reserva)}
                            className="reserva-link reserva-link--cancelar"
                          >
                            Cancelar reserva
                          </button>
                        )}

                      <button
                        onClick={() => abrirModalIncidente(reserva)}
                        className="reserva-link reserva-link--problema"
                      >
                        Reportar problema
                      </button>

                      {temAuditoria(reserva.status) && (
                        <button
                          onClick={() => setPainelAuditoriaReserva(reserva)}
                          className="reserva-link reserva-link--auditoria"
                        >
                          Ver auditoria
                        </button>
                      )}
                    </div>
                  </div>

                  {isAcaoAtiva(reserva.statusVisual) ? (
                    <button
                      className={`badge ${getStatusClass(reserva.statusVisual)}`}
                      onClick={() => handleAcao(reserva)}
                    >
                      {reserva.statusVisual} &gt;
                    </button>
                  ) : (
                    <span
                      className={`badge ${getStatusClass(reserva.statusVisual)}`}
                    >
                      {reserva.statusVisual}
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>
      </main>

      {modalAberto && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Cancelar Reserva</h3>
            <p>
              Você está cancelando a reserva para{" "}
              <strong>{reservaParaCancelar?.veiculo}</strong> no dia{" "}
              {reservaParaCancelar?.data}.
            </p>
            <p
              style={{ fontSize: "12px", color: "#d9534f", marginTop: "10px" }}
            >
              Atenção: cancelamentos com pouca antecedência podem impactar a sua
              nota na plataforma.
            </p>
            <textarea
              placeholder="Descreva o motivo do cancelamento..."
              value={motivoCancelamento}
              onChange={(e) => setMotivoCancelamento(e.target.value)}
              rows="4"
              style={{
                width: "100%",
                marginTop: "15px",
                padding: "10px",
                borderRadius: "4px",
              }}
            />
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: "10px",
                marginTop: "20px",
              }}
            >
              <button
                onClick={() => setModalAberto(false)}
                style={{
                  padding: "8px 16px",
                  background: "#ccc",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                Voltar
              </button>
              <button
                onClick={confirmarCancelamento}
                style={{
                  padding: "8px 16px",
                  background: "#d9534f",
                  color: "white",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                Confirmar Cancelamento
              </button>
            </div>
          </div>
        </div>
      )}

      {modalIncidenteAberto && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Reportar Problema</h3>
            <p>
              Descreva o problema encontrado no{" "}
              <strong>{recursoParaIncidente?.veiculo}</strong>.
            </p>
            <textarea
              placeholder="Ex: O ar condicionado não está funcionando, ou o pneu está murcho..."
              value={descricaoIncidente}
              onChange={(e) => setDescricaoIncidente(e.target.value)}
              rows="4"
              style={{
                width: "100%",
                marginTop: "15px",
                padding: "10px",
                borderRadius: "4px",
              }}
            />
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: "10px",
                marginTop: "20px",
              }}
            >
              <button
                onClick={() => setModalIncidenteAberto(false)}
                style={{
                  padding: "8px 16px",
                  background: "#ccc",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                Cancelar
              </button>
              <button
                onClick={confirmarReporteIncidente}
                style={{
                  padding: "8px 16px",
                  background: "#f0ad4e",
                  color: "white",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                Enviar Relatório
              </button>
            </div>
          </div>
        </div>
      )}

      {painelScoreAberto && (
        <PainelHistoricoScore
          usuarioId={user.id}
          onFechar={() => setPainelScoreAberto(false)}
        />
      )}

      {painelAuditoriaReserva && (
        <PainelAuditoria
          reserva={{
            ...painelAuditoriaReserva,
            sala: painelAuditoriaReserva.veiculo,
          }}
          onFechar={() => setPainelAuditoriaReserva(null)}
        />
      )}
    </div>
  );
}

export default PerfilVeiculo;
