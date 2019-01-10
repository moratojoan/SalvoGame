package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api")
public class SalvoController {

    //Attributes
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Request Methods
//    @GetMapping("/games")
    @RequestMapping("/games")
    public Map<String,Object> getMapOfCurrentPlayerAndAllGamesDTO(Authentication authentication){
        return makeMapOfCurrentPlayerAndAllGamesDTO(authentication);
    }

//    @PostMapping("/games")
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> getResponseEntityOfCreateNewGamePOST(Authentication authentication){
        return makeResponseEntityOfCreateNewGamePOST(authentication);
    }

    //    @GetMapping("/game_view/{nn}")
    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String,Object>> getMapOfGAmePlayerByIdDTO(@PathVariable("nn") long gpId, Authentication authentication){
        return makeResponseEntityOfGamePlayerByIdDTO(gpId, authentication);
    }

    //    @GetMapping("/leaderboard")
    @RequestMapping("/leaderboard")
    public List<Map<String,Object>> getListOfLeaderboardDTO(){
        return makeListOfLeaderboardDTO(playerRepository.findAll());
    }

//    @PostMapping("/players")
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getResponseEntityOfCreateNewPlayerPOST(@RequestParam String userName, @RequestParam String password) {
        return makeResponseEntityOfCreateNewPlayerPOST(userName,password);
    }

//    @PostMapping(path = "/game/{nn}/players")
    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getResponseEntitiyOfJoinGamePOST(@PathVariable("nn") long gameId, Authentication authentication){
        return makeResponseEntityOfJoinGamePOST(gameId, authentication);
    }

    //Support Methods
    private boolean isUserLoggedIn(Authentication authentication) {
        return !(authentication == null ||
                authentication instanceof AnonymousAuthenticationToken ||
                authentication.getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("USER")));
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private boolean emailValidate(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    //DTO Methods
    private Map<String,Object> makeMapOfCurrentPlayerAndAllGamesDTO(Authentication authentication){
        Map<String, Object> dto = new LinkedHashMap<>();
        if(isUserLoggedIn(authentication)){
            dto.put("player",makeMapOfPlayerDTO(playerRepository.findByUserName(authentication.getName())));
        }
        dto.put("games", makeListOfGamesDTO(gameRepository.findAll()));
        return dto;
    }

    private List<Map<String,Object>> makeListOfGamesDTO(List<Game> gameList){
        return gameList.stream().map(this::makeMapOfGameDTO).collect(toList());
    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfGamePlayerByIdDTO(long gpId, Authentication authentication) {
        if(isUserLoggedIn(authentication)){
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            Optional<GamePlayer> gpOptional = gamePlayerRepository.findById(gpId);
            if(gpOptional.isPresent()){
                GamePlayer gp = gpOptional.get();
                if(currentPlayer.getId().equals(gp.getPlayer().getId())){
                    return new ResponseEntity<>(makeMapOfGamePlayerByIdDTO(gpId), HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(makeMap("error", "UNAUTHORIZED"), HttpStatus.UNAUTHORIZED);
    }

    private Map<String, Object> makeMapOfGamePlayerByIdDTO(long gpId){
        Optional<GamePlayer> optionalGamePlayer = gamePlayerRepository.findById(gpId);
        Map<String, Object> dto = optionalGamePlayer.map(gamePlayer -> makeMapOfGameDTO(gamePlayer.getGame())).orElse(null);
        if(dto != null){
            dto.put("ships", makeListOfShipsDTO(optionalGamePlayer.get().getShipSet()));
            dto.put("salvoes", makeMapOfSalvoDTO(optionalGamePlayer.get().getGame().getGamePlayerSet()));
        }
        return dto;
    }

    private Map<String,Object> makeMapOfGameDTO(Game game){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id",game.getId());
        dto.put("created",game.getCreationDateTime().getTime());
        dto.put("gamePlayers", makeListOfGamePlayersDTO(game.getGamePlayerSet()));
        return dto;
    }

    private List<Map<String,Object>> makeListOfGamePlayersDTO(Set<GamePlayer> gamePlayerSet) {
        return gamePlayerSet.stream().sorted(Comparator.comparing(GamePlayer::getId)).map(this::makeMapOfGamePlayerDTO).collect(toList());
    }

    private Map<String,Object> makeMapOfGamePlayerDTO(GamePlayer gamePlayer){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id",gamePlayer.getId());
        dto.put("player", makeMapOfPlayerDTO(gamePlayer.getPlayer()));
        if(gamePlayer.getPlayer().getScore(gamePlayer.getGame())!=null){
            dto.put("score", gamePlayer.getPlayer().getScore(gamePlayer.getGame()).getScore());
        } else {
            dto.put("score", null);
        }
        return dto;
    }

    private Map<String,Object> makeMapOfPlayerDTO(Player player) {
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id",player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

    private List<Map<String,Object>> makeListOfShipsDTO(Set<Ship> shipSet) {
        return shipSet.stream().map(this::makeMapOfShipDTO).collect(toList());
    }

    private Map<String,Object> makeMapOfShipDTO(Ship ship){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("type",ship.getType());
        dto.put("locations", ship.getShipLocations());
        return dto;
    }

    private Map<String, Object> makeMapOfSalvoDTO(Set<GamePlayer> gamePlayerSet){
        List<GamePlayer> gamePlayerList = new ArrayList<>(gamePlayerSet);
        Map<String,Object> dto = new LinkedHashMap<>();
        for (GamePlayer gamePlayer : gamePlayerList) {
            dto.put(gamePlayer.getId().toString(), makeMapOfSalvoTurnDTO(gamePlayer.getSalvoSet()));
        }
        return dto;
    }

    private Map<String, Object> makeMapOfSalvoTurnDTO(Set<Salvo> salvoSet) {
        Map<String,Object> dto = new LinkedHashMap<>();
        List<Salvo> salvoList = new ArrayList<>(salvoSet);
        for (Salvo salvo: salvoList){
            dto.put(salvo.getTurn().toString(),salvo.getSalvoLocations());
        }
        return dto;
    }

    private List<Map<String,Object>> makeListOfLeaderboardDTO(List<Player> playerList){
        return playerList.stream().map(this::makeMapOfPlayerInLeaderboardDTO).sorted(this::comparator).collect(toList());
    }

    private int comparator(Map<String, Object> a, Map<String, Object> b) {
        if ((double) a.get("totalScore") - (double) b.get("totalScore") > 0) {
            return -1;
        } else if ((double) a.get("totalScore") - (double) b.get("totalScore") < 0) {
            return 1;
        } else {
            if ((int) a.get("numberOfGamesEnded") - (int) b.get("numberOfGamesEnded") > 0) {
                return -1;
            } else if ((int) a.get("numberOfGamesEnded") - (int) b.get("numberOfGamesEnded") < 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private Map<String, Object> makeMapOfPlayerInLeaderboardDTO(Player player) {
        Map<String, Object> dto = makeMapOfPlayerDTO(player);
        List<Double> scoresList = player.getGamePlayerSet().stream().filter(gamePlayer -> !Objects.isNull(gamePlayer.getScore())).map(gamePlayer -> gamePlayer.getScore().getScore()).collect(toList());
        dto.put("totalScore", scoresList.stream().mapToDouble(value -> value).sum());
        dto.put("numberOfGamesEnded",scoresList.size());
        dto.put("wins",scoresList.stream().filter(value -> value==1).collect(toList()).size());
        dto.put("draws",scoresList.stream().filter(value -> value==0.5).collect(toList()).size());
        dto.put("losses",scoresList.stream().filter(value -> value==0).collect(toList()).size());
        return dto;
    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfCreateNewPlayerPOST(String userName, String password) {
        String userNameTrim = userName.trim();

        if (userNameTrim.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        } else if(userNameTrim.contains(" ")){
            return new ResponseEntity<>(makeMap("error", "Invalid email. Whitespaces not allowed."), HttpStatus.FORBIDDEN);
        } else if(!userNameTrim.contains("@")){
            return new ResponseEntity<>(makeMap("error", "Invalid email. @ is needed."), HttpStatus.FORBIDDEN);
        } else if(StringUtils.countOccurrencesOf(userNameTrim, "@")>1){
            return new ResponseEntity<>(makeMap("error", "Invalid email. Only one @ is allowed."), HttpStatus.FORBIDDEN);
        } else if (password.contains(" ")){
            return new ResponseEntity<>(makeMap("error", "Invalid password. Whitespaces not allowed."), HttpStatus.FORBIDDEN);
        } else if(!emailValidate(userNameTrim)){
            return new ResponseEntity<>(makeMap("error", "Invalid email. Regular Expression."), HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(userName);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.CONFLICT);
        }

        Player newPlayer = playerRepository.save(new Player(userNameTrim, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("id", newPlayer.getId()), HttpStatus.CREATED);
    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfCreateNewGamePOST(Authentication authentication) {
        if(isUserLoggedIn(authentication)){
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            Game newGame = new Game();
            gameRepository.save(newGame);
            GamePlayer newGamePlayer = new GamePlayer(currentPlayer,newGame);
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>(makeMap("gpId",newGamePlayer.getId()),HttpStatus.CREATED);
        }
        return new ResponseEntity<>(makeMap("error", "Login to create a new game"), HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfJoinGamePOST(long gameId, Authentication authentication) {
        if(isUserLoggedIn(authentication)){
            Optional<Game> optionalGameSelected = gameRepository.findById(gameId);
            if(optionalGameSelected.isPresent()){
                Game gameSelected = optionalGameSelected.get();
                if(gameSelected.getGamePlayerSet().size()<2){
                    Player currentPlayer = playerRepository.findByUserName(authentication.getName());
                    GamePlayer newGamePlayer = new GamePlayer(currentPlayer,gameSelected);
                    gamePlayerRepository.save(newGamePlayer);
                    return new ResponseEntity<>(makeMap("gpId",newGamePlayer.getId()),HttpStatus.CREATED);
                }
                return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(makeMap("error", "Login to join game"), HttpStatus.UNAUTHORIZED);
    }
}
