package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;


@Entity
public class Game {

    //Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private LocalDateTime creationDateTime;

    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<GamePlayer> gamePlayerSet = new HashSet<>();

    //Constructors
    public Game(){
        this.creationDateTime = LocalDateTime.now();
    }

    //Getters and Setters of the Attributes
    public Long getId(){
        return this.id;
    }

    public LocalDateTime getCreationDateTime(){
        return this.creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Set<GamePlayer> getGamePlayerSet() {
        return this.gamePlayerSet;
    }

    //Other Methods
    public List<Player> getPlayers(){
        return this.gamePlayerSet.stream().map(GamePlayer::getPlayer).collect(toList());
    }
}
