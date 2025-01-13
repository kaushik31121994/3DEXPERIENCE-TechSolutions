<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../emxJSValidation.inc"%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@include file = "../components/emxComponentsCommonUtilAppInclude.inc"%>


<%-- @page import="com.matrixone.apps.domain.util.FrameworkLicenseUtil" --%>
<%-- @page import="com.matrixone.apps.framework.ui.UIUtil" --%>
<%-- @page import="com.matrixone.apps.domain.util.MapList" --%>
<%-- @page import="matrix.db.JPO" --%>
<%-- @page import="matrix.db.Context" --%>
<%-- @page import="java.util.Map" --%>
<%-- @page import="java.util.HashMap" --%>
<%-- @page import="matrix.util.StringList" --%>

 <!--
<link rel="stylesheet" href="../common/styles/emxUIDefault.css" type="text/css" />
<link rel="stylesheet" href="../common/styles/emxUIForm.css" type="text/css" />
<link rel="stylesheet" href="../common/styles/emxUIList.css" type="text/css" /> 
-->


<!--
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script language="javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="javascript" type="text/javascript" src="../components/emxComponentsJSFunctions.js"></script> 
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICalendar.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script> 
-->

<%

String sMode = (String) emxGetParameter(request, "mode");
String sOriginatorUpdateValue = "";

if("fetchTableAndToolbar".equals(sMode)) {
	Map methodArgs = new HashMap();

	String sSelectedTypes = (String) emxGetParameter(request, "typepattern");
	//System.out.println("sSelectedTypes==>"+sSelectedTypes);
	methodArgs.put("types",sSelectedTypes);
	String[] Args = JPO.packArgs(methodArgs);
	String sResult = (String) JPO.invoke(context, "ABBSearchutil", null, "getTypeSpecificTableAndToolbar", Args, String.class);
	System.out.println(" K sep 9 :: sResult==>"+sResult);
	out.clear();
	out.write(sResult);	
}
else if("getRangeAttributes".equals(sMode)) {
	//System.out.println("In getRangeAttributes");
	Map methodArgs = new HashMap();
	String sSelectedTypes = (String) emxGetParameter(request, "types");
	StringList slSelectedTypes = new StringList();
	String sValue = "";
	if(UIUtil.isNotNullAndNotEmpty(sSelectedTypes))
	{
		if(sSelectedTypes.contains("|"))
		{
			slSelectedTypes = FrameworkUtil.split(sSelectedTypes, "|");
			for(int i = 0; i < slSelectedTypes.size(); i++)
			{
				if(UIUtil.isNullOrEmpty(sValue))
					sValue += (String)slSelectedTypes.get(i);
				else
					sValue += ","+(String)slSelectedTypes.get(i);
			}
		}
		else
			sValue = sSelectedTypes;
	}
	methodArgs.put("types",sValue);
	String[] Args = JPO.packArgs(methodArgs);
	String sResult = (String) JPO.invoke(context, "ABBSearchutil", null, "getRangeAttributesForTypes", Args, String.class);
	System.out.println("sResult at range attributes==>"+sResult);
	out.clear();
	out.write(sResult);
}
else if("assignOwner".equals(sMode)) {
	String sPersonId = "";
	String sPersonName = "";
	sOriginatorUpdateValue = "";
	DomainObject domPerson;
	String sTableRowIds[] = emxGetParameterValues(request, "emxTableRowId");
	for(int j = 0; j < sTableRowIds.length; j++)
	{
		sPersonId = sTableRowIds[j];
		if(sPersonId.contains("|"))
		{
			sPersonId = sPersonId.replaceAll("\\|","");
		}
		domPerson = new DomainObject(sPersonId);
		sPersonName = (String)domPerson.getInfo(context, "name");
		
		if(UIUtil.isNotNullAndNotEmpty(sOriginatorUpdateValue))
			sOriginatorUpdateValue += "|" + sPersonName;
		else
			sOriginatorUpdateValue = sPersonName;
	}
%>
<html>
<body>
<input type="hidden" id="hiddenId" name="hiddenName" value="<%=sOriginatorUpdateValue%>"> 
</body>
</html>
<script language="javascript">
	console.log("entering submit jsp script ");
	console.log("existing owner val = "+getTopWindow().getWindowOpener().parent.document.getElementById('originator').value);
	getTopWindow().getWindowOpener().parent.document.getElementById('originator').value = document.getElementById('hiddenId').value;
	getTopWindow().closeWindow();
</script>
<%
}
else if("fetchPolicyStateCondition".equals(sMode)) {
	String sPolicyStateInfo = (String) emxGetParameter(request, "state");
	System.out.println("ABBSearchutil ::: sState :: "+sPolicyStateInfo);
	Map mPolicyState = new HashMap();
	
	if(sPolicyStateInfo.contains(",")) {
		StringList slPolicyStateInfo = FrameworkUtil.split(sPolicyStateInfo, ",");
		for(int i = 0; i < slPolicyStateInfo.size(); i++) {
			String sPolicyKey = ( FrameworkUtil.split((slPolicyStateInfo.get(i)), ".")).get(0);
			if(!mPolicyState.containsKey(sPolicyKey)) {
				mPolicyState.put(sPolicyKey, "");
			}
		}
		//System.out.println("ABBSearchutil ::: mPolicyState :: "+mPolicyState);
		
		for(int j = 0; j < slPolicyStateInfo.size(); j++) {
			String sFullInfo = slPolicyStateInfo.get(j);
			StringList slFullInfo = FrameworkUtil.split(sFullInfo, ".");
			String sPolicyInfo = slFullInfo.get(0);
			String sStateInfo = slFullInfo.get(1);
			//System.out.println("ABBSearchutil ::: sPolicyInfo :: "+sPolicyInfo);
			if(mPolicyState.containsKey(sPolicyInfo)) {
				String sMapPolicyStateKeyValue = (String) mPolicyState.get(sPolicyInfo);
				if(UIUtil.isNotNullAndNotEmpty(sMapPolicyStateKeyValue))
					sMapPolicyStateKeyValue += "," + sStateInfo;
				else
					sMapPolicyStateKeyValue = sStateInfo;
					
				mPolicyState.put(sPolicyInfo, sMapPolicyStateKeyValue);
			}
		}					
	}
	else {
		StringList slFullInfo = FrameworkUtil.split(sPolicyStateInfo, ".");
		String sPolicyInfo = slFullInfo.get(0);
		String sStateInfo = slFullInfo.get(1);
		mPolicyState.put(sPolicyInfo, sStateInfo);
	}
	
	System.out.println("ABBSearchutil ::: mPolicyState :: "+mPolicyState);
	
	// Framing the result
	String sResult = "";
	Iterator mPolicyStateKeyItr = mPolicyState.keySet().iterator();
	while(mPolicyStateKeyItr.hasNext()) {
		String sMapKey = (String) mPolicyStateKeyItr.next();
		
		// Common Document :: START
		if("Common Document".equals(sMapKey)) {
			String sPolicyName = "ABB Common Document";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
								
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);
						if(sState.equals("Frozen")) {
							String sStateActual = "Peer Review";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else if(sState.equals("Released")) {
							String sStateActual = "RnD Release";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else {						
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sState;
							else
								sStatesInformation += sState;							
						}						
					}					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					if(sState.equals("Frozen")) {
						String sStateActual = "Peer Review";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else if(sState.equals("Released")) {
						String sStateActual = "RnD Release";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else {						
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;							
					}
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Common Document :: END		

		
		// Reference Document :: START
		if("Reference Document".equals(sMapKey)) {
			String sPolicyName = "ABB Reference Document";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
			
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					sStatesInformation += sState;
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Reference Document :: END
	
		// Cancelled :: START
		if("Cancelled".equals(sMapKey)) {
			String sPolicyName = "Cancelled";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
			
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					sStatesInformation += sState;
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Cancelled :: END		
		
		
		// Issue_Support_Process :: START
		if("Issue Support Process".equals(sMapKey)) {
			String sPolicyName = "ABB Issue Support Process";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
								
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);					
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;																			
					}					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					
					if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
						sStatesInformation += "|" + sState;
					else
						sStatesInformation += sState;							

				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Issue_Support_Process :: END		

		// ABB_Issue_RnD_Process :: START
		if("Issue RnD Process".equals(sMapKey)) {
			String sPolicyName = "ABB Issue RnD Process";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
								
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);					
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;																			
					}					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					
					if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
						sStatesInformation += "|" + sState;
					else
						sStatesInformation += sState;							

				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// ABB_Issue_RnD_Process :: END

		// Part :: START
		if("Part".equals(sMapKey)) {
			String sPolicyName = "EC Part";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
								
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);
						if(sState.equals("Frozen")) {
							String sStateActual = "Review";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						//else if(sState.equals("In Work")) {
						else if(sState.equals("Create")) {
							String sStateActual = "Preliminary";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else if(sState.equals("Released")) {
							String sStateActual = "Release";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else {						
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sState;
							else
								sStatesInformation += sState;							
						}						
					}					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					if(sState.equals("Frozen")) {
						String sStateActual = "Review";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else if(sState.equals("Released")) {
						String sStateActual = "Release";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					//else if(sState.equals("In Work")) {
					else if(sState.equals("Create")) {
						String sStateActual = "Preliminary";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else {						
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;							
					}
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Part :: END	

		// Change Request :: START
		if("Change Request".equals(sMapKey)) {
			String sPolicyName = "Change Request";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
								
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);
						if(sState.equals("Draft")) {
							String sStateActual = "Create";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else if(sState.equals("Evaluation Review")) {
							String sStateActual = "In Review";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else if(sState.equals("Completed")) {
							String sStateActual = "Complete";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else {						
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sState;
							else
								sStatesInformation += sState;							
						}						
					}					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					if(sState.equals("Draft")) {
						String sStateActual = "Create";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else if(sState.equals("Evaluation Review")) {
						String sStateActual = "In Review";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else if(sState.equals("Completed")) {
						String sStateActual = "Complete";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else {						
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;							
					}
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Change Request :: END	


		// Change Order :: START
		if("Change Order".equals(sMapKey)) {
			String sPolicyName = "Fast track Change";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
								
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);
						if(sState.equals("Draft")) {
							String sStateActual = "Prepare";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}

						else if(sState.equals("Completed")) {
							String sStateActual = "Complete";
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sStateActual;
							else
								sStatesInformation += sStateActual;							
						}
						else {						
							if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
								sStatesInformation += "|" + sState;
							else
								sStatesInformation += sState;							
						}						
					}					
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					if(sState.equals("Draft")) {
						String sStateActual = "Prepare";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}

					else if(sState.equals("Completed")) {
						String sStateActual = "Complete";
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sStateActual;
						else
							sStatesInformation += sStateActual;							
					}
					else {						
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;							
					}
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Change Order :: END

		// Raw Material :: START
		if("Raw Material".equals(sMapKey)) {
			String sPolicyName = "ABB Raw Material";
			String sStatesInformation = "";
			String sSelectedStates = (String) mPolicyState.get(sMapKey);
			if(UIUtil.isNotNullAndNotEmpty(sSelectedStates)) {
			
				//multiple states selected : START
				if(sSelectedStates.contains(",")) {
					StringList slSelectedStates = FrameworkUtil.split(sSelectedStates, ",");
					for(int k = 0; k < slSelectedStates.size(); k++) {
						String sState = slSelectedStates.get(k);
						
						if(UIUtil.isNotNullAndNotEmpty(sStatesInformation))
							sStatesInformation += "|" + sState;
						else
							sStatesInformation += sState;							
						
					}				
				}//multiple states selected : END
				
				//single state selected : START
				else {
					String sState = sSelectedStates;
					sStatesInformation += sState;
				}
				//single state selected : END
				
				if(UIUtil.isNotNullAndNotEmpty(sResult))
					sResult += "," + sPolicyName +"-" +sStatesInformation;
				else
					sResult += sPolicyName +"-" +sStatesInformation;
			}
		}
		// Raw Material :: END
		
	}// end of map iteration

	System.out.println("ABBSearchutil :: sResult :: "+sResult);
	out.clear();
	out.write(sResult);

}//end of mode: fetch policy & state condition
%>

