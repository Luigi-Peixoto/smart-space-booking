import { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../contexts/AuthContext";
import { getReservasUsuario, cancelarReserva, getUsuarioById } from "../../services/api";
import "./Perfil.css";

function Perfil() {
  const { user, refreshUser, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const [reservas, setReservas] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modalAberto, setModalAberto] = useState(false);
  const [reservaParaCancelar, setReservaParaCancelar] = useState(null);
  const [motivoCancelamento, setMotivoCancelamento] = useState(""); 

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
      const response = await getReservasUsuario(user.id);
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
      if(r.motivoCancelamento === "NO_SHOW_AUTOMATICO") {
        statusVisual = "EXPIRADA";
      } else {
        statusVisual = "CANCELADA";
      }
    }

    return {
      id: r.id,
      sala: r.salaNome ? `Sala ${r.salaNome}` : `Sala #${r.salaId}`,
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
      case "FAZER CHECK-IN":  return "status-checkin";
      case "FAZER CHECK-OUT": return "status-checkout";
      case "CANCELAR":        return "status-cancelar";
      case "CONCLUÍDA":       return "status-concluida";
      case "CANCELADA":       return "status-cancelada";
      case "EXPIRADA":        return "status-cancelada";
      default:                return "";
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
      await cancelarReserva(reservaParaCancelar.id, user.id, motivoCancelamento);
      setModalAberto(false);
      await carregarReservas();
      await refreshUser();
    } catch (error) {
      console.error(error);
      alert("Erro ao cancelar a reserva. Verifique a conexão com o servidor.");
    }
  }

  function handleAcao(reserva) {
    switch (reserva.statusVisual) {
      case "FAZER CHECK-IN": navigate(`/checkin/${reserva.id}`); break;
      case "FAZER CHECK-OUT": navigate(`/checkout/${reserva.id}`); break;
      case "CANCELAR": abrirModalCancelamento(reserva); break;
      default: break;
    }
  }

  const isAcaoAtiva = (statusVisual) =>
    ["FAZER CHECK-IN", "FAZER CHECK-OUT", "CANCELAR"].includes(statusVisual);

  return (
    <div className="perfil-container">
      <main className="perfil-content">
        <h1 className="page-title">Perfil</h1>

        <section className="user-card">
          <div className="user-details">
            <h2 className="user-name">{user?.nome || "Usuário Desconhecido"}</h2>
            <p className="user-email">{user?.email || "email@ssb-corp.com"}</p>
            <div className="trust-score-badge">
              <span className="score-label">TRUST SCORE:</span>
              <span className="score-value">★ {user?.trustScore || 0}/100</span>
            </div>
          </div>
          <button className="logout-button" onClick={handleLogout}>
            🚪 Sair da Conta
          </button>
        </section>

        <section className="history-section">
          <div className="history-header">
            <h2>Minhas Reservas</h2>
            <p>Gerencie suas utilizações de espaços de trabalho.</p>
          </div>

          {loading ? (
            <p style={{ color: "#666" }}>Carregando reservas...</p>
          ) : reservas.length === 0 ? (
            <p style={{ color: "#666" }}>Você ainda não possui reservas registradas.</p>
          ) : (
            <div className="reservations-list">
              {reservas.map((reserva) => (
                <div
                  key={reserva.id}
                  className={`reservation-card ${getStatusClass(reserva.statusVisual)}`}
                >
                  <div className="reservation-info">
                    <h3>{reserva.sala}</h3>
                    <div className="reservation-datetime">
                      <span>📅 {reserva.data}</span>
                      <span>🕒 {reserva.horario}</span>
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
                    <span className={`badge ${getStatusClass(reserva.statusVisual)}`}>
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
            <p>Você está cancelando a reserva para <strong>{reservaParaCancelar?.sala}</strong> no dia {reservaParaCancelar?.data}.</p>
            
            <p className="modal-warning" style={{ fontSize: '12px', color: '#d9534f', marginTop: '10px' }}>
              Atenção: Cancelamentos com menos de 2 horas de antecedência podem impactar a sua nota na plataforma.
            </p>

            <textarea 
              placeholder="Descreva o motivo do cancelamento..."
              value={motivoCancelamento}
              onChange={(e) => setMotivoCancelamento(e.target.value)}
              rows="4"
              style={{ width: '100%', marginTop: '15px', padding: '10px', borderRadius: '4px' }}
            />

            <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
              <button 
                onClick={() => setModalAberto(false)}
                style={{ padding: '8px 16px', background: '#ccc', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
              >
                Voltar
              </button>
              <button 
                onClick={confirmarCancelamento}
                style={{ padding: '8px 16px', background: '#d9534f', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
              >
                Confirmar Cancelamento
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Perfil;
