<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title></title>

    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
        google.load("jquery", "1.4.2");
    </script>

    <script type= "text/javascript" src="../../js/sorttable.js"></script>
    <script type="text/javascript" src="../../js/results.js"></script>

    <style>
        table.sortable thead {
            background-color:#eee;
            color:#666666;
            font-weight: bold;
            cursor: default;
        }
    </style>
</head>
<body>
    <h1 id="missingData">Waiting on the response...</h1>
    <h3 id="organism">Target organism: </h3>

    <table class="sortable" id="resultsTable">
        <thead>
            <tr>
                <th>ID</th>
                <th>Score</th>
                <th>1:1 Comparison</th>
            </tr>
        </thead>

        <tbody>
        </tbody>
    </table>

</body>
</html>