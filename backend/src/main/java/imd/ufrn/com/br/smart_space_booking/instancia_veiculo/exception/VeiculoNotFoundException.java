package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.exception;

public class VeiculoNotFoundException extends RuntimeException {
    public VeiculoNotFoundException(Long id) {
        super("Veículo com ID " + id + " não encontrado!");
    }
}