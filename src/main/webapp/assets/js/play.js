/**
 * @author Sujith T
 * 
 * <!In God We Trust>
 */
$(document).ready(function() {

    //remove start ticker
    $(".ticker").text("");
    $(".ticker").removeClass("ticker");
    
    //this is to keep default checked
    if(selectedBox != "") {
        $("input[value='" + selectedBox + "']").attr("checked", "true");
        var indicator = $("input[value='" + selectedBox + "']").parent().parent().find(".indicator");
        $(indicator).addClass("ticker");
        $(indicator).text("START");
    }
    
    $("#player_area").find("input[name='squareId']").attr("disabled", true);
    $("#opponent_area").find("input[name='squareId']").attr("disabled", true);

    if(isMyTurn) {
        $("input[name='btnplay']").removeAttr("disabled");
        $("input[name='btnplay']").show();
    } else {
        $("input[name='btnplay']").attr("disabled", true);
        $("input[name='btnplay']").hide();
    }
    
    //if current players turn + no start square enable only their player_area squares
    if(isMyTurn && startSquare == '') {
        $("#player_area").find("input[name='squareId']").removeAttr("disabled");
    }
    
    //if current players turn + square is already selected enable only that square
    if(isMyTurn && startSquare != '') {
        $("#player_area").find("input[name='squareId'][checked='checked']").removeAttr("disabled");
        $("#opponent_area").find("input[name='squareId'][checked='checked']").removeAttr("disabled");
    }
    
    //when any 0 values there not allowing user to make it as a selection
    $(".spot").each(function(inx, ele) {
        if($(ele).text() == "0") {
            $(ele).parent().parent().find("input[name='squareId']").attr("disabled", true);
        }
    }); 
    
    //the game is won
    if(winner != "") {
        $("input[name='btnrematch']").removeAttr("disabled");
        $("input[name='btnrematch']").show();
        $("input[name='btndecline']").attr("disabled", true);
        $("input[name='btndecline']").hide();        
    } else {
        $("input[name='btnrematch']").attr("disabled", true);
        $("input[name='btnrematch']").hide();
        $("input[name='btndecline']").removeAttr("disabled");
        $("input[name='btndecline']").show();        
    }
});