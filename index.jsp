<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Sequence Input</title>
  
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  <script type="text/javascript">
    google.load("jquery", "1.4.2");
  </script>
  <script type="text/javascript" src="js/simpletabs_1.3.js"></script>
  <style type="text/css">
    @import "css/simpletabs.css";
  </style>
  
  <script type="text/javascript" src="js/mainpage.js"></script>
</head>

<body>
  <h2>Codon Analysis</h2>
 
  <div class="simpleTabs">
    <ul class="simpleTabsNavigation">
      <li><a href="#">Upload a File</a></li>
      <li><a href="#">Input Sequence</a></li>
    </ul>
    <div class="simpleTabsContent">
      Codon Sequence: <input type="file" id="sequenceFile"><br>
      File Format: 
      <select id="fileType">
        <option value="FASTA">FASTA</option>
	<option value="GenBank">GenBank</option>
	<option value="Plain Text">Plain Text</option>
      </select>
    </div>
    <div class="simpleTabsContent">
      Name: <input type="text" id="sequenceName"> <br>
      Codon Sequence: <input type="text" id="sequenceText"><br>
    </div>
  </div><br>

  <div id="generalInput">
    Comparison Host: 
    <select id="comparison">
      <option value="Custom List">Custom List</option>
      <option value="GenBank">GenBank</option>
      <option value="RefSeq" selected="selected">RefSeq</option>
    </select><br>
    <div id="customList" style="display:none">
      <table>
        <tbody>
          <tr>
            <td>
	          Species:<input type="text" id="speciesInput"/>
              Input Type:
	          <select id="speciesInputType">
                <option value="Organism Name">Organism Name</option>
	            <option value="Taxonomy Id">Taxonomy Id</option>
              </select>
              <button type="button" id="add">Add New Row</button>
              <button type="button" id="remove" style="display:none">Remove</button>
            </td>
          </tr>  
        </tbody>
      </table>
    </div>
    <button type="button" id="submitRequest" disabled>Submit Request</button>
  </div> 
</body>
</html>