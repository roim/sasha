<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"> 
        <title>Sasha</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/2.2.2/css/bootstrap.min.css">
        <link rel="stylesheet" href="//rro.im/wp-content/uploads/2013/12/darkstrap.min_.css">
    </head>

    <body>
        <center>
            <h1 style="font-family:&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif;font-weight:500;line-height:1.1">Sasha</h1>
            <hr/>

            <form class="form-inline" style="width:600px">
                <fieldset>
                    <input type="text" name="q" class="input-small" placeholder="File" value="{{file}}" required="true" autofocus="true" style="height:30px;width:350px">
                    .
                    <input type="text" name="ext" class="input-small" placeholder="Extension" value="{{extension}}" style="height:30px;width:100px">
                    <button type="submit" class="btn btn-inverse" style="height:30px">Search</button>
                </fieldset>
            </form>

            % if file != "":
            <br/>
            <h1 style="font-family:&quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif;font-weight:500;line-height:1.1">Results</h1>
            <table style="width:700px" cellspacing=10 cellpadding=5 class="table table-hover well">
                <thead>
                    <tr>
                        <th>File</th>
                        <th>Path</th>
                        <th>Size</th>
                    </tr>
                </thead>
                <tbody>
                % for hit in search_hits:
                    <tr>
                        <td>{{hit["Name"]}}{{hit["Extension"]}}</td>
                        <td>{{hit["Path"]}}{{hit["Name"]}}{{hit["Extension"]}}</td>
                        <td>{{hit["Size"]/1024}} kB</td>
                    </tr>
                % end
                </tbody>
            </table>
            % end

            % if quote != "":
            <div style="width:600px">
                <p>{{quote["quote"]}}</p>
                <small>{{quote["author"]}}</small>
            </div>
            % end

        </center>
    </body>
</html>
