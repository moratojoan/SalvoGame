package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<GamePlayer> gamePlayerList = this.getGame().getGamePlayerSet().stream().filter(gamePlayer -> !gamePlayer.getId().equals(this.getId())).collect(toList());
        if(gamePlayerList.size()>0){
            return gamePlayerList.get(0);
        }else{
            return null;
        }
    }
}
