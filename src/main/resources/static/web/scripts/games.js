var app = new Vue({
    el: "#app",
    data:{
        urlApiGames: "/api/games",
        gamesData: null
    },
    methods:{
        startFetch: function(url){
            fetch(url,{
                method: "GET"
            }).then(response => response.json())
            .then(myData => {
                this.gamesData=myData;
                console.log(myData);
            });
        }
    },
    created: function(){
        this.startFetch(this.urlApiGames);
    }

});