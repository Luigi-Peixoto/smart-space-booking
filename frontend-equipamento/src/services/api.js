import axios from "axios";
import apiFiles from "./apiFiles";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

export const loginUsuario = (dados) => api.post("/usuarios/acesso", dados);
export const getUsuarioById = (id) => api.get(`/usuarios/${id}`);

export const getEquipamentos = () => api.get("/equipamentos");
export const getEquipamentoById = (id) => api.get(`/equipamentos/${id}`);
export const deletarEquipamento = (id, usuarioId) =>
  api.delete(`/equipamentos/${id}`, { headers: { "X-Usuario-Id": usuarioId } });
export const cadastrarEquipamento = (equipamentoData, usuarioId) =>
  api.post("/equipamentos", equipamentoData, {
    headers: { "X-Usuario-Id": usuarioId },
  });
export const atualizarEquipamento = (id, equipamentoData, usuarioId) =>
  api.put(`/equipamentos/${id}`, equipamentoData, {
    headers: { "X-Usuario-Id": usuarioId },
  });

export const criarReserva = (reservaData) =>
  api.post("/reservas-equipamento", reservaData);
export const getReservasUsuario = (usuarioId) =>
  api.get(`/reservas-equipamento/usuario/${usuarioId}`);
export const getReservaById = (id) => api.get(`/reservas-equipamento/${id}`);

export const getHorariosOcupados = (equipamentoId, data) =>
  api.get("/reservas-equipamento/ocupados", {
    params: { equipamentoId, data },
  });

export const cancelarReserva = (reservaId, usuarioId, motivo) =>
  api.put(
    `/reservas-equipamento/${reservaId}/cancelar`,
    { motivo: motivo },
    { headers: { "X-Usuario-Id": usuarioId } },
  );

export const fazerCheckIn = (reservaId, usuarioId, arquivos) => {
  const formData = new FormData();
  arquivos.forEach((arquivo) => formData.append("imagens", arquivo));

  return api.post(`/auditorias/checkin/${reservaId}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      "X-Usuario-Id": usuarioId,
    },
  });
};

export const fazerCheckOut = (reservaId, usuarioId, arquivos) => {
  const formData = new FormData();
  arquivos.forEach((arquivo) => formData.append("imagens", arquivo));

  return api.post(`/auditorias/checkout/${reservaId}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      "X-Usuario-Id": usuarioId,
    },
  });
};

export const getRegras = (usuarioId) =>
  api.get("/regras", { headers: { "X-Usuario-Id": usuarioId } });
export const criarRegra = (data, usuarioId) =>
  api.post("/regras", data, { headers: { "X-Usuario-Id": usuarioId } });
export const atualizarRegra = (id, data, usuarioId) =>
  api.put(`/regras/${id}`, data, { headers: { "X-Usuario-Id": usuarioId } });
export const deletarRegra = (id, usuarioId) =>
  api.delete(`/regras/${id}`, { headers: { "X-Usuario-Id": usuarioId } });

// CRUD único pra todo ajuste de TrustScore configurável — penalidade por
// evento, exigência mínima por sensibilidade do recurso e restrições de uso
// (concorrência, tamanho do kit, ou o que cada hotspot declarar), todos
// filtrados por "categoria" (EVENTO/EXIGENCIA/RESTRICAO).
export const getRegrasTrustScore = (categoria, usuarioId) =>
  api.get("/regras-trust-score", { params: { categoria }, headers: { "X-Usuario-Id": usuarioId } });
export const criarRegraTrustScore = (data, usuarioId) =>
  api.post("/regras-trust-score", data, { headers: { "X-Usuario-Id": usuarioId } });
export const atualizarRegraTrustScore = (id, data, usuarioId) =>
  api.put(`/regras-trust-score/${id}`, data, { headers: { "X-Usuario-Id": usuarioId } });
export const deletarRegraTrustScore = (id, usuarioId) =>
  api.delete(`/regras-trust-score/${id}`, { headers: { "X-Usuario-Id": usuarioId } });
export const getRestricoesDisponiveis = (usuarioId, tipoRecurso) =>
  api.get("/regras-trust-score/restricoes-disponiveis", { params: { tipoRecurso }, headers: { "X-Usuario-Id": usuarioId } });
export const getEventosDisponiveis = (usuarioId, tipoRecurso) =>
  api.get("/regras-trust-score/eventos-disponiveis", { params: { tipoRecurso }, headers: { "X-Usuario-Id": usuarioId } });
export const getNiveisExigenciaDisponiveis = (usuarioId, tipoRecurso) =>
  api.get("/regras-trust-score/niveis-exigencia-disponiveis", { params: { tipoRecurso }, headers: { "X-Usuario-Id": usuarioId } });

export const reportarIncidente = (recursoId, descricao, usuarioId) =>
  api.post("/incidentes", { recursoId, descricao }, { headers: { "X-Usuario-Id": usuarioId } });

export const getIncidentesPendentes = (adminId) =>
  api.get("/incidentes/pendentes", { headers: { "X-Usuario-Id": adminId } });

export const aprovarIncidente = (incidenteId, adminId) =>
  api.patch(
    `/incidentes/${incidenteId}/aprovar`,
    {},
    { headers: { "X-Usuario-Id": adminId } },
  );

export const rejeitarIncidente = (incidenteId, adminId) =>
  api.patch(
    `/incidentes/${incidenteId}/rejeitar`,
    {},
    { headers: { "X-Usuario-Id": adminId } },
  );

export const uploadArquivo = (arquivo) => {
  const formData = new FormData();
  formData.append("file", arquivo);
  return apiFiles.post("/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const getTrustScoreHistorico = (usuarioId) =>
  api.get(`/usuarios/${usuarioId}/trust-score/historico`);

export const getAuditoriasPorReserva = (reservaId) =>
  api.get(`/auditorias/reserva/${reservaId}`);

export default api;
