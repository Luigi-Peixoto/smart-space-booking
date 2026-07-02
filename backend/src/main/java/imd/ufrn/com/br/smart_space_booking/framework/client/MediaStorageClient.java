package imd.ufrn.com.br.smart_space_booking.framework.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MediaStorageClient {

    private final RestTemplate restTemplate;

    @Value("${media.server.url}")
    private String mediaServerUrl;

    public MediaStorageClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Busca os bytes de uma imagem no MS de mídia pelo ID.
     */
    public byte[] buscarImagem(String imageId) {
        String url = mediaServerUrl + "/" + Long.parseLong(imageId);
        return restTemplate.getForObject(url, byte[].class);
    }

    /**
     * Deleta uma imagem no MS de mídia pelo ID.
     * Chamado quando a auditoria é rejeitada por imagem inválida,
     * para não deixar arquivos órfãos no MS.
     */
    public void deletarImagem(String imageId) {
        String url = mediaServerUrl + "/" + Long.parseLong(imageId);
        restTemplate.delete(url);
    }
}