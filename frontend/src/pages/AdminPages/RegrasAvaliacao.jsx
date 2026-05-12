import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRegras, criarRegra, atualizarRegra, deletarRegra } from '../../services/api';
import './Admin.css';
import './RegrasAvaliacao.css';

function RegrasAvaliacao() {
  const navigate = useNavigate();
  const [regras, setRegras] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalAberto, setModalAberto] = useState(false);
  const [regraEditando, setRegraEditando] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [form, setForm] = useState({
    nome: '', descricao: '',
    limiBonus: 8, deltaBonus: 10,
    limiPenalidade: 5, deltaPenalidade: -15,
  });

  useEffect(() => { carregarRegras(); }, []);

  async function carregarRegras() {
    try {
      const res = await getRegras();
      setRegras(res.data);
    } catch (e) {
      alert('Erro ao carregar regras.');
    } finally {
      setLoading(false);
    }
  }

  function abrirNovo() {
    setRegraEditando(null);
    setForm({ nome: '', descricao: '', limiBonus: 8, deltaBonus: 10, limiPenalidade: 5, deltaPenalidade: -15 });
    setModalAberto(true);
  }

  function abrirEditar(regra) {
    setRegraEditando(regra);
    setForm({
      nome: regra.nome,
      descricao: regra.descricao,
      limiBonus: regra.limiBonus,
      deltaBonus: regra.deltaBonus,
      limiPenalidade: regra.limiPenalidade,
      deltaPenalidade: regra.deltaPenalidade,
    });
    setModalAberto(true);
  }

  async function salvar() {
    if (!form.nome.trim() || !form.descricao.trim()) {
      alert('Nome e descrição são obrigatórios.');
      return;
    }
    if (form.limiPenalidade >= form.limiBonus) {
      alert('O limiar de bônus deve ser maior que o de penalidade.');
      return;
    }
    setSalvando(true);
    try {
      if (regraEditando) {
        await atualizarRegra(regraEditando.id, form);
      } else {
        await criarRegra(form);
      }
      await carregarRegras();
      setModalAberto(false);
    } catch (e) {
      alert('Erro ao salvar: ' + (e.response?.data || e.message));
    } finally {
      setSalvando(false);
    }
  }

  async function deletar(id) {
    if (!window.confirm('Remover esta regra permanentemente?')) return;
    try {
      await deletarRegra(id);
      setRegras(prev => prev.filter(r => r.id !== id));
    } catch {
      alert('Erro ao remover regra.');
    }
  }

  function set(k) { return (v) => setForm(f => ({ ...f, [k]: v })); }
  const sign = (d) => d > 0 ? `+${d}` : `${d}`;

  if (loading) return <div className="admin-container"><main className="admin-main"><p>Carregando regras...</p></main></div>;

  return (
    <div className="admin-container">
      <main className="admin-main">

        {/* Header */}
        <div className="page-header">
          <div>
            <h1 className="page-title">Regras de Avaliação</h1>
            <p className="regras-subtitulo">
              Configure os critérios que a IA usa para avaliar o check-out (nota 0–10 por critério).
            </p>
          </div>
          <div className="regras-header-actions">
            <button className="btn-secondary" onClick={() => navigate('/admin')}>
              <span className="material-icons">arrow_back</span>
              Voltar
            </button>
            <button className="btn-primary" onClick={abrirNovo}>
              <span className="material-icons">add</span>
              Nova Regra
            </button>
          </div>
        </div>

        {/* Lista vazia */}
        {regras.length === 0 && (
          <div className="regras-empty">
            <span className="material-icons regras-empty-icon">rule</span>
            <p>Nenhuma regra cadastrada ainda.</p>
            <p className="regras-empty-sub">Crie regras para que a IA possa avaliar os check-outs.</p>
            <button className="btn-primary" onClick={abrirNovo}>Criar primeira regra</button>
          </div>
        )}

        {/* Grid de cards */}
        {regras.length > 0 && (
          <div className="regras-grid">
            {regras.map(r => (
              <div key={r.id} className="regra-card">
                <div className="regra-card-header">
                  <h3 className="regra-nome">{r.nome}</h3>
                  <div className="room-actions">
                    <span className="material-icons action-icon" onClick={() => abrirEditar(r)}>edit</span>
                    <span className="material-icons action-icon delete" onClick={() => deletar(r.id)}>delete</span>
                  </div>
                </div>

                <p className="regra-descricao">{r.descricao}</p>

                {/* Limiares */}
                <div className="regra-limiares">
                  <div className="regra-limiar regra-limiar--bonus">
                    <span className="regra-limiar-label">▲ BÔNUS</span>
                    <div className="regra-limiar-valores">
                      <span className="regra-limiar-cond">nota ≥ <strong>{r.limiBonus}</strong></span>
                      <span className="regra-limiar-delta">{sign(r.deltaBonus)} pts</span>
                    </div>
                  </div>
                  <div className="regra-limiar regra-limiar--penalidade">
                    <span className="regra-limiar-label">▼ PENALIDADE</span>
                    <div className="regra-limiar-valores">
                      <span className="regra-limiar-cond">nota &lt; <strong>{r.limiPenalidade}</strong></span>
                      <span className="regra-limiar-delta">{sign(r.deltaPenalidade)} pts</span>
                    </div>
                  </div>
                </div>

                {/* Barra de zonas */}
                <div className="regra-barra-container">
                  <div className="regra-barra">
                    <div className="regra-zona regra-zona--pen" style={{ width: `${r.limiPenalidade * 10}%` }} />
                    <div className="regra-zona regra-zona--neutra" style={{ width: `${(r.limiBonus - r.limiPenalidade) * 10}%` }} />
                    <div className="regra-zona regra-zona--bonus" style={{ width: `${(10 - r.limiBonus) * 10}%` }} />
                  </div>
                  <div className="regra-barra-labels">
                    <span className="regra-barra-label--pen">penalidade &lt;{r.limiPenalidade}</span>
                    <span className="regra-barra-label--neutra">neutro</span>
                    <span className="regra-barra-label--bonus">bônus ≥{r.limiBonus}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modal */}
      {modalAberto && (
        <div
          style={{
            position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 1000, padding: '1rem',
          }}
          onClick={e => e.target === e.currentTarget && !salvando && setModalAberto(false)}
        >
          <div style={{
            background: '#fff', borderRadius: 15, padding: '2rem',
            width: '100%', maxWidth: 500, boxShadow: '0 8px 32px rgba(0,0,0,0.15)',
          }}>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div>
                <h2 style={{ color: 'var(--primary-color)', fontSize: '1.2rem', fontWeight: 700 }}>
                  {regraEditando ? 'Editar regra' : 'Nova regra'}
                </h2>
                <p style={{ color: 'var(--text-gray)', fontSize: '0.85rem', marginTop: 2 }}>
                  A IA dará nota 0–10 para este critério no check-out
                </p>
              </div>
              {!salvando && (
                <button onClick={() => setModalAberto(false)}
                  style={{ background: 'none', border: 'none', fontSize: '1.3rem', cursor: 'pointer', color: '#999' }}>
                  ✕
                </button>
              )}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>

              <div>
                <label className="regras-label">Nome do critério *</label>
                <input className="regras-input" value={form.nome}
                  onChange={e => set('nome')(e.target.value)}
                  placeholder="Ex: Limpeza geral" />
              </div>

              <div>
                <label className="regras-label">Instrução para a IA</label>
                <textarea className="regras-input regras-textarea"
                  value={form.descricao}
                  onChange={e => set('descricao')(e.target.value)}
                  placeholder="Ex: Verifique se superfícies, chão e mesas estão livres de sujeira."
                  rows={2} />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>

                {/* Bônus */}
                <div style={{ background: 'rgba(40,167,69,0.05)', border: '1px solid rgba(40,167,69,0.2)', borderRadius: 10, padding: '1rem' }}>
                  <p style={{ color: 'var(--success)', fontSize: '0.75rem', fontWeight: 800, letterSpacing: '0.08em', marginBottom: '0.75rem' }}>▲ BÔNUS</p>
                  <label className="regras-label">Nota mínima (0–10)</label>
                  <input type="number" className="regras-input" min={0} max={10}
                    value={form.limiBonus}
                    onChange={e => set('limiBonus')(Number(e.target.value))} />
                  <label className="regras-label" style={{ marginTop: '0.75rem' }}>Delta (+pts)</label>
                  <input type="number" className="regras-input" min={0} max={50}
                    value={form.deltaBonus}
                    onChange={e => set('deltaBonus')(Number(e.target.value))} />
                </div>

                {/* Penalidade */}
                <div style={{ background: 'rgba(220,53,69,0.05)', border: '1px solid rgba(220,53,69,0.2)', borderRadius: 10, padding: '1rem' }}>
                  <p style={{ color: 'var(--danger)', fontSize: '0.75rem', fontWeight: 800, letterSpacing: '0.08em', marginBottom: '0.75rem' }}>▼ PENALIDADE</p>
                  <label className="regras-label">Nota máxima (0–10)</label>
                  <input type="number" className="regras-input" min={0} max={10}
                    value={form.limiPenalidade}
                    onChange={e => set('limiPenalidade')(Number(e.target.value))} />
                  <label className="regras-label" style={{ marginTop: '0.75rem' }}>Delta (-pts)</label>
                  <input type="number" className="regras-input" min={-50} max={0}
                    value={form.deltaPenalidade}
                    onChange={e => set('deltaPenalidade')(Number(e.target.value))} />
                </div>

              </div>

              <p style={{ fontSize: '0.8rem', color: '#888', textAlign: 'center', background: '#f8f8f8', borderRadius: 8, padding: '0.6rem' }}>
                <span style={{ color: 'var(--danger)' }}>nota &lt; {form.limiPenalidade} → {sign(form.deltaPenalidade)} pts</span>
                {'  ·  '}
                <span>neutro</span>
                {'  ·  '}
                <span style={{ color: 'var(--success)' }}>nota ≥ {form.limiBonus} → +{form.deltaBonus} pts</span>
              </p>

            </div>

            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button onClick={() => setModalAberto(false)} disabled={salvando}
                style={{ background: 'transparent', border: '1px solid #ddd', borderRadius: 8, padding: '0.7rem 1.2rem', cursor: 'pointer', fontWeight: 600, color: 'var(--text-gray)' }}>
                Cancelar
              </button>
              <button onClick={salvar} disabled={salvando} className="btn-primary">
                {salvando ? '⏳ Salvando...' : regraEditando ? 'Salvar' : 'Criar regra'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default RegrasAvaliacao;