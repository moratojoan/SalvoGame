package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ScoreRepository scoreRepository;


    @RequestMapping("/games")
    public Map<String,Object> getMapOfCurrentPlayerAndAllGamesDTO(Authentication authentication){
        return makeMapOfCurrentPlayerAndAllGamesDTO(authentication);
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> getResponseEntityOfCreateNewGamePOST(Authentication authentication){
        return makeResponseEntityOfCreateNewGamePOST(authentication);
    }

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String,Object>> getMapOfGamePlayerByIdDTO(
            @PathVariable("nn") long gpId,
            Authentication authentication){
        return makeResponseEntityOfGamePlayerByIdDTO(gpId, authentication);
    }

    @RequestMapping("/leaderboard")
    public List<Map<String,Object>> getListOfLeaderboardDTO(){
        return makeListOfLeaderboardDTO(playerRepository.findAll());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getResponseEntityOfCreateNewPlayerPOST(
            @RequestParam String userName,
            @RequestParam String password) {
        return makeResponseEntityOfCreateNewPlayerPOST(userName,password);
    }

    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getResponseEntitiyOfJoinGamePOST(
            @PathVariable("nn") long gameId,
            Authentication authentication){
        return makeResponseEntityOfJoinGamePOST(gameId, authentication);
    }

    @RequestMapping(path = "/games/players/{nn}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getResponseEntityOfPlaceShipsPOST(
            @PathVariable("nn") long gpId,
            @RequestBody List<Ship> ships,
            Authentication authentication){
        return makeResponseEntityOfPlaceShipsPOST(gpId, ships, authentication);
    }

    @RequestMapping(path = "/games/players/{nn}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getResponseEntityOfSalvoPost(
            @PathVariable("nn") long gpId,
            @RequestBody Salvo salvo,
            Authentication authentication){
        return makeResponseEntityOfSalvoPOST(gpId, salvo, authentication);
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

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfGamePlayerByIdDTO(long gpId, Authentication authentication){
        if(!isUserLoggedIn(authentication)){
            return new ResponseEntity<>(makeMap("error", "User not Logged In"), HttpStatus.UNAUTHORIZED);
        }


        Optional<GamePlayer> gpOptional = gamePlayerRepository.findById(gpId);
        if(!gpOptional.isPresent()){
            return new ResponseEntity<>(makeMap("error", "Game Player Not Found"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer gamePlayer = gpOptional.get();
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        if(!currentPlayer.getId().equals(gamePlayer.getPlayer().getId())){
            return new ResponseEntity<>(makeMap("error", "This Game Player does not belong to the User"), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(makeMapOfGamePlayerByIdDTO(gpId), HttpStatus.OK);
    }

    private Map<String, Object> makeMapOfGamePlayerByIdDTO(long gpId){
        Optional<GamePlayer> optionalGamePlayer = gamePlayerRepository.findById(gpId);
        Map<String, Object> dto = optionalGamePlayer.map(gamePlayer -> makeMapOfGameDTO(gamePlayer.getGame())).orElse(null);

        if(dto != null){
            this.gameOverController(optionalGamePlayer.get().getGame());
            dto.put("status", makeMapOfGameStateDTO(optionalGamePlayer.get()));
            dto.put("ships", makeListOfShipsDTO(optionalGamePlayer.get().getShipSet()));
            dto.put("salvoes", makeMapOfSalvoDTO(optionalGamePlayer.get().getGame().getGamePlayerSet()));
            dto.put("hits", makeMapOfSalvoHitsDTO(optionalGamePlayer.get().getGame().getGamePlayerSet()));
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

    private void gameOverController(Game game) {
        if(game.isTheGameOver() && game.getScoreSet().size()==0){
            this.addScores(game);
        }
    }

    private void addScores(Game game){
        List<GamePlayer> gamePlayers= new ArrayList<>(game.getGamePlayerSet());
        GamePlayer gamePlayer1 = gamePlayers.get(0);
        GamePlayer gamePlayer2 = gamePlayers.get(1);

        Score sc1 = new Score(game, gamePlayer1.getPlayer(),gamePlayer1.calculateScore());
        Score sc2 = new Score(game, gamePlayer2.getPlayer(),gamePlayer2.calculateScore());

        scoreRepository.save(sc1);
        scoreRepository.save(sc2);

    }

    private Map<String,Object> makeMapOfGameStateDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("message", gamePlayer.getGameStatusMessage());
        dto.put("allowedToEnterSalvo", gamePlayer.isAllowedToEnterSalvo());
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

    private Map<String, Object> makeMapOfSalvoHitsDTO(Set<GamePlayer> gamePlayerSet) {
        List<GamePlayer> gamePlayerList = new ArrayList<>(gamePlayerSet);
        Map<String,Object> dto = new LinkedHashMap<>();
        for (GamePlayer gamePlayer : gamePlayerList) {
            dto.put(gamePlayer.getId().toString(),gamePlayer.getMapOfShipsState());
        }
        return dto;
    }

    private List<Map<String,Object>> makeListOfLeaderboardDTO(List<Player> playerList){
        return playerList.stream().map(this::makeMapOfPlayerForTheLeaderboardDTO).sorted(this::comparator).collect(toList());
    }

    private Map<String, Object> makeMapOfPlayerForTheLeaderboardDTO(Player player) {
        Map<String, Object> dto = makeMapOfPlayerDTO(player);
        List<Double> scoresList = player.getGamePlayerSet().stream().filter(gamePlayer -> !Objects.isNull(gamePlayer.getScore())).map(gamePlayer -> gamePlayer.getScore().getScore()).collect(toList());
        dto.put("totalScore", scoresList.stream().mapToDouble(value -> value).sum());
        dto.put("numberOfGamesEnded",scoresList.size());
        dto.put("wins",scoresList.stream().filter(value -> value==1).collect(toList()).size());
        dto.put("draws",scoresList.stream().filter(value -> value==0.5).collect(toList()).size());
        dto.put("losses",scoresList.stream().filter(value -> value==0).collect(toList()).size());
        return dto;
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

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfCreateNewPlayerPOST(String userName, String password) {
        String userNameTrim = userName.trim();

        if (userNameTrim.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        }

        if(userNameTrim.contains(" ")){
            return new ResponseEntity<>(makeMap("error", "Invalid email. Whitespaces not allowed."), HttpStatus.FORBIDDEN);
        }

        if(!userNameTrim.contains("@")){
            return new ResponseEntity<>(makeMap("error", "Invalid email. @ is needed."), HttpStatus.FORBIDDEN);
        }

        if(StringUtils.countOccurrencesOf(userNameTrim, "@")>1){
            return new ResponseEntity<>(makeMap("error", "Invalid email. Only one @ is allowed."), HttpStatus.FORBIDDEN);
        }

        if(!emailValidateRegExp(userNameTrim)){
            return new ResponseEntity<>(makeMap("error", "Invalid email. Regular Expression."), HttpStatus.FORBIDDEN);
        }

        if (password.contains(" ")){
            return new ResponseEntity<>(makeMap("error", "Invalid password. Whitespaces not allowed."), HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(userName);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.CONFLICT);
        }

        Player newPlayer = playerRepository.save(new Player(userNameTrim, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("id", newPlayer.getId()), HttpStatus.CREATED);
    }

    private boolean emailValidateRegExp(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfCreateNewGamePOST(Authentication authentication){
        if(!isUserLoggedIn(authentication)){
            return new ResponseEntity<>(makeMap("error", "User not Logged In"), HttpStatus.UNAUTHORIZED);
        }

        Game newGame = new Game();
        gameRepository.save(newGame);

        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer newGamePlayer = new GamePlayer(currentPlayer, newGame);
        gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(makeMap("gpId",newGamePlayer.getId()),HttpStatus.CREATED);

    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfJoinGamePOST(long gameId, Authentication authentication){
        if(!isUserLoggedIn(authentication)){
            return new ResponseEntity<>(makeMap("error", "User not Logged In"), HttpStatus.UNAUTHORIZED);
        }

        Optional<Game> optionalGameSelected = gameRepository.findById(gameId);
        if(!optionalGameSelected.isPresent()){
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }

        Game gameSelected = optionalGameSelected.get();
        if(gameSelected.getGamePlayerSet().size() >= 2){
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }

        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        if(gameSelected.getGamePlayerSet().stream().anyMatch(gamePlayer -> gamePlayer.getPlayer().getId().equals(currentPlayer.getId()))){
            return new ResponseEntity<>(makeMap("error", "You are already in this game"), HttpStatus.FORBIDDEN);
        }

        GamePlayer newGamePlayer = new GamePlayer(currentPlayer,gameSelected);
        gamePlayerRepository.save(newGamePlayer);
        return new ResponseEntity<>(makeMap("gpId",newGamePlayer.getId()),HttpStatus.CREATED);

    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfPlaceShipsPOST(long gpId, List<Ship> ships, Authentication authentication) {
        if(!isUserLoggedIn(authentication)){
            return new ResponseEntity<>(makeMap("error", "User not Logged In"), HttpStatus.UNAUTHORIZED);
        }

        Optional<GamePlayer> optionalGamePlayer = gamePlayerRepository.findById(gpId);
        if(!optionalGamePlayer.isPresent()){
            return new ResponseEntity<>(makeMap("error", "No Game Player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer currentGamePlayer = optionalGamePlayer.get();
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        if(!currentGamePlayer.getPlayer().getId().equals(currentPlayer.getId())){
            return new ResponseEntity<>(makeMap("error", "The current user is not the game player the ID references"), HttpStatus.UNAUTHORIZED);
        }

        if(!currentGamePlayer.getShipSet().isEmpty()){
            return new ResponseEntity<>(makeMap("error", "The user already has ships placed"), HttpStatus.FORBIDDEN);
        }

        for (Ship ship : ships) {
            currentGamePlayer.addShip(ship);
            shipRepository.save(ship);
        }
        return new ResponseEntity<>(makeMap("ok", "Ships Saved"), HttpStatus.CREATED);

    }

    private ResponseEntity<Map<String, Object>> makeResponseEntityOfSalvoPOST(long gpId, Salvo salvo, Authentication authentication){

        if(!isUserLoggedIn(authentication)){
            return new ResponseEntity<>(makeMap("error", "User not Logged In"), HttpStatus.UNAUTHORIZED);
        }

        Optional<GamePlayer> optionalGamePlayer = gamePlayerRepository.findById(gpId);
        if(!optionalGamePlayer.isPresent()){
            return new ResponseEntity<>(makeMap("error", "No Game Player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer currentGamePlayer = optionalGamePlayer.get();
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        if(!currentGamePlayer.getPlayer().getId().equals(currentPlayer.getId())){
            return new ResponseEntity<>(makeMap("error", "The current user is not the game player the ID references"), HttpStatus.UNAUTHORIZED);
        }

        if(currentGamePlayer.getGame().getGamePlayerSet().size()<2){
            return new ResponseEntity<>(makeMap("error", "Waiting for an Opponent"), HttpStatus.FORBIDDEN);
        }

        GamePlayer opponentGamePlayer = currentGamePlayer.getOpponent();
        if(opponentGamePlayer.getShipSet().size() == 0){
            return new ResponseEntity<>(makeMap("error", "Wait your Opponent to place his ships"), HttpStatus.FORBIDDEN);
        }

        if(currentGamePlayer.getGame().isTheGameOver()){
            return new ResponseEntity<>(makeMap("error", "The Game is Over"), HttpStatus.FORBIDDEN);
        }

        if(!currentGamePlayer.isTheGamePlayersTurn()){
            return new ResponseEntity<>(makeMap("error", "It is not your turn"), HttpStatus.FORBIDDEN);
        }

        if(salvo.getSalvoLocations().size() != 5){
            return new ResponseEntity<>(makeMap("error", "5 shots in a Salvo"), HttpStatus.FORBIDDEN);
        }

        boolean salvoOverlapWithItself = salvoOverlapWithItself(salvo.getSalvoLocations());
        boolean salvoOverlapWithOtherTurnsSalvoes = currentGamePlayer.getSalvoSet().stream().anyMatch(salvoOtherTurn -> shotsOverlapWithOtherTurnsSalvoes(salvoOtherTurn.getSalvoLocations(), salvo.getSalvoLocations()));
        if(salvoOverlapWithItself || salvoOverlapWithOtherTurnsSalvoes){
            return new ResponseEntity<>(makeMap("error", "Shots Overlapped"), HttpStatus.FORBIDDEN);
        }

        salvo.setTurn(currentGamePlayer.getSalvoSet().size()+1);
        currentGamePlayer.addSalvo(salvo);
        salvoRepository.save(salvo);
        return new ResponseEntity<>(makeMap("ok", "Salvo added"), HttpStatus.CREATED);
    }

    private boolean salvoOverlapWithItself(List<String> salvoLocations) {

        for(String shot: salvoLocations){
            List<String> salvoLocationsExceptThisShot = new ArrayList<>(salvoLocations);
            salvoLocationsExceptThisShot.remove(shot);
            if(salvoLocationsExceptThisShot.stream().anyMatch(shot::equals)){
                return true;
            }
        }

        return false;
    }

    private boolean shotsOverlapWithOtherTurnsSalvoes(List<String> salvoOtherTurnLocations, List<String> salvoLocations) {

        for(String shot: salvoOtherTurnLocations){
            if(salvoLocations.stream().anyMatch(shot::equals)){
                return true;
            }
        }

        return false;
    }
}
