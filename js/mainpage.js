//Page setup
var customListDictionary = {};

$(document).ready(function() {
  $("#comparison").change(function() {
    if($("#comparison").val() == "Custom List") {
      $("#customList").show()
      validateFormsNotEmpty()
    }
    else {
      $("#customList").hide() 
      validateFormsNotEmpty()
    }
  })

  $("#customList").find("#add").click(addToCustomList)
  $("#customList").find("#add").click(validateCustomListButtons)
  $("#submitRequest").click(submitRequest)
  validateFormsNotEmpty()
  bindValidate()
});

//Submit request to server
function submitRequest() {
    data = new FormData()

    if(isUsingCustomList()) {
        var customList = []
        rows = $("#customList").find("tbody").children().children()
        $.each(rows, function(index, value) {
            val = $(value).find("#speciesInput").val()
            if(customListDictionary[val]) {
                customList.push(customListDictionary[val])
            }
            else {
                customList.push(val)
            }
        })
        data.append('customList', customList)
    }
    else {
        data.append('comparisonHost', $("#comparison").val())
    }

    if(isUploadingFile()) {
        data.append('file', $("#sequenceFile")[0].files[0])
        data.append('fileType', $("#fileType").val())
    }
    else {
        data.append('sequenceName', $("#sequenceName").val())
        data.append('sequenceText', $("#sequenceText").val())
    }

    $.ajax({
        url: '/codonpdx/submitRequest', //Need to find out what this is
        data: data,
        processData: false,
        contentType: false,
        type: 'POST',
        success: function(response) {
            if(response["UUID"]) {
                window.location.href = "resultsView/" + response["UUID"]; //Need to find out what this is as well
            }
            else {
                alert("Something went wrong") //Need to make this better
            }
        },
        error: function() {
            alert("Something went wrong") //Need to make this better
        }
    })
}

//Remove a row from the custom list
function removeFromCustomList() {
  $(this).parent().parent().remove()
  bindValidate()
  validateFormsNotEmpty()
}

//Make sure there are add and remove buttons showing where they need to be
function validateCustomListButtons() {
  customList = $("#customList")
  if($(customList).find("td").length == 1) {
    $(customList).find("#remove").hide()
    $(customList).find("#add").show()
  }
 
  $(customList).find("tr:last").find("#add").show()
}

//Add something to the custom list and bind buttons
function addToCustomList() {
  newRow = '<tr><td>Species:<input type="text" id="speciesInput"/>\
             <button type="button" id="add">Add New Row</button>\
             <button type="button" id="remove">Remove</button></td></tr>'
  $(this).hide()
  $(this).parent().find("#remove").show()
  $(this).parent().find("#remove").click(removeFromCustomList)
  $(this).parent().find("#remove").click(validateCustomListButtons)
  $('#customList').find('tbody').append(newRow)
  $("#customList tr:last").find("#add").click(addToCustomList)
  $("#customList tr:last").find("#remove").click(removeFromCustomList)
  $("#customList tr:last").find("#add").click(validateCustomListButtons)
  $("#customList tr:last").find("#remove").click(validateCustomListButtons)
  bindValidate()
  validateFormsNotEmpty()
}

//Bind all changeable fields so that when they are changed the forms are validated
function bindValidate() {
  $("#remove").click(validateFormsNotEmpty)
  $("#add").click(validateFormsNotEmpty)
  $("#sequenceName").bind("change keyup input paste", validateFormsNotEmpty)
  $("#sequenceText").bind("change keyup input paste", validateFormsNotEmpty)
  $("#sequenceFile").bind("change keyup input paste", validateFormsNotEmpty)
  $("#speciesInput").bind("change keyup input paste", validateFormsNotEmpty)
  rows = $("#customList").find("tbody").children().children()
  $.each(rows, function(index, value) {
      $(value).find("#speciesInput").unbind("change keyup input paste");
      $(value).find("#speciesInput").bind("change keyup input paste", validateFormsNotEmpty)
      $(value).find("#speciesInput").bind("keyup", autoCompleteText)
      $(value).find("#speciesInput").autocomplete({
          source: []
      });
  })
  $("#comparison").bind("change keyup input paste", validateFormsNotEmpty)
  $(".simpleTabsNavigation").bind("click change keyup input paste",validateFormsNotEmpty)
}

//Returns true if custom list is selected
function isUsingCustomList() {
  return $("#customList").is(":visible")
}

//Returns true if the file upload tab is selected
function isUploadingFile() {
  return $(".simpleTabs").find("a")[0].className.indexOf("current") != -1
}

//Make sure that no fields that should not be empty are empty
function validateFormsNotEmpty() {
    valid = true
    tabs = $(".simpleTabs").find("a")
    if(isUploadingFile()) {
        valid = valid && ($("#sequenceFile").val() != "")
    }
    else {
        valid = valid && ($("#sequenceText").val() != "") && ($("#sequenceName").val() != "")
    }

    if(isUsingCustomList()) {
        rows = $("#customList").find("tbody").children().children()
        $.each(rows, function(index, value) {
            valid = valid && ($(value).find("#speciesInput").val() != "")
        })
    }

    if(valid)
        $("#submitRequest").removeAttr("disabled")
    else
        $("#submitRequest").attr('disabled', 'disabled')
}

function autoCompleteText() {
    $.ajax({
        url: '/codonpdx/list/' + $(this).val(),
        context: this,
        type: 'GET',
        success: function(response) {
            if(response && response.list) {
                var autoCompleteArray = [];
                for(var index in response.list) {
                    assession = response.list[index][0]
                    description = response.list[index][1]
                    autoCompleteArray.push(assession)
                    autoCompleteArray.push(description)
                    customListDictionary[description] =  assession
                }
                $( this ).autocomplete({
                    source: autoCompleteArray
                });
            }
            else {
            }
        },
        error: function() {
        }
    })
}
