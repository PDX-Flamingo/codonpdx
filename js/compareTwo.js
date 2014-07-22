/**
 * Created by wes.risenmay on 7/21/14.
 */
var test = jQuery.parseJSON('{"NC_005816.1":{"Phe\/F":{"ttc":0.4295774647887324,"ttt":0.5704225352112676},"Gly\/G":{"ggg":0.20670391061452514,"ggt":0.20670391061452514,"ggc":0.2737430167597765,"gga":0.3128491620111732},"Gln\/Q":{"caa":0.44047619047619047,"cag":0.5595238095238095},"Ile\/I":{"ata":0.3701657458563536,"att":0.287292817679558,"atc":0.3425414364640884},"Leu\/L":{"tta":0.24381625441696114,"ttg":0.11307420494699646,"ctg":0.20848056537102475,"ctt":0.17667844522968199,"cta":0.07773851590106007,"ctc":0.18021201413427562},"Arg\/R":{"cgt":0.10181818181818182,"cgc":0.16,"cga":0.13454545454545455,"cgg":0.1709090909090909,"aga":0.27636363636363637,"agg":0.15636363636363637},"Met\/M":{"atg":1.0},"Val\/V":{"gtg":0.20625,"gtc":0.225,"gtt":0.31875,"gta":0.25},"STOP":{"tag":0.104,"tga":0.456,"taa":0.44},"description":"NC_005816","Ser\/S":{"tcg":0.13662790697674418,"agc":0.1569767441860465,"tca":0.24127906976744187,"agt":0.125,"tcc":0.1569767441860465,"tct":0.18313953488372092},"Pro\/P":{"cca":0.2670807453416149,"ccc":0.2236024844720497,"cct":0.2670807453416149,"ccg":0.2422360248447205},"His\/H":{"cac":0.44329896907216493,"cat":0.5567010309278351},"Trp\/W":{"tgg":1.0},"Thr\/T":{"acg":0.23684210526315788,"act":0.19298245614035087,"acc":0.18421052631578946,"aca":0.38596491228070173},"Tyr\/Y":{"tat":0.6282051282051282,"tac":0.3717948717948718},"Glu\/E":{"gag":0.31313131313131315,"gaa":0.6868686868686869},"Cys\/C":{"tgc":0.4838709677419355,"tgt":0.5161290322580645},"Ala\/A":{"gcc":0.23976608187134502,"gca":0.29239766081871343,"gcg":0.15789473684210525,"gct":0.30994152046783624},"Asn\/N":{"aac":0.5033112582781457,"aat":0.4966887417218543},"Asp\/D":{"gac":0.47619047619047616,"gat":0.5238095238095238},"Lys\/K":{"aaa":0.718562874251497,"aag":0.281437125748503}},"NC_008432.1":{"Phe\/F":{"ttc":0.6568627450980392,"ttt":0.3431372549019608},"Gly\/G":{"ggg":0.19662921348314608,"ggt":0.21348314606741572,"ggc":0.21910112359550563,"gga":0.3707865168539326},"Gln\/Q":{"caa":0.6457399103139013,"cag":0.3542600896860987},"Ile\/I":{"ata":0.4135514018691589,"att":0.26635514018691586,"atc":0.32009345794392524},"Leu\/L":{"tta":0.21404109589041095,"ttg":0.05821917808219178,"ctg":0.10102739726027397,"ctt":0.14554794520547945,"cta":0.3133561643835616,"ctc":0.1678082191780822},"Arg\/R":{"cgt":0.20869565217391303,"cgc":0.08260869565217391,"cga":0.14782608695652175,"cgg":0.09130434782608696,"aga":0.3,"agg":0.16956521739130434},"Met\/M":{"atg":1.0},"Val\/V":{"gtg":0.1724137931034483,"gtc":0.20689655172413793,"gtt":0.18719211822660098,"gta":0.43349753694581283},"STOP":{"tag":0.30275229357798167,"tga":0.22935779816513763,"taa":0.46788990825688076},"description":"NC_008432","Ser\/S":{"tcg":0.08333333333333333,"agc":0.14015151515151514,"tca":0.3068181818181818,"agt":0.0928030303030303,"tcc":0.23295454545454544,"tct":0.14393939393939395},"Pro\/P":{"cca":0.3090909090909091,"ccc":0.32045454545454544,"cct":0.2840909090909091,"ccg":0.08636363636363636},"His\/H":{"cac":0.504,"cat":0.496},"Trp\/W":{"tgg":1.0},"Thr\/T":{"acg":0.14012738853503184,"act":0.20169851380042464,"acc":0.2823779193205945,"aca":0.37579617834394907},"Tyr\/Y":{"tat":0.4539249146757679,"tac":0.5460750853242321},"Glu\/E":{"gag":0.3870967741935484,"gaa":0.6129032258064516},"Cys\/C":{"tgc":0.5833333333333334,"tgt":0.4166666666666667},"Ala\/A":{"gcc":0.3168724279835391,"gca":0.3497942386831276,"gcg":0.09053497942386832,"gct":0.24279835390946503},"Asn\/N":{"aac":0.5799256505576208,"aat":0.4200743494423792},"Asp\/D":{"gac":0.5384615384615384,"gat":0.46153846153846156},"Lys\/K":{"aaa":0.7159533073929961,"aag":0.2840466926070039}}}')

$(document).ready(function() {

    var data = test;
    var chartValues = [];

    var idArray = [];
    for(var id in data) {
        idArray.push(id);
    }
    var id = idArray[0]

    for(var amino in data[id]) {
        for(var triplet in data[id][amino]) {
            if(amino != "description") {
                chartValues.push(
                    {"triplet": amino + " " + triplet,
                        "amino": amino,
                        "target": data[id][amino][triplet],
                        "other": data[idArray[1]][amino][triplet]})
            }
        }
    }


    var chart = AmCharts.makeChart("chartdiv", {
        "theme": "none",
        "type": "serial",
        "dataProvider": chartValues,
        "valueAxes": [{
            //"unit": "%",
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
            //"clustered":false,
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


    /*var plot = $.jqplot('chart',
        [array], {
        seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            // Show point labels to the right ('e'ast) of each bar.
            // edgeTolerance of -15 allows labels flow outside the grid
            // up to 15 pixels.  If they flow out more than that, they
            // will be hidden.
            pointLabels: { show: true, location: 'e', edgeTolerance: -15 },
            // Rotate the bar shadow as if bar is lit from top right.
            shadowAngle: 135,
            // Here's where we tell the chart it is oriented horizontally.
            rendererOptions: {
                barDirection: 'horizontal'
            }
        },
        axes: {
            yaxis: {
                renderer: $.jqplot.CategoryAxisRenderer
            }
        }
    });*/
})