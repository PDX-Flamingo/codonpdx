/**
 * Created by wes.risenmay on 7/21/14.
 */
$(document).ready(function() {
    URI = window.location.href.split("/");

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
            else if(json != null && json[URI[6]]) {
                var chartValues = [];

                var idArray = [];
                for(var id in data) {
                    idArray.push(id);
                }
                var id = idArray[0]
                $("#first").append(idArray[0])
                $("#second").append(idArray[1])
                var table = $("#table").find("tbody")

                for(var amino in data[id]) {
                    for(var triplet in data[id][amino]) {
                        if(amino != "description") {
                            chartValues.push(
                                {"triplet": amino + " " + triplet,
                                    "amino": amino,
                                    "target": data[id][amino][triplet],
                                    "other": data[idArray[1]][amino][triplet]})
                            table.append('<tr><td>' + amino + " " + triplet + '</td><td>' + data[id][amino][triplet] + '</td><td>' + data[idArray[1]][amino][triplet] + '</td></tr>')
                        }
                    }
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
                        "title": "Ratio",
                    }],
                    "startDuration": 1,
                    "graphs": [{
                        "balloonText": "[[category]] : <b> [[value]]</b>",
                        "fillAlphas": 0.9,
                        "lineAlpha": 0.2,
                        "title": idArray[0],
                        "type": "column",
                        "valueField": "target"
                    }, {
                        "balloonText": "[[category]] : <b>[[value]]</b>",
                        "fillAlphas": 0.9,
                        "lineAlpha": 0.2,
                        "title": idArray[1],
                        "type": "column",
                        "columnWidth":0.5,
                        "valueField": "other"
                    }],
                    "plotAreaFillAlphas": 0.1,
                    "categoryField": "triplet",
                    "rotate": true,
                    "categoryAxis": {
                        "gridPosition": "start"
                    }
                });
            }
            else {
                alert("Something went wrong")
            }
        },
        error: function() {
            alert("Something went wrong")
        }
    })
})