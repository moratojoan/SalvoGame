var app = new Vue({
    el: "#app",
    data: {
        gridSize: 11,
        rowNames: ["", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        columnNames: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        urlApiGameViewGPID: "/api/game_view/",
        searchObj: null,
        gameData: null,
        currentPlayer: null,
        oponentPlayer: null,
        loading: true
    },
    methods: {
        getURLParams: function () {
            var obj = {};
            var search = location.search.substr(1);
            search = search.split("&");
            for (let i = 0; i < search.length; i++) {
                let subSearch = search[i].split("=");
                if (!obj[subSearch[0]]) {
                    obj[subSearch[0]] = subSearch[1];
                }
            }
            this.searchObj = obj;
            console.log(this.searchObj);
        },
        startFetch: function (url) {
            fetch(url, {
                    method: "GET"
                }).then(response => response.json())
                .then(myData => {
                    this.gameData = myData;
                    console.log(myData);
                    this.playerUserNames();
                    this.loading = false;
                });
        },
        getCellPosition: function (i) {
            i--; //The v-for starts with i=1. I want that starts with i=0.
            var inRow = Math.trunc(i / 11);
            var inColumn = i % 11;
            if (inColumn == 0) {
                return this.rowNames[inRow]
            } else {
                return this.rowNames[inRow] + inColumn;
            }
        },
        getCellClass: function (i) {
            var cellPosition = this.getCellPosition(i);
            var ships = this.gameData.ships;
            for (let j = 0; j < ships.length; j++) {
                if (ships[j].locations.includes(cellPosition)) {
                    return "grid-item-withShip";
                }
            }
            return "grid-item";
        },
        getCellName: function (i) {
            var cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition)) {
                return cellPosition;
            }
            return null;
        },
        playerUserNames: function () {
            var gamePlayers = this.gameData.gamePlayers;
            if (gamePlayers.length == 2) {
                if (gamePlayers[0].id == this.searchObj.gp) {
                    this.currentPlayer = gamePlayers[0].player.email;
                    this.oponentPlayer = gamePlayers[1].player.email;
                } else {
                    this.currentPlayer = gamePlayers[1].player.email;
                    this.oponentPlayer = gamePlayers[0].player.email;
                }
            } else {
                this.currentPlayer = gamePlayers[0].player.email;
                this.oponentPlayer = "'WAITING FOR AN OPONENT!'";
            }
        }
    },
    created: function () {
        this.getURLParams();
        this.startFetch(this.urlApiGameViewGPID + this.searchObj.gp);
    }
});