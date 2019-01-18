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
        loading: true,
        shipDragged: null,
        listOfShips: [{
                type: "Aircraft Carrier",
                shipLocations: []
            },
            {
                type: "Battleship",
                shipLocations: []
            },
            {
                type: "Submarine",
                shipLocations: []
            },
            {
                type: "Destroyer",
                shipLocations: []
            },
            {
                type: "Patrol Boat",
                shipLocations: []
            }
        ]
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
        getRow: function (i) {
            //The v-for starts with i=1. I want that starts with i=0.
            i--;
            return Math.trunc(i / 11);
        },
        getColumn(i) {
            //The v-for starts with i=1. I want that starts with i=0.
            i--;
            return i % 11;
        },
        getCellPosition: function (i) {
            var inRow = this.getRow(i);
            var inColumn = this.getColumn(i);
            if (inColumn == 0) {
                return this.rowNames[inRow]
            } else {
                return this.rowNames[inRow] + inColumn;
            }
        },
        isAMarginCell: function (i) {
            var cellPosition = this.getCellPosition(i);
            if (this.rowNames.includes(cellPosition) || this.columnNames.includes(cellPosition) || i == 1) {
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
                if (thereIsASalvo[0]) {
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
        areAllShipsPlaced: function () {
            var areAllShipsPlaced = true;
            for(let i=0; i<this.listOfShips.length;i++){
                if(this.listOfShips[i].shipLocations==0){
                    areAllShipsPlaced = false;
                }
            }
            return areAllShipsPlaced;
        },
        postShipsToServer: function () {
            if(this.areAllShipsPlaced()){
                fetch("/api/games/players/" + this.gamePlayersObj.gp.id + "/ships", {
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify(this.listOfShips)
                })
                .then(function (response) {
                    return response.json();
                }).then(function (myData) {
                    if (myData.error) {
                        console.log(myData.error);
                    } else {
                        console.log(myData.ok);
                        window.location.reload();
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });
            }else{
                alert("You have to place all the ships!");
            }
        },
        showImg: function (imgId) {
            document.getElementById(imgId).style.display = "block";
        },
        hideImg: function (imgId) {
            document.getElementById(imgId).style.display = "none";
        },
        flipShip: function (shipId, btnId) {
            var ship = document.getElementById(shipId);
            var btn = document.getElementById(btnId);
            var firstCell;
            var shipIsInGrid = true;
            for(let i=0; i<this.listOfShips.length; i++){
                if(this.listOfShips[i].type == ship.getAttribute("data-shipType")){
                    if(this.listOfShips[i].shipLocations.length != 0){
                        firstCell = document.querySelector("[data-cellPosition="+ this.listOfShips[i].shipLocations[0]+"]");
                    }else{
                        shipIsInGrid = false;
                    }
                    
                }
            }
            switch (ship.getAttribute("data-direction")) {
                case "H":
                    ship.setAttribute("data-direction", "V");
                    ship.className = shipId + "-" + "V";
                    btn.setAttribute("src", "styles/img/VToH.png");
                    if(shipIsInGrid){
                        let newPositions = this.getShipLocations(ship, firstCell);
                        if(!this.correctPlaced(firstCell,ship,newPositions)){
                            ship.className += " red";
                            setTimeout(()=>{
                                ship.setAttribute("data-direction", "H");
                                ship.className = shipId + "-" + "H";
                                btn.setAttribute("src", "styles/img/HToV.png");
                            },500);
                        }else{
                            for(let i=0; i<this.listOfShips.length; i++){
                                if(this.listOfShips[i].type == ship.getAttribute("data-shipType")){
                                    this.listOfShips[i].shipLocations = newPositions;
                                }
                            }
                        }
                    }
                    break;
                case "V":
                    ship.setAttribute("data-direction", "H");
                    ship.className = shipId + "-" + "H";
                    btn.setAttribute("src", "styles/img/HToV.png");
                    if(shipIsInGrid){
                        let newPositions = this.getShipLocations(ship, firstCell);
                        if(!this.correctPlaced(firstCell,ship,newPositions)){
                            ship.className += " red";
                            setTimeout(()=>{
                                ship.setAttribute("data-direction", "V");
                                ship.className = shipId + "-" + "V";
                                btn.setAttribute("src", "styles/img/VToH.png");
                            },500);
                        }else{
                            for(let i=0; i<this.listOfShips.length; i++){
                                if(this.listOfShips[i].type == ship.getAttribute("data-shipType")){
                                    this.listOfShips[i].shipLocations = newPositions;
                                }
                            }
                        }
                    }
                    break;
            }
        },
        dragStart: function (ev) {
            this.shipDragged = ev.target;
            setTimeout(() => (this.shipDragged.className = 'invisible'), 0);
        },
        dragEnter: function (ev) {
            Array.from(document.getElementsByClassName("grid-item-drop-allowed")).map(cell => {
                cell.className = "grid-item-drop"
            });
            Array.from(document.getElementsByClassName("grid-item-drop-denied")).map(cell => {
                cell.className = "grid-item-drop"
            });

            var cell = ev.target;
            var cellColumn = +cell.getAttribute("data-cellColumn");
            var cellRow = +cell.getAttribute("data-cellRow");
            var shipDirection = this.shipDragged.getAttribute("data-direction");
            var shipLength = +this.shipDragged.getAttribute("data-length");
            if (cellColumn != 0 && cellRow != 0) {
                if (this.correctPlaced(cell, this.shipDragged, this.getShipLocations(this.shipDragged, cell))) {
                    cell.setAttribute("data-drop","droppable");
                    if (shipDirection == "H") {
                        this.printCellsHoritzontal(cellRow, cellColumn, shipLength, "grid-item-drop-allowed");
                    } else {
                        this.printCellsVertical(cellRow, cellColumn, shipLength, "grid-item-drop-allowed");
                    }
                } else {
                    cell.setAttribute("data-drop","undroppable");
                    if (shipDirection == "H") {
                        this.printCellsHoritzontal(cellRow, cellColumn, shipLength, "grid-item-drop-denied");
                    } else {
                        this.printCellsVertical(cellRow, cellColumn, shipLength, "grid-item-drop-denied");
                    }
                }
            }
        },
        correctPlaced: function (cell, ship, currentLocations) {
            var shipDirection = ship.getAttribute("data-direction");
            var shipLength = +ship.getAttribute("data-length");
            if (!this.areShipsOverlap(ship.getAttribute("data-shipType"), currentLocations)){
                if (shipDirection == "H") {
                    let cellColumn = +cell.getAttribute("data-cellColumn");
                    if (cellColumn + shipLength - 1 <= 10) {
                        return true;
                    }
                } else {
                    let cellRow = +cell.getAttribute("data-cellRow");
                    if (cellRow + shipLength - 1 <= 10) {
                        return true;
                    }
                }
            }
            return false;
        },
        areShipsOverlap: function (shipType, currentLocations) {
            for(let i=0; i<currentLocations.length; i++){
                for(let j=0; j<this.listOfShips.length;j++){
                    if(shipType!=this.listOfShips[j].type && this.listOfShips[j].shipLocations.includes(currentLocations[i])){
                        return true;
                    }
                }
            }  
        },
        printCellsVertical: function (cellRow, cellColumn, shipLength, className) {
            var ini = cellRow;
            var end = cellRow + shipLength - 1;
            if (end > 10) {
                end = 10;
            }
            for (let i = ini; i <= end; i++) {
                document.querySelector("[data-cellPosition=" + this.rowNames[i] + cellColumn + "]").className = className;
            }
        },
        printCellsHoritzontal: function (cellRow, cellColumn, shipLength, className) {
            var ini = cellColumn;
            var end = cellColumn + shipLength - 1;
            if (end > 10) {
                end = 10;
            }
            for (let i = ini; i <= end; i++) {
                document.querySelector("[data-cellPosition=" + this.rowNames[cellRow] + i + "]").className = className;
            }
        },
        allowDrop: function (ev) {
            ev.preventDefault();
        },
        dragLeave: function (ev) {
            // console.log("Leave");
            // console.log(ev.target.getAttribute("data-cellPosition"));
        },
        dragDrop: function (ev) {
            ev.preventDefault();
            if (ev.target.getAttribute("data-drop") == "droppable") {
                ev.target.appendChild(document.getElementById(this.shipDragged.id));
                for(let i=0; i<this.listOfShips.length; i++){
                    if(this.listOfShips[i].type == this.shipDragged.getAttribute("data-shipType")){
                        this.listOfShips[i].shipLocations = this.getShipLocations(this.shipDragged, ev.target);
                    }
                }
            }
        },
        getShipLocations: function (ship, firstCell) {
            var shipDirection = ship.getAttribute("data-direction");
            var shipLength = +ship.getAttribute("data-length");
            var cellColumn = +firstCell.getAttribute("data-cellColumn");
            var cellRow = +firstCell.getAttribute("data-cellRow");
            var locations = [];
            if(shipDirection == "H"){
                let ini = cellColumn;
                let end = cellColumn + shipLength - 1;
                for (let i = ini; i <= end; i++) {
                    locations.push(this.rowNames[cellRow] + i);
                }
            }else{
                let ini = cellRow;
                let end = cellRow + shipLength - 1;
                for (let i = ini; i <= end; i++) {
                    locations.push(this.rowNames[i] + cellColumn);
                }
            }
            return locations;
        },
        dragEnd: function () {
            this.shipDragged.className = this.shipDragged.id + "-" + this.shipDragged.getAttribute("data-direction");
            this.shipDragged = null;
            Array.from(document.getElementsByClassName("grid-item-drop-allowed")).map(cell => {
                cell.className = "grid-item-drop"
            });
            Array.from(document.getElementsByClassName("grid-item-drop-denied")).map(cell => {
                cell.className = "grid-item-drop"
            });
        },
    },
    created: function () {
        this.getURLParams();
        this.startFetch(this.urlApiGameViewGPID + this.gamePlayersObj.gp.id);
    }
});