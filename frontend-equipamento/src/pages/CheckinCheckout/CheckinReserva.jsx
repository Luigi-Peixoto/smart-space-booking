import { useContext, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AuthContext } from "../../contexts/AuthContext";
import { fazerCheckIn } from "../../services/api";
import "./CheckinReserva.css";

const TEMPO_MINIMO_LOADING_MS = 2000;
function esperar(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

// A API exige exatamente 2 imagens nesta ordem: (1) visão geral do
// equipamento, (2) visão do kit completo com todos os acessórios.
const SLOTS = [
  {
    chave: "geral",
    titulo: "Foto Geral do Equipamento",
    instrucao: "Tire uma foto mostrando o aparelho principal.",
  },
  {
    chave: "kit",
    titulo: "Foto do Kit Completo",
    instrucao: "Tire uma foto com todos os acessórios do kit dispostos e visíveis.",
  },
];

function CheckinReserva({ onClose }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user, refreshUser } = useContext(AuthContext);

  const [fotos, setFotos] = useState({});
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);

  function handleFileChange(chave, e) {
    if (carregando) return;
    const arquivo = e.target.files[0];
    if (!arquivo) return;
    setErro(null);
    setFotos((prev) => {
      if (prev[chave]) URL.revokeObjectURL(prev[chave].url);
      return {
        ...prev,
        [chave]: { arquivo, url: URL.createObjectURL(arquivo) },
      };
    });
    e.target.value = "";
  }

  function removerFoto(chave) {
    if (carregando) return;
    setFotos((prev) => {
      if (prev[chave]) URL.revokeObjectURL(prev[chave].url);
      const { [chave]: _removida, ...resto } = prev;
      return resto;
    });
  }

  const completo = SLOTS.every((slot) => fotos[slot.chave]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro(null);

    if (!completo) {
      setErro({
        tipo: "geral",
        mensagem:
          "Envie as 2 fotos exigidas (visão geral e kit completo) para confirmar o check-in.",
      });
      return;
    }

    // A ordem importa: a API espera [GERAL, KIT] nessa sequência exata.
    const arquivos = SLOTS.map((slot) => fotos[slot.chave].arquivo);

    setCarregando(true);
    try {
      const [resultado] = await Promise.allSettled([
        fazerCheckIn(id, user.id, arquivos),
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

      // HTTP 422 = imagem inválida OU equipamento incorreto
      if (status === 422) {
        Object.values(fotos).forEach((f) => URL.revokeObjectURL(f.url));
        setFotos({});

        const tipoErro = mensagemBackend?.includes("não corresponde ao reservado")
          ? "recurso_incorreto"
          : "imagem_invalida";

        setErro({
          tipo: tipoErro,
          mensagem:
            mensagemBackend ||
            "As imagens não passaram na validação. Tente novamente.",
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

  useEffect(() => {
    return () => Object.values(fotos).forEach((f) => URL.revokeObjectURL(f.url));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const iconeErro =
    erro?.tipo === "recurso_incorreto"
      ? "📦"
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
          <h2 className="page-title">Check-in do Equipamento</h2>
          <p>São necessárias 2 fotos: uma geral do equipamento e outra do kit completo com todos os acessórios.</p>
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

          <div className="checkin-slots">
            {SLOTS.map((slot) => {
              const foto = fotos[slot.chave];
              return (
                <div key={slot.chave} className="checkin-slot">
                  <div className="checkin-slot-header">
                    <span className="checkin-slot-titulo">{slot.titulo}</span>
                    <span className="checkin-slot-instrucao">{slot.instrucao}</span>
                  </div>

                  {foto ? (
                    <div className="checkin-slot-preview">
                      <img
                        src={foto.url}
                        alt={slot.titulo}
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
                  ) : (
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
                      <p className="checkin-upload-texto">Clique para adicionar foto</p>
                      <p className="checkin-upload-subtexto">JPG, PNG ou WEBP</p>
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
                  )}
                </div>
              );
            })}
          </div>

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
              disabled={carregando || !completo}
            >
              {carregando ? "Validando..." : "Confirmar Check-in"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default CheckinReserva;
