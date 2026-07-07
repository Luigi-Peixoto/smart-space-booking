import { useState, useEffect, useContext } from 'react';
import { AuthContext } from "../../contexts/AuthContext";
import { useNavigate } from 'react-router-dom';
import {
  getRegrasTrustScore,
  criarRegraTrustScore,
  atualizarRegraTrustScore,
  deletarRegraTrustScore,
  getRestricoesDisponiveis,
  getEventosDisponiveis,
  getNiveisExigenciaDisponiveis,
} from '../../services/api';
import './Admin.css';
import './RegrasAvaliacao.css';

const TIPO_RECURSO = 'EQUIPAMENTO';

// Deriva um título legível a partir da chave — eventos e restrições vêm do
// backend só com (chave, descricao); a chave de concorrência é o próprio
// TIPO_RECURSO sem sufixo (ex: "EQUIPAMENTO"), as demais têm um sufixo depois de ":"
// (ex: "EQUIPAMENTO:DEVOLUCAO_ATRASADA") ou nenhum prefixo (eventos prontos, ex:
// "CANCELAMENTO_TARDIO").
function humanizarChave(chave) {
  if (chave === TIPO_RECURSO) return 'Limite de reservas simultâneas';
  const partes = chave.split(':');
  const base = partes.length > 1 ? partes[1] : partes[0];
  const texto = base.replaceAll('_', ' ').toLowerCase();
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

function RegrasTrustScore() {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [eventosDisponiveis, setEventosDisponiveis] = useState([]);
  const [linhas, setLinhas] = useState({});
  const [niveisDisponiveis, setNiveisDisponiveis] = useState([]);
  const [linhasExigencia, setLinhasExigencia] = useState({});
  const [restricoesDisponiveis, setRestricoesDisponiveis] = useState([]);
  const [linhasRestricao, setLinhasRestricao] = useState({});

  const [loading, setLoading] = useState(true);
  const [salvandoEvento, setSalvandoEvento] = useState(null);
  const [salvandoNivel, setSalvandoNivel] = useState(null);
  const [salvandoChave, setSalvandoChave] = useState(null);

  useEffect(() => { carregar(); }, []);

  async function carregar() {
    try {
      const [
        resEventosConfig, resExigenciaConfig, resRestricaoConfig,
        resEventosDisponiveis, resNiveisDisponiveis, resRestricoesDisponiveis,
      ] = await Promise.all([
        getRegrasTrustScore('EVENTO', user.id),
        getRegrasTrustScore('EXIGENCIA', user.id),
        getRegrasTrustScore('RESTRICAO', user.id),
        getEventosDisponiveis(user.id, TIPO_RECURSO),
        getNiveisExigenciaDisponiveis(user.id, TIPO_RECURSO),
        getRestricoesDisponiveis(user.id, TIPO_RECURSO),
      ]);

      setEventosDisponiveis(resEventosDisponiveis.data);
      const porEvento = {};
      resEventosDisponiveis.data.forEach(({ chave, deltaPadrao, parametroPadrao }) => {
        const existente = resEventosConfig.data.find(r => r.chave === chave);
        porEvento[chave] = existente
          ? { id: existente.id, delta: existente.valorPrincipal, parametro: existente.valorSecundario ?? '', usandoPadrao: false }
          : { id: null, delta: deltaPadrao, parametro: parametroPadrao ?? '', usandoPadrao: true };
      });
      setLinhas(porEvento);

      setNiveisDisponiveis(resNiveisDisponiveis.data);
      const porNivel = {};
      resNiveisDisponiveis.data.forEach(({ chave, scoreMinimoPadrao }) => {
        const existente = resExigenciaConfig.data.find(r => r.chave === chave);
        porNivel[chave] = existente
          ? { id: existente.id, scoreMinimo: existente.valorPrincipal, usandoPadrao: false }
          : { id: null, scoreMinimo: scoreMinimoPadrao, usandoPadrao: true };
      });
      setLinhasExigencia(porNivel);

      setRestricoesDisponiveis(resRestricoesDisponiveis.data);
      const porChave = {};
      resRestricoesDisponiveis.data.forEach(({ chave, valorPrincipalPadrao, valorSecundarioPadrao }) => {
        const existente = resRestricaoConfig.data.find(r => r.chave === chave);
        porChave[chave] = existente
          ? { id: existente.id, valorPrincipal: existente.valorPrincipal, scoreMinimoParaExceder: existente.valorSecundario, usandoPadrao: false }
          : { id: null, valorPrincipal: valorPrincipalPadrao, scoreMinimoParaExceder: valorSecundarioPadrao, usandoPadrao: true };
      });
      setLinhasRestricao(porChave);
    } catch {
      alert('Erro ao carregar as regras de TrustScore.');
    } finally {
      setLoading(false);
    }
  }

  function set(chave, campo) {
    return (v) => setLinhas(prev => ({ ...prev, [chave]: { ...prev[chave], [campo]: v } }));
  }

  function setExigencia(chave, campo) {
    return (v) => setLinhasExigencia(prev => ({ ...prev, [chave]: { ...prev[chave], [campo]: v } }));
  }

  function setRestricao(chave, campo) {
    return (v) => setLinhasRestricao(prev => ({ ...prev, [chave]: { ...prev[chave], [campo]: v } }));
  }

  async function salvar(evento) {
    const linha = linhas[evento.chave];
    if (linha.delta === '' || linha.delta === null || Number.isNaN(Number(linha.delta))) {
      alert('Informe o delta (penalidade).');
      return;
    }

    setSalvandoEvento(evento.chave);
    try {
      const payload = {
        categoria: 'EVENTO',
        chave: evento.chave,
        valorPrincipal: Number(linha.delta),
        valorSecundario: linha.parametro !== '' && linha.parametro !== null ? Number(linha.parametro) : null,
        descricao: evento.descricao,
      };
      if (linha.id) {
        await atualizarRegraTrustScore(linha.id, payload, user.id);
      } else {
        await criarRegraTrustScore(payload, user.id);
      }
      await carregar();
    } catch (e) {
      alert('Erro ao salvar: ' + (e.response?.data || e.message));
    } finally {
      setSalvandoEvento(null);
    }
  }

  async function restaurarPadrao(evento) {
    const linha = linhas[evento.chave];
    if (!linha?.id) return;
    if (!window.confirm('Remover a configuração salva e voltar ao valor padrão do sistema?')) return;

    setSalvandoEvento(evento.chave);
    try {
      await deletarRegraTrustScore(linha.id, user.id);
      await carregar();
    } catch {
      alert('Erro ao restaurar o padrão.');
    } finally {
      setSalvandoEvento(null);
    }
  }

  async function salvarExigencia(nivel) {
    const linha = linhasExigencia[nivel.chave];
    if (linha.scoreMinimo === '' || linha.scoreMinimo === null || Number.isNaN(Number(linha.scoreMinimo))) {
      alert('Informe o score mínimo.');
      return;
    }

    setSalvandoNivel(nivel.chave);
    try {
      const payload = {
        categoria: 'EXIGENCIA',
        chave: nivel.chave,
        valorPrincipal: Number(linha.scoreMinimo),
        descricao: nivel.descricao,
      };
      if (linha.id) {
        await atualizarRegraTrustScore(linha.id, payload, user.id);
      } else {
        await criarRegraTrustScore(payload, user.id);
      }
      await carregar();
    } catch (e) {
      alert('Erro ao salvar: ' + (e.response?.data || e.message));
    } finally {
      setSalvandoNivel(null);
    }
  }

  async function restaurarPadraoExigencia(nivel) {
    const linha = linhasExigencia[nivel.chave];
    if (!linha?.id) return;
    if (!window.confirm('Remover a configuração salva e voltar ao valor padrão do sistema?')) return;

    setSalvandoNivel(nivel.chave);
    try {
      await deletarRegraTrustScore(linha.id, user.id);
      await carregar();
    } catch {
      alert('Erro ao restaurar o padrão.');
    } finally {
      setSalvandoNivel(null);
    }
  }

  async function salvarRestricao(restricao) {
    const linha = linhasRestricao[restricao.chave];
    if (!linha.valorPrincipal || Number(linha.valorPrincipal) < 1) {
      alert('Informe um valor limite de pelo menos 1.');
      return;
    }
    if (linha.scoreMinimoParaExceder === '' || linha.scoreMinimoParaExceder === null || Number.isNaN(Number(linha.scoreMinimoParaExceder))) {
      alert('Informe o score mínimo para exceder o limite.');
      return;
    }

    setSalvandoChave(restricao.chave);
    try {
      const payload = {
        categoria: 'RESTRICAO',
        chave: restricao.chave,
        valorPrincipal: Number(linha.valorPrincipal),
        valorSecundario: Number(linha.scoreMinimoParaExceder),
        descricao: restricao.descricao,
      };
      if (linha.id) {
        await atualizarRegraTrustScore(linha.id, payload, user.id);
      } else {
        await criarRegraTrustScore(payload, user.id);
      }
      await carregar();
    } catch (e) {
      alert('Erro ao salvar: ' + (e.response?.data || e.message));
    } finally {
      setSalvandoChave(null);
    }
  }

  async function restaurarPadraoRestricao(chave) {
    const linha = linhasRestricao[chave];
    if (!linha?.id) return;
    if (!window.confirm('Remover a configuração salva e voltar ao valor padrão do sistema?')) return;

    setSalvandoChave(chave);
    try {
      await deletarRegraTrustScore(linha.id, user.id);
      await carregar();
    } catch {
      alert('Erro ao restaurar o padrão.');
    } finally {
      setSalvandoChave(null);
    }
  }

  if (loading) {
    return (
      <div className="admin-container">
        <main className="admin-main"><p>Carregando regras de TrustScore...</p></main>
      </div>
    );
  }

  return (
    <div className="admin-container">
      <main className="admin-main">

        <div className="page-header">
          <div>
            <h1 className="page-title">Regras de TrustScore</h1>
            <p className="regras-subtitulo">
              Ajuste a severidade das penalidades e o impacto do TrustScore no uso do equipamento.
              Configurações não salvas usam o valor padrão do sistema.
            </p>
          </div>
          <button className="btn-secondary" onClick={() => navigate('/admin')}>
            <span className="material-icons">arrow_back</span>
            Voltar
          </button>
        </div>

        <h2 className="page-title" style={{ fontSize: '1.15rem', marginTop: '0.5rem' }}>Penalidades por evento</h2>
        <p className="regras-subtitulo">
          Eventos do ciclo de vida da reserva que penalizam o TrustScore do usuário.
        </p>
        <div className="regras-grid">
          {eventosDisponiveis.map(evento => {
            const linha = linhas[evento.chave] || {};
            const salvando = salvandoEvento === evento.chave;

            return (
              <div key={evento.chave} className="regra-card">
                <div className="regra-card-header">
                  <h3 className="regra-nome">{humanizarChave(evento.chave)}</h3>
                  {!linha.usandoPadrao && (
                    <span
                      className="material-icons action-icon"
                      title="Restaurar padrão"
                      onClick={() => !salvando && restaurarPadrao(evento)}
                    >
                      restart_alt
                    </span>
                  )}
                </div>

                <p className="regra-descricao">{evento.descricao}</p>

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
                      onChange={e => set(evento.chave, 'delta')(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="regras-label">Parâmetro adicional</label>
                    {evento.parametroPadrao !== null ? (
                      <input
                        type="number"
                        className="regras-input"
                        min={0}
                        value={linha.parametro ?? ''}
                        onChange={e => set(evento.chave, 'parametro')(e.target.value)}
                      />
                    ) : (
                      <input
                        type="text"
                        className="regras-input"
                        value="Este evento não usa parâmetro adicional"
                        disabled
                        style={{ color: 'var(--text-gray)', fontStyle: 'italic' }}
                      />
                    )}
                  </div>
                </div>

                <button
                  className="btn-primary"
                  style={{ marginTop: '0.5rem', width: '100%', justifyContent: 'center' }}
                  disabled={salvando}
                  onClick={() => salvar(evento)}
                >
                  {salvando ? '⏳ Salvando...' : 'Salvar'}
                </button>
              </div>
            );
          })}
        </div>

        <h2 className="page-title" style={{ fontSize: '1.15rem', marginTop: '2rem' }}>Exigência mínima para reservar</h2>
        <p className="regras-subtitulo">
          Os 3 níveis definem o TrustScore mínimo exigido pra reservar. A classificação de cada equipamento
          num nível é automática (veja o critério em cada card) — só o score mínimo é editável aqui.
        </p>
        <div className="regras-grid">
          {niveisDisponiveis.map(nivel => {
            const linha = linhasExigencia[nivel.chave] || {};
            const salvando = salvandoNivel === nivel.chave;

            return (
              <div key={nivel.chave} className="regra-card">
                <div className="regra-card-header">
                  <h3 className="regra-nome">{humanizarChave(nivel.chave)} exigência</h3>
                  {!linha.usandoPadrao && (
                    <span
                      className="material-icons action-icon"
                      title="Restaurar padrão"
                      onClick={() => !salvando && restaurarPadraoExigencia(nivel)}
                    >
                      restart_alt
                    </span>
                  )}
                </div>

                <p className="regra-descricao">{nivel.descricao}</p>

                {linha.usandoPadrao && (
                  <span className="regra-limiar-label" style={{ color: 'var(--text-gray)' }}>
                    USANDO PADRÃO DO SISTEMA
                  </span>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.25rem' }}>
                  <div>
                    <label className="regras-label">TrustScore mínimo pra reservar</label>
                    <input
                      type="number"
                      className="regras-input"
                      min={0}
                      max={100}
                      value={linha.scoreMinimo ?? ''}
                      onChange={e => setExigencia(nivel.chave, 'scoreMinimo')(e.target.value)}
                    />
                  </div>
                </div>

                <button
                  className="btn-primary"
                  style={{ marginTop: '0.5rem', width: '100%', justifyContent: 'center' }}
                  disabled={salvando}
                  onClick={() => salvarExigencia(nivel)}
                >
                  {salvando ? '⏳ Salvando...' : 'Salvar'}
                </button>
              </div>
            );
          })}
        </div>

        <h2 className="page-title" style={{ fontSize: '1.15rem', marginTop: '2rem' }}>Restrições adicionais</h2>
        <p className="regras-subtitulo">
          Cada restrição mede uma coisa diferente (veja a descrição do card). O "valor limite" é o
          número que dispara o bloqueio; o TrustScore mínimo é o que o usuário precisa ter pra
          ultrapassar esse limite.
        </p>
        <div className="regras-grid">
          {restricoesDisponiveis.map(restricao => {
            const linha = linhasRestricao[restricao.chave] || {};
            const salvando = salvandoChave === restricao.chave;

            return (
              <div key={restricao.chave} className="regra-card">
                <div className="regra-card-header">
                  <h3 className="regra-nome">{humanizarChave(restricao.chave)}</h3>
                  {!linha.usandoPadrao && (
                    <span
                      className="material-icons action-icon"
                      title="Restaurar padrão"
                      onClick={() => !salvando && restaurarPadraoRestricao(restricao.chave)}
                    >
                      restart_alt
                    </span>
                  )}
                </div>

                <p className="regra-descricao">{restricao.descricao}</p>

                {linha.usandoPadrao && (
                  <span className="regra-limiar-label" style={{ color: 'var(--text-gray)' }}>
                    USANDO PADRÃO DO SISTEMA
                  </span>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.25rem' }}>
                  <div>
                    <label className="regras-label">Valor limite</label>
                    {restricao.valorPrincipalUsado ? (
                      <input
                        type="number"
                        className="regras-input"
                        min={1}
                        value={linha.valorPrincipal ?? ''}
                        onChange={e => setRestricao(restricao.chave, 'valorPrincipal')(e.target.value)}
                      />
                    ) : (
                      <input
                        type="text"
                        className="regras-input"
                        value="Não usado por esta restrição"
                        disabled
                        style={{ color: 'var(--text-gray)', fontStyle: 'italic' }}
                      />
                    )}
                  </div>
                  <div>
                    <label className="regras-label">TrustScore mínimo pra exceder</label>
                    <input
                      type="number"
                      className="regras-input"
                      min={0}
                      max={100}
                      value={linha.scoreMinimoParaExceder ?? ''}
                      onChange={e => setRestricao(restricao.chave, 'scoreMinimoParaExceder')(e.target.value)}
                    />
                  </div>
                </div>

                <button
                  className="btn-primary"
                  style={{ marginTop: '0.5rem', width: '100%', justifyContent: 'center' }}
                  disabled={salvando}
                  onClick={() => salvarRestricao(restricao)}
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

export default RegrasTrustScore;
