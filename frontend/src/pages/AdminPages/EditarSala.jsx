import { useState, useEffect, useContext  } from 'react';
import { AuthContext } from "../../contexts/AuthContext";
import { useNavigate, useParams } from 'react-router-dom';
import { getSalaById, atualizarSala } from '../../services/api';
import { uploadArquivo } from '../../services/apiFiles'; 
import './CadastroSala.css';
import './Admin.css';
import '../../App.css';

function EditarSala() {
  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false); 

  const [arquivoSelecionado, setArquivoSelecionado] = useState(null);

  const [formData, setFormData] = useState({
    nome: '',
    capacidade: '',
    local: '',
    status: 'ATIVA',
    tipoSala: 'REUNIAO',
    caracteristicasTexto: '',
    imagensIdsExistentes: []
  });

  useEffect(() => {
    async function carregarDadosDaSala() {
      try {
        const response = await getSalaById(id);
        const sala = response.data;

        setFormData({
          nome: sala.nome,
          capacidade: sala.capacidade,
          local: sala.local,
          status: sala.status,
          tipoSala: sala.tipo,
          caracteristicasTexto: sala.caracteristicas ? sala.caracteristicas.join(', ') : '',
          imagensIdsExistentes: sala.imagens
        });
      } catch (error) {
        console.error("Erro ao carregar dados da sala:", error);
        alert("Erro ao buscar dados da sala.");
        navigate('/admin');
      } finally {
        setLoading(false);
      }
    }
    carregarDadosDaSala();
  }, [id, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSalvando(true);

    try {
      let idsImagensFinais = [...formData.imagensIdsExistentes];
      if (arquivoSelecionado) {
        const novoId = await uploadArquivo(arquivoSelecionado);
        if (novoId) {
          idsImagensFinais = [novoId.toString()]; 
        }
      }

      const listaCaracteristicas = formData.caracteristicasTexto
        .split(',')
        .map(item => item.trim())
        .filter(item => item !== "");

      const dadosParaEnviar = {
        nome: formData.nome,
        capacidade: parseInt(formData.capacidade),
        local: formData.local,
        status: formData.status,
        tipoSala: formData.tipoSala,
        caracteristicas: listaCaracteristicas,
        imagens: idsImagensFinais
      };

      await atualizarSala(id, dadosParaEnviar, user.id);
      
      alert("Sala atualizada com sucesso!");
      navigate('/admin');
    } catch (error) {
      console.error("Erro ao atualizar sala:", error);
      alert("Falha ao atualizar a sala.");
    } finally {
      setSalvando(false);
    }
  };

  if (loading) return <div className="admin-container"><p>Carregando dados...</p></div>;

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Atualizar Sala</h1>
        </div>

        <form onSubmit={handleSubmit} className="cadastro-form">
          <div className="input-group">
            <label>Nome da Sala</label>
            <input 
              type="text" required 
              value={formData.nome}
              onChange={(e) => setFormData({...formData, nome: e.target.value})}
            />
          </div>

          <div className="form-row">
            <div className="input-group">
              <label>Capacidade</label>
              <input 
                type="number" required 
                value={formData.capacidade}
                onChange={(e) => setFormData({...formData, capacidade: e.target.value})}
              />
            </div>
            <div className="input-group">
              <label>Status</label>
              <select 
                value={formData.status}
                onChange={(e) => setFormData({...formData, status: e.target.value})}
              >
                <option value="ATIVA">Ativa</option>
                <option value="MANUTENCAO">Manutenção</option>
              </select>
            </div>
          </div>

          <div className="input-group">
            <label>Tipo de Sala</label>
            <select 
              value={formData.tipoSala}
              onChange={(e) => setFormData({...formData, tipoSala: e.target.value})}
            >
              <option value="REUNIAO">Sala de Reunião</option>
              <option value="CONFERENCIA">Sala de Conferência</option>
              <option value="LABORATORIO">Laboratório</option>
              <option value="ESTUDO_INDIVIDUAL">Sala de Estudo Individual</option>
            </select>
          </div>

          <div className="input-group">
            <label>Localização</label>
            <input 
              type="text" required 
              value={formData.local}
              onChange={(e) => setFormData({...formData, local: e.target.value})}
            />
          </div>

          <div className="input-group">
            <label>Características (Separe por vírgula)</label>
            <input 
              type="text" 
              value={formData.caracteristicasTexto}
              onChange={(e) => setFormData({...formData, caracteristicasTexto: e.target.value})}
            />
          </div>
          <div className="input-group">
            <label>Trocar Imagem da Sala</label>
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

export default EditarSala;