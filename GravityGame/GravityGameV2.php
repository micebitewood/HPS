<html>
    <head>
        <title>Dr Ecco</title>
        <link href="../../style.css" rel="stylesheet" type="text/css" media="screen" />
        <meta http-equiv="Cache-control" content="no-cache" />
        <meta charset="UTF-8">
    </head>
    <div class="post">
        <h2 class="title"><a href="#">Gravity Game V2</a></h2>
    </div>
    <SCRIPT>
        function theWinner(){
            if(document.gravity.getWinner() != "unknown"){
                top.document.location = "index.php?task=GravityGameV2&winner="+document.gravity.getWinner()+"&ws="+document.gravity.getWinnerScore();
            }else{
                alert ("the winner is not known !");
            }
        }
    </SCRIPT>
    <p><u><b>Instructions</b></u></p>
    <p>Insert instructions here</p>
    <center>
    <FORM name ="gameWinner">
        <input type="button" value="Save my score" onClick="theWinner()">
    </FORM>
    <applet name="gravity" archive="GravityGameV2.jar" CODE="GravityGameApplet.class" WIDTH="751" HEIGHT="525" MAYSCRIPT="mayscript">
    Java Error
    </applet>
    </center>
    <br /><br />
<!-- display the last and best scores -->
<!-- open the connection with the dabatase -->
    <?php
        // functions.php in case of an opening in the same window
        // ../../functions.php in case of an opening in a new window
        include 'functions.php';
        getScores("GravityGameV2");
        ?>
</html>
