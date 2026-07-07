import { useState, useEffect, useContext  } from 'react';
import { AuthContext } from "../../contexts/AuthContext";
import { useNavigate, useParams } from 'react-router-dom';
import { getEquipamentoById, atualizarEquipamento } from '../../services/api';
import { uploadArquivo } from '../../services/apiFiles';
import './CadastroEquipamento.css';
import './Admin.css';
import '../../App.css';

function EditarEquipamento() {
  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false);

  const [arquivoSelecionado, setArquivoSelecionado] = useState(null);
  const [subItens, setSubItens] = useState([]);

  const [formData, setFormData] = useState({
    nome: '',
    numeroSerie: '',
    marca: '',
    modelo: '',
    status: 'ATIVA',
    tipo: 'PROJETOR',
    imagensIdsExistentes: []
  });

  useEffect(() => {
    async function carregarDadosDoEquipamento() {
      try {
        const response = await getEquipamentoById(id);
        const equipamento = response.data;

        setFormData({
          nome: equipamento.nome,
          numeroSerie: equipamento.numeroSerie,
          marca: equipamento.marca,
          modelo: equipamento.modelo,
          status: equipamento.status,
          tipo: equipamento.tipo,
          imagensIdsExistentes: equipamento.imagens
        });
        setSubItens(
          (equipamento.subItens || []).map((item) => ({
            nome: item.nome,
            descricao: item.descricao || '',
          }))
        );
      } catch (error) {
        console.error("Erro ao carregar dados do equipamento:", error);
        alert("Erro ao buscar dados do equipamento.");
        navigate('/admin');
      } finally {
        setLoading(false);
      }
    }
    carregarDadosDoEquipamento();
  }, [id, navigate]);

  const adicionarSubItem = () => {
    setSubItens((prev) => [...prev, { nome: '', descricao: '' }]);
  };

  const removerSubItem = (indexParaRemover) => {
    setSubItens((prev) => prev.filter((_, index) => index !== indexParaRemover));
  };

  const atualizarSubItem = (index, campo, valor) => {
    setSubItens((prev) =>
      prev.map((item, i) => (i === index ? { ...item, [campo]: valor } : item))
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (subItens.some((item) => !item.nome.trim())) {
      alert("Todo item do kit precisa de um nome.");
      return;
    }

    setSalvando(true);

    try {
      let idsImagensFinais = [...formData.imagensIdsExistentes];
      if (arquivoSelecionado) {
        const novoId = await uploadArquivo(arquivoSelecionado);
        if (novoId) {
          idsImagensFinais = [novoId.toString()];
        }
      }

      const dadosParaEnviar = {
        nome: formData.nome,
        numeroSerie: formData.numeroSerie,
        marca: formData.marca,
        modelo: formData.modelo,
        status: formData.status,
        tipo: formData.tipo,
        subItens: subItens.map((item) => ({
          nome: item.nome.trim(),
          descricao: item.descricao.trim(),
          imagens: [],
        })),
        imagens: idsImagensFinais
      };

      await atualizarEquipamento(id, dadosParaEnviar, user.id);

      alert("Equipamento atualizado com sucesso!");
      navigate('/admin');
    } catch (error) {
      console.error("Erro ao atualizar equipamento:", error);
      alert("Falha ao atualizar o equipamento.");
    } finally {
      setSalvando(false);
    }
  };

  if (loading) return <div className="admin-container"><p>Carregando dados...</p></div>;

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Atualizar Equipamento</h1>
        </div>

        <form onSubmit={handleSubmit} className="cadastro-form">
          <div className="input-group">
            <label>Nome do Equipamento</label>
            <input
              type="text" required
              value={formData.nome}
              onChange={(e) => setFormData({...formData, nome: e.target.value})}
            />
          </div>

          <div className="form-row">
            <div className="input-group">
              <label>Número de Série</label>
              <input
                type="text" required
                value={formData.numeroSerie}
                onChange={(e) => setFormData({...formData, numeroSerie: e.target.value})}
              />
            </div>
            <div className="input-group">
              <label>Status</label>
              <select
                value={formData.status}
                onChange={(e) => setFormData({...formData, status: e.target.value})}
              >
                <option value="ATIVA">Ativo</option>
                <option value="MANUTENCAO">Manutenção</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="input-group">
              <label>Marca</label>
              <input
                type="text" required
                value={formData.marca}
                onChange={(e) => setFormData({...formData, marca: e.target.value})}
              />
            </div>
            <div className="input-group">
              <label>Modelo</label>
              <input
                type="text" required
                value={formData.modelo}
                onChange={(e) => setFormData({...formData, modelo: e.target.value})}
              />
            </div>
          </div>

          <div className="input-group">
            <label>Tipo de Equipamento</label>
            <select
              value={formData.tipo}
              onChange={(e) => setFormData({...formData, tipo: e.target.value})}
            >
              <option value="PROJETOR">Projetor</option>
              <option value="NOTEBOOK">Notebook</option>
              <option value="MICROFONE">Microfone</option>
              <option value="CAMERA">Câmera</option>
              <option value="OUTRO">Outro</option>
            </select>
          </div>

          <div className="input-group">
            <label>Itens do Kit</label>
            {subItens.map((item, index) => (
              <div className="form-row" key={index} style={{ alignItems: 'center' }}>
                <input
                  type="text"
                  placeholder="Nome do item"
                  value={item.nome}
                  onChange={(e) => atualizarSubItem(index, 'nome', e.target.value)}
                />
                <input
                  type="text"
                  placeholder="Descrição (opcional)"
                  value={item.descricao}
                  onChange={(e) => atualizarSubItem(index, 'descricao', e.target.value)}
                />
                <button
                  type="button"
                  onClick={() => removerSubItem(index)}
                  className="btn-remove-file"
                  title="Remover item do kit"
                >
                  <span className="material-icons">close</span>
                </button>
              </div>
            ))}
            <button
              type="button"
              className="btn-cancel"
              style={{ marginTop: '8px', width: 'fit-content' }}
              onClick={adicionarSubItem}
            >
              <span className="material-icons" style={{ verticalAlign: 'middle', fontSize: '18px' }}>add</span>
              {' '}Adicionar item ao kit
            </button>
          </div>

          <div className="input-group">
            <label>Trocar Imagem do Equipamento</label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setArquivoSelecionado(e.target.files[0])}
            />
            {formData.imagensIdsExistentes.length > 0 && !arquivoSelecionado && (
              <p className="helper-text">Já existe uma imagem salva. Selecione um arquivo se desejar trocar.</p>
            )}
          </div>

          <div className="cadastro-actions">
            <button type="button" className="btn-cancel" onClick={() => navigate('/admin')}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary btn-save" disabled={salvando}>
              {salvando ? "Salvando..." : "Salvar Alterações"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default EditarEquipamento;
