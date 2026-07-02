import { useState, useEffect, useContext } from 'react';
import { AuthContext } from "../../contexts/AuthContext";
import { useNavigate } from 'react-router-dom';
import {
  getRegrasTrustScoreEvento,
  criarRegraTrustScoreEvento,
  atualizarRegraTrustScoreEvento,
  deletarRegraTrustScoreEvento,
} from '../../services/api';
import './Admin.css';
import './RegrasAvaliacao.css';

// Conjunto fechado de eventos — definido pelo backend (enum TrustScoreEvento).
// O admin só ajusta severidade (delta) e, quando existir, o parâmetro numérico;
// não é possível criar ou remover um evento pela UI.
const EVENTOS = [
  {
    evento: 'CANCELAMENTO_TARDIO',
    titulo: 'Cancelamento Tardio',
    descricao: 'Penalidade aplicada quando o usuário cancela a reserva a menos de X horas do início.',
    temParametro: true,
    labelParametro: 'Janela de antecedência (horas)',
    padraoDelta: -10,
    padraoParametro: 2,
  },
  {
    evento: 'NO_SHOW',
    titulo: 'No-Show',
    descricao: 'Penalidade aplicada quando o usuário não comparece e a reserva é cancelada automaticamente.',
    temParametro: false,
    padraoDelta: -15,
  },
  {
    evento: 'EXCESSO_CANCELAMENTOS',
    titulo: 'Excesso de Cancelamentos',
    descricao: 'Penalidade extra quando o usuário cancela mais reservas do que o limite permitido na mesma semana.',
    temParametro: true,
    labelParametro: 'Limite (cancelamentos por semana)',
    padraoDelta: -20,
    padraoParametro: 3,
  },
];

function RegrasTrustScoreEvento() {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [linhas, setLinhas] = useState({});
  const [loading, setLoading] = useState(true);
  const [salvandoEvento, setSalvandoEvento] = useState(null);

  useEffect(() => { carregar(); }, []);

  async function carregar() {
    try {
      const res = await getRegrasTrustScoreEvento(user.id);
      const porEvento = {};
      EVENTOS.forEach(cfg => {
        const existente = res.data.find(r => r.evento === cfg.evento);
        porEvento[cfg.evento] = existente
          ? {
              id: existente.id,
              delta: existente.delta,
              parametro: existente.parametro ?? '',
              usandoPadrao: false,
            }
          : {
              id: null,
              delta: cfg.padraoDelta,
              parametro: cfg.padraoParametro ?? '',
              usandoPadrao: true,
            };
      });
      setLinhas(porEvento);
    } catch {
      alert('Erro ao carregar as penalidades de TrustScore.');
    } finally {
      setLoading(false);
    }
  }

  function set(evento, campo) {
    return (v) => setLinhas(prev => ({ ...prev, [evento]: { ...prev[evento], [campo]: v } }));
  }

  async function salvar(cfg) {
    const linha = linhas[cfg.evento];
    if (linha.delta === '' || linha.delta === null || Number.isNaN(Number(linha.delta))) {
      alert('Informe o delta (penalidade).');
      return;
    }

    setSalvandoEvento(cfg.evento);
    try {
      const payload = {
        evento: cfg.evento,
        delta: Number(linha.delta),
        parametro: cfg.temParametro && linha.parametro !== '' ? Number(linha.parametro) : null,
      };
      if (linha.id) {
        await atualizarRegraTrustScoreEvento(linha.id, payload, user.id);
      } else {
        await criarRegraTrustScoreEvento(payload, user.id);
      }
      await carregar();
    } catch (e) {
      alert('Erro ao salvar: ' + (e.response?.data || e.message));
    } finally {
      setSalvandoEvento(null);
    }
  }

  async function restaurarPadrao(cfg) {
    const linha = linhas[cfg.evento];
    if (!linha?.id) return;
    if (!window.confirm('Remover a configuração salva e voltar ao valor padrão do sistema?')) return;

    setSalvandoEvento(cfg.evento);
    try {
      await deletarRegraTrustScoreEvento(linha.id, user.id);
      await carregar();
    } catch {
      alert('Erro ao restaurar o padrão.');
    } finally {
      setSalvandoEvento(null);
    }
  }

  if (loading) {
    return (
      <div className="admin-container">
        <main className="admin-main"><p>Carregando penalidades...</p></main>
      </div>
    );
  }

  return (
    <div className="admin-container">
      <main className="admin-main">

        <div className="page-header">
          <div>
            <h1 className="page-title">Penalidades de TrustScore</h1>
            <p className="regras-subtitulo">
              Ajuste a severidade dos eventos de cancelamento e no-show das reservas.
              Eventos marcados como "padrão" ainda não têm configuração salva e usam o valor padrão do sistema.
            </p>
          </div>
          <button className="btn-secondary" onClick={() => navigate('/admin')}>
            <span className="material-icons">arrow_back</span>
            Voltar
          </button>
        </div>

        <div className="regras-grid">
          {EVENTOS.map(cfg => {
            const linha = linhas[cfg.evento] || {};
            const salvando = salvandoEvento === cfg.evento;

            return (
              <div key={cfg.evento} className="regra-card">
                <div className="regra-card-header">
                  <h3 className="regra-nome">{cfg.titulo}</h3>
                  {!linha.usandoPadrao && (
                    <span
                      className="material-icons action-icon"
                      title="Restaurar padrão"
                      onClick={() => !salvando && restaurarPadrao(cfg)}
                    >
                      restart_alt
                    </span>
                  )}
                </div>

                <p className="regra-descricao">{cfg.descricao}</p>

                {linha.usandoPadrao && (
                  <span className="regra-limiar-label" style={{ color: 'var(--text-gray)' }}>
                    USANDO PADRÃO DO SISTEMA
                  </span>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.25rem' }}>
                  <div>
                    <label className="regras-label">Delta aplicado (pts)</label>
                    <input
                      type="number"
                      className="regras-input"
                      max={0}
                      value={linha.delta ?? ''}
                      onChange={e => set(cfg.evento, 'delta')(e.target.value)}
                    />
                  </div>

                  {cfg.temParametro && (
                    <div>
                      <label className="regras-label">{cfg.labelParametro}</label>
                      <input
                        type="number"
                        className="regras-input"
                        min={0}
                        value={linha.parametro ?? ''}
                        onChange={e => set(cfg.evento, 'parametro')(e.target.value)}
                      />
                    </div>
                  )}
                </div>

                <button
                  className="btn-primary"
                  style={{ marginTop: '0.5rem', width: '100%', justifyContent: 'center' }}
                  disabled={salvando}
                  onClick={() => salvar(cfg)}
                >
                  {salvando ? '⏳ Salvando...' : 'Salvar'}
                </button>
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}

export default RegrasTrustScoreEvento;
