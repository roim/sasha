<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"> 
        <title>Sasha</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css">
        <link rel="stylesheet" href="//rro.im/wp-content/uploads/2013/12/darkstrap.min_.css">
        <script src="//netdna.bootstrapcdn.com/bootstrap/3.0.2/js/bootstrap.min.js"></script>
    </head>

    <body>
        <center>
            <h1>Sasha</h1>
            <hr/>
            <form class="form-horizontal">
                <div class="input-group" style="width:350px;margin-bottom:3px">
                    <span class="input-group-addon" style="width:90px">
                        File name
                    </span>
                    <input class="form-control" type="text" name="q" style="height:30px">
                </div>
                <div class="input-group" style="width:350px">
                    <span class="input-group-addon" style="width:90px">
                        Extension
                    </span>
                    <input class="form-control" type="text" name="ext" style="height:30px">
                </div>
                <br/>
                <div class="input-group" style="width:130px">
                    <input class="form-control" type="submit">
                </div>
            </form>

            <br/>
            <hr/>

            <h1>Results</h1>
            <table border="1" cellspacing=10 cellpadding=5 class="zebra-striped">
                <th>File</th>
                <th>Path</th>
                <th>Size</th>
                % for hit in search_hits:
                <tr>
                    <td>{{hit["Name"]}}{{hit["Extension"]}}</td>
                    <td>{{hit["Path"]}}{{hit["Name"]}}{{hit["Extension"]}}</td>
                    <td>{{hit["Size"]/1024}} kB</td>
                </tr>
                % end
            </table>
        </center>
    </body>
</html>