package imd.ufrn.com.br.smart_space_booking.framework.exception;

/** Usuário não tem TrustScore suficiente para reservar o recurso solicitado. */
public class TrustScoreInsuficienteException extends RegraNegocioException {
    public TrustScoreInsuficienteException(String message) {
        super(message);
    }
}
