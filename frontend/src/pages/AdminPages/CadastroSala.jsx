import { useState } from "react";
import { AuthContext } from "../../contexts/AuthContext";
import { useNavigate } from "react-router-dom";
import "../../App.css";
import { cadastrarSala } from "../../services/api";
import { uploadArquivo } from "../../services/apiFiles";
import "./Admin.css";
import "./CadastroSala.css";

function CadastroSala() {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [formData, setFormData] = useState({
    nome: "",
    capacidade: "",
    local: "",
    status: "ATIVA",
    tipoSala: "REUNIAO",
    caracteristicasTexto: "",
    imagem: "",
  });

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

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (arquivosSelecionados.length === 0) {
      alert("Por favor, selecione pelo menos uma imagem para a sala.");
      return;
    }

    setCarregando(true);

    try {
      const uploadPromises = arquivosSelecionados.map((file) =>
        uploadArquivo(file),
      );
      const imageIDs = await Promise.all(uploadPromises);

      const listaCaracteristicas = formData.caracteristicasTexto
        .split(",")
        .map((item) => item.trim())
        .filter((item) => item !== "");

      const dadosParaEnviar = {
        ...formData,
        capacidade: parseInt(formData.capacidade),
        caracteristicas: listaCaracteristicas,
        imagens: imageIDs,
      };

      await cadastrarSala(dadosParaEnviar, user.id);

      alert("Sala cadastrada com sucesso!");
      navigate("/admin");
    } catch (error) {
      console.error("Erro no cadastro:", error);
      alert("Erro ao processar imagens ou cadastrar sala.");
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Cadastro de Sala</h1>
        </div>

        <form onSubmit={handleSubmit} className="cadastro-form">
          <div className="input-group">
            <label>Nome da Sala (ex: B402 - IMD)</label>
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
              <label>Capacidade (Pessoas)</label>
              <input
                type="number"
                required
                placeholder="Ex: 10"
                value={formData.capacidade}
                onChange={(e) =>
                  setFormData({ ...formData, capacidade: e.target.value })
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
                <option value="ATIVA">Ativa</option>
                <option value="MANUTENCAO">Manutenção</option>
              </select>
            </div>
          </div>

          <div className="input-group">
            <label>Tipo de Sala</label>
            <select
              value={formData.tipoSala}
              onChange={(e) =>
                setFormData({ ...formData, tipoSala: e.target.value })
              }
            >
              <option value="REUNIAO">Sala de Reunião</option>
              <option value="CONFERENCIA">Sala de Conferência</option>
              <option value="LABORATORIO">Laboratório</option>
              <option value="ESTUDO_INDIVIDUAL">
                Sala de Estudo Individual
              </option>
            </select>
          </div>

          <div className="input-group">
            <label>Localização (Prédio/Andar)</label>
            <input
              type="text"
              required
              placeholder="Ex: Terceiro andar, B402"
              value={formData.local}
              onChange={(e) =>
                setFormData({ ...formData, local: e.target.value })
              }
            />
          </div>

          <div className="input-group">
            <label>Características (Separe por vírgula)</label>
            <input
              type="text"
              placeholder="Ex: TV, Ar-condicionado, Wi-Fi"
              value={formData.caracteristicasTexto}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  caracteristicasTexto: e.target.value,
                })
              }
            />
            <small style={{ color: "#666", fontSize: "12px" }}>
              Os itens serão listados individualmente na visualização.
            </small>
          </div>

          <div className="input-group">
            <label>Fotos da Sala</label>
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
              {carregando ? "Fazendo Upload..." : "Salvar Sala"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default CadastroSala;
