<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../components/emxComponentsCommonUtilAppInclude.inc"%>
<%--@include file = "../emxJSValidation.inc" --%>
<%--@include file = "../common/emxUIConstantsInclude.inc"--%>
<%--@include file = "../emxTagLibInclude.inc"--%>
<%--@include file = "emxComponentsNoCache.inc"--%>

<%@ page import="org.w3c.dom.*"%>
<%@ page import="java.io.File"%>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@ page import="javax.xml.parsers.DocumentBuilder"%>

<%@ page import="org.xml.sax.SAXException"%>
<%@ page import="org.xml.sax.SAXParseException"%>

<link rel="stylesheet" href="../common/styles/emxUIDefault.css" type="text/css" />
<!-- <link rel="stylesheet" href="../common/styles/emxUIForm.css" type="text/css" />
<!-- <link rel="stylesheet" href="../common/styles/emxUIList.css" type="text/css" /> -->

<script language="javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="text/javascript" src="../common/scripts/jquery-latest.js"></script> 
<!-- <script src="jquery-3.6.0.min.js"></script> -->
<!-- <script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script> 
<!-- <script language="javascript" type="text/javascript" src="../common/scripts/emxUICalendar.js"></script>  -->
<!-- <script language="javascript" type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<!-- <script language="javascript" src="../common/scripts/emxUIConstants.js"></script> -->

