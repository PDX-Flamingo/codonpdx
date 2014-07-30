/**
 * Created by wes.risenmay on 7/21/14.
 */

var test = {
    "NR_043598.1":6.09097567733726
    ,"NG_033251.1":2.1765233598378306,
    "NC_013761.1":4.3301469436537685,
    "NG_008148.1":2.1104698740077588,
    "NW_006918865.1":5.787050782311936,"NR_114413.1":6.087545230232171,"NC_014271.1":5.68757048054799,"NR_026117.1":7.2280106976842475,"NR_044562.1":7.566447881409501,
    "NW_006918297.1":3.3938032656816413,"NW_006917867.1":6.851664503814195,"NC_015808.1":5.933917434610158,"NR_029360.1":6.082971362261912,"NR_028988.1":9.571779302457843,
    "NR_112344.1":9.015887784279997,"NC_003477.1":5.703578348895861,"NW_006917797.1":7.650812390058489,"NC_004071.1":6.219567514161194,"NR_104950.1":7.731490383259447,
    "NW_006918917.1":4.360513900237879,"NC_020498.1":6.5191297739030745,"NW_006917729.1":6.534396017918776,"NR_119821.1":6.986398501883604,"NR_111799.1":8.645642615615008,
    "NW_006918330.1":4.791066743577456,"NC_001956.1":5.506982491816964,"NR_120167.1":9.614608932343835,"NC_010348.1":6.487877345511952,"NR_027209.1":8.073786151369598,
    "NR_119639.1":6.35345955384954,"NC_014714.1":10.455954066295535,"NC_008141.1":9.702240902852198,"NG_032011.2":1.132350731987886,"NC_020745.1":4.19148544452781,
    "NR_120102.1":8.644762935479683,"NC_003612.1":14.530319989450595,"NR_040902.1":7.11846810498366,"NW_006920364.1":6.095097286704065,"NC_002762.1":4.665542717252189,
    "NR_041092.1":8.55543699158854,"NG_023325.1":1.9322282816847371,"NW_006919303.1":9.839180040326317, "target": "NC_005816.1"};

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
                    newRow = '<tr><td>' + k + '</td><td>' + data[k] + '</td><td><a href="' + url +'">' + k + '</a></td></tr>'
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



