package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

    //Request Methods
    @RequestMapping("/games")
    public Map<String,Object> getMapOfCurrentPlayerAndAllGamesDTO(Authentication authentication){
        return makeMapOfCurrentPlayerAndAllGamesDTO(authentication);
    }

    @RequestMapping("/game_view/{nn}")
    public Map<String, Object> getMapOfGamePlayerByIdDTO(@PathVariable("nn") long gpId){
        return makeMapOfGamePlayerByIdDTO(gpId);
    }

    @RequestMapping("/leaderboard")
    public List<Map<String,Object>> getListOfLeaderboardDTO(){
        return makeListOfLeaderboardDTO(playerRepository.findAll());
    }

    //Support Methods
    private boolean isUserLoggedIn(Authentication authentication) {
        return !(authentication == null || authentication instanceof AnonymousAuthenticationToken);
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
        return playerList.stream().map(this::makeMapOfPlayerInLeaderboardDTO).collect(toList());
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
}