<script language="javascript">
  //method to send all Search params to Table
  function queryParams() { 
		//alert("In queryParams");
	  var vTypePattern = document.getElementById("typepattern").value;
	  
	  var vEleSystem= document.querySelector("#System");
	  var vSystem = "";
	  if(vEleSystem != null) {
		var vSystem = document.getElementById("System").value;
		if(vSystem.includes("|"))
			vTypePattern=vTypePattern.replaceAll("|", ",");	
	  } 
	  console.log(":: vSystem :: "+vSystem);
	  
	  
	//Added for ABB :: Display Names :: START	
	vTypePattern = document.getElementById("typenameactualid").value
	//console.log("new:: vTypePattern :: "+vTypePattern);
	
	var vTypeDisplayPattern = document.getElementById("typepattern").value;
	if(vTypePattern == "*" && vTypeDisplayPattern != "*") {
		//if(vTypeDisplayPattern.contains("*")) 
			vTypePattern = vTypeDisplayPattern;
			console.log("vTypePattern = "+vTypePattern);
		//else if(!vTypeDisplayPattern.contains("ABB")) {
		//	var tempStr = "ABB "+vTypeDisplayPattern;			
		//}
	}	
	//Added for ABB :: Display Names :: END
	
	//Added for ABB : State : Start
	var vState = document.getElementById("statenameactualid").value;
	console.log("sep 30::  vState = "+vState);	
	//Added for ABB : State : End
	  
	  
	 // alert(vTypePattern);
	  if(vTypePattern == "") {
		  alert("please update type field..");
		  return;
	  }
	
	// fetch configured toolbar and table
	var toolbar = "ABBRealTimeSearchToolbar";
	var table = "AEFGeneralSearchResults";
	var tableheader = "Generic Search Results";
	//alert(vTypePattern);
	//fetching respective table and toolbar based on type pattern 
	if(!vTypePattern.includes("*"))
	{
		if(vTypePattern.includes("|"))
			vTypePattern=vTypePattern.replaceAll("|", ",");
		var strURL = "../abbsupportcentral/ABBSearchUtil.jsp?mode=fetchTableAndToolbar&typepattern="+vTypePattern;
		var xmlhttp1;
		if (window.XMLHttpRequest) {// Mozilla/Safari
			xmlhttp1 = new XMLHttpRequest();
		} else if (window.ActiveXObject) {// IE
			xmlhttp1 = new ActiveXObject("Microsoft.XMLHTTP");
		} else {
			alert('Sorry, your browser does not support XML HTTP Request!');
		}		
		xmlhttp1.open("GET", strURL, false);
		xmlhttp1.setRequestHeader("Content-Type", "text");
		xmlhttp1.send(null);
		if ((xmlhttp1.readyState == 4 || xmlhttp1.readyState == "complete" ))
		{
			var responseText = xmlhttp1.responseText;			
			if(!(responseText == "" || responseText == null)) {
				if(responseText.includes("|")) {
					var vArrResponse = responseText.split("|");
					table = (vArrResponse[0].split(":"))[1];
					console.log("table :: "+table);
					toolbar = (vArrResponse[1].split(":"))[1];
					console.log("toolbar :: "+toolbar);
					tableheader = (vArrResponse[2].split(":"))[1];
				}
			}				
		}
	}
	
	//highestrevision 
	var isHighestRevision;
	var rev_chkbox = document.getElementById('rev_chkbox');
	if(rev_chkbox.checked == true)
	  isHighestRevision = "true";
	else
	  isHighestRevision = "false";
	  
	  var vNamePattern = document.getElementById("namepattern").value;
	  var vRevPattern = document.getElementById("revision").value;
	  var vOwnerPattern = document.getElementById("originator").value;
	  var vOriginated = document.getElementById("date_filter").value;
	  var vObjLimit = document.getElementById("limit2").value;
	  vTypePattern = vTypePattern.replaceAll("|",",");
	  vOwnerPattern = vOwnerPattern.replaceAll("|",",");
	  if(vOriginated.includes(" "))
		  vOriginated = vOriginated.replaceAll(" ", "");
	  
	  //do not modify below variable
	  //const "&" = "-AMP-";
	  
	  var vTableURL="../common/emxIndentedTable.jsp?table="+table+"&"+"program=ABBSearchutil:processFormParams"+"&"+"type="+vTypePattern+"&"+"name="+vNamePattern+"&"+"revision="+vRevPattern+"&"+"owner="+vOwnerPattern+"&"+"date="+vOriginated+"&"+"limit="+vObjLimit+"&"+"toolbar="+toolbar+"&"+"selection=multiple"+"&"+"header="+tableheader;
	  //alert(vTableURL);
	  //process range attribute fields and framing a URL paramter 
	  var vRangeAttrURL = "";
	  const vAttrValueSeperator = ":";
	  const vAttrAttrSeperator = "|";
	  const vRangeSeperator = "-RR-";	  
	  var vRangeAttributes = document.querySelectorAll('#RangeAttributes > div');	  
	  //alert("vRangeAttributes==>"+vRangeAttributes);
	  if(vRangeAttributes != null)
	  {
		  vRangeAttrURL += "&"+"rangeattributes=" ;
		  for(var j = 0; j < vRangeAttributes.length; j++) 
		  { //alert("In for loop");
			  if(j>0) {
				vRangeAttrURL += ",";
			  }		  
			  var vRangeContentID = vRangeAttributes[j].id;
			 // alert("vRangeContentID==>"+vRangeContentID);
			  var vArrRangeContentFieldIDs = document.querySelectorAll("#"+vRangeContentID+" > input");
			 // alert("#"+vRangeContentID+" > input");
			//  alert("vArrRangeContentFieldIDs==>"+vArrRangeContentFieldIDs);
			  vRangeContentID += "~";
			  var vStorageRangeAttr = "";
			  if(vArrRangeContentFieldIDs != null)
			  {
				  for(var k = 0; k < vArrRangeContentFieldIDs.length; k++)
				  {
					  var vRangeAttrInfo = "";
					  var vRangeContentsFieldId = vArrRangeContentFieldIDs[k].id;
					 // alert("vRangeContentsFieldId==>"+vRangeContentsFieldValue);
					  var vRangeContentsFieldValue = document.getElementById(vRangeContentsFieldId).value;
					//  alert("vRangeContentsFieldValue==>"+vArrRangeContentFieldIDs);
					  if(vRangeContentsFieldValue != "" && vRangeContentsFieldValue != null)
					  {
							var vExtractAttrFromID = (vRangeContentsFieldId.split("_"))[1];
							//console.log("vExtractAttrFromID : "+vExtractAttrFromID);						
							if(vRangeContentsFieldValue.includes("|"))
								vRangeContentsFieldValue = vRangeContentsFieldValue.replaceAll("|",vRangeSeperator);							
							vRangeAttrInfo += vExtractAttrFromID + vAttrValueSeperator + vRangeContentsFieldValue;
							//alert("vRangeAttrInfo==>"+vRangeAttrInfo);
							//console.log("vRangeAttrInfo : "+vRangeAttrInfo);
					  }
					  if(vRangeAttrInfo != "") 
					  {
						  if(vStorageRangeAttr != "")
							  vStorageRangeAttr += vAttrAttrSeperator + vRangeAttrInfo;
						  else
							  vStorageRangeAttr += vRangeAttrInfo;
						  
					  }
				  }
				  vRangeContentID += vStorageRangeAttr;				  
			  }
			  vRangeAttrURL += vRangeContentID;
		  }
	  }	  
	  //console.log("final vRangeAttrURL = "+vRangeAttrURL);
	  
	  vTableURL += vRangeAttrURL;	  	  
	  vTableURL += "&"+"dateoption="+document.getElementById('datefilters_id').value;	  
	  vTableURL += "&" + "highestrevisionchecked="+isHighestRevision;
	  
	  
	  //adding system param in table url
	  if(vSystem != null) {
		vTableURL += "&" + "system="+vSystem;
	  }

	  //adding state param in table url
	  vTableURL += "&" + "state="+vState;
	  
	  console.log("sep 23 :: vTableURL = "+vTableURL);
	  
	  vTableURL = encodeURI(vTableURL);
	  //alert(vTableURL);
	  var vTableFrame = document.getElementById('enoviatableframe');	  
	  vTableFrame.src = vTableURL;	  
  }
	function retainFormValue(){
	/*var fieldName = document.getElementById("namepattern");
	var fieldRev = document.getElementById('revision').value;
	var fieldOwner = document.getElementById('originator').value;
	var fieldOriginated = document.getElementById('date_filter').value;
	alert(fieldName.value);*/
	//var reloadurl = "../abbsupportcentral/ABBRealTimeSearch.jsp?name="+fieldName+"&revision="+fieldRev+"&owner="+fieldOwner+"&date="+fieldOriginated+;
	//reloadurl = encodeURI(reloadurl);
	}
  // method to update type field
  function getTypeChooserCustom() {
	//retainFormValue();
	/*var fieldName = document.getElementById("namepattern");
	var fieldRev = document.getElementById('revision').value;
	var fieldOwner = document.getElementById('originator').value;
	var fieldOriginated = document.getElementById('date_filter').value;*/
    var strURL="../common/ABBemxTypeChooser.jsp?fieldNameDisplay=txtTypeDisplay&fieldNameActual=hdnType&SelectType=multiselect&SelectAbstractTypes=true&ObserveHidden=true&ShowIcons=true&suiteKey=abbsupportcentral&ReloadOpener=true&InclusionList=ABB Part Classification,ABB Product Design Issue,Change Order,Change Request,DOCUMENTS,ABB Part Raw Material,ABB MNS iS Software&ExclusionList=ABB Reference Document";
	showModalDialog(strURL, 450, 500, true);
//	var fieldType = document.getElementById('typepattern').value;
//	var reloadurl = "../abbsupportcentral/ABBRealTimeSearch.jsp?type="+fieldType+"&name="+fieldName+"&revision="+fieldRev+"&owner="+fieldOwner+"&date="+fieldOriginated;
	//reloadurl = encodeURI(reloadurl);
  }
  
  //// method to reset search refinement page
  function setDefaultSearchRefinement() {	 
	 //var vRefreshURL = "../abbsupportcentral/ABBRealTimeSearch.jsp?type=*&name=*&revision=*&owner=*&date=&limit=100";
	 var vRefreshURL = "../abbsupportcentral/ABBRealTimeSearch.jsp?type=Change Order|Change Request|DOCUMENTS|ABB Part Classification|ABB Product Design Issue&typedisplay=Change Order|Change Request|DOCUMENTS|Part Classification|Product Design Issue&name=*&revision=*&owner=*&date=&limit=100";	 
	var reloadicon = document.querySelector("#reloading");
	encodeURI(vRefreshURL);	
	vRefreshURL = encodeURI(vRefreshURL);
	reloadicon.style.display = 'block';
	$( "#SearchPart" ).load(vRefreshURL + " #SearchPart > *" );	  
  }
  
  //method to update Originator field
  function getPersonChooserCustom() {
	  var URL = "../common/emxFullSearch.jsp?field=TYPES=type_Person&HelpMarker=emxhelpfullsearch&table=ECSearchPersonsTable&selection=multiple&submitURL=../abbsupportcentral/ABBSearchUtil.jsp?mode=assignOwner";
	  showModalDialog(URL, 500, 400);
  }
  
  //method to reset fields to default values
  function resetField(input_id) {
	  document.getElementById(input_id).value = "*";
  }

  //method to clear fields
  function clearField(input_id) {
	  document.getElementById(input_id).value = "";
  }
  
  //method opening popup containing attribute range values
  function showHideRangeValues(DivCtrID, RangeSelectsID) {
	var div= document.querySelector("#"+DivCtrID);
	var inp= document.querySelector("#"+RangeSelectsID);
	var rect= inp.getClientRects();
	div.style.display= 'block';
	div.style.left= rect[0].left+'px';
	div.style.top= rect[0].bottom+'px';
  } 
 
  //updates range attribute field
  function updateTextBox(DivCtrID, RangeSelectID, RangeFieldID) {
	var field= document.querySelector("#"+RangeFieldID);
	field.value = Array.prototype.filter.call( document.getElementById(RangeSelectID).options, el => el.selected).map(el => el.value).join("|");
	var div= document.querySelector("#"+DivCtrID);
	div.style.display = 'none';	
  } 
  
  // closes attribute ranges popup
  function disablepopup(DivCtrID) {
	 var div= document.querySelector('#'+DivCtrID);
	 div.style.display = 'none';	 
  }
  
  // limit field validations
  function validateLimit(limitid) {
	  var limit = document.getElementById(limitid).value;
	  var limitform = document.getElementById('limitform');
	  if(limit == "") {
			alert("Can't be empty. Restoring default value!");
			document.getElementById(limitid).value = "100";
	  }	  
	  if(isNaN(limit)) {
		  document.getElementById(limitid).value = "100";
		  alert("please enter a number only!");
	  }  
  }
  
  // opens date selection popup
  function getDateDialog(datepopup, newdatebox) {
	var div= document.querySelector("#"+datepopup);
	var inp= document.querySelector("#"+newdatebox);
	var rect= inp.getClientRects();
	div.style.display= 'block';
	div.style.left= rect[0].left+'px';
	//div.style.top= rect[0].bottom+'px';
	div.style.top= '0px';	  
  }
  
  //loads selected date option
  function loadSelectedDateFilter(Selected) {
	  if(Selected == "On" || Selected == "On or before" || Selected == "On or after")
	  {
		  document.getElementById('label_date1').style.display = 'none';
		  var div = document.querySelector("#div_date1");
		  div.style.display= 'block';
		  var div2 = document.querySelector("#div_date2");
		  div2.style.display = 'none';
	  }
	  else if(Selected == "Between")
	  {
		  document.getElementById('label_date1').style.display = 'inline';
		  var div = document.querySelector("#div_date1");
		  var div2 = document.querySelector("#div_date2");
		  div.style.display= 'block';
		  div2.style.display= 'block';
	  }
  }
  
  //updates date field
  function updateNewDate(DateCtr, DateSelect, DateInput) {
	  var select = document.querySelector("#"+DateSelect);
	  var selectvalue = select.value;
	  if(selectvalue == "On" || selectvalue == "On or before" || selectvalue == "On or after")
	  {
		  var input1 = (document.querySelectorAll("#div_date1 > input"))[0];
		  var dateinput = document.querySelector("#"+DateInput);
		  dateinput.value = input1.value;
	  }
	  else if(selectvalue == "Between")
	  {
		  var input1 = (document.querySelectorAll("#div_date1 > input"))[0];
		  var input2 = (document.querySelectorAll("#div_date2 > input"))[0];
		  var dateinput = document.querySelector("#"+DateInput);
		  dateinput.value = input1.value+" to "+input2.value;
	  }
	  var ctr = document.querySelector("#"+DateCtr);
	  ctr.style.display = "none";
  }
  
  //closes date popup 
  function disableDatePopup(DateCtr) {
	  var date1 = document.querySelector("#div_date1");
	  date1.style.display = "none";	  
	  var date2 = document.querySelector("#div_date2");
	  date2.style.display = "none";
	  var ctr = document.querySelector("#"+DateCtr);
	  ctr.style.display = "none";	  
  }
  
//calls search on enter key 
function executeSearch(e) {
	console.log("hitting keypress..");
	if (e.keyCode == 13) {
        queryParams();
	}
}
  //method load range attributes post type selection
  function loadRangeAttributesOnDone() {
	  var vTypes = document.getElementById('typepattern').value;
	  
	  //Added for ABB :: Display Names :: START
	  vTypes = document.getElementById('typenameactualid').value;
	  var vTypeDisplay = document.getElementById('typepattern').value;
	  //Added for ABB :: Display Names :: END
	  
	  //	 alert("vTypes:"+vTypes);
		//alert("In loadRangeAttributesOnDone");
		var url = "../abbsupportcentral/ABBSearchUtil.jsp?mode=getRangeAttributes&types="+vTypes;
		//alert(url);
		url = encodeURI(url);
        var xmlhttp1;
        if (window.XMLHttpRequest) {// Mozilla/Safari
            xmlhttp1 = new XMLHttpRequest();
        } else if (window.ActiveXObject) {// IE
            xmlhttp1 = new ActiveXObject("Microsoft.XMLHTTP");
        } else {
            alert('Sorry, your browser does not support XML HTTP Request!');
        }
        xmlhttp1.open("GET", url, false);
        xmlhttp1.setRequestHeader("Content-Type", "text");
        xmlhttp1.send(null);
        if ((xmlhttp1.readyState == 4 || xmlhttp1.readyState == "complete" ))
        {
            var responseText = xmlhttp1.responseText;
			//alert(responseText);
			var vRangeAttributes = responseText;           
			
			if(vRangeAttributes != null)
			{
				var vType = vTypes;
				var vName = document.getElementById('namepattern').value;
				var vRev = document.getElementById('revision').value;
				var vOwner = document.getElementById('originator').value;
				var vOriginated = document.getElementById('date_filter').value;
				var vLimit = document.getElementById('limit2').value;
				
				//Added for ABB :: Display Names :: START
				
				var reloadurl = "../abbsupportcentral/ABBRealTimeSearch.jsp?type="+vType+"&name="+vName+"&revision="+vRev+"&owner="+vOwner+"&date="+vOriginated+"&limit="+vLimit+"&rangeattributes="+vRangeAttributes+"&typedisplay="+vTypeDisplay;
				//var reloadurl = "../abbsupportcentral/ABBRealTimeSearch.jsp?type="+vType+"&name="+vName+"&revision="+vRev+"&owner="+vOwner+"&date="+vOriginated+"&limit="+vLimit+"&rangeattributes="+vRangeAttributes;
				
				//Added for ABB :: Display Names :: END
				
				reloadurl = encodeURI(reloadurl);
			//	alert("reloadurl==>"+reloadurl);
				//location.href = reloadurl;
				$( "#SearchPart" ).load(reloadurl + " #SearchPart > *" );
				
			}
        }	  
  } 

//validate revision
function doRevison(chkbox_id, rev_id) {
	console.log("on click rev checkbox..");
	var rev_chkbox = document.getElementById(chkbox_id);
	var rev_field = document.getElementById(rev_id);
	if (rev_chkbox.checked == true) {
		rev_field.value = "*";
		rev_field.setAttribute('readonly', true);
	}
	else
		rev_field.removeAttribute('readonly');
}

function openTextArea(divtextareaid, textareaid, nameid) {
	//alert("proceed..");
	var namefield = document.getElementById(nameid);
	namefield.setAttribute('readonly', true);
	var div= document.querySelector("#"+divtextareaid);
	var inp= document.querySelector("#"+textareaid);
	var rect= inp.getClientRects();
	div.style.display= 'block';
	div.style.left= rect[0].left+'px';
	//div.style.top= rect[0].bottom+'px';
	div.style.top= '0px';
	inp.focus();
	document.getElementById(textareaid).focus();
}

function updateNameField(nameCtr, textareaid, nameid) {

	var textarea = document.querySelector("#"+textareaid);
	var contentdata = textarea.value;
	var arrContentData = contentdata.split("\n");
	//alert("contentdata = "+contentdata);
	var nameUpdate = "";
	for(var i = 0; i < arrContentData.length; i++) {
		//console.log("arrContentData "+i+" : "+arrContentData[i]);
		if(i == arrContentData.length-1)
			nameUpdate += arrContentData[i];
		else
			nameUpdate += arrContentData[i] + "|";
		
	}
	
	console.log("nameUpdate = "+nameUpdate);
	var nameinputfield = document.querySelector("#"+nameid);
	nameinputfield.value = nameUpdate;

	textarea.value = "";	
	var ctr = document.querySelector("#"+nameCtr);
	ctr.style.display = "none";	
	nameinputfield.removeAttribute('readonly');
}

function closeTextArea(textareactr, textarea, nameid) {
	  var textarea = document.querySelector("#"+textarea);
	  textarea.value = "";
	  var ctr = document.querySelector("#"+textareactr);
	  var namefield = document.querySelector("#"+nameid);
	  namefield.removeAttribute('readonly');
	  ctr.style.display = "none";	
}

  //updates State field
  function updateTextBoxForState(DivCtrID, RangeSelectID, SystemDisplay, SystemActual) {
	var fielddisplay= document.querySelector("#"+SystemDisplay);
	var fielddisplayvalue = "";
	var fieldactual = document.querySelector("#"+SystemActual);
	var vSelected = Array.prototype.filter.call( document.getElementById(RangeSelectID).options, el => el.selected).map(el => el.value).join("|");
	fielddisplay.value = vSelected;
	
	if(vSelected != null) {
		//vSelected = vSelected.replaceAll(".","-DS-");
		vSelected = vSelected.replaceAll("|",",");
		var strURL = "../abbsupportcentral/ABBSearchUtil.jsp?mode=fetchPolicyStateCondition&state="+vSelected;
		var xmlhttp12;
		if (window.XMLHttpRequest) {// Mozilla/Safari
			xmlhttp12 = new XMLHttpRequest();
		} else if (window.ActiveXObject) {// IE
			xmlhttp12 = new ActiveXObject("Microsoft.XMLHTTP");
		} else {
			alert('Sorry, your browser does not support XML HTTP Request!');
		}		
		xmlhttp12.open("GET", strURL, false);
		xmlhttp12.setRequestHeader("Content-Type", "text");
		xmlhttp12.send(null);
		if ((xmlhttp12.readyState == 4 || xmlhttp12.readyState == "complete" ))
		{
			var responseText2 = xmlhttp12.responseText;
			console.log("responseText2 :: "+responseText2);
			fieldactual.value = responseText2;
			document.getElementById('statenameactualid').value = responseText2;
		}	
	}	
	
	var div= document.querySelector("#"+DivCtrID);
	div.style.display= 'none';	
  }
  
  function resetTypeField(dispid, actualid) {
	document.getElementById(dispid).value = "Change Order|Change Request|DOCUMENTS|Part Classification|Product Design Issue";
	document.getElementById(actualid).value = "Change Order|Change Request|DOCUMENTS|ABB Part Classification|ABB Product Design Issue";
  }
  
    function clearStateField(dispid, actualid) {
	  document.getElementById(dispid).value = "";
	  document.getElementById(actualid).value = "";
	}
</script>

<style>

html,
body {
  height: 100%;
  margin: 0;
}

 div, p {
  border: 1px solid #ddd;
  padding: 0px;
  
}

#SearchPart {
	/* position: fixed; */
	top: 0;
	left: 0;
	right: 70%;
    /* overflow-x: hidden; */
    /* overflow-y: scroll; */
	width: 30%;
}

#SearchPart div:hover {background-color: #92a8d1;}

#completespace {
	border: 1px solid #ddd;
	padding: 0px;
	/* position: absolute; */
	bottom: 0;	
}

#ResultsPart {
	margin-left: 30%; 
	top: 0;
	/* left: 30%; */
	right: 0;
	width: 70%;
	height: 100%;
	border: 1px solid #ddd;
	border-collapse: collapse;
	position: fixed;
}
	
#RangeHeading {
	background-color:#d8d8d8;
}	

#reloading {
	/*position: absolute; /*or fixed
    margin-right: 70%; */
	
	
	position: relative;
	margin-right: 0;
	border: none !important;
	float: right;
	display: none; 
	
	/*position: relative;
	left: 50%;
	top: 50%;
	display: none; */
}
</style>

<!DOCTYPE html>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<html>

<body>
<%

Locale locale = context.getLocale();
//System.out.println("******************Inside jsp ABBRealTimeSearch.jsp********");
String sTypePattern = (String)emxGetParameter(request, "type");
System.out.println("sep 22 :: sTypePattern==>"+sTypePattern);
String sNamePattern = (String)emxGetParameter(request, "name");
String sRevPattern = (String)emxGetParameter(request, "revision");
String sOwnerPattern = (String)emxGetParameter(request, "owner");
String sOriginatedPattern = (String)emxGetParameter(request, "date");
String sObjLimit = (String)emxGetParameter(request, "limit");
String sTableURL = (String)emxGetParameter(request, "tableURL");
String sRangeAttributes = (String)emxGetParameter(request, "rangeattributes");
String sSystem = (String)emxGetParameter(request, "system");
if(UIUtil.isNullOrEmpty(sSystem))
	sSystem = "";

String sStateActual = "";
String sStateDisplay = "";	

System.out.println("sep 22 :: sRangeAttributes = "+sRangeAttributes);
System.out.println("sep 22 :: post range updates sTableURL = "+sTableURL);

//Added for ABB :: Display Names :: START
String sTypeNameDisplayValue = (String)emxGetParameter(request, "typedisplay");
System.out.println("sep 22 :: sTypeNameDisplayValue = "+sTypeNameDisplayValue);
if(sTypeNameDisplayValue == null || "".equals(sTypeNameDisplayValue)) {
	//sTypeNameDisplayValue = "*";
	sTypeNameDisplayValue = "Change Order|Change Request|DOCUMENTS|Part Classification|Product Design Issue";
	//Change Order|Change Request|DOCUMENTS|Part Classification|Product Design Issue
}
//System.out.println("sTypeNameDisplayValue = "+sTypeNameDisplayValue);
//Added for ABB :: Display Names :: END


if(UIUtil.isNullOrEmpty(sTypePattern)) {
	//sTypePattern = "*";
	sTypePattern = "Change Order|Change Request|DOCUMENTS|ABB Part Classification|ABB Product Design Issue";
}
if(UIUtil.isNullOrEmpty(sNamePattern))
	sNamePattern = "*";
if(UIUtil.isNullOrEmpty(sRevPattern))
	sRevPattern = "*";
if(UIUtil.isNullOrEmpty(sOwnerPattern))
	sOwnerPattern = "*";
if(sOriginatedPattern == null)
	sOriginatedPattern = "";
if(UIUtil.isNullOrEmpty(sObjLimit))
	sObjLimit = "100";
if(UIUtil.isNullOrEmpty(sTableURL))
	sTableURL = "../common/emxIndentedTable.jsp?table=AEFGeneralSearchResults&header=Search Results&program=ABBSearchutil:dummy&toolbar=ABBRealTimeSearchToolbar";	

//System.out.println("sTableURL==>"+sTableURL);
StringList slRangeAttributes = new StringList();
if(UIUtil.isNotNullAndNotEmpty(sRangeAttributes))
{
	if(sRangeAttributes.contains(","))
		slRangeAttributes = FrameworkUtil.split(sRangeAttributes, ",");
	else
		slRangeAttributes.add(sRangeAttributes);
}

String sTypeSpecificToolbar = null;
String sTypeSpecificUITable = null;
//System.out.println("sTypePattern==>"+sTypePattern);
if(UIUtil.isNotNullAndNotEmpty(sTypePattern))
{
	String sTypePatternLabel = sTypePattern;
	if(sTypePattern.contains(" "))
		sTypePatternLabel = sTypePattern.replaceAll(" ","_");
	
	sTypeSpecificToolbar = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Toolbar.Type."+sTypePatternLabel);
	//System.out.println("sTypeSpecificToolbar==>"+sTypeSpecificToolbar);
	if(sTypeSpecificToolbar.contains("ABB.RealTimeSearch."))
		sTypeSpecificToolbar = null;
	
	sTypeSpecificUITable = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Table.Type."+sTypePatternLabel);
	//System.out.println("sTypeSpecificUITable==>"+sTypeSpecificUITable);
	if(sTypeSpecificUITable.contains("ABB.RealTimeSearch."))
		sTypeSpecificUITable = null;	
}

//list options for type field
String sTypeAheadAllTypes = (String) MqlUtil.mqlCommand(context, "list type * where 'abstract == false'");
StringList slTypeAheadTypes = FrameworkUtil.split(sTypeAheadAllTypes, "\n");
String sTypeAheadAllTypes2 = EnoviaResourceBundle.getProperty(context,"emxSystem",locale,"emxFramework.GenericSearch.Types");
StringList slTypeAheadTypes2 = FrameworkUtil.split(sTypeAheadAllTypes2, ",");
int iSlTypeAheadTypes2Size = slTypeAheadTypes2.size();
ArrayList alTypeAheadTypes = new ArrayList<String>();
for(int typecounter = 0; typecounter < iSlTypeAheadTypes2Size; typecounter++)
{
	String sTypeActualName = PropertyUtil.getSchemaProperty(context, slTypeAheadTypes2.get(typecounter));
	if(UIUtil.isNotNullAndNotEmpty(sTypeActualName))
		alTypeAheadTypes.add(sTypeActualName);
}
slTypeAheadTypes2 = new StringList(alTypeAheadTypes);

%>

<!-- Search Refinement Frame-->
<div border="1" width="30%" id="SearchPart">

<div width="100%">
<b>Search Refinement</b>
<div id="reloading"><img src="../common/images/ABBCustomSearchReload.gif" /></div>
<!-- reloading... -->
</div>

<div width="100%">
<p align="right">
<button id="searchicon" type="button"  style="padding: 0;border:none;" title="search" onclick="queryParams()"><img src="../common/images/utilSearchButtonAdvanced.png" /></button>
<button type="button"  style="padding: 0;border:none;" title="refresh" onclick="setDefaultSearchRefinement()"><img src="../common/images/utilSearchReset.png" /></button>
</p>
</div>

<div width="100%">
<br><br>
Limit to<input type="text" id="limit2" value="<%=sObjLimit%>" size="3" onblur="validateLimit(this.id)" />Results
<br><br>
</div>

<div id="typediv" width="100%">
<label id="typelabel" style="color:rgb(132, 16, 0);"><i><b>Type</b></i></label><br>

<!-- Added for ABB: Display Names :: START-->
<input type="hidden" id="typenameactualid" name="typenameactual" value="<%=sTypePattern%>">
<!-- sTypePattern -->
<!-- Added for ABB: Display Names :: END-->

<input type="text" style="width:75%" list="TypeAheadTypes" id="typepattern" value="<%=sTypeNameDisplayValue%>" autocomplete="on" onblur="loadRangeAttributesOnDone()" onkeypress="executeSearch(event)" readonly /> 
  <datalist id="TypeAheadTypes">
  <%
  int iTypeAheadTypesSize = slTypeAheadTypes2.size();
  for(int typeindex = 0; typeindex < iTypeAheadTypesSize; typeindex++)
  {
	  %>
	  <option value="<%= slTypeAheadTypes2.get(typeindex) %>">
	  <%
  }
  %>
  </datalist>
<input type="button" value="..." onclick="getTypeChooserCustom()"/>
<button type="button"  style="padding: 0;border:none" title="reset" onclick="resetTypeField('typepattern', 'typenameactualid')"><img src="../common/images/ABB_RefreshBtn.png" /></button>
<br> <br> 
</div>

<div width="100%">

<label><b>Name</b></label><br>
<input type="text" style="width:75%" id="namepattern" value="<%=sNamePattern%>" onfocus="openTextArea('div_name_textarea', 'name_textarea', 'namepattern')" onkeypress="executeSearch(event)"/>
<button type="button"  style="padding: 0;border:none" title="reset" onclick="resetField('namepattern')"><img src="../common/images/ABB_RefreshBtn.png" /></button>
<br> <br> 

<div id="div_name_textarea" style='display:none;position:static;z-index:10;background:white;border:1;hover:#ddd;'>
<textarea id="name_textarea"></textarea>
<button type="button"  style="padding:0;border:none" title="submit" onclick="updateNameField('div_name_textarea', 'name_textarea', 'namepattern')"><img src="../common/images/buttonMiniDone.gif" /></button>
<button type="button"  style="padding:0;border:none" title="cancel" onclick="closeTextArea('div_name_textarea', 'name_textarea', 'namepattern')"><img src="../common/images/buttonMiniCancel.gif" /></button>
</div>

</div>

<div width="100%">
<label><b>Revision</b></label><br>
<input type="text" style="width:60%" id="revision" value="<%=sRevPattern%>" />
<input type="checkbox" id="rev_chkbox" onclick="doRevison(this.id, 'revision')">Highest 
<button type="button"  style="padding: 0;border:none" title="reset" onclick="resetField('revision')"><img src="../common/images/ABB_RefreshBtn.png" /></button>
<br> <br> 
</div>

<div width="100%">
<label><b>Originator</b></label><br>
<input type="text" id="originator" style="width:75%" value="<%=sOwnerPattern%>"/>
<input type="button" value="..." onclick="getPersonChooserCustom()"/>
<button type="button"  style="padding: 0;border:none" title="reset" onclick="resetField('originator')"><img src="../common/images/ABB_RefreshBtn.png" /></button>
<br> <br>
</div>

<div width="100%">
<label><b>Originated</b></label><br>
<input type="text" style="width:75%" id="date_filter" value="<%=sOriginatedPattern%>"  readonly /> 
<input type="button" value="..." onclick="getDateDialog('datepopup' , 'date_filter')"/>
<button type="button" style="padding: 0;border:none" title="clear" onclick="clearField('date_filter')"><img src="../common/images/ABB_RefreshBtn.png" /></button>

<div id='datepopup' style='display:none;position:static;z-index:10;background:white;border:1;hover:#ddd;'>
<select id="datefilters_id" name="datefilters" onchange="loadSelectedDateFilter(this.value)" >
  <option value="" disabled selected hidden>select date option</option>
  <option value="On">On</option>
  <option value="On or after">On or after</option>
  <option value="On or before">On or before</option>
  <option value="Between">Between</option>
</select>
<div id="div_date1" style='display:none;border:0'>
<label id="label_date1">From</label><br>
<input type="date" id="date1" value="" size="20"/> 
</div>
<div id="div_date2" style='display:none;border:0'>
<label>To</label><br>
<input type="date" id="date2" value="" size="20"/> 
</div>
<button type="button"  style="padding:0;border:none" title="submit" onclick="updateNewDate('datepopup', 'datefilters_id', 'date_filter')"><img src="../common/images/buttonMiniDone.gif" /></button>
<button type="button"  style="padding:0;border:none" title="cancel" onclick="disableDatePopup('datepopup')"><img src="../common/images/buttonMiniCancel.gif" /></button>
</div>

</div>

<!-- ABB :: State :: START -->
<%
String sDataForStateField = sTypePattern;
String[] sArrPolicyStates = new String[] {sDataForStateField};
Map mResultPolicyStates = JPO.invoke(context, "ABBSearchutil", null, "getPolicyStates", sArrPolicyStates, Map.class);
%>
<div id="State_Main" width="100%">

<div id="State_Ranges" style='display:none;position:absolute;z-index:10;background:white'>
<select id="State_Ranges_Select" value="" multiple>
<%
Iterator mResultKeyItr = mResultPolicyStates.keySet().iterator();
while(mResultKeyItr.hasNext()) {
	String sStatePrefix = (String) mResultKeyItr.next();
	StringList slStateDispValues  = (StringList) mResultPolicyStates.get(sStatePrefix);
	if(slStateDispValues != null && !slStateDispValues.isEmpty()) {
		for(int m = 0; m < slStateDispValues.size(); m++) {
			String sStateRange = slStateDispValues.get(m);
			%><option><%=sStatePrefix+"."+sStateRange%></option><%
		}
	}
}	
%>				
</select>
<button type="button"  style="padding:0;border:none" title="submit" onclick="updateTextBoxForState('State_Ranges','State_Ranges_Select','State', 'statenameactualid')"><img src="../common/images/buttonMiniDone.gif" /></button>
<button type="button"  style="padding:0;border:none" title="cancel" onclick="disablepopup('State_Ranges')"><img src="../common/images/buttonMiniCancel.gif" /></button>
</div>
<input type="hidden" id="statenameactualid" name="statenameactual" value="<%=sStateActual%>" />
<label><b>State</b></label><br>
<input type="text" style="width:75%" id="State" readonly value="<%=sStateDisplay%>" />
<button type="button" onclick="showHideRangeValues('State_Ranges', 'State_Ranges_Select')">...</button>				
<button type="button"  style="padding:0;border:none" title="clear" onclick="clearStateField('State', 'statenameactualid')"><img src="../common/images/ABB_RefreshBtn.png"/></button>

</div>
<!-- ABB :: State :: END -->


<!-- Added for ABB Latest :: System Field :: START -->
<%
String sPartRawMaterialTypesExclusionList = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.PartRawMaterial.ExclusionList");
//System.out.println("sPartRawMaterialTypesExclusionList = "+sPartRawMaterialTypesExclusionList);
StringList slRawMaterialExclusionList = new StringList();
if(!sPartRawMaterialTypesExclusionList.equals("ABB.RealTimeSearch.PartRawMaterial.ExclusionList")) {
	slRawMaterialExclusionList = FrameworkUtil.split(sPartRawMaterialTypesExclusionList, ",");
}
boolean bRawMaterialExists = false;
if(!slRawMaterialExclusionList.isEmpty()) {
	StringList sTypePatternList = new StringList();
	if(sTypePattern.contains("|"))
		sTypePatternList = FrameworkUtil.split(sTypePattern, "|");
	else
		sTypePatternList.add(sTypePattern);
	
	for(int z = 0; z < sTypePatternList.size(); z++) {
		if(slRawMaterialExclusionList.contains( sTypePatternList.get(z)) ) {
			bRawMaterialExists = true;
			break;
		}
	}	
}
//System.out.println("bRawMaterialExists = "+bRawMaterialExists);

if(!sTypePattern.contains("ABB Product Design Issue") && !bRawMaterialExists) {%>
<div id="System_Main" width="100%">

<div id="System_Ranges" style='display:none;position:absolute;z-index:10;background:white'>
<select id="System_Ranges_Select" value="" multiple>
<%
String sSystemAttributeRanges = MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", "ABB System", "range", "|");
StringList slSystemAttributeRanges = FrameworkUtil.split(sSystemAttributeRanges, "|");
int iSystemAttrRangesSize = slSystemAttributeRanges.size();
for(int y = 0; y < iSystemAttrRangesSize; y++)
{
	String sSystemRange = slSystemAttributeRanges.get(y);
	sSystemRange = sSystemRange.replaceAll("= ", "");
	%>				
	<option><%=sSystemRange%></option>
	<%
}
%>
</select>
<button type="button"  style="padding:0;border:none" title="submit" onclick="updateTextBox('System_Ranges','System_Ranges_Select','System')"><img src="../common/images/buttonMiniDone.gif" /></button>
<button type="button"  style="padding:0;border:none" title="cancel" onclick="disablepopup('System_Ranges')"><img src="../common/images/buttonMiniCancel.gif" /></button>
</div>
<label><b>System</b></label><br>
<input type="text" style="width:75%" id="System" readonly value="<%=sSystem%>" />
<button type="button" onclick="showHideRangeValues('System_Ranges', 'System_Ranges_Select')">...</button>				
<button type="button"  style="padding: 0;border:none" title="clear" onclick="clearField('System')"><img src="../common/images/ABB_RefreshBtn.png" /></button>

</div>
<%}%>
<!-- Added for ABB Latest :: System Field :: END -->


<div width="100%" id="RangeAttributes">
<%
int iSLRangeAttributesSize = slRangeAttributes.size();
//System.out.println("iSLRangeAttributesSize==>"+iSLRangeAttributesSize);
System.out.println("sep 22:: slRangeAttributes==>"+slRangeAttributes);
// do not modify below string variable
final String sSpcSep = "-S-"; 

for(int i = 0; i < iSLRangeAttributesSize; i++)
{	
	String sTypeAttributes = (String)slRangeAttributes.get(i);	
	StringList slTypeAttributes = FrameworkUtil.split(sTypeAttributes,":");
	String sType = slTypeAttributes.get(0);
	String sTypeNameActual = sType;
	//System.out.println("K Sep 13 sTypeNameActual = "+sTypeNameActual);
	String sTypeID = "";
	if(sType.contains(" "))
		sTypeID = sType.replaceAll(" ",sSpcSep);
	else
		sTypeID = sType;	
	String sAttributes_ = slTypeAttributes.get(1);	
	//multiple values for same type
	if(sAttributes_.contains("|"))
	{				
		%>
			<div id="<%=sTypeID%>" width="30%">
					
		<%
		StringList slFinalFormAttributes = FrameworkUtil.split(sAttributes_,"|");
		for(int k = 0; k < slFinalFormAttributes.size(); k++)
		{
			String sFormAttributeFinal = slFinalFormAttributes.get(k);
			String sFormAttributeFinalID = sFormAttributeFinal;
			String sLabel = sFormAttributeFinal;
			//System.out.println("before:: sLabel==>"+sLabel);
			sLabel = i18nNow.getAttributeI18NString(sLabel, context.getLocale().getDisplayLanguage());
			//System.out.println("after:: sLabel==>"+sLabel);
			if(UIUtil.isNullOrEmpty(sLabel)) {
				sLabel = sFormAttributeFinal;
			}
			
			
			if(sFormAttributeFinal.contains("ABB System"))
			{
				sLabel = "System";
			}
			else if(sFormAttributeFinal.contains("ABB Issue Product"))
			{
				sLabel = "Product";
			}
			else if(sFormAttributeFinal.contains("ABB Critical Part"))
			{
					sLabel = "Critical Part";
			}
			
			//System.out.println("K Sep 13 sFormAttributeFinal==>"+sFormAttributeFinal);
			String sFormAttributeRangesFinal = (String)MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", sFormAttributeFinal, "range", "|");
			//System.out.println("K Sep 13 sFormAttributeRangesFinal ==> "+sFormAttributeRangesFinal);
			
			//ABB: 19359 : START
			if(UIUtil.isNullOrEmpty(sFormAttributeRangesFinal) && "ABB Product Design Issue".equals(sTypeNameActual)) {
				
				String[] ArgsForXML = new String[] {sFormAttributeFinal};				
				sFormAttributeRangesFinal = JPO.invoke(context, "ABBSearchutil", null, "getXmlRanges", ArgsForXML, String.class);
				//System.out.println("K Sep 13 sFormAttributeRangesFinal XML==> "+sFormAttributeRangesFinal);
			}
			//ABB: 19359 : END
	
				if(sFormAttributeFinal.contains(" "))
					sFormAttributeFinalID = sFormAttributeFinal.replaceAll(" ",sSpcSep);

			%>
				<!-- Range Values :: START -->
			
				<div id='<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>' style='display:none;position:absolute;z-index:10;background:white'>
				<select id="<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges_Select"%>" value="" multiple>				
			<%
			StringList slAttributeSpecificRanges = FrameworkUtil.split(sFormAttributeRangesFinal, "|");
			for(int r = 0; r < slAttributeSpecificRanges.size(); r++)
			{
				String sRange = slAttributeSpecificRanges.get(r);
				sRange = sRange.replaceAll("= ", "");
				%>				
				<option><%=sRange%></option>
				<%
			}
			%>
				</select>				
				<button type="button"  style="padding:0;border:none" title="submit" onclick="updateTextBox('<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>','<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges_Select"%>','<%=sTypeID+"_"+sFormAttributeFinalID%>')"><img src="../common/images/buttonMiniDone.gif" /></button>
				<button type="button"  style="padding:0;border:none" title="cancel" onclick="disablepopup('<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>')"><img src="../common/images/buttonMiniCancel.gif" /></button>
				</div> 
								
				<label><b><%=sLabel%></b></label><br>
				<input type="text" style="width:75%" id="<%=sTypeID+"_"+sFormAttributeFinalID%>" readonly value="" />
				<button type="button" onclick="showHideRangeValues('<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>', '<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges_Select"%>')">...</button>				
				
				<button type="button"  style="padding: 0;border:none" title="clear" onclick="clearField('<%=sTypeID+"_"+sFormAttributeFinalID%>')"><img src="../common/images/ABB_RefreshBtn.png" /></button>	
				<br> <br>
				
				<%
		}
			 %> </div> <%
	}
	else
	{
		String sFormAttributeFinal = sAttributes_;
		String sFormAttributeFinalID = sFormAttributeFinal;
		String sLabel = sFormAttributeFinal;
		System.out.println("sFormAttributeFinal==>"+sFormAttributeFinal);
		//System.out.println("sFormAttributeFinal==>"+sFormAttributeFinal);
		String sFormAttributeRangesFinal="";
		if(!"*".equals(sType))
		{
			sFormAttributeRangesFinal = (String)MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", sFormAttributeFinal, "range", "|");
		
			if(sFormAttributeFinal.contains("ABB System"))
			{
					sLabel = "System";
			}
			else if(sFormAttributeFinal.contains("ABB Issue Product"))
			{
					sLabel = "Product";
			}
			else if(sFormAttributeFinal.contains("ABB Critical Part"))
			{
					sLabel = "Critical Part";
			}
			
			if(sFormAttributeFinal.contains(" "))
				sFormAttributeFinalID = sFormAttributeFinal.replaceAll(" ",sSpcSep);

				
			// latest addition : START
			System.out.println("sFormAttributeFinalID==>"+sFormAttributeFinalID);
			System.out.println("sTypeID==>"+sTypeID);
			System.out.println("sFormAttributeRangesFinal==>"+sFormAttributeRangesFinal);
			
			// latest addition : END
			%>
				<div width="30%" id="<%=sTypeID%>">
			
				<div id='<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>' style='display:none;position:absolute;z-index:10;background:white'>
				<select id="<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges_Select"%>" value="" multiple>				

			<%		
			StringList slAttributeSpecificRanges = FrameworkUtil.split(sFormAttributeRangesFinal, "|");
					   
				for(int r = 0; r < slAttributeSpecificRanges.size(); r++)
				{
					String sRange = slAttributeSpecificRanges.get(r);
					sRange = sRange.replaceAll("= ", "");
					System.out.println("sRange==>"+sRange);
					%>				
					<option><%=sRange%></option>
					<%
				}
				%>
				</select>
				<button type="button"  style="padding: 0;border:none" title="submit" onclick="updateTextBox('<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>','<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges_Select"%>','<%=sTypeID+"_"+sFormAttributeFinalID%>')"><img src="../common/images/buttonMiniDone.gif" /></button>
				<button type="button"  style="padding: 0;border:none" title="cancel" onclick="disablepopup('<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>')"><img src="../common/images/buttonMiniCancel.gif" /></button>
				</div> 			

				<label><b><%=sLabel%></b></label><br>
				<input type="text" style="width:75%" id="<%=sTypeID+"_"+sFormAttributeFinalID%>" readonly value="" />
				<button type="button" onclick="showHideRangeValues('<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges"%>', '<%=sTypeID+"_"+sFormAttributeFinalID+"_Ranges_Select"%>')">...</button>													
				<button type="button"  style="padding:0;border:none" title="clear" onclick="clearField('<%=sTypeID+"_"+sFormAttributeFinalID%>')"><img src="../common/images/ABB_RefreshBtn.png" /></button>	
				<br> <br>
				</div>
				<%	
		}
	}	

}
//System.out.println("sTableURL at the end==>"+sTableURL);
%>
</div>


</div>	

<!-- Search Results Table Frame-->
<div width="70%" height="100%" id="ResultsPart">
<iframe id="enoviatableframe" style="top:0;margin-left:0;padding:0;float:left;border:1px solid #ddd;display:block;" width="100%" height="100%" src="<%=sTableURL%>"></iframe>
</div>

</body>
</html>