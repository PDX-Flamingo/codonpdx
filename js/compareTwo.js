/**
 * Created by wes.risenmay on 7/21/14.
 */
$(document).ready(function() {
    URI = window.location.href.split("/");
    var tempChart;
    $("#scroll").click(function() {
        $('html, body').animate({
            scrollTop: $("#titleToScrollTo").offset().top
        }, 500);
    });
    $("#scrollUp").click(function() {
        $('html, body').animate({
            scrollTop: $("#scroll").offset().top
        }, 500);
    });
    $("#printerFriendly").click(function() {
       $("#chartdiv").width("511pt")
       tempChart.validateNow();
    });


    $.ajax({
        url: 'http://capstonebb.cs.pdx.edu:8080/codonpdx/results/' + URI[5] + "/" + URI[6],
        type: 'GET',
        success: function(response) {
            var json = jQuery.parseJSON(response)

            if(json == null) {
                json = response
            }

            data = json
            if(response.Error) {
                alert(response.Error)
            }
            else if(json != null) {
                var chartValues = [];

                var idArray = [];
                for(var id in data) {
                    if(id != URI[5]) {
                        idArray.push(id);
                    }
                }
                var id = URI[5]
                $('#triplet').parent().append("<th class =\"sorttable_numeric\" id=\"" + id + "\"></th>")
                $('#' + id).append("Input Sequence")
                for(var index in idArray) {
                    $('#triplet').parent().append("<th class =\"sorttable_numeric\" id=\"" + idArray[index].replace(/\./g, '') + "\"></th>")
                    $('#' + idArray[index].replace(/\./g, '')).append(idArray[index])
                }
                var table = $("#table").find("tbody")

                for(var amino in data[id]) {
                    for(var triplet in data[id][amino]) {
                        if(amino != "description") {
                            tableString = '<tr><td>' + amino + " " + triplet + '</td><td>' + data[id][amino][triplet] + '</td>'
                            chartValuesObject = {
                                           "triplet": amino + " " + triplet,
                                           "amino": amino,
                                           "target": data[id][amino][triplet],
                                           }
                            for(var index in idArray) {
                                chartValuesObject["other" + index] = data[idArray[index]][amino][triplet]
                                tableString += '<td>' + data[idArray[index]][amino][triplet] + '</td>'
                            }
                            chartValues.push(chartValuesObject)
                            tableString += '</tr>'
                            table.append(tableString)
                        }
                    }
                }

                sorttable.makeSortable(document.getElementById("table"));

                var graphItems = []
                graphItems.push({
                                  "balloonText": "[[category]] : <b> [[value]]</b>",
                                  "fillAlphas": 0.9,
                                  "lineAlpha": 0.2,
                                  "title": "Input sequence",
                                  "type": "column",
                                  "valueField": "target"
                                })

                for(var index in idArray) {
                    graphItems.push({
                                  "balloonText": "[[category]] : <b> [[value]]</b>",
                                  "fillAlphas": 0.9,
                                  "lineAlpha": 0.2,
                                  "title": idArray[index],
                                  "type": "column",
                                  "columnWidth":0.5,
                                  "valueField": "other" + index
                    })
                }

                var chart = AmCharts.makeChart("chartdiv", {
                    "title": "title",
                    "theme": "none",
                    "type": "serial",
                    "dataProvider": chartValues,
                    "legend": {
                        "markerType": "circle",
                        "position": "top",
                        "marginRight": 80,
                        "autoMargins": false
                    },
                    "valueAxes": [{
                        "position": "top",
                        "title": "Ratio"
                    }],
                    "startDuration": 1,
                    "graphs": graphItems,
                    "plotAreaFillAlphas": 0.1,
                    "categoryField": "triplet",
                    "rotate": true,
                    "categoryAxis": {
                        "gridPosition": "start"
                    }
                });
                tempChart = chart;
            }
            else {
                alert("The provided UUID in the url does not have corresponding results on the server.")
            }
        },
        error: function() {
            alert("The server returned an error on this request. Make sure your UUID is correct and refresh.")
        }
    })
})