
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
  validateFormsNotEmpty()
  bindValidate()
});

function removeFromCustomList() {
  $(this).parent().parent().remove()
  bindValidate()
  validateFormsNotEmpty()
}

function validateCustomListButtons() {
  customList = $("#customList")
  if($(customList).find("td").length == 1) {
    $(customList).find("#remove").hide()
    $(customList).find("#add").show()
  }
 
  $(customList).find("tr:last").find("#add").show()
}

function addToCustomList() {
  newRow = '<tr><td>Species:<input type="text" id="speciesInput"/>\
             Input Type:\
             <select>\
               <option value="Organism Name">Organism Name</option>\
               <option value="Taxonomy Id">Taxonomy Id</option>\
             </select>\
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

function bindValidate() {
  $("#remove").click(validateFormsNotEmpty)
  $("#add").click(validateFormsNotEmpty)
  $("#sequenceName").bind("change keyup input paste", validateFormsNotEmpty)
  $("#sequenceText").bind("change keyup input paste", validateFormsNotEmpty)
  $("#sequenceFile").bind("change keyup input paste", validateFormsNotEmpty)
  $("#speciesInput").bind("change keyup input paste", validateFormsNotEmpty)
  rows = $("#customList").find("tbody").children().children()
  $.each(rows, function(index, value) {
    $(value).find("#speciesInput").bind("change keyup input paste", validateFormsNotEmpty)
  })
  $("#comparison").bind("change keyup input paste", validateFormsNotEmpty)
  $(".simpleTabsNavigation").bind("click change keyup input paste",validateFormsNotEmpty)
}

function validateFormsNotEmpty() {
  valid = true
  tabs = $(".simpleTabs").find("a")
  if(tabs[0].className.indexOf("current") != -1) {
    valid = valid && ($("#sequenceFile").val() != "")
  }
  else {
    valid = valid && ($("#sequenceText").val() != "") && ($("#sequenceName").val() != "")
  }

  if($("#customList").is(":visible")) {
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

