import axios from 'axios';

const BASE_URL = "http://localhost:8088/api/file-server/v1/files"; 
console.log('FILE SERVER URL:', BASE_URL); // Adicione este log para verificar a URL

const apiFiles = axios.create({
  baseURL: BASE_URL,
});

export const uploadArquivo = async (file) => {
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await apiFiles.post('/upload', formData);
    return response.data.data.id; 
  } catch (error) {
    console.error("Erro no upload:", error);
    return null;
  }
};

export default apiFiles;