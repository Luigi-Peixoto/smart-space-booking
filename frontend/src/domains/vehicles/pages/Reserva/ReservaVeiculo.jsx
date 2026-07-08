import { useContext, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import SSBLogo from "../../../../assets/SSBLogo.png";
import imagemMockada from "../../../../assets/mockImagemSala.jpg";
import { AuthContext } from "../../../../contexts/AuthContext";
import "../../../../pages/Reserva/Reserva.css";
import {
  criarReservaVeiculo,
  getHorariosOcupadosVeiculo,
  getVeiculoById,
} from "../../../../services/api";

const FILE_SERVER_URL = "http://localhost:8088/api/file-server/v1/files";
const USER_ID = 1;

const getHoraAtual = () => {
  const agora = new Date();
  const horas = String(agora.getHours()).padStart(2, "0");
  const minutos = String(agora.getMinutes()).padStart(2, "0");
  return `${horas}:${minutos}`;
};

function toMinutes(timeStr) {
  if (!timeStr) return 0;
  const [h, m] = timeStr.split(":").map(Number);
  return h * 60 + m;
}

function overlaps(startA, endA, startB, endB) {
  return (
    toMinutes(startA) < toMinutes(endB) && toMinutes(endA) > toMinutes(startB)
  );
}

function buildZonedDateTime(dateStr, timeStr) {
  return `${dateStr}T${timeStr}:00-03:00`;
}

function formatLongDate(dateStr) {
  const date = new Date(dateStr + "T00:00:00");
  const options = {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  };
  return date.toLocaleDateString("pt-BR", options);
}

function calcDurationLabel(inicio, fim) {
  const diffMin = toMinutes(fim) - toMinutes(inicio);
  if (diffMin <= 0) return "0h";
  const hours = Math.floor(diffMin / 60);
  const mins = diffMin % 60;
  let label = "";
  if (hours > 0) label += `${hours}h`;
  if (mins > 0) label += ` ${mins}min`;
  return label.trim();
}

const getDataLocalISO = () => {
  const d = new Date();
  const ano = d.getFullYear();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
};

export default function ReservaVeiculo() {
  const { user } = useContext(AuthContext);
  const { id } = useParams();

  const navigate = useNavigate();

  const [erro, setErro] = useState(false);
  const [veiculo, setVeiculo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [showModal, setShowModal] = useState(false);

  const [data, setData] = useState(getDataLocalISO());

  const [inicio, setInicio] = useState(getHoraAtual());
  const [fim, setFim] = useState("");
  const [horariosOcupados, setHorariosOcupados] = useState([]);
  const [conflito, setConflito] = useState(false);

  const userId = user?.id || USER_ID;

  useEffect(() => {
    async function carregarVeiculo() {
      try {
        const response = await getVeiculoById(id);
        if (!response.data) throw new Error("Veículo não encontrado");
        setVeiculo(response.data);
      } catch (error) {
        console.error("Erro ao carregar veículo:", error);
        setErro(true);
      } finally {
        setLoading(false);
      }
    }
    carregarVeiculo();
  }, [id]);

  useEffect(() => {
    async function carregarDisponibilidade() {
      try {
        const response = await getHorariosOcupadosVeiculo(id, data);
        setHorariosOcupados(response.data);
      } catch (error) {
        console.error("Erro ao carregar horários ocupados:", error);
      }
    }

    if (data && id) {
      carregarDisponibilidade();
    }
  }, [data, id]);

  useEffect(() => {
    const hasConflito = horariosOcupados.some((h) =>
      overlaps(inicio, fim, h.inicio, h.fim),
    );
    setConflito(hasConflito);
  }, [inicio, fim, horariosOcupados]);

  const handleConfirmar = async () => {
    if (conflito || submitting) return;

    const reservaData = {
      usuarioId: userId,
      recursoId: Number(id),
      tipo: "PADRAO",
      inicioDateTime: buildZonedDateTime(data, inicio),
      fimDateTime: buildZonedDateTime(data, fim),
    };

    try {
      setSubmitting(true);
      await criarReservaVeiculo(reservaData);
      setShowModal(true);
    } catch (error) {
      console.error("Erro ao criar reserva:", error);
      alert(
        error.response?.data ||
          "Falha na reserva. Verifique se o horário já não foi preenchido.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading)
    return <div className="loading-screen">Carregando veículo...</div>;

  if (erro) {
    return (
      <div className="admin-container">
        <main className="admin-main centered-content">
          <div className="error-screen">
            <i className="material-icons error-icon">
              sentiment_very_dissatisfied
            </i>
            <h2>Veículo não encontrado</h2>
            <p>
              Não conseguimos localizar os detalhes deste veículo. <br />
              Ele pode ter sido removido ou o link está incorreto.
            </p>
            <button
              className="btn-error-fallback"
              onClick={() => navigate("/veiculos/home")}
            >
              <i className="material-icons">arrow_back</i>
              Voltar para a Home
            </button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="admin-container">
      {showModal && (
        <div className="modal-overlay modal-overlay-blurred">
          <div className="modal-card-refinement">
            <div className="modal-branding">
              <img src={SSBLogo} alt="Logo SSB" />
              <span>Smart Space Booking</span>
            </div>

            <div className="modal-status">
              <i className="material-icons success-indication">check_circle</i>
              <h2>Reserva Confirmada!</h2>
            </div>

            <div className="modal-detailed-confirmation">
              <p>
                Sua reserva para <strong>{veiculo?.nome}</strong> em{" "}
                <strong>{formatLongDate(data)}</strong>, das{" "}
                <strong>{inicio}</strong> às <strong>{fim}</strong>, para um
                total de{" "}
                <strong>{calcDurationLabel(inicio, fim)} de uso</strong>, foi
                concluída com sucesso.
              </p>
            </div>

            <div className="modal-booking-reference">
              <span>Código de Reserva: SSB-00123-ABC</span>
              <span className="timestamp">
                Confirmada em {new Date().toLocaleString()}
              </span>
            </div>

            <div className="modal-actions-refined">
              <button
                className="btn-voltar-home-premium"
                onClick={() => navigate("/veiculos/home")}
              >
                <i className="material-icons">home</i>
                <i className="material-icons">arrow_back</i>
                Voltar para Home
              </button>
            </div>
          </div>
        </div>
      )}

      <main className="admin-main centered-content">
        <div className="reserva-wrapper">
          <h1 className="reserva-title">Reserva</h1>

          <div className="reserva-card">
            <div className="reserva-sala-header">
              <div className="sala-info-main">
                <img
                  src={
                    veiculo.imagens?.[0]
                      ? `${FILE_SERVER_URL}/${veiculo.imagens[0]}`
                      : imagemMockada
                  }
                  alt="Veículo"
                  className="sala-img-mini"
                />
                <div className="sala-textos">
                  <h2 className="sala-nome-destaque">{veiculo?.nome}</h2>
                  <div className="sala-tags">
                    <span>
                      <i className="material-icons">directions_car</i>{" "}
                      {veiculo?.marca} {veiculo?.modelo}
                    </span>
                    <span>
                      <i className="material-icons">badge</i> {veiculo?.placa}
                    </span>
                    <span>
                      <i className="material-icons">palette</i> {veiculo?.cor}
                    </span>
                  </div>
                </div>
              </div>
              <button className="btn-voltar-figma" onClick={() => navigate(-1)}>
                <i className="material-icons">arrow_back</i> Voltar
              </button>
            </div>

            <div className="reserva-body">
              <div className="form-group">
                <label className="figma-label">Data</label>
                <div className="input-with-icon">
                  <input
                    type="date"
                    value={data}
                    onChange={(e) => setData(e.target.value)}
                    className="figma-input-large"
                    min={getDataLocalISO()}
                  />
                  <i className="material-icons">calendar_month</i>
                </div>
              </div>

              <div className="horarios-row">
                <div className="form-group">
                  <label className="figma-label">
                    <i className="material-icons">login</i> Início
                  </label>
                  <input
                    type="time"
                    value={inicio}
                    className={`figma-input-small ${conflito ? "input-error" : ""}`}
                    onChange={(e) => setInicio(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="figma-label">
                    <i className="material-icons">logout</i> Término
                  </label>
                  <input
                    type="time"
                    value={fim}
                    className={`figma-input-small ${conflito ? "input-error" : ""}`}
                    onChange={(e) => setFim(e.target.value)}
                  />
                </div>
              </div>

              <div className="indisponiveis-section">
                <span className="label-caps">
                  HORÁRIOS INDISPONÍVEIS PARA ESTE DIA
                </span>
                <div className="chips-container">
                  {horariosOcupados.length > 0 ? (
                    horariosOcupados.map((h, i) => (
                      <div key={i} className="chip-ocupado">
                        <i className="material-icons">not_interested</i>{" "}
                        {h.label}
                      </div>
                    ))
                  ) : (
                    <p style={{ color: "#a0aec0", fontSize: "14px" }}>
                      Nenhuma reserva para esta data.
                    </p>
                  )}
                </div>
              </div>

              <div className="reserva-footer-row">
                <div className="total-box">
                  <span className="label-caps">TOTAL DA RESERVA</span>
                  <p className="total-text">
                    {calcDurationLabel(inicio, fim)} de uso
                  </p>
                </div>
                <button
                  className="btn-confirmar-figma"
                  onClick={handleConfirmar}
                  disabled={submitting || conflito}
                >
                  {submitting ? "Processando..." : "Confirmar Reserva"}
                  <i className="material-icons">arrow_forward</i>
                </button>
              </div>

              {conflito && (
                <p className="error-message-text">
                  O horário selecionado conflita com uma reserva existente.
                </p>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
