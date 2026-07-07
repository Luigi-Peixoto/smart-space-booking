import { useEffect, useState } from "react";
import { getTrustScoreHistorico } from "../../../../services/api";
import "./PainelHistoricoScore.css";

function PainelHistoricoScore({ usuarioId, onFechar }) {
  const [historico, setHistorico] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function carregar() {
      try {
        const res = await getTrustScoreHistorico(usuarioId);
        setHistorico(res.data);
      } catch (e) {
        console.error("Erro ao carregar histórico:", e);
      } finally {
        setLoading(false);
      }
    }
    carregar();
  }, [usuarioId]);

  function formatarData(dataStr) {
    return new Date(dataStr).toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  return (
    <>
      {/* Overlay escuro ao fundo */}
      <div className="painel-overlay" onClick={onFechar} />

      {/* Painel lateral */}
      <aside className="painel-historico">
        <div className="painel-header">
          <div>
            <h2 className="painel-titulo">Histórico de Trust Score</h2>
            <p className="painel-subtitulo">
              Todas as alterações da sua reputação
            </p>
          </div>
          <button className="painel-fechar" onClick={onFechar}>
            ✕
          </button>
        </div>

        <div className="painel-corpo">
          {loading ? (
            <p className="painel-vazio">Carregando...</p>
          ) : historico.length === 0 ? (
            <p className="painel-vazio">Nenhuma alteração registrada ainda.</p>
          ) : (
            <ul className="painel-lista">
              {historico.map((item, index) => (
                <li key={item.id} className="painel-item">
                  {/* Linha vertical da timeline */}
                  <div className="painel-timeline">
                    <div
                      className={`painel-dot ${item.delta > 0 ? "dot-bonus" : item.delta < 0 ? "dot-penalidade" : "dot-neutro"}`}
                    />
                    {index < historico.length - 1 && (
                      <div className="painel-linha" />
                    )}
                  </div>

                  <div className="painel-item-conteudo">
                    <div className="painel-item-topo">
                      <span className="painel-item-descricao">
                        {item.descricao ||
                          item.regraNome ||
                          "Ajuste de sistema"}
                      </span>
                      <span
                        className={`painel-delta ${item.delta > 0 ? "delta-positivo" : item.delta < 0 ? "delta-negativo" : "delta-neutro"}`}
                      >
                        {item.delta > 0 ? `+${item.delta}` : item.delta}
                      </span>
                    </div>

                    <div className="painel-item-rodape">
                      <span className="painel-score-trace">
                        {item.scoreAnterior} →{" "}
                        <strong>{item.scorePosterior}</strong>
                      </span>
                      <span className="painel-data">
                        {formatarData(item.criadoEm)}
                      </span>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </aside>
    </>
  );
}

export default PainelHistoricoScore;
