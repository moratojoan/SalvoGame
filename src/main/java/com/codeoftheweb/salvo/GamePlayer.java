package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    //Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    private Date joinedDateTime;

    @OneToMany(mappedBy="gamePlayer", fetch= FetchType.EAGER)
    private Set<Ship> shipSet = new HashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvoSet = new HashSet<>();

    //Constructors
    public GamePlayer(){
    }

    public GamePlayer(Player player, Game game){
        this.player = player;
        this.game = game;
        this.joinedDateTime = new Date();
    }

    //Getters and Setters of the Attributes
    public Long getId(){
        return this.id;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Date getJoinedDateTime() {
        return this.joinedDateTime;
    }

    public void setJoinedDateTime(Date joinedDateTime) {
        this.joinedDateTime = joinedDateTime;
    }

    public Set<Ship> getShipSet(){
        return this.shipSet;
    }

    public Set<Salvo> getSalvoSet(){
        return this.salvoSet;
    }


    //Other Methods
    public void addShip(Ship ship){
        ship.setGamePlayer(this);
        this.shipSet.add(ship);
    }

    public void addSalvo(Salvo salvo){
        salvo.setGamePlayer(this);
        this.salvoSet.add(salvo);
    }

    public Score getScore(){
        return this.getPlayer().getScore(this.getGame());
    }

    public GamePlayer getOpponent(){
        List<GamePlayer> gamePlayerListFiltered = this.getGame()
                .getGamePlayerSet()
                .stream()
                .filter(gamePlayer -> !gamePlayer.getId().equals(this.getId()))
                .collect(toList());

        if(gamePlayerListFiltered.size()==0){
            return null;
        }

        return gamePlayerListFiltered.get(0);
    }

    public boolean isTheGamePlayersTurn(){
        GamePlayer opponentGamePlayer = this.getOpponent();

        if(opponentGamePlayer == null){
            return false;
        }

        if(opponentGamePlayer.getShipSet().size() == 0){
            return false;
        }

        return (this.getSalvoSet().size()==opponentGamePlayer.getSalvoSet().size()
                ||
                this.getSalvoSet().size() == opponentGamePlayer.getSalvoSet().size()-1);

    }

    public Map<String, Map<String,Object>> getMapOfShipsState(){
        Map<String, Map<String,Object>> mapOfShipsState = new HashMap<>();

        for(Ship ship: this.getShipSet()){
            mapOfShipsState.put(ship.getType(),ship.getMapOfShipState());
        }

        return mapOfShipsState;
    }

    public String getGameStatusMessage(){
        //Place Ships, Wait, Enter Salvo, Game Over
        if(this.getShipSet().size() == 0) {
            return "Place Ships";
        } else if(this.getGame().isTheGameOver()){
            return "The Game Is Over";
        }else if(this.isTheGamePlayersTurn()){
            return "Enter Salvo";
        }else {
            return "Wait For The Other Player";
        }
    }

    public boolean isAllowedToEnterSalvo() {
        return (this.getShipSet().size() != 0) && !this.getGame().isTheGameOver() && this.isTheGamePlayersTurn();
    }

    public boolean sunkAllOpponentShips(){
        List<String> listOfSalvoLocations = this.getSalvoSet().stream().map(Salvo::getSalvoLocations).flatMap(Collection::stream).collect(toList());
        List<String> listOfShipLocationsOfOpponent = this.getOpponent().getShipSet().stream().map(Ship::getShipLocations).flatMap(Collection::stream).collect(toList());

        return listOfSalvoLocations.containsAll(listOfShipLocationsOfOpponent);
    }

    public Double calculateScore(){
        if(!this.getGame().isTheGameOver()){
            return null;
        }

        if(this.sunkAllOpponentShips() && this.getOpponent().sunkAllOpponentShips()){
            return 0.5;
        } else if(this.sunkAllOpponentShips() && !this.getOpponent().sunkAllOpponentShips()){
            return 1.0;
        }else {
            return 0.0;
        }
    }
}
