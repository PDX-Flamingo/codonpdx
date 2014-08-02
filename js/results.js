/**
 * Created by wes.risenmay on 7/21/14.
 */
$(document).ready(function() {
    URI = window.location.href.split("/");
    $.ajax({
        url: 'http://capstonebb.cs.pdx.edu:8080/codonpdx/results/' + URI[5],
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
                    newRow = '<tr><td>' + k + '</td><td>' + '<a href="' + url +'"</a>' + data[k][1] + '</td><td>' + data[k][2] + '</td><td>' + data[k][0] + '</td></tr>'
                    if(k != "target") {
                        $("#resultsTable").find("tbody").append(newRow)
                    }
                }
                var csvUrl = window.location.href.substring(0,window.location.href.indexOf("resultsView")) + "dlCSV/" + URI[5]
                $("#csv").append('<a href="' + csvUrl + '">Download the results</a>')
                $("#csv").show();
                $("#missingData").hide()
            }
            else {
                alert("Something went wrong")
            }
        },
        error: function() {
            alert("Something went wrong")
        }
    })


});



