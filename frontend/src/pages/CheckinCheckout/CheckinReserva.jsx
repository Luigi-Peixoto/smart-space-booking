import { useContext, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AuthContext } from "../../contexts/AuthContext";
import { fazerCheckIn } from "../../services/api";
import "./CheckinReserva.css";

const TEMPO_MINIMO_LOADING_MS = 2000;
function esperar(ms) { return new Promise(r => setTimeout(r, ms)); }

function CheckinReserva({ onClose }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user } = useContext(AuthContext);

  const [arquivos, setArquivos] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);

  function handleFileChange(e) {
    if (carregando) return;
    const selecionados = Array.from(e.target.files);
    if (!selecionados.length) return;
    setErro(null);
    const novosPreviews = selecionados.map(arquivo => ({
      url: URL.createObjectURL(arquivo),
      nome: arquivo.name,
    }));
    setArquivos(prev => [...prev, ...selecionados]);
    setPreviews(prev => [...prev, ...novosPreviews]);
    e.target.value = "";
  }

  function removerImagem(index) {
    if (carregando) return;
    URL.revokeObjectURL(previews[index].url);
    setArquivos(prev => prev.filter((_, i) => i !== index));
    setPreviews(prev => prev.filter((_, i) => i !== index));
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro(null);

    if (!arquivos.length) {
      setErro({ tipo: "geral", mensagem: "Adicione pelo menos uma foto da sala para confirmar o check-in." });
      return;
    }

    setCarregando(true);
    try {
      const [resultado] = await Promise.allSettled([
        fazerCheckIn(id, user.id, arquivos),
        esperar(TEMPO_MINIMO_LOADING_MS),
      ]);

      if (resultado.status === "rejected") throw resultado.reason;

      if (onClose) onClose();
      else navigate(-1);
    } catch (error) {
      console.error("Erro no check-in:", error);
      const status = error.response?.status;
      const mensagemBackend = error.response?.data;

      // HTTP 422 = imagem inválida OU sala incorreta
      if (status === 422) {
        previews.forEach(p => URL.revokeObjectURL(p.url));
        setArquivos([]);
        setPreviews([]);

        const tipoErro = mensagemBackend?.includes("sala reservada")
          ? "sala_incorreta"
          : "imagem_invalida";

        setErro({
          tipo: tipoErro,
          mensagem: mensagemBackend || "As imagens não passaram na validação. Tente novamente.",
        });
      } else {
        setErro({
          tipo: "geral",
          mensagem: mensagemBackend || "Erro ao processar o check-in. Tente novamente.",
        });
      }
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    return () => previews.forEach(p => URL.revokeObjectURL(p.url));
  }, [previews]);

  const iconeErro = erro?.tipo === "sala_incorreta" ? "🏢" : erro?.tipo === "imagem_invalida" ? "📷" : "⚠️";

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
          <h2 className="page-title">Check-in da Sala</h2>
          <p>Tire uma foto do ambiente para confirmar sua presença.</p>
        </div>

        <form onSubmit={handleSubmit} className="checkin-form">

          {erro && (
            <div className={`checkin-erro ${erro.tipo !== "geral" ? "checkin-erro--imagem" : "checkin-erro--geral"}`}>
              <span className="checkin-erro-icone">{iconeErro}</span>
              <p>{erro.mensagem}</p>
            </div>
          )}

          <div
            className={`checkin-upload-zona ${carregando ? "checkin-upload-zona--desabilitada" : ""}`}
            onClick={() => !carregando && document.getElementById("input-fotos-checkin").click()}
          >
            <span className="material-icons checkin-upload-icone">add_a_photo</span>
            <p className="checkin-upload-texto">Clique para adicionar fotos</p>
            <p className="checkin-upload-subtexto">JPG, PNG ou WEBP · Múltiplas imagens permitidas</p>
            <input
              id="input-fotos-checkin"
              type="file"
              accept="image/*"
              multiple
              capture="environment"
              onChange={handleFileChange}
              disabled={carregando}
              style={{ display: "none" }}
            />
          </div>

          {previews.length > 0 && (
            <div className="checkin-previews">
              {previews.map((preview, index) => (
                <div key={index} className="checkin-preview-item">
                  <img src={preview.url} alt={preview.nome} className="checkin-preview-img" />
                  <button
                    type="button"
                    className="checkin-preview-remover"
                    onClick={() => removerImagem(index)}
                    disabled={carregando}
                    title="Remover imagem"
                  >✕</button>
                </div>
              ))}
              {!carregando && (
                <div
                  className="checkin-preview-adicionar"
                  onClick={() => document.getElementById("input-fotos-checkin").click()}
                  title="Adicionar mais imagens"
                >
                  <span>+</span>
                </div>
              )}
            </div>
          )}

          <div className="checkin-actions">
            <button type="button" className="btn-cancel" onClick={onClose || (() => navigate(-1))} disabled={carregando}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary btn-save" disabled={carregando || !arquivos.length}>
              {carregando ? "Validando..." : `Confirmar Check-in${arquivos.length > 0 ? ` (${arquivos.length})` : ""}`}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default CheckinReserva;