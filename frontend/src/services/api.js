import axios from "axios";
import apiFiles from "./apiFiles";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

export const loginUsuario = (dados) => api.post("/usuarios/acesso", dados);

export const getSalas = () => api.get("/salas");
export const getSalaById = (id) => api.get(`/salas/${id}`);
export const deletarSala = (id) => api.delete(`/salas/${id}`);
export const cadastrarSala = (salaData) => api.post("/salas", salaData);
export const atualizarSala = (id, salaData) =>
  api.put(`/salas/${id}`, salaData);

export const criarReserva = (reservaData) => api.post("/reservas", reservaData);
export const getReservasUsuario = (usuarioId) =>
  api.get(`/reservas/usuario/${usuarioId}`);
export const getReservaById = (id) => api.get(`/reservas/${id}`);

export const getHorariosOcupados = (salaId, data) =>
  api.get("/reservas/ocupados", {
    params: { salaId, data },
  });

  // export const cancelarReserva = (reservaId, usuarioId) =>
  // api.delete(`/reservas/${reservaId}/cancelar`, {
  //   headers: { "X-Usuario-Id": usuarioId },
  // });

export const fazerCheckIn = (reservaId, usuarioId, arquivos) => {
  const formData = new FormData();
  arquivos.forEach((arquivo) => formData.append("imagens", arquivo));

  return api.post(`/auditorias/checkin/${reservaId}`, formData, {
    headers: { "Content-Type": "multipart/form-data", "X-Usuario-Id": usuarioId },
  });
};

export const fazerCheckOut = (reservaId, usuarioId, arquivos) => {
  const formData = new FormData();
  arquivos.forEach((arquivo) => formData.append("imagens", arquivo));

  return api.post(`/auditorias/checkout/${reservaId}`, formData, {
    headers: { "Content-Type": "multipart/form-data", "X-Usuario-Id": usuarioId },
  });
};

export const getRegras = () => api.get("/regras");
export const criarRegra = (data) => api.post("/regras", data);
export const atualizarRegra = (id, data) => api.put(`/regras/${id}`, data);
export const deletarRegra = (id) => api.delete(`/regras/${id}`);

export const uploadArquivo = (arquivo) => {
  const formData = new FormData();
  formData.append("file", arquivo);
  return apiFiles.post("/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export default api;
