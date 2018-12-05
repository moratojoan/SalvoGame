package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    private LocalDateTime joinedDateTime;

    @OneToMany(mappedBy="gamePlayer", fetch= FetchType.EAGER)
    private Set<Ship> shipSet = new HashSet<>();

    //Constructors
    public GamePlayer(){
    }

    public GamePlayer(Player player, Game game){
        this.player = player;
        this.game = game;
        this.joinedDateTime = LocalDateTime.now();
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

    public LocalDateTime getJoinedDateTime() {
        return this.joinedDateTime;
    }

    public void setJoinedDateTime(LocalDateTime joinedDateTime) {
        this.joinedDateTime = joinedDateTime;
    }

    public Set<Ship> getShipSet(){
        return this.shipSet;
    }

    //Other Methods
    public void addShip(Ship ship){
        ship.setGamePlayer(this);
        this.shipSet.add(ship);
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "player=" + player +
                ", game=" + game +
                ", joinedDateTime=" + joinedDateTime +
                '}';
    }
}
