package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;


@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

//    @Bean
//    public CommandLineRunner initData(PlayerRepository repository,
//                                      GameRepository repository2,
//                                      GamePlayerRepository repository3,
//                                      ShipRepository repository4,
//                                      SalvoRepository repository5,
//                                      ScoreRepository repository6) {
//        return args -> {
//            //Create Players
//            Player p1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
//            Player p2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
//            Player p3 = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
//            Player p4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));
//            repository.save(p1);
//            repository.save(p2);
//            repository.save(p3);
//            repository.save(p4);
//
//            //Create Games
//            Game g1 = new Game();
//            Game g2 = new Game();
//            Game g3 = new Game();
//            Game g4 = new Game();
//            Game g5 = new Game();
//            Game g6 = new Game();
//            Game g7 = new Game();
//            Game g8 = new Game();
//            g2.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(3600)));
//            g3.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(2*3600)));
//            g4.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(3*3600)));
//            g5.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(4*3600)));
//            g6.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(5*3600)));
//            g7.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(6*3600)));
//            g8.setCreationDateTime(Date.from(g1.getCreationDateTime().toInstant().plusSeconds(7*3600)));
//            repository2.save(g1);
//            repository2.save(g2);
//            repository2.save(g3);
//            repository2.save(g4);
//            repository2.save(g5);
//            repository2.save(g6);
//            repository2.save(g7);
//            repository2.save(g8);
//
//            //Create GamePlayers
//            GamePlayer gp1 = new GamePlayer(p1, g1);
//            GamePlayer gp2 = new GamePlayer(p2, g1);
//
//            GamePlayer gp3 = new GamePlayer(p1, g2);
//            GamePlayer gp4 = new GamePlayer(p2, g2);
//
//            GamePlayer gp5 = new GamePlayer(p2, g3);
//            GamePlayer gp6 = new GamePlayer(p4, g3);
//
//            GamePlayer gp7 = new GamePlayer(p2, g4);
//            GamePlayer gp8 = new GamePlayer(p1, g4);
//
//            GamePlayer gp9 = new GamePlayer(p4, g5);
//            GamePlayer gp10 = new GamePlayer(p1, g5);
//
//            GamePlayer gp11 = new GamePlayer(p3, g6);
//
//            GamePlayer gp12 = new GamePlayer(p4, g7);
//
//            GamePlayer gp13 = new GamePlayer(p3, g8);
//            GamePlayer gp14 = new GamePlayer(p4, g8);
//            repository3.save(gp1);
//            repository3.save(gp2);
//            repository3.save(gp3);
//            repository3.save(gp4);
//            repository3.save(gp5);
//            repository3.save(gp6);
//            repository3.save(gp7);
//            repository3.save(gp8);
//            repository3.save(gp9);
//            repository3.save(gp10);
//            repository3.save(gp11);
//            repository3.save(gp12);
//            repository3.save(gp13);
//            repository3.save(gp14);
//
//            //Create Ships
//            Ship s1 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("H2", "H3", "H4")));
//            gp1.addShip(s1);
//            repository4.save(s1);
//            Ship s2 = new Ship("Submarine", new ArrayList<>(Arrays.asList("E1", "F1", "G1")));
//            gp1.addShip(s2);
//            repository4.save(s2);
//            Ship s3 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("B4", "B5")));
//            gp1.addShip(s3);
//            repository4.save(s3);
//            Ship s4 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp2.addShip(s4);
//            repository4.save(s4);
//            Ship s5 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("F1", "F2")));
//            gp2.addShip(s5);
//            repository4.save(s5);
//
//
//            Ship s6 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp3.addShip(s6);
//            repository4.save(s6);
//            Ship s7 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "C7")));
//            gp3.addShip(s7);
//            repository4.save(s7);
//            Ship s8 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
//            gp4.addShip(s8);
//            repository4.save(s8);
//            Ship s9 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
//            gp4.addShip(s9);
//            repository4.save(s9);
//
//
//            Ship s10 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp5.addShip(s10);
//            repository4.save(s10);
//            Ship s11 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "C7")));
//            gp5.addShip(s11);
//            repository4.save(s11);
//            Ship s12 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
//            gp6.addShip(s12);
//            repository4.save(s12);
//            Ship s13 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
//            gp6.addShip(s13);
//            repository4.save(s13);
//
//
//            Ship s14 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp7.addShip(s14);
//            repository4.save(s14);
//            Ship s15 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "C7")));
//            gp7.addShip(s15);
//            repository4.save(s15);
//            Ship s16 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
//            gp8.addShip(s16);
//            repository4.save(s16);
//            Ship s17 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
//            gp8.addShip(s17);
//            repository4.save(s17);
//
//
//            Ship s18 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp9.addShip(s18);
//            repository4.save(s18);
//            Ship s19 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "C7")));
//            gp9.addShip(s19);
//            repository4.save(s19);
//            Ship s20 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
//            gp10.addShip(s20);
//            repository4.save(s20);
//            Ship s21 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
//            gp10.addShip(s21);
//            repository4.save(s21);
//
//
//            Ship s22 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp11.addShip(s22);
//            repository4.save(s22);
//            Ship s23 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "C7")));
//            gp11.addShip(s23);
//            repository4.save(s23);
//
//
//            Ship s24 = new Ship("Destroyer", new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
//            gp13.addShip(s24);
//            repository4.save(s24);
//            Ship s25 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("C6", "C7")));
//            gp13.addShip(s25);
//            repository4.save(s25);
//            Ship s26 = new Ship("Submarine", new ArrayList<>(Arrays.asList("A2", "A3", "A4")));
//            gp14.addShip(s26);
//            repository4.save(s26);
//            Ship s27 = new Ship("Patrol Boat", new ArrayList<>(Arrays.asList("G6", "H6")));
//            gp14.addShip(s27);
//            repository4.save(s27);
//
//
//            //Create Salvoes
//            Salvo salvo1 = new Salvo(1,new ArrayList<>(Arrays.asList("B5", "C5","F1")));
//            gp1.addSalvo(salvo1);
//            repository5.save(salvo1);
//            Salvo salvo2 = new Salvo(1,new ArrayList<>(Arrays.asList("B5", "B5","B6")));
//            gp2.addSalvo(salvo2);
//            repository5.save(salvo2);
//            Salvo salvo3 = new Salvo(2,new ArrayList<>(Arrays.asList("F2", "D5")));
//            gp1.addSalvo(salvo3);
//            repository5.save(salvo3);
//            Salvo salvo4 = new Salvo(2,new ArrayList<>(Arrays.asList("E1", "H3","A2")));
//            gp2.addSalvo(salvo4);
//            repository5.save(salvo4);
//
//
//            Salvo salvo5 = new Salvo(1,new ArrayList<>(Arrays.asList("A2", "A4","G6")));
//            gp3.addSalvo(salvo5);
//            repository5.save(salvo5);
//            Salvo salvo6 = new Salvo(1,new ArrayList<>(Arrays.asList("B5", "D5","C7")));
//            gp4.addSalvo(salvo6);
//            repository5.save(salvo6);
//            Salvo salvo7 = new Salvo(2,new ArrayList<>(Arrays.asList("A3", "H6")));
//            gp3.addSalvo(salvo7);
//            repository5.save(salvo7);
//            Salvo salvo8 = new Salvo(2,new ArrayList<>(Arrays.asList("C5","C6")));
//            gp4.addSalvo(salvo8);
//            repository5.save(salvo8);
//
//
//            Salvo salvo9 = new Salvo(1,new ArrayList<>(Arrays.asList("G6", "H6","A4")));
//            gp5.addSalvo(salvo9);
//            repository5.save(salvo9);
//            Salvo salvo10 = new Salvo(1,new ArrayList<>(Arrays.asList("H1", "H2","H3")));
//            gp6.addSalvo(salvo10);
//            repository5.save(salvo10);
//            Salvo salvo11 = new Salvo(2,new ArrayList<>(Arrays.asList("A2", "A3","D8")));
//            gp5.addSalvo(salvo11);
//            repository5.save(salvo11);
//            Salvo salvo12 = new Salvo(2,new ArrayList<>(Arrays.asList("E1", "F2","G3")));
//            gp6.addSalvo(salvo12);
//            repository5.save(salvo12);
//
//
//            Salvo salvo13 = new Salvo(1,new ArrayList<>(Arrays.asList("A3", "A4","F7")));
//            gp7.addSalvo(salvo13);
//            repository5.save(salvo13);
//            Salvo salvo14 = new Salvo(1,new ArrayList<>(Arrays.asList("B5", "C6","H1")));
//            gp8.addSalvo(salvo14);
//            repository5.save(salvo14);
//            Salvo salvo15 = new Salvo(2,new ArrayList<>(Arrays.asList("A2", "G6","H6")));
//            gp7.addSalvo(salvo15);
//            repository5.save(salvo15);
//            Salvo salvo16 = new Salvo(2,new ArrayList<>(Arrays.asList("C5", "C7","D5")));
//            gp8.addSalvo(salvo16);
//            repository5.save(salvo16);
//
//
//            Salvo salvo17 = new Salvo(1,new ArrayList<>(Arrays.asList("A1", "A2","A3")));
//            gp9.addSalvo(salvo17);
//            repository5.save(salvo17);
//            Salvo salvo18 = new Salvo(1,new ArrayList<>(Arrays.asList("B5", "B6","C7")));
//            gp10.addSalvo(salvo18);
//            repository5.save(salvo18);
//            Salvo salvo19 = new Salvo(2,new ArrayList<>(Arrays.asList("G6", "G7","G8")));
//            gp9.addSalvo(salvo19);
//            repository5.save(salvo19);
//            Salvo salvo20 = new Salvo(2,new ArrayList<>(Arrays.asList("C6", "D6","E6")));
//            gp10.addSalvo(salvo20);
//            repository5.save(salvo20);
//            Salvo salvo21 = new Salvo(3,new ArrayList<>(Arrays.asList("H1", "H8")));
//            gp10.addSalvo(salvo21);
//            repository5.save(salvo21);
//
//
//            //Create Scores
//            Score sc1 = new Score(g1,p1,1.0);
//            Score sc2 = new Score(g1,p2,1.0);
//            Score sc3 = new Score(g2,p1,0.5);
//            Score sc4 = new Score(g2,p2,0.5);
//            Score sc5 = new Score(g3,p2,0.0);
//            Score sc6 = new Score(g3,p4,1.0);
//            Score sc7 = new Score(g4,p2,0.5);
//            Score sc8 = new Score(g4,p1,0.5);
//            repository6.save(sc1);
//            repository6.save(sc2);
//            repository6.save(sc3);
//            repository6.save(sc4);
//            repository6.save(sc5);
//            repository6.save(sc6);
//            repository6.save(sc7);
//            repository6.save(sc8);
//
//        };
//    }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName-> {
            Player player = playerRepository.findByUserName(inputName);
            if (player != null) {
                return new User(player.getUserName(), player.getPassword(), AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/rest/**").denyAll()
                .antMatchers("/api/games").permitAll()
                .antMatchers("/api/leaderboard").permitAll()
                .antMatchers("/api/login").permitAll()
                .antMatchers("/api/logout").hasAuthority("USER")
                .antMatchers("/api/game_view/**").hasAuthority("USER")
                .antMatchers("/web/games.html").permitAll()
                .antMatchers("/web/game.html").hasAuthority("USER");

        http.formLogin()
                .usernameParameter("userName")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
