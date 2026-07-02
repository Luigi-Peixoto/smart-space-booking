package imd.ufrn.com.br.smart_space_booking.instancia_sala.exception;

public class SalaNotFoundException extends RuntimeException {
    public SalaNotFoundException(Long id) {
        super("Sala não encontrada com id: " + id);
    }

    public SalaNotFoundException(String message) {
        super(message);
    }
}