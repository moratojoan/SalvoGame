var app = new Vue({
    el: "#app",
    data: {
        gridSize: 11,
        rowNames: ["", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        columnNames: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        urlApiGameViewGPID: "/api/game_view/",
        gamePlayersObj: {
            gp: {
                id: null,
                email: null
            },
            oponent: {
                id: null,
                email: null
            }
        },
        gameData: null,
        loading: true
    },
    methods: {
        getURLParams: function () {
            // var obj = {};
            // var search = location.search.substr(1);
            // search = search.split("&");
            // for (let i = 0; i < search.length; i++) {
            //     let subSearch = search[i].split("=");
            //     if (!obj[subSearch[0]]) {
            //         obj[subSearch[0]] = {
            //             id: subSearch[1] * 1
            //         };
            //     }
            // }
            // this.gamePlayersObj = obj;
            // console.log(this.gamePlayersObj);

            var parsedUrl = new URL(window.location.href);
            this.gamePlayersObj.gp.id = parsedUrl.searchParams.get("gp") * 1;
        },
        startFetch: function (url) {
            fetch(url, {
                    method: "GET"
                }).then(response => response.json())
                .then(myData => {
                    if (myData.error) {
                        console.log(myData.error);
                        alert(myData.error);
                        history.back();
                    } else {
                        this.gameData = myData;
                        console.log(myData);
                        this.fillGamePlayersObj();
                        this.loading = false;
                    }
                })
                .catch(error => {
                    console.log('Request failure: ', error);
                });
        },
        logout: function () {
            fetch("/api/logout", {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                })
                .then(response => {
                    console.log('Request success: ', response);
                    if (response.ok) {
                        this.goToHomePage();
                    }
                })
                .catch(error => {
                    console.log('Request failure: ', error);
                });
        },
        goToHomePage: function () {
            window.location.href = "games.html";
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
        isAMarginCell: function(i){
            var cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition) || i==1) {
                return true;
            }
        },
        thereIsAShip: function (i) {
            var cellPosition = this.getCellPosition(i);
            var ships = this.gameData.ships;
            for (let j = 0; j < ships.length; j++) {
                if (ships[j].locations.includes(cellPosition)) {
                    return true;
                }
            }
            return false;
        },
        thereIsASalvoOponent: function (i) {
            var salvoes = this.gameData.salvoes[this.gamePlayersObj.oponent.id];
            var cellPosition = this.getCellPosition(i);
            for (key in salvoes) {
                if (salvoes[key].includes(cellPosition)) {
                    return [true, key];
                }
            }
            return [false];
        },
        thereIsASalvoGP: function (i) {
            var salvoes = this.gameData.salvoes[this.gamePlayersObj.gp.id];
            var cellPosition = this.getCellPosition(i);
            for (key in salvoes) {
                if (salvoes[key].includes(cellPosition)) {
                    return [true, key];
                }
            }
            return [false];
        },
        getCellNameShips: function (i) {
            var cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition)) {
                return cellPosition;
            } else {
                var thereIsASalvo = this.thereIsASalvoOponent(i);
                if (thereIsASalvo[0] && this.thereIsAShip(i)) {
                    return thereIsASalvo[1];
                }
            }
            return null;
        },
        getCellNameSalvoes: function (i) {
            var cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition)) {
                return cellPosition;
            } else {
                var thereIsASalvo = this.thereIsASalvoGP(i);
                if (thereIsASalvo[0]) {
                    return thereIsASalvo[1];
                }
            }
            return null;
        },
        fillGamePlayersObj: function () {
            var gamePlayers = this.gameData.gamePlayers;
            if (gamePlayers.length == 2) {
                if (gamePlayers[0].id == this.gamePlayersObj.gp.id) {
                    this.gamePlayersObj.gp.email = gamePlayers[0].player.email;
                    this.gamePlayersObj.oponent.id = gamePlayers[1].id;
                    this.gamePlayersObj.oponent.email = gamePlayers[1].player.email;
                } else {
                    this.gamePlayersObj.gp.email = gamePlayers[1].player.email;
                    this.gamePlayersObj.oponent.id = gamePlayers[0].id;
                    this.gamePlayersObj.oponent.email = gamePlayers[0].player.email;
                }
            } else {
                this.gamePlayersObj.gp.email = gamePlayers[0].player.email;
                this.gamePlayersObj.oponent.email = "'WAITING FOR AN OPONENT!'";
            }
        },
        postShipsToServer: function () {
            var listOfShips = [{
                    "type": "destroyer",
                    "shipLocations": ["A1", "B1", "C1"]
                },
                {
                    "type": "patrol boat",
                    "shipLocations": ["H5", "H6"]
                }
            ];

            fetch("/api/games/players/" + this.gamePlayersObj.gp.id + "/ships", {
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify(listOfShips)
                })
                .then(function (response) {
                    return response.json();
                }).then(function (myData) {
                    if (myData.error) {
                        console.log(myData.error);
                    } else {
                        console.log(myData.ok);
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });
        },
        showImg: function (imgId) {
            document.getElementById(imgId).style.display = "block";
        },
        hideImg: function (imgId) {
            document.getElementById(imgId).style.display = "none";
        },
        flipShip: function(divId, imgId) {
            var div = document.getElementById(divId);
            var img = document.getElementById(imgId);
            switch (div.getAttribute("data-direction")) {
                case "H":
                    div.setAttribute("data-direction", "V");
                    div.className = divId + "-" + "V";
                    img.setAttribute("src", "styles/img/VToH.png");
                    break;
                case "V":
                    div.setAttribute("data-direction", "H");
                    div.className = divId + "-" + "H";
                    img.setAttribute("src", "styles/img/HToV.png");
                    break;
            }
        },
        dragStart: function (ev) {
            ev.dataTransfer.setData("text", ev.target.id);
            setTimeout(() => (document.getElementById(ev.target.id).className = 'invisible'), 0);
        },
        dragEnd: function (id) {
            document.getElementById(id).className = document.getElementById(id).id + "-" + document.getElementById(id).getAttribute("data-direction");
        },
        allowDrop: function (ev) {
            ev.preventDefault();
        },
        dragDrop: function(ev) {
            ev.preventDefault();
            console.log(ev.target);
            console.log(ev.target.getAttribute("data-drop"));
            if (ev.target.getAttribute("data-drop") == "dropable") {
                var data = ev.dataTransfer.getData("text");
                document.getElementById(data).className = document.getElementById(data).id;
                ev.target.appendChild(document.getElementById(data));
            }
        }
    },
    created: function () {
        this.getURLParams();
        this.startFetch(this.urlApiGameViewGPID + this.gamePlayersObj.gp.id);
    }
});