import { useState, useContext  } from "react";
import { AuthContext } from "../../contexts/AuthContext";
import { useNavigate } from "react-router-dom";
import "../../App.css";
import { cadastrarEquipamento } from "../../services/api";
import { uploadArquivo } from "../../services/apiFiles";
import "./Admin.css";
import "./CadastroEquipamento.css";

function CadastroEquipamento() {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [formData, setFormData] = useState({
    nome: "",
    numeroSerie: "",
    marca: "",
    modelo: "",
    status: "ATIVA",
    tipo: "PROJETOR",
  });

  const [subItens, setSubItens] = useState([]);
  const [arquivosSelecionados, setArquivosSelecionados] = useState([]);
  const [carregando, setCarregando] = useState(false);

  const handleFileChange = (e) => {
    const novosFiles = Array.from(e.target.files);
    setArquivosSelecionados((prev) => [...prev, ...novosFiles]);
    e.target.value = null;
  };

  const removerArquivo = (indexParaRemover) => {
    setArquivosSelecionados((prev) =>
      prev.filter((_, index) => index !== indexParaRemover),
    );
  };

  const adicionarSubItem = () => {
    setSubItens((prev) => [...prev, { nome: "", descricao: "" }]);
  };

  const removerSubItem = (indexParaRemover) => {
    setSubItens((prev) =>
      prev.filter((_, index) => index !== indexParaRemover),
    );
  };

  const atualizarSubItem = (index, campo, valor) => {
    setSubItens((prev) =>
      prev.map((item, i) => (i === index ? { ...item, [campo]: valor } : item)),
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (arquivosSelecionados.length === 0) {
      alert("Por favor, selecione pelo menos uma imagem para o equipamento.");
      return;
    }

    if (subItens.some((item) => !item.nome.trim())) {
      alert("Todo item do kit precisa de um nome.");
      return;
    }

    setCarregando(true);

    try {
      const uploadPromises = arquivosSelecionados.map((file) =>
        uploadArquivo(file),
      );
      const imageIDs = await Promise.all(uploadPromises);

      const dadosParaEnviar = {
        ...formData,
        subItens: subItens.map((item) => ({
          nome: item.nome.trim(),
          descricao: item.descricao.trim(),
          imagens: [],
        })),
        imagens: imageIDs.map((id) => String(id)),
      };

      await cadastrarEquipamento(dadosParaEnviar, user.id);

      alert("Equipamento cadastrado com sucesso!");
      navigate("/admin");
    } catch (error) {
      console.error("Erro no cadastro:", error);
      alert("Erro ao processar imagens ou cadastrar equipamento.");
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Cadastro de Equipamento</h1>
        </div>

        <form onSubmit={handleSubmit} className="cadastro-form">
          <div className="input-group">
            <label>Nome do Equipamento (ex: Projetor Epson - Auditório)</label>
            <input
              type="text"
              required
              placeholder="Digite o nome identificador"
              value={formData.nome}
              onChange={(e) =>
                setFormData({ ...formData, nome: e.target.value })
              }
            />
          </div>

          <div className="form-row">
            <div className="input-group">
              <label>Número de Série</label>
              <input
                type="text"
                required
                placeholder="Ex: SN-2024-00123"
                value={formData.numeroSerie}
                onChange={(e) =>
                  setFormData({ ...formData, numeroSerie: e.target.value })
                }
              />
            </div>
            <div className="input-group">
              <label>Status Inicial</label>
              <select
                value={formData.status}
                onChange={(e) =>
                  setFormData({ ...formData, status: e.target.value })
                }
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
                type="text"
                required
                placeholder="Ex: Epson"
                value={formData.marca}
                onChange={(e) =>
                  setFormData({ ...formData, marca: e.target.value })
                }
              />
            </div>
            <div className="input-group">
              <label>Modelo</label>
              <input
                type="text"
                required
                placeholder="Ex: PowerLite X49"
                value={formData.modelo}
                onChange={(e) =>
                  setFormData({ ...formData, modelo: e.target.value })
                }
              />
            </div>
          </div>

          <div className="input-group">
            <label>Tipo de Equipamento</label>
            <select
              value={formData.tipo}
              onChange={(e) =>
                setFormData({ ...formData, tipo: e.target.value })
              }
            >
              <option value="PROJETOR">Projetor</option>
              <option value="NOTEBOOK">Notebook</option>
              <option value="MICROFONE">Microfone</option>
              <option value="CAMERA">Câmera</option>
              <option value="OUTRO">Outro</option>
            </select>
          </div>

          <div className="input-group">
            <label>Itens do Kit (opcional)</label>
            <small style={{ color: "#666", fontSize: "12px" }}>
              Subitens que acompanham o equipamento (ex: controle remoto, cabo
              HDMI, case).
            </small>
            {subItens.map((item, index) => (
              <div className="form-row" key={index} style={{ alignItems: "center" }}>
                <input
                  type="text"
                  placeholder="Nome do item (ex: Controle remoto)"
                  value={item.nome}
                  onChange={(e) =>
                    atualizarSubItem(index, "nome", e.target.value)
                  }
                />
                <input
                  type="text"
                  placeholder="Descrição (opcional)"
                  value={item.descricao}
                  onChange={(e) =>
                    atualizarSubItem(index, "descricao", e.target.value)
                  }
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
              style={{ marginTop: "8px", width: "fit-content" }}
              onClick={adicionarSubItem}
            >
              <span className="material-icons" style={{ verticalAlign: "middle", fontSize: "18px" }}>add</span>
              {" "}Adicionar item ao kit
            </button>
          </div>

          <div className="input-group">
            <label>Fotos do Equipamento</label>
            <input
              type="file"
              multiple
              accept="image/*"
              onChange={handleFileChange}
              className="file-input"
            />
          </div>

          <div className="file-list-preview">
            {arquivosSelecionados.map((arquivo, index) => (
              <div key={index} className="file-item">
                <span className="material-icons">image</span>
                <span className="file-name">{arquivo.name}</span>
                <button
                  type="button"
                  onClick={() => removerArquivo(index)}
                  className="btn-remove-file"
                >
                  <span className="material-icons">close</span>
                </button>
              </div>
            ))}
          </div>

          {arquivosSelecionados.length > 0 && (
            <small>
              {arquivosSelecionados.length} imagem(ns) preparadas para upload.
            </small>
          )}

          <div className="cadastro-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={() => navigate("/admin")}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary btn-save"
              disabled={carregando}
            >
              {carregando ? "Fazendo Upload..." : "Salvar Equipamento"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default CadastroEquipamento;
