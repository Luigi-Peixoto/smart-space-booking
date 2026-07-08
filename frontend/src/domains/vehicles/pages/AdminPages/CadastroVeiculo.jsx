import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../../../App.css";
import { AuthContext } from "../../../../contexts/AuthContext";
import "../../../../pages/AdminPages/Admin.css";
import { cadastrarVeiculo } from "../../../../services/api";
import { uploadArquivo } from "../../../../services/apiFiles";
import "./CadastroVeiculo.css";

function CadastroVeiculo() {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [formData, setFormData] = useState({
    nome: "",
    placa: "",
    chassi: "",
    renavam: "",
    marca: "",
    modelo: "",
    cor: "",
    status: "ATIVA",
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
      alert("Por favor, selecione pelo menos uma imagem para o veículo.");
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
        placa: formData.placa.toUpperCase(),
        imagens: imageIDs,
      };

      await cadastrarVeiculo(dadosParaEnviar, user.id);

      alert("Veículo cadastrado com sucesso!");
      navigate("/veiculos/admin");
    } catch (error) {
      console.error("Erro no cadastro:", error);
      alert("Erro ao processar imagens ou cadastrar veículo.");
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Cadastro de Veículo</h1>
        </div>

        <form onSubmit={handleSubmit} className="cadastro-form">
          <div className="input-group">
            <label>Nome/Identificação (ex: Corolla Frota 01)</label>
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
              <label>Marca</label>
              <input
                type="text"
                required
                placeholder="Ex: Toyota"
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
                placeholder="Ex: Corolla"
                value={formData.modelo}
                onChange={(e) =>
                  setFormData({ ...formData, modelo: e.target.value })
                }
              />
            </div>
          </div>

          <div className="form-row">
            <div className="input-group">
              <label>Cor</label>
              <input
                type="text"
                required
                placeholder="Ex: Prata"
                value={formData.cor}
                onChange={(e) =>
                  setFormData({ ...formData, cor: e.target.value })
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
            <label>Placa</label>
            <input
              type="text"
              required
              placeholder="Ex: ABC1D23"
              value={formData.placa}
              onChange={(e) =>
                setFormData({ ...formData, placa: e.target.value })
              }
            />
          </div>

          <div className="form-row">
            <div className="input-group">
              <label>Chassi</label>
              <input
                type="text"
                required
                placeholder="17 caracteres"
                maxLength={17}
                value={formData.chassi}
                onChange={(e) =>
                  setFormData({ ...formData, chassi: e.target.value })
                }
              />
            </div>
            <div className="input-group">
              <label>RENAVAM</label>
              <input
                type="text"
                required
                placeholder="11 dígitos"
                maxLength={11}
                value={formData.renavam}
                onChange={(e) =>
                  setFormData({ ...formData, renavam: e.target.value })
                }
              />
            </div>
          </div>

          <div className="input-group">
            <label>Fotos do Veículo</label>
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
              onClick={() => navigate("/veiculos/admin")}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary btn-save"
              disabled={carregando}
            >
              {carregando ? "Fazendo Upload..." : "Salvar Veículo"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default CadastroVeiculo;
