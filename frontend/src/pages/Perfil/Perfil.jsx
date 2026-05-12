import { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../contexts/AuthContext";
import { getReservasUsuario/*, cancelarReserva*/ } from "../../services/api";
import "./Perfil.css";

function Perfil() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const [reservas, setReservas] = useState([]);
  const [loading, setLoading] = useState(true);

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

  async function cancelReserva(id) {
    if (!window.confirm("Tem certeza que deseja cancelar esta reserva?")) return;
    try {
      //await cancelarReserva(id, user.id);
      await carregarReservas();
    } catch (error) {
      alert("Erro ao cancelar a reserva. Tente novamente.");
    }
  }

  function handleAcao(reserva) {
    switch (reserva.statusVisual) {
      case "FAZER CHECK-IN":  navigate(`/checkin/${reserva.id}`); break;
      case "FAZER CHECK-OUT": navigate(`/checkout/${reserva.id}`); break;
      case "CANCELAR":        cancelReserva(reserva.id); break;
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
    </div>
  );
}

export default Perfil;
