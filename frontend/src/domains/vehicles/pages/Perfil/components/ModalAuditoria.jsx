import { useRef, useState } from "react";
import "./ModalAuditoria.css";

function ModalAuditoria({ tipo, reserva, onConfirmar, onFechar, processando }) {
  const [arquivos, setArquivos] = useState([]);
  const [previews, setPreviews] = useState([]);
  const inputRef = useRef(null);

  const isCheckIn = tipo === "checkin";
  const titulo = isCheckIn ? "Check-in" : "Check-out";
  const descricao = isCheckIn
    ? "Fotografe a sala antes de utilizá-la. A IA irá validar as condições do ambiente."
    : "Fotografe a sala após o uso. A IA irá avaliar e classificar o estado do ambiente.";
  const corAcento = isCheckIn ? "#1976d2" : "#e65100";

  function handleSelecionarArquivos(e) {
    const selecionados = Array.from(e.target.files);
    if (!selecionados.length) return;

    const novosPreviews = selecionados.map((arquivo) => ({
      url: URL.createObjectURL(arquivo),
      nome: arquivo.name,
    }));

    setArquivos((prev) => [...prev, ...selecionados]);
    setPreviews((prev) => [...prev, ...novosPreviews]);
    e.target.value = "";
  }

  function removerImagem(index) {
    URL.revokeObjectURL(previews[index].url);
    setArquivos((prev) => prev.filter((_, i) => i !== index));
    setPreviews((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleConfirmar() {
    if (!arquivos.length) return;
    await onConfirmar(arquivos);
  }

  function handleOverlayClick(e) {
    if (e.target === e.currentTarget && !processando) onFechar();
  }

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal-box">
        {/* Cabeçalho */}
        <div className="modal-header" style={{ borderTopColor: corAcento }}>
          <div className="modal-titulo-grupo">
            <span className="modal-icone" style={{ color: corAcento }}>
              {isCheckIn ? "📷" : "🔍"}
            </span>
            <div>
              <h2 className="modal-titulo">{titulo}</h2>
              <p className="modal-subtitulo">
                {reserva.sala} · {reserva.data} · {reserva.horario}
              </p>
            </div>
          </div>
          {!processando && (
            <button className="modal-fechar" onClick={onFechar}>
              ✕
            </button>
          )}
        </div>

        {/* Descrição */}
        <p className="modal-descricao">{descricao}</p>

        {/* Zona de upload */}
        <div
          className="modal-upload-zona"
          onClick={() => inputRef.current?.click()}
          style={{ borderColor: corAcento }}
        >
          <span className="modal-upload-icone">🖼️</span>
          <p className="modal-upload-texto">Clique para selecionar imagens</p>
          <p className="modal-upload-subtexto">
            JPG, PNG ou WEBP · Múltiplas imagens permitidas
          </p>
          <input
            ref={inputRef}
            type="file"
            accept="image/*"
            multiple
            style={{ display: "none" }}
            onChange={handleSelecionarArquivos}
          />
        </div>

        {/* Prévia das imagens */}
        {previews.length > 0 && (
          <div className="modal-previews">
            {previews.map((preview, index) => (
              <div key={index} className="modal-preview-item">
                <img
                  src={preview.url}
                  alt={preview.nome}
                  className="modal-preview-img"
                />
                <button
                  className="modal-preview-remover"
                  onClick={() => removerImagem(index)}
                  disabled={processando}
                  title="Remover imagem"
                >
                  ✕
                </button>
              </div>
            ))}

            {/* Card para adicionar mais */}
            <div
              className="modal-preview-adicionar"
              onClick={() => inputRef.current?.click()}
              title="Adicionar mais imagens"
            >
              <span>+</span>
            </div>
          </div>
        )}

        {/* Rodapé */}
        <div className="modal-rodape">
          <button
            className="modal-btn-cancelar"
            onClick={onFechar}
            disabled={processando}
          >
            Cancelar
          </button>
          <button
            className="modal-btn-confirmar"
            onClick={handleConfirmar}
            disabled={!arquivos.length || processando}
            style={{ backgroundColor: corAcento }}
          >
            {processando
              ? "⏳ Analisando..."
              : `Enviar ${arquivos.length > 0 ? `(${arquivos.length})` : ""}`}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ModalAuditoria;
