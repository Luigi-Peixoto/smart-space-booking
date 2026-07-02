package imd.ufrn.com.br.smart_space_booking.config;

import java.time.ZonedDateTime;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.instancia_sala.repository.SalaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.enums.UsuarioStatus;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreEventoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.TrustScoreHistoricoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;
    private final RegraAvaliacaoRepository regraRepository;
    private final RegraTrustScoreEventoRepository regraTrustScoreEventoRepository;
    private final TrustScoreHistoricoRepository trustScoreHistoricoRepository;

    public DataSeeder(UsuarioRepository usuarioRepository, SalaRepository salaRepository,
                      ReservaRepository reservaRepository, RegraAvaliacaoRepository regraRepository,
                      RegraTrustScoreEventoRepository regraTrustScoreEventoRepository,
                      TrustScoreHistoricoRepository trustScoreHistoricoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
        this.regraRepository = regraRepository;
        this.regraTrustScoreEventoRepository = regraTrustScoreEventoRepository;
        this.trustScoreHistoricoRepository = trustScoreHistoricoRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            System.out.println("Banco de dados já populado. Ignorando o Seeder.");
            return;
        }

        System.out.println("Iniciando o Database Seeder...");

        // Populando Regras de Avaliação (critérios de nota 0-10 usados pela IA no checkout)
        // Nenhuma seedada por padrão hoje — o admin cria via /regras conforme a necessidade
        // de cada tipo de recurso (ex: "Sala suja", "Equipamento danificado").

        // Populando severidade dos eventos estruturais do ciclo de vida da reserva
        RegraTrustScoreEvento cancelamentoTardio = new RegraTrustScoreEvento();
        cancelamentoTardio.setEvento(TrustScoreEvento.CANCELAMENTO_TARDIO);
        cancelamentoTardio.setParametro(2); // janela em horas
        cancelamentoTardio.setDelta(-15);
        cancelamentoTardio.setDescricao("Penalidade por cancelar com menos de 2 horas de antecedência.");

        RegraTrustScoreEvento noShow = new RegraTrustScoreEvento();
        noShow.setEvento(TrustScoreEvento.NO_SHOW);
        noShow.setDelta(-15);
        noShow.setDescricao("Penalidade por não comparecer à reserva confirmada.");

        RegraTrustScoreEvento excessoCancelamentos = new RegraTrustScoreEvento();
        excessoCancelamentos.setEvento(TrustScoreEvento.EXCESSO_CANCELAMENTOS);
        excessoCancelamentos.setParametro(3); // limite de cancelamentos por semana
        excessoCancelamentos.setDelta(-20);
        excessoCancelamentos.setDescricao("Penalidade para quem cancela mais de 3 reservas na mesma semana.");

        regraTrustScoreEventoRepository.saveAll(List.of(cancelamentoTardio, noShow, excessoCancelamentos));

        // Populando Usuários
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

        // Populando Salas
//        Sala salaReuniao = new Sala("Sala de Reunião Alpha", "Bloco A, 1º Andar",
//                StatusSala.ATIVA, TipoSala.REUNIAO, 10,
//                List.of("Projetor", "Ar Condicionado", "Quadro Branco"),
//                List.of("url_imagem_alpha.jpg"));
//
//        Sala lab = new Sala("Laboratório de Informática", "Bloco B, Térreo",
//                StatusSala.ATIVA, TipoSala.LABORATORIO, 30,
//                List.of("30 Computadores", "Ar Condicionado", "Lousa Digital"),
//                List.of("url_imagem_lab.jpg"));
//
//        salaRepository.saveAll(List.of(salaReuniao, lab));

        ZonedDateTime amanha = ZonedDateTime.now().plusDays(1);
        
//        Reserva reservaNormal = new Reserva();
//        reservaNormal.setSala(salaReuniao);
//        reservaNormal.setUsuario(user1);
//        reservaNormal.setInicioDateTime(amanha.withHour(14).withMinute(0));
//        reservaNormal.setFimDateTime(amanha.withHour(16).withMinute(0));
//        reservaNormal.setTipo(ReservaTipo.PADRAO);
//        reservaNormal.setStatus(ReservaStatus.CONFIRMADA);

//        reservaRepository.save(reservaNormal);

//        TrustScoreHistorico historico = new TrustScoreHistorico();
//        historico.setUsuario(user1);
//        historico.setReserva(null);
//        historico.setRegra(null);
//        historico.setDelta(0);
//        historico.setScoreAnterior(100);
//        historico.setScorePosterior(100);
//        historico.setDescricao("Score inicial do usuário.");
//        trustScoreHistoricoRepository.save(historico);

        System.out.println("Database Seeder concluído com sucesso! Temos 2 usuários, 2 salas e 1 reserva inicial.");
    }
}