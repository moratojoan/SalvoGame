var app = new Vue({
    el: "#app",
    data: {
        urlApiGames: "/api/games",
        urlApiLeaderBoard: "/api/leaderboard",
        gamesList: null,
        currentPlayer: null,
        leaderBoard: null,
        loading: true
    },
    methods: {
        //FetchList with Promise.all()
        fetchJson: function (url, init) {
            return fetch(url, init).then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response.statusText);
            });
        },
        fetchJsonList: function (urls, init){
            return Promise.all(urls.map(url => this.fetchJson(url, init)));
        },
        startFetchList: function(urls, init){
            this.fetchJsonList(urls, init).then(values => {
                this.gamesList = values[0].games;
                if(values[0].player){
                    this.currentPlayer = values[0].player;
                }
                this.leaderBoard = values[1];
                this.loading = false;
            });
        },
        //Fetch inside another Fetch
        startFetch: function (url) {
            fetch(url, {
                    method: "GET"
                }).then(response => response.json())
                .then(myData => {
                    this.gamesList = myData;
                    console.log(myData);
                    // this.leaderBoard = this.createLeaderBoardInfo(this.gamesList);
                    this.leaderBoardFetch();
                    this.loading = false;
                });
        },
        leaderBoardFetch: function () {
            fetch("/api/leaderboard", {
                    method: "GET"
                }).then(response => response.json())
                .then(leaderBoardData => this.leaderBoard = leaderBoardData);
        },
        //Method to convert milliseds to StringHour
        convertMillisecondsToDate: function(milliseconds){
            var date = new Date(milliseconds);
            var dateString = date.getDate() +"/"+(date.getMonth()+1)+"/"+date.getFullYear()+" - "+date.getHours()+":"+date.getMinutes();
            return dateString;
        },
        //Methods to create the Leaderboard Table. But with the second fetch, these functions are not needed.
        createLeaderBoardInfo: function (gamesList) {
            var playersList = this.createPlayersList(gamesList);
            for (let i = 0; i < playersList.length; i++) {
                playersList[i].totalScore = this.getTotalScore(gamesList, playersList[i].id);
                playersList[i].numberOfGamesEnded = this.getNumberOfGamesEnded(gamesList, playersList[i].id);
                playersList[i].wins = this.getNumberOfWins(gamesList, playersList[i].id);
                playersList[i].draws = this.getNumberOfDraws(gamesList, playersList[i].id);
                playersList[i].losses = this.getNumberOfLosses(gamesList, playersList[i].id);
            }

            playersList.sort((a, b) => {
                if (a.totalScore - b.totalScore > 0) {
                    return -1;
                } else if (a.totalScore - b.totalScore < 0) {
                    return 1;
                } else {
                    if (a.numberOfGamesEnded - b.numberOfGamesEnded > 0) {
                        return -1;
                    } else if (a.numberOfGamesEnded - b.numberOfGamesEnded < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            return playersList;
        },
        createPlayersList: function (gamesList) {
            var playersList = [];
            for (let i = 0; i < gamesList.length; i++) {
                for (let j = 0; j < gamesList[i].gamePlayers.length; j++) {
                    if (!this.isPlayerAdded(playersList, gamesList[i].gamePlayers[j].player.id)) {
                        var newPlayer = {
                            id: gamesList[i].gamePlayers[j].player.id,
                            email: gamesList[i].gamePlayers[j].player.email
                        }
                        playersList.push(newPlayer);
                    }
                }
            }
            return playersList;
        },
        isPlayerAdded: function (playersList, playerId) {
            return playersList.some(player => playerId === player.id);
        },
        getTotalScore: function (gamesList, playerId) {
            var totalScore = 0;
            for (let i = 0; i < gamesList.length; i++) {
                for (let j = 0; j < gamesList[i].gamePlayers.length; j++) {
                    if (gamesList[i].gamePlayers[j].player.id === playerId && gamesList[i].gamePlayers[j].score !== null) {
                        totalScore += gamesList[i].gamePlayers[j].score;
                    }
                }
            }
            return totalScore;
        },
        getNumberOfGamesEnded: function (gamesList, playerId) {
            var totalGamesEnded = 0;
            for (let i = 0; i < gamesList.length; i++) {
                for (let j = 0; j < gamesList[i].gamePlayers.length; j++) {
                    if (gamesList[i].gamePlayers[j].player.id === playerId && gamesList[i].gamePlayers[j].score !== null) {
                        totalGamesEnded++;
                    }
                }
            }
            return totalGamesEnded;
        },
        getNumberOfWins: function (gamesList, playerId) {
            var wins = 0;
            for (let i = 0; i < gamesList.length; i++) {
                for (let j = 0; j < gamesList[i].gamePlayers.length; j++) {
                    if (gamesList[i].gamePlayers[j].player.id === playerId && gamesList[i].gamePlayers[j].score === 1) {
                        wins++;
                    }
                }
            }
            return wins;
        },
        getNumberOfDraws: function (gamesList, playerId) {
            var draws = 0;
            for (let i = 0; i < gamesList.length; i++) {
                for (let j = 0; j < gamesList[i].gamePlayers.length; j++) {
                    if (gamesList[i].gamePlayers[j].player.id === playerId && gamesList[i].gamePlayers[j].score === 0.5) {
                        draws++;
                    }
                }
            }
            return draws;
        },
        getNumberOfLosses: function (gamesList, playerId) {
            var losses = 0;
            for (let i = 0; i < gamesList.length; i++) {
                for (let j = 0; j < gamesList[i].gamePlayers.length; j++) {
                    if (gamesList[i].gamePlayers[j].player.id === playerId && gamesList[i].gamePlayers[j].score === 0) {
                        losses++;
                    }
                }
            }
            return losses;
        }
    },
    created: function () {
        // this.startFetch(this.urlApiGames);
        var urls = [this.urlApiGames, this.urlApiLeaderBoard];
        var init = {method: "GET"};
        this.startFetchList(urls,init);
    }

});