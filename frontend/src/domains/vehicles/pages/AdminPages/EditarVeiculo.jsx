import { useContext, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../../../../App.css";
import { AuthContext } from "../../../../contexts/AuthContext";
import "../../../../pages/AdminPages/Admin.css";
import "../../../../pages/AdminPages/CadastroSala.css";
import { atualizarVeiculo, getVeiculoById } from "../../../../services/api";
import { uploadArquivo } from "../../../../services/apiFiles";
import "./CadastroVeiculo.css";

function EditarVeiculo() {
  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false);

  const [arquivoSelecionado, setArquivoSelecionado] = useState(null);

  const [formData, setFormData] = useState({
    nome: "",
    placa: "",
    chassi: "",
    renavam: "",
    marca: "",
    modelo: "",
    cor: "",
    status: "ATIVA",
    imagensIdsExistentes: [],
  });

  useEffect(() => {
    async function carregarDadosDoVeiculo() {
      try {
        const response = await getVeiculoById(id);
        const veiculo = response.data;

        setFormData({
          nome: veiculo.nome,
          placa: veiculo.placa,
          chassi: veiculo.chassi,
          renavam: veiculo.renavam,
          marca: veiculo.marca,
          modelo: veiculo.modelo,
          cor: veiculo.cor,
          status: veiculo.status,
          imagensIdsExistentes: veiculo.imagens,
        });
      } catch (error) {
        console.error("Erro ao carregar dados do veículo:", error);
        alert("Erro ao buscar dados do veículo.");
        navigate("/veiculos/admin");
      } finally {
        setLoading(false);
      }
    }
    carregarDadosDoVeiculo();
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

      const dadosParaEnviar = {
        nome: formData.nome,
        placa: formData.placa.toUpperCase(),
        chassi: formData.chassi,
        renavam: formData.renavam,
        marca: formData.marca,
        modelo: formData.modelo,
        cor: formData.cor,
        status: formData.status,
        imagens: idsImagensFinais,
      };

      await atualizarVeiculo(id, dadosParaEnviar, user.id);

      alert("Veículo atualizado com sucesso!");
      navigate("/veiculos/admin");
    } catch (error) {
      console.error("Erro ao atualizar veículo:", error);
      alert("Falha ao atualizar o veículo.");
    } finally {
      setSalvando(false);
    }
  };

  if (loading)
    return (
      <div className="admin-container">
        <p>Carregando dados...</p>
      </div>
    );

  return (
    <div className="admin-container">
      <main className="admin-main">
        <div className="page-header">
          <h1 className="page-title">Atualizar Veículo</h1>
        </div>

        <form onSubmit={handleSubmit} className="cadastro-form">
          <div className="input-group">
            <label>Nome/Identificação</label>
            <input
              type="text"
              required
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
                value={formData.cor}
                onChange={(e) =>
                  setFormData({ ...formData, cor: e.target.value })
                }
              />
            </div>
            <div className="input-group">
              <label>Status</label>
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
                maxLength={11}
                value={formData.renavam}
                onChange={(e) =>
                  setFormData({ ...formData, renavam: e.target.value })
                }
              />
            </div>
          </div>

          <div className="input-group">
            <label>Trocar Imagem do Veículo</label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setArquivoSelecionado(e.target.files[0])}
            />
            {formData.imagensIdsExistentes.length > 0 &&
              !arquivoSelecionado && (
                <p className="helper-text">
                  Já existe uma imagem salva. Selecione um arquivo se desejar
                  trocar.
                </p>
              )}
          </div>

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
              disabled={salvando}
            >
              {salvando ? "Salvando..." : "Salvar Alterações"}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default EditarVeiculo;
