/**
 * Created by wes.risenmay on 7/21/14.
 */
$(document).ready(function() {
    URI = window.location.href.split("/");
    $("#compareChecked").click(function() {
        var rows = $("#resultsTable").find("tr :checked");
        var toCompare = [];
        $.each(rows, function(index, value) {
            toCompare.push($($(value).parent().parent().find("td")[0]).text())
        });

        var attachedString = "";
        $.each(toCompare, function(index, value) {
            if(index > 0) {
                attachedString += "&&&" + value;
            }
            else {
                attachedString += value;
            }
        })

        window.location.replace(window.location.href + attachedString);
    });
    $.ajax({
        url: '/codonpdx/results/' + URI[5],
        type: 'GET',
        success: function(response) {
            var json = jQuery.parseJSON(response)

            if(json == null) {
                json = response
            }

            if(response.Error) {
                alert(response.Error)
            }
            else if(json != null && json["target"]) {
                var data = json;
                var UUID = URI[5];

                $("#organism").append(data["target"])

                for (var k in data){
                    url = window.location.href + "/" + k
                    newRow = '<tr><td>' + k + '</td><td>' + '<a href="' + url +'"</a>' + data[k][1] + '</td><td>' + data[k][2] + '</td><td>' + data[k][0] + '</td><td><input type="checkbox" id="compare"></td></tr>'
                    if(k != "target") {
                        $("#resultsTable").find("tbody").append(newRow)
                    }
                }
                var csvUrl = window.location.href.substring(0,window.location.href.indexOf("resultsView")) + "dlCSV/" + URI[5]
                $("#csv").append('<a href="' + csvUrl + '">Download the results</a>')
                $("#csv").show();
                $("#missingData").hide()
                $("#compareChecked").removeAttr("disabled")
            }
            else {
                alert("The provided UUID in the url does not have corresponding results on the server.")
            }
        },
        error: function() {
            alert("The server returned an error on this request. Make sure your UUID is correct and refresh.")
        }
    })


});



