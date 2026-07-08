import { useContext, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AuthContext } from "../../../../contexts/AuthContext";
import "../../../../pages/CheckinCheckout/CheckinReserva.css";
import { fazerCheckIn } from "../../../../services/api";

const TEMPO_MINIMO_LOADING_MS = 2000;
function esperar(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

const SLOTS = [
  {
    chave: "externa",
    label: "Foto Externa",
    descricao: "Visão externa da lataria do veículo",
  },
  {
    chave: "interna",
    label: "Foto Interna",
    descricao: "Visão interna — bancos e painel",
  },
  {
    chave: "placa",
    label: "Foto da Placa",
    descricao: "Foto aproximada e legível da placa",
  },
];

function CheckinReservaVeiculo({ onClose }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user, refreshUser } = useContext(AuthContext);

  const [fotos, setFotos] = useState({
    externa: null,
    interna: null,
    placa: null,
  });
  const [previews, setPreviews] = useState({
    externa: null,
    interna: null,
    placa: null,
  });
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);

  function handleFileChange(chave, e) {
    if (carregando) return;
    const arquivo = e.target.files[0];
    if (!arquivo) return;
    setErro(null);

    if (previews[chave]) URL.revokeObjectURL(previews[chave]);

    setFotos((prev) => ({ ...prev, [chave]: arquivo }));
    setPreviews((prev) => ({ ...prev, [chave]: URL.createObjectURL(arquivo) }));
    e.target.value = "";
  }

  function removerFoto(chave) {
    if (carregando) return;
    if (previews[chave]) URL.revokeObjectURL(previews[chave]);
    setFotos((prev) => ({ ...prev, [chave]: null }));
    setPreviews((prev) => ({ ...prev, [chave]: null }));
  }

  const todasPreenchidas = SLOTS.every((slot) => fotos[slot.chave] !== null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro(null);

    if (!todasPreenchidas) {
      setErro({
        tipo: "geral",
        mensagem:
          "Envie as 3 fotos (externa, interna e placa) para confirmar o check-in.",
      });
      return;
    }

    const arquivosOrdenados = [fotos.externa, fotos.interna, fotos.placa];

    setCarregando(true);
    try {
      const [resultado] = await Promise.allSettled([
        fazerCheckIn(id, user.id, arquivosOrdenados),
        esperar(TEMPO_MINIMO_LOADING_MS),
      ]);

      if (resultado.status === "rejected") throw resultado.reason;

      await refreshUser();

      if (onClose) onClose();
      else navigate(-1);
    } catch (error) {
      console.error("Erro no check-in:", error);
      const status = error.response?.status;
      const mensagemBackend = error.response?.data;

      if (status === 422) {
        Object.values(previews).forEach(
          (url) => url && URL.revokeObjectURL(url),
        );
        setFotos({ externa: null, interna: null, placa: null });
        setPreviews({ externa: null, interna: null, placa: null });

        const tipoErro = mensagemBackend?.includes("veículo reservado")
          ? "recurso_incorreto"
          : "imagem_invalida";

        setErro({
          tipo: tipoErro,
          mensagem:
            mensagemBackend ||
            "As imagens não passaram na validação. Refaça as 3 fotos.",
        });
      } else {
        setErro({
          tipo: "geral",
          mensagem:
            mensagemBackend || "Erro ao processar o check-in. Tente novamente.",
        });
      }
    } finally {
      setCarregando(false);
    }
  };

  const iconeErro =
    erro?.tipo === "recurso_incorreto"
      ? "🚗"
      : erro?.tipo === "imagem_invalida"
        ? "📷"
        : "⚠️";

  return (
    <div className="admin-container">
      {carregando && (
        <div className="checkin-loading-overlay">
          <div className="checkin-loading-box">
            <div className="checkin-spinner" />
            <p>Validando com IA...</p>
            <span>Isso pode levar alguns segundos.</span>
          </div>
        </div>
      )}

      <main className="admin-main">
        <div className="page-header">
          <h2 className="page-title">Check-in do Veículo</h2>
          <p>Envie as 3 fotos exigidas para confirmar a retirada do veículo.</p>
        </div>

        <form onSubmit={handleSubmit} className="checkin-form">
          {erro && (
            <div
              className={`checkin-erro ${erro.tipo !== "geral" ? "checkin-erro--imagem" : "checkin-erro--geral"}`}
            >
              <span className="checkin-erro-icone">{iconeErro}</span>
              <p>{erro.mensagem}</p>
            </div>
          )}

          {SLOTS.map((slot) => (
            <div key={slot.chave}>
              <label
                className="checkin-upload-texto"
                style={{ display: "block", marginBottom: "8px" }}
              >
                {slot.label}
              </label>

              {!previews[slot.chave] ? (
                <div
                  className={`checkin-upload-zona ${carregando ? "checkin-upload-zona--desabilitada" : ""}`}
                  onClick={() =>
                    !carregando &&
                    document.getElementById(`input-foto-${slot.chave}`).click()
                  }
                >
                  <span className="material-icons checkin-upload-icone">
                    add_a_photo
                  </span>
                  <p className="checkin-upload-texto">Clique para adicionar</p>
                  <p className="checkin-upload-subtexto">{slot.descricao}</p>
                  <input
                    id={`input-foto-${slot.chave}`}
                    type="file"
                    accept="image/*"
                    capture="environment"
                    onChange={(e) => handleFileChange(slot.chave, e)}
                    disabled={carregando}
                    style={{ display: "none" }}
                  />
                </div>
              ) : (
                <div className="checkin-previews">
                  <div className="checkin-preview-item">
                    <img
                      src={previews[slot.chave]}
                      alt={slot.label}
                      className="checkin-preview-img"
                    />
                    <button
                      type="button"
                      className="checkin-preview-remover"
                      onClick={() => removerFoto(slot.chave)}
                      disabled={carregando}
                      title="Remover imagem"
                    >
                      ✕
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}

          <div className="checkin-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose || (() => navigate(-1))}
              disabled={carregando}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary btn-save"
              disabled={carregando || !todasPreenchidas}
            >
              {carregando ? "Validando..." : "Confirmar Check-in"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default CheckinReservaVeiculo;
