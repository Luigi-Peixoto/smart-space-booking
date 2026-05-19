import { useEffect, useState } from "react";
import { getAuditoriasPorReserva } from "../../../../services/api";
import "./PainelAuditoria.css";

function PainelAuditoria({ reserva, onFechar }) {
  const [auditorias, setAuditorias] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function carregar() {
      try {
        const res = await getAuditoriasPorReserva(reserva.id);
        setAuditorias(res.data);
      } catch (e) {
        console.error("Erro ao carregar auditorias:", e);
      } finally {
        setLoading(false);
      }
    }
    carregar();
  }, [reserva.id]);

  function formatarData(dataStr) {
    return new Date(dataStr).toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function notaCor(nota) {
    if (nota >= 8) return "#388e3c";
    if (nota >= 5) return "#f57c00";
    return "#d32f2f";
  }

  const checkin = auditorias.find((a) => a.tipo === "CHECK_IN");
  const checkout = auditorias.find((a) => a.tipo === "CHECK_OUT");

  return (
    <>
      <div className="pa-overlay" onClick={onFechar} />
      <aside className="pa-painel">
        <div className="pa-header">
          <div>
            <h2 className="pa-titulo">Auditoria da Reserva</h2>
            <p className="pa-subtitulo">
              {reserva.sala} · {reserva.data} · {reserva.horario}
            </p>
          </div>
          <button className="pa-fechar" onClick={onFechar}>
            ✕
          </button>
        </div>

        <div className="pa-corpo">
          {loading ? (
            <p className="pa-vazio">Carregando...</p>
          ) : auditorias.length === 0 ? (
            <p className="pa-vazio">
              Nenhuma auditoria registrada para esta reserva.
            </p>
          ) : (
            <div className="pa-secoes">
              {checkin && (
                <section className="pa-secao">
                  <div className="pa-secao-header">
                    <span className="pa-secao-icone">📷</span>
                    <div className="pa-secao-info">
                      <h3 className="pa-secao-titulo">Check-in</h3>
                      <p className="pa-secao-data">
                        {formatarData(checkin.dateCreated)}
                      </p>
                    </div>
                    <span
                      className={`pa-badge ${checkin.aprovado ? "pa-badge--aprovado" : "pa-badge--reprovado"}`}
                    >
                      {checkin.aprovado ? "Aprovado" : "Reprovado"}
                    </span>
                  </div>
                  {checkin.observacaoGeral && (
                    <p className="pa-observacao">"{checkin.observacaoGeral}"</p>
                  )}
                </section>
              )}

              {checkin && checkout && <div className="pa-divisor" />}

              {checkout && (
                <section className="pa-secao">
                  <div className="pa-secao-header">
                    <span className="pa-secao-icone">🔍</span>
                    <div className="pa-secao-info">
                      <h3 className="pa-secao-titulo">Check-out</h3>
                      <p className="pa-secao-data">
                        {formatarData(checkout.dateCreated)}
                      </p>
                    </div>
                    <span
                      className={`pa-badge ${checkout.aprovado ? "pa-badge--aprovado" : "pa-badge--reprovado"}`}
                    >
                      {checkout.aprovado ? "Aprovado" : "Reprovado"}
                    </span>
                  </div>
                  {checkout.observacaoGeral && (
                    <p className="pa-observacao">
                      "{checkout.observacaoGeral}"
                    </p>
                  )}
                  {checkout.criterios?.length > 0 && (
                    <div className="pa-criterios">
                      <p className="pa-criterios-titulo">
                        Critérios avaliados pela IA
                      </p>
                      <ul className="pa-criterios-lista">
                        {checkout.criterios.map((c, i) => (
                          <li key={i} className="pa-criterio-item">
                            <span className="pa-criterio-nome">
                              {c.nome || `Critério ${i + 1}`}
                            </span>
                            <div className="pa-criterio-direita">
                              <div className="pa-criterio-barra">
                                <div
                                  className="pa-criterio-barra-fill"
                                  style={{
                                    width: `${(c.nota / 10) * 100}%`,
                                    backgroundColor: notaCor(c.nota),
                                  }}
                                />
                              </div>
                              <span
                                className="pa-criterio-nota"
                                style={{ color: notaCor(c.nota) }}
                              >
                                {c.nota}/10
                              </span>
                            </div>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  {checkout.deltaTrustScoreAplicado !== null &&
                    checkout.deltaTrustScoreAplicado !== undefined && (
                      <div
                        className={`pa-delta ${checkout.deltaTrustScoreAplicado >= 0 ? "pa-delta--positivo" : "pa-delta--negativo"}`}
                      >
                        <span>Impacto no Trust Score</span>
                        <strong>
                          {checkout.deltaTrustScoreAplicado > 0
                            ? `+${checkout.deltaTrustScoreAplicado}`
                            : checkout.deltaTrustScoreAplicado}
                        </strong>
                      </div>
                    )}
                </section>
              )}

              {checkin && !checkout && (
                <div className="pa-pendente">
                  <span>🕐</span>
                  <p>Check-out ainda não realizado.</p>
                </div>
              )}
            </div>
          )}
        </div>
      </aside>
    </>
  );
}

export default PainelAuditoria;
