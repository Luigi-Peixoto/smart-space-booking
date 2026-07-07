package imd.ufrn.com.br.smart_space_booking.config;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaTipo;
import imd.ufrn.com.br.smart_space_booking.framework.enums.StatusRecurso;
import imd.ufrn.com.br.smart_space_booking.framework.enums.UsuarioStatus;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.TrustScoreHistorico;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.TrustScoreHistoricoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.enums.TipoSala;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.model.Sala;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.repository.SalaRepository;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model.Veiculo;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.repository.VeiculoRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final VeiculoRepository veiculoRepository;
    private final ReservaRepository reservaRepository;
    private final RegraAvaliacaoRepository regraRepository;
    private final RegraTrustScoreRepository regraTrustScoreRepository;
    private final TrustScoreHistoricoRepository trustScoreHistoricoRepository;

    public DataSeeder(UsuarioRepository usuarioRepository, SalaRepository salaRepository,
                      VeiculoRepository veiculoRepository,
                      ReservaRepository reservaRepository, RegraAvaliacaoRepository regraRepository,
                      RegraTrustScoreRepository regraTrustScoreRepository,
                      TrustScoreHistoricoRepository trustScoreHistoricoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.veiculoRepository = veiculoRepository;
        this.reservaRepository = reservaRepository;
        this.regraRepository = regraRepository;
        this.regraTrustScoreRepository = regraTrustScoreRepository;
        this.trustScoreHistoricoRepository = trustScoreHistoricoRepository;
    }

    @Override
    public void run(String... args) {
        if (salaRepository.count() > 0) {
            System.out.println("Banco de dados já populado. Ignorando o Seeder.");
            return;
        }

        System.out.println("Iniciando o Database Seeder...");

        RegraTrustScore cancelamentoTardio = new RegraTrustScore();
        cancelamentoTardio.setCategoria(CategoriaRegraTrustScore.EVENTO);
        cancelamentoTardio.setChave("CANCELAMENTO_TARDIO");
        cancelamentoTardio.setValorPrincipal(-15);
        cancelamentoTardio.setValorSecundario(2);
        cancelamentoTardio.setDescricao("Penalidade por cancelar com menos de 2 horas de antecedência.");

        RegraTrustScore noShow = new RegraTrustScore();
        noShow.setCategoria(CategoriaRegraTrustScore.EVENTO);
        noShow.setChave("NO_SHOW");
        noShow.setValorPrincipal(-15);
        noShow.setDescricao("Penalidade por não comparecer à reserva confirmada.");

        RegraTrustScore excessoCancelamentos = new RegraTrustScore();
        excessoCancelamentos.setCategoria(CategoriaRegraTrustScore.EVENTO);
        excessoCancelamentos.setChave("EXCESSO_CANCELAMENTOS");
        excessoCancelamentos.setValorPrincipal(-20);
        excessoCancelamentos.setValorSecundario(3);
        excessoCancelamentos.setDescricao("Penalidade para quem cancela mais de 3 reservas na mesma semana.");

        regraTrustScoreRepository.saveAll(List.of(cancelamentoTardio, noShow, excessoCancelamentos));

        Usuario admin = new Usuario();
        admin.setNome("Admin");
        admin.setEmail("admin@admin.com");
        admin.setPerfil("ADMIN");
        admin.setTrustScore(100);
        admin.setStatus(UsuarioStatus.ATIVO);

        Usuario user1 = new Usuario();
        user1.setNome("João Silva");
        user1.setEmail("joao@ufrn.edu.br");
        user1.setPerfil("USER");
        user1.setTrustScore(100);
        user1.setStatus(UsuarioStatus.ATIVO);

        usuarioRepository.saveAll(List.of(admin, user1));

        Sala salaReuniao = new Sala("Sala de Reunião Alpha", StatusRecurso.ATIVA, "Bloco A, 1º Andar",
                TipoSala.REUNIAO, 10,
                List.of("Projetor", "Ar Condicionado", "Quadro Branco"),
                List.of("url_imagem_alpha.jpg"));

        Sala lab = new Sala("Laboratório de Informática", StatusRecurso.ATIVA, "Bloco B, Térreo",
                TipoSala.LABORATORIO, 30,
                List.of("30 Computadores", "Ar Condicionado", "Lousa Digital"),
                List.of("url_imagem_lab.jpg"));

        salaRepository.saveAll(List.of(salaReuniao, lab));

        Veiculo carroFrota = new Veiculo("Corolla Frota 01", StatusRecurso.ATIVA,
                "ABC1D23", "9BWZZZ377VT004251", "12345678901",
                "Corolla", "Toyota", "Prata",
                List.of("url_imagem_corolla.jpg"));

        Veiculo vanFrota = new Veiculo("Van Frota 02", StatusRecurso.ATIVA,
                "XYZ9E87", "9BWZZZ377VT009876", "98765432109",
                "Sprinter", "Mercedes-Benz", "Branco",
                List.of("url_imagem_sprinter.jpg"));

        veiculoRepository.saveAll(List.of(carroFrota, vanFrota));

        ZonedDateTime amanha = ZonedDateTime.now().plusDays(1);

        Reserva reservaNormal = new Reserva();
        reservaNormal.setRecurso(salaReuniao);
        reservaNormal.setUsuario(user1);
        reservaNormal.setInicioDateTime(amanha.withHour(14).withMinute(0));
        reservaNormal.setFimDateTime(amanha.withHour(16).withMinute(0));
        reservaNormal.setTipo(ReservaTipo.PADRAO);
        reservaNormal.setStatus(ReservaStatus.CONFIRMADA);

        reservaRepository.save(reservaNormal);

        TrustScoreHistorico historico = new TrustScoreHistorico();
        historico.setUsuario(user1);
        historico.setReserva(null);
        historico.setRegra(null);
        historico.setDelta(0);
        historico.setScoreAnterior(100);
        historico.setScorePosterior(100);
        historico.setDescricao("Score inicial do usuário.");
        trustScoreHistoricoRepository.save(historico);

        System.out.println("Database Seeder concluído com sucesso! Temos 2 usuários, 2 salas, 2 veículos e 1 reserva inicial.");
    }
}