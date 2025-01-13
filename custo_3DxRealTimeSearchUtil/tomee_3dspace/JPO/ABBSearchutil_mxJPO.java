import matrix.db.Context;
import matrix.db.JPO;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import matrix.db.BusinessType;
import matrix.db.Vault;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.matrixone.apps.framework.ui.UIUtil;

import java.text.SimpleDateFormat;

//Added below for ABB: 19359
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ABBSearchutil_mxJPO
{
	
	/**
	* Constructor.
	*
	* @param context the eMatrix <code>Context</code> object.
	* @param args holds no arguments.
	* @throws Exception if the operation fails.
	* @since EC 9.5.JCI.0.
	**/
	
	public ABBSearchutil_mxJPO(Context context, String[] args) throws Exception {
	  
	}
	
	//method to query all paramters entered in search refinement and display the results
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String getParentType(Context context,String sType)throws Exception{
       String paType = "";     
        try{
           BusinessType busType= new BusinessType(sType,new Vault("ABB vault"));
           paType =busType.getParent(context);

           if(paType != null && paType.length()>0 && (paType.equals(PropertyUtil.getSchemaProperty(context, "type_Part")) || paType.equals("Change") ||  paType.equals("DOCUMENTS") || paType.equals("Issue"))){
              return paType;
           }else{
             paType = getParentType(context,paType);
           }
       }catch(Exception e){
           throw e;
       }
        return paType;
	}
   
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList processFormParams(Context context, String args[]) throws Exception
	{
		Map PgmMap = (Map) JPO.unpackArgs(args);
		//System.out.println("processFormParams pgm map : "+PgmMap);
		
		String sRangeAttributesInfo = (String) PgmMap.get("rangeattributes");
		//System.out.println("sep 23 :: sRangeAttributesInfo==>"+sRangeAttributesInfo);
		
		String sSystem = (String) PgmMap.get("system");
		//System.out.println("sep 23 :: sSystem ==>"+sSystem);
		
		String sStateConditionFinal = "";
		String sStateInfo = (String) PgmMap.get("state");
		if(UIUtil.isNotNullAndNotEmpty(sStateInfo)) {
			if(sStateInfo.contains(",")) {
				StringList slPolicyStateInfo = FrameworkUtil.split(sStateInfo, ",");
				for(int z = 0; z < slPolicyStateInfo.size(); z++) {
					String sPolicyStateInfo = slPolicyStateInfo.get(z);
					if(sPolicyStateInfo.contains("-")) {
						StringList sPolicyStateinfoList = FrameworkUtil.split(sPolicyStateInfo, "-");
						String sPolicy = sPolicyStateinfoList.get(0);
						String sState = sPolicyStateinfoList.get(1);
						String sStateCond = "";
						String sPolicyStateCond = "";
						if(sState.contains("|")) {
							StringList slState = FrameworkUtil.split(sState, "|");
							for(int y = 0; y < slState.size(); y++) {
								if(UIUtil.isNotNullAndNotEmpty(sStateCond))
									sStateCond += " || " + "current == '"+slState.get(y)+"'";									
								else
									sStateCond += "current == '"+slState.get(y)+"'";								
							}
						}
						else {
							sStateCond = "current == '"+sState+"'";
						}
						sStateCond = "("+ sStateCond + ")";
						sPolicyStateCond = "policy == '"+sPolicy+"' && "+sStateCond;
						
						if(UIUtil.isNotNullAndNotEmpty(sStateConditionFinal))
							sStateConditionFinal += " || " +"("+ sPolicyStateCond+")";
						else
							sStateConditionFinal += "("+sPolicyStateCond+")";
						
					}
				}
				
			}
			else {
				String sPolicyStateInfo = sStateInfo;
				if(sPolicyStateInfo.contains("-")) {
					StringList sPolicyStateinfoList = FrameworkUtil.split(sPolicyStateInfo, "-");
					String sPolicy = sPolicyStateinfoList.get(0);
					String sState = sPolicyStateinfoList.get(1);
					String sStateCond = "";
					String sPolicyStateCond = "";
					if(sState.contains("|")) {
						StringList slState = FrameworkUtil.split(sState, "|");
						for(int y = 0; y < slState.size(); y++) {
							if(UIUtil.isNotNullAndNotEmpty(sStateCond))
								sStateCond += " || " + "current == '"+slState.get(y)+"'";
							else								
								sStateCond += "current == '"+slState.get(y)+"'";
						}
					}
					else {
						sStateCond = "current == '"+sState+"'";
					}
					sStateCond = "("+ sStateCond + ")";
					sPolicyStateCond = "policy == '"+sPolicy+"' && "+sStateCond;
					
					if(UIUtil.isNotNullAndNotEmpty(sStateConditionFinal))
						sStateConditionFinal += " || " +"("+ sPolicyStateCond+")";
					else
						sStateConditionFinal += "("+sPolicyStateCond+")";					
				}
			}
		}
		System.out.println("oct 1 :: sStateConditionFinal = "+sStateConditionFinal);
		
		MapList mlReturnSearchResults = new MapList();
		
		String sTypePattern = (String)PgmMap.get("type");
		
		if(UIUtil.isNotNullAndNotEmpty(sTypePattern) && sTypePattern.contains("|"))
			sTypePattern = sTypePattern.replaceAll("|",",");
		
		String sNamePattern = (String)PgmMap.get("name");
		if(sNamePattern.contains("|"))
			sNamePattern = sNamePattern.replace('|', ',');
		
		String sRevisionPattern = (String)PgmMap.get("revision");
		
		String sOwnerPattern = (String)PgmMap.get("owner");
		if(UIUtil.isNotNullAndNotEmpty(sOwnerPattern) && sOwnerPattern.contains("|"))
			sOwnerPattern = sOwnerPattern.replaceAll("|",",");		

		// processing date field and forming condition for WHERE clause :: START
		String sNewDatePattern = (String)PgmMap.get("date");
		String sOriginatedCond = "";
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		if(sNewDatePattern.contains("to"))
		{
			sNewDatePattern = sNewDatePattern.replaceAll("to", "|");
			StringList slDates = FrameworkUtil.split(sNewDatePattern, "|");
			String sFromDate = slDates.get(0);
			StringList slDateChunksNew = FrameworkUtil.split(sFromDate,"-");
			String sYearNew = slDateChunksNew.get(0);
			String sMonthNew = slDateChunksNew.get(1);
			String sDayNew = slDateChunksNew.get(2);
			sFromDate = sMonthNew+"/"+sDayNew+"/"+sYearNew;
			Date dFrom= sdf.parse(sFromDate);
			sFromDate = "\'"+sdf.format(dFrom)+" 12:00:00 AM\'";
			
			String sToDate = slDates.get(1);
			slDateChunksNew = FrameworkUtil.split(sToDate,"-");
			sYearNew = slDateChunksNew.get(0);
			sMonthNew = slDateChunksNew.get(1);
			sDayNew = slDateChunksNew.get(2);
			sToDate = sMonthNew+"/"+sDayNew+"/"+sYearNew;			
			Date dTo= sdf.parse(sToDate);
			sToDate = "\'"+sdf.format(dTo)+" 11:59:59 PM\'";

			sOriginatedCond = "(originated >= "+sFromDate+" && originated <= "+sToDate+")";
		}
		else if(UIUtil.isNotNullAndNotEmpty(sNewDatePattern))
		{
			StringList slDateChunksNew = FrameworkUtil.split(sNewDatePattern,"-");
			String sYearNew = slDateChunksNew.get(0);
			String sMonthNew = slDateChunksNew.get(1);
			String sDayNew = slDateChunksNew.get(2);
			sNewDatePattern = sMonthNew+"/"+sDayNew+"/"+sYearNew;
			Date dDate = sdf.parse(sNewDatePattern);
			String sDateOption = (String) PgmMap.get("dateoption");
			if(sDateOption.equals("On"))
			{
				sOriginatedCond = "(originated >= "+"\'"+sdf.format(dDate)+" 12:00:00 AM\'"+" && "+"originated <= "+"\'"+sdf.format(dDate)+" 11:59:59 PM\'"+")";
			}
			else if(sDateOption.equals("On or before"))
			{
				sOriginatedCond = "(originated <= "+"\'"+sdf.format(dDate)+" 11:59:59 PM\'"+")";
			}
			else if(sDateOption.equals("On or after"))
			{
				sOriginatedCond = "(originated >= "+"\'"+sdf.format(dDate)+" 12:00:00 AM\'"+")";
			}						
		}
		//System.out.println("sOriginatedCond = "+sOriginatedCond);
		// processing date field and forming condition for WHERE clause :: END
		
		String sWhere = "";
				
		//highest revision filter
		String sHighestRevCond = "";
		String sHighestRevisionChecked = (String) PgmMap.get("highestrevisionchecked");
		if(UIUtil.isNotNullAndNotEmpty(sHighestRevisionChecked))
		{
			if("true".equals(sHighestRevisionChecked))
				sHighestRevCond = "(revision == last)";				
		}
		
		String sLimit = (String)PgmMap.get("limit");
		int iObjLimit = 0;		
		if(UIUtil.isNotNullAndNotEmpty(sLimit))
			iObjLimit = Integer.parseInt(sLimit);

		//processing range attribute values and forming conditions for Where clause : START
		String sRangeAttrWhere = "";
		// do not modify the below final variables
		final String sSpcSep = "-S-";
		final String sRangeSep = "-RR-";
		final String sAttrValSep = ":";
		final String sAttrAttrSep = "|";
		
		if(UIUtil.isNotNullAndNotEmpty(sRangeAttributesInfo))
		{
			if(sRangeAttributesInfo.contains(","))
			{
				StringList slTypesSegregation = FrameworkUtil.split(sRangeAttributesInfo,",");
				int islTypesSegregationSize = slTypesSegregation.size();
				for(int i = 0; i < islTypesSegregationSize; i++)
				{
					String sTypeSpecificCond = "";
					
					String sSegregatedType = slTypesSegregation.get(i);
					if(sSegregatedType.contains(sAttrValSep))
					{
						int iTypeSeperatorIndex = sSegregatedType.indexOf("~");
						String sFoundType = sSegregatedType.substring(0, iTypeSeperatorIndex);
						sSegregatedType = sSegregatedType.replace(sFoundType+"~","");
						if(sFoundType.contains(sSpcSep))
							sFoundType = sFoundType.replaceAll(sSpcSep, " ");
						//System.out.println("sFoundType = "+sFoundType);						
						sTypeSpecificCond = "(type == '"+sFoundType+"'";
						if(sSegregatedType.contains(sAttrAttrSep))
						{
							StringList slAttributeSegregation = FrameworkUtil.split(sSegregatedType , "|");
							int islAttributeSegregationsize = slAttributeSegregation.size();
							for(int j = 0; j < islAttributeSegregationsize; j++)
							{
								String sAttributeValue = (slAttributeSegregation.get(j)).replaceAll(sAttrValSep, "|");
								StringList slAttributeValueSegregation = FrameworkUtil.split(sAttributeValue,"|");
								String sRangeAttributeName = slAttributeValueSegregation.get(0);
								if(sRangeAttributeName.contains(sSpcSep))
									sRangeAttributeName = sRangeAttributeName.replaceAll(sSpcSep," ");
								//System.out.println("sRangeAttributeName==>"+sRangeAttributeName);
								String sRangeAttributeValue = slAttributeValueSegregation.get(1);
								if(!sRangeAttributeValue.contains(sRangeSep))
									sTypeSpecificCond += " && attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";
								else
								{
									sTypeSpecificCond += " && (";
									String sMultiRangesCond = "";
									sRangeAttributeValue = sRangeAttributeValue.replaceAll(sRangeSep, "|");
									StringList slMultiRanges = FrameworkUtil.split(sRangeAttributeValue, "|");
									for(int rangeitr = 0; rangeitr < slMultiRanges.size(); rangeitr++)
									{
										if(UIUtil.isNullOrEmpty(sMultiRangesCond))
											sMultiRangesCond += "attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
										else
											sMultiRangesCond += " || attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
									}
									sTypeSpecificCond += sMultiRangesCond + ")";
								}
							}
							
						}
						else
						{
							sSegregatedType = sSegregatedType.replaceAll(sAttrValSep, "|");
							StringList slAttributeValueSegregation = FrameworkUtil.split(sSegregatedType, "|");
							String sRangeAttributeName = slAttributeValueSegregation.get(0);
								if(sRangeAttributeName.contains(sSpcSep))
									sRangeAttributeName = sRangeAttributeName.replaceAll(sSpcSep," ");
							String sRangeAttributeValue = slAttributeValueSegregation.get(1);
								if(!sRangeAttributeValue.contains(sRangeSep))
									sTypeSpecificCond += " && attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";
								else
								{
									sTypeSpecificCond += " && (";
									String sMultiRangesCond = "";
									sRangeAttributeValue = sRangeAttributeValue.replaceAll(sRangeSep, "|");
									StringList slMultiRanges = FrameworkUtil.split(sRangeAttributeValue, "|");
									for(int rangeitr = 0; rangeitr < slMultiRanges.size(); rangeitr++)
									{
										if(UIUtil.isNullOrEmpty(sMultiRangesCond))
											sMultiRangesCond += "attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
										else
											sMultiRangesCond += " || attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
									}
									sTypeSpecificCond += sMultiRangesCond + ")";
								}							
						}
						sTypeSpecificCond += ")";
						
					}
					//System.out.println("sTypeSpecificCond = "+sTypeSpecificCond);
					if(UIUtil.isNotNullAndNotEmpty(sRangeAttrWhere)) {
						if(UIUtil.isNotNullAndNotEmpty(sTypeSpecificCond))
							sRangeAttrWhere += " || " + sTypeSpecificCond;
					}
					else {
						if(UIUtil.isNotNullAndNotEmpty(sTypeSpecificCond))
							sRangeAttrWhere += sTypeSpecificCond;
					}
				}
			}
			else
			{
					if(sRangeAttributesInfo.contains(sAttrValSep))
					{
						int iTypeSeperatorIndex = sRangeAttributesInfo.indexOf("~");
						String sFoundType = sRangeAttributesInfo.substring(0, iTypeSeperatorIndex);
						sRangeAttributesInfo = sRangeAttributesInfo.replace(sFoundType+"~","");
						if(sFoundType.contains(sSpcSep))
							sFoundType = sFoundType.replaceAll(sSpcSep, " ");
						//System.out.println("sFoundType = "+sFoundType);
						//String sTypeSpecificCond = "(type == '"+sFoundType+"'";
						String sTypeSpecificCond = "";
						if(sRangeAttributesInfo.contains(sAttrAttrSep))
						{
							StringList slAttributeSegregation = FrameworkUtil.split(sRangeAttributesInfo , "|");
							int islAttributeSegregationsize = slAttributeSegregation.size();
							for(int j = 0; j < islAttributeSegregationsize; j++)
							{
								String sTypeAttributes = slAttributeSegregation.get(j);
								sTypeAttributes = sTypeAttributes.replaceAll(sAttrValSep, "|");
								StringList slAttributeValueSegregation = FrameworkUtil.split(sTypeAttributes,"|");
								String sRangeAttributeName = slAttributeValueSegregation.get(0);
								if(sRangeAttributeName.contains(sSpcSep))
									sRangeAttributeName = sRangeAttributeName.replaceAll(sSpcSep," ");
								//System.out.println("sRangeAttributeName = "+sRangeAttributeName);
								String sRangeAttributeValue = slAttributeValueSegregation.get(1);
								if(!sRangeAttributeValue.contains(sRangeSep)) {
									//modifed for ABB : START
									//sTypeSpecificCond += " && attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";
									if(UIUtil.isNullOrEmpty(sTypeSpecificCond))
										sTypeSpecificCond += "attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";			
									else
										sTypeSpecificCond += " && attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";
									//modifed for ABB : END
								}
								else
								{
									//modifed for ABB : START
									//sTypeSpecificCond += " && (";
									if(UIUtil.isNullOrEmpty(sTypeSpecificCond))
										sTypeSpecificCond += "(";
									else
										sTypeSpecificCond += " && (";
									//modifed for ABB : END
									
									//sTypeSpecificCond += "(";
									String sMultiRangesCond = "";
									sRangeAttributeValue = sRangeAttributeValue.replaceAll(sRangeSep, "|");
									StringList slMultiRanges = FrameworkUtil.split(sRangeAttributeValue, "|");
									for(int rangeitr = 0; rangeitr < slMultiRanges.size(); rangeitr++)
									{
										if(UIUtil.isNullOrEmpty(sMultiRangesCond))
											sMultiRangesCond += "attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
										else
											sMultiRangesCond += " || attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
									}
									sTypeSpecificCond += sMultiRangesCond + ")";
								}

								//if(j != islAttributeSegregationsize-1)
								//	sTypeSpecificCond = " && ";
							}
							
						}//upto here
						
						else
						{
							sRangeAttributesInfo = sRangeAttributesInfo.replaceAll(sAttrValSep, "|");
							StringList slAttributeSegregation = FrameworkUtil.split(sRangeAttributesInfo, "|");
							String sRangeAttributeName = slAttributeSegregation.get(0);
							//System.out.println("sRangeAttributeName = "+sRangeAttributeName);
								if(sRangeAttributeName.contains(sSpcSep))
									sRangeAttributeName = sRangeAttributeName.replaceAll(sSpcSep," ");
							String sRangeAttributeValue = slAttributeSegregation.get(1);
								if(!sRangeAttributeValue.contains(sRangeSep)) {
									//sTypeSpecificCond += " && attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";
									sTypeSpecificCond += "attribute[" + sRangeAttributeName + "] == '"+sRangeAttributeValue+"'";
								}
								else
								{
									//sTypeSpecificCond += " && (";
									sTypeSpecificCond += "(";
									String sMultiRangesCond = "";
									sRangeAttributeValue = sRangeAttributeValue.replaceAll(sRangeSep, "|");
									StringList slMultiRanges = FrameworkUtil.split(sRangeAttributeValue, "|");
									for(int rangeitr = 0; rangeitr < slMultiRanges.size(); rangeitr++)
									{
										if(UIUtil.isNullOrEmpty(sMultiRangesCond))
											sMultiRangesCond += "attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
										else
											sMultiRangesCond += " || attribute[" + sRangeAttributeName +"] == '"+slMultiRanges.get(rangeitr)+"'";
									}
									sTypeSpecificCond += sMultiRangesCond + ")";
								}							
						}
						//sTypeSpecificCond += ")";
						sTypeSpecificCond += "";
						System.out.println("else sTypeSpecificCond = "+sTypeSpecificCond);
						sRangeAttrWhere += sTypeSpecificCond;
						
					}				
			}
		}
		System.out.println(" sRangeAttrWhere = "+sRangeAttrWhere);
		if(UIUtil.isNotNullAndNotEmpty(sRangeAttrWhere))
			sRangeAttrWhere = "("+sRangeAttrWhere+")";		
		//System.out.println("sRangeAttrWhere = "+sRangeAttrWhere);
		
		//processing range attribute values and forming conditions for Where clause : END
				
		//appending all generated conditions to WHERE clause
		if(UIUtil.isNotNullAndNotEmpty(sOriginatedCond))			
			sWhere += sOriginatedCond; 		
		if(UIUtil.isNotNullAndNotEmpty(sRangeAttrWhere))
		{
			if(UIUtil.isNotNullAndNotEmpty(sWhere))
				sWhere += " && "+sRangeAttrWhere;
			else
				sWhere += sRangeAttrWhere;
		}		
		if(UIUtil.isNotNullAndNotEmpty(sHighestRevCond))
		{
			if(UIUtil.isNotNullAndNotEmpty(sWhere))
				sWhere += " && "+sHighestRevCond;
			else
				sWhere += sHighestRevCond;
		}
		
		
		// Added for ABB: to eliminate version objects in search results :: START
		if(UIUtil.isNotNullAndNotEmpty(sWhere))
			sWhere += " && "+"(policy != Version)";
		else
			sWhere += "(policy != Version)";
		// Added for ABB: to eliminate version objects in search results :: END

		String sSystemWhere = "";
		if(UIUtil.isNotNullAndNotEmpty(sSystem)) {			
			if(sSystem.contains("|")) {
				StringList slSystemRanges = FrameworkUtil.split(sSystem, "|");
				for(int x = 0; x < slSystemRanges.size(); x++) {
					if(UIUtil.isNullOrEmpty(sSystemWhere))
						sSystemWhere += "attribute[ABB System] == '"+slSystemRanges.get(x)+"'";
					else
						sSystemWhere += " || attribute[ABB System] == '"+slSystemRanges.get(x)+"'";
				}
			}
			else {
				sSystemWhere = "attribute[ABB System] == '"+sSystem+"'";
			}
		}
		
		if(UIUtil.isNotNullAndNotEmpty(sSystemWhere)) 
			sWhere += " && ("+sSystemWhere+")";
			
		if(UIUtil.isNotNullAndNotEmpty(sStateConditionFinal)) 
			sWhere += " && ("+sStateConditionFinal+")";
			
		System.out.println("Sep 24 sWhere = "+sWhere);
		
		StringList slObjSels = new StringList();
		slObjSels.add(DomainConstants.SELECT_ID);
		slObjSels.add(DomainConstants.SELECT_TYPE);
		slObjSels.add(DomainConstants.SELECT_NAME);
		slObjSels.add(DomainConstants.SELECT_REVISION);
		slObjSels.add(DomainConstants.SELECT_DESCRIPTION);
		slObjSels.add(DomainConstants.SELECT_POLICY);
		slObjSels.add(DomainConstants.SELECT_CURRENT);
		//System.out.println("expand type check :: Aug 17 :: DEBUG");
		//System.out.println("sTypePattern==>"+sTypePattern);
		//System.out.println("sNamePattern==>"+sNamePattern);
		//System.out.println("sRevisionPattern==>"+sRevisionPattern);
		//System.out.println("sOwnerPattern==>"+sOwnerPattern);
		//System.out.println("sWhere==>"+sWhere);
		mlReturnSearchResults = DomainObject.findObjects(context, 
														 sTypePattern,  
														sNamePattern,
														sRevisionPattern,  
														sOwnerPattern, 
														"ABB vault,ABB Local Vault", //vault
														sWhere, // obj where 
														"", // queryName
														true, //expandtype 
														slObjSels, 
														(short)iObjLimit);

		//System.out.println("mlReturnSearchResults = "+mlReturnSearchResults);
		//System.out.println("mlReturnSearchResults size ==>"+mlReturnSearchResults.size());
		//System.out.println("mlReturnSearchResults==>"+mlReturnSearchResults);
		return mlReturnSearchResults;
	}
	
	//returns empty table initially when the search page loads
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList dummy(Context context, String args[]) throws Exception
	{
		MapList mlDummy = new MapList();	
		return mlDummy;
	}
	
	//method to delete objects from the Search Results Table
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String deleteObjects(Context context, String args[]) throws Exception
	{
		String strErrorMessage = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource", context.getLocale(), "ABB.RealTimeSearch.Delete.NoAccess.Message");
		try {
			StringList slObjects = new StringList();
			String sContextUser = context.getUser();
			
			for(int i=0;i<args.length;i++) 
			{
				String sObjectId = args[i];
				//System.out.println("print delete obj data :: "+sObjectId);
				StringList slObjList = FrameworkUtil.split(sObjectId,"|");
				sObjectId = (String)slObjList.get(slObjList.size()-3);
				DomainObject doObj = new DomainObject(sObjectId);
				
				StringList slObjSels = new StringList();
				slObjSels.add("current.access[delete]");
				slObjSels.add(DomainConstants.SELECT_CURRENT);
				slObjSels.add(DomainConstants.SELECT_OWNER);
				slObjSels.add(DomainConstants.SELECT_NAME);
				
				Map mDeleteObjInfo = doObj.getInfo(context, slObjSels);
				String sHasDeleteAccess = (String) mDeleteObjInfo.get("current.access[delete]");
				String sState = (String) mDeleteObjInfo.get("current");
				String strOwner = (String) mDeleteObjInfo.get("owner");
				String strName = (String) mDeleteObjInfo.get("name");
								
				if ("TRUE".equals(sHasDeleteAccess)) {
					slObjects.add(sObjectId);
				} 
				else 
				{
					return strErrorMessage+"\n"+strName;
				}
			}			
			if(slObjects.size() > 0)
			{
				String[] objIds = new String[slObjects.size()];
				objIds = slObjects.toArray(objIds);
				DomainObject.deleteObjects(context, objIds);
				strErrorMessage = "success";					
			}			
		} catch(Exception ex) {
			strErrorMessage = ex.getMessage();
			ex.printStackTrace();
		}
		return strErrorMessage;
	}	

	@com.matrixone.apps.framework.ui.ProgramCallable
	public String getRangeAttributesForTypes(Context context, String args[]) throws Exception
	{
		String sReturnAttributes = "";		
		Map PgmMap = (Map) JPO.unpackArgs(args);
		//System.out.println("PgmMap = "+PgmMap);
		String sTypeAttributeSet = "";
		String sRangeAttributes = "";
		String sTypes = (String) PgmMap.get("types");
		System.out.println("oct 8 :: sTypes = "+sTypes);
		
		// Get Range Attributes From Config Properties : START
		Locale locale = context.getLocale();
		if(sTypes.contains(","))
		{
			/*System.out.println("multiple types!!!!! = ");
			StringList slTypes = FrameworkUtil.split(sTypes, ",");
			int slTypesSize = slTypes.size();
			for(int i = 0; i < slTypesSize; i++)
			{
				sTypeAttributeSet = "";
				String sType = slTypes.get(i);
				String sTypeLabel = sType;
				
				System.out.println("sep 22 : : sTypeLabel = "+sTypeLabel);
//if()
					if("ABB Product Design Issue".equals(sTypeLabel))
					{							
						sTypeLabel = sTypeLabel.replaceAll(" ","_");
						sRangeAttributes = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.RangeAttributes.Type."+sTypeLabel);
					} 				
					else if("DOCUMENTS".equals(sTypeLabel))
					{
						sRangeAttributes = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.RangeAttributes.Type."+sTypeLabel);
					}
					else
					{
						String sParent = getParentType(context,sTypeLabel);
						sRangeAttributes = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.RangeAttributes.Type."+sParent);
						System.out.println("sRangeAttributes = "+sRangeAttributes);
					}
				//System.out.println("sRangeAttributes = "+sRangeAttributes);
				if(!sRangeAttributes.contains("ABB.RealTimeSearch."))
				{
					//if(!sReturnAttributes.contains(sRangeAttributes))
					//{	
						sTypeAttributeSet += sType+":"+sRangeAttributes;
						if(UIUtil.isNullOrEmpty(sReturnAttributes))
							sReturnAttributes += sTypeAttributeSet;
						else
							sReturnAttributes += ","+sTypeAttributeSet;		
					//}
				}
			}*/
			sReturnAttributes = "";
		}//end of multiple types
		
		else
		{
			String sType = sTypes;
			String sTypeLabel = sType;
			
			//System.out.println("sTypeLabel = "+sTypeLabel);
			if("ABB Product Design Issue".equals(sTypeLabel))
			{	
				sTypeLabel = sTypeLabel.replaceAll(" ", "_");	
				sRangeAttributes = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.RangeAttributes.Type."+sTypeLabel);
			}else if("DOCUMENTS".equals(sTypeLabel))
			{
					sRangeAttributes = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.RangeAttributes.Type."+sTypeLabel);
			} 
			else
			{
				if(!"*".equals(sTypeLabel))
				{
					String sParent = getParentType(context,sTypeLabel);
					boolean bcheckRawmaterialAndMNSISSoftware = false;
					bcheckRawmaterialAndMNSISSoftware = checkParentRawMaterialAndMNSISSoftware(context, sTypeLabel);
					System.out.println("oct 8 :: bcheckRawmaterialAndMNSISSoftware==>"+bcheckRawmaterialAndMNSISSoftware);
					
					if(!bcheckRawmaterialAndMNSISSoftware)
						sRangeAttributes = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.RangeAttributes.Type."+sParent);
					else
						sRangeAttributes = "";
				}				
			}
			//System.out.println("sRangeAttributes = "+sRangeAttributes);
			if(!sRangeAttributes.contains("ABB.RealTimeSearch.") && UIUtil.isNotNullAndNotEmpty(sRangeAttributes))
			{			
				sTypeAttributeSet += sType+":"+sRangeAttributes;
				if(UIUtil.isNullOrEmpty(sReturnAttributes))
					sReturnAttributes += sTypeAttributeSet;
			}
		}
		// Get Range Attributes From Config Properties : END
		
		
		/* Get All Range Attributes : START
		if(sTypes.contains(","))
		
			StringList slTypes = FrameworkUtil.split(sTypes, ",");
			int slTypesSize = slTypes.size();
			//System.out.println("slTypesSize = "+slTypesSize);
			for(int i = 0; i < slTypesSize; i++)
			{
				sTypeAttributeSet = "";
				String sType = slTypes.get(i);
				String sTypeAttributes = (String) MqlUtil.mqlCommand(context, "print type $1 select $2 dump $3", sType, "attribute", "|");
				//System.out.println("sTypeAttributes = "+sTypeAttributes);
				if(UIUtil.isNotNullAndNotEmpty(sTypeAttributes))
				{
					if(sTypeAttributes.contains("|"))
					{
						
						StringList slAttributesOfType = FrameworkUtil.split(sTypeAttributes,"|");
						int slAttributesOfTypeSize = slAttributesOfType.size();
						//System.out.println("slAttributesOfTypeSize = "+slAttributesOfTypeSize);
						for(int j = 0; j < slAttributesOfTypeSize; j++)
						{
							String sAttribute = slAttributesOfType.get(j);
							String sAttributeRanges = (String) MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", sAttribute, "range", "|");
							if(UIUtil.isNotNullAndNotEmpty(sAttributeRanges))
							{
								System.out.println("multiple types :: count statement");
								if(UIUtil.isNullOrEmpty(sTypeAttributeSet))
									sTypeAttributeSet += sType+":"+sAttribute;
								else
									sTypeAttributeSet += "|"+sAttribute;
							}
						}
					}
					else
					{
						String sAttribute = sTypeAttributes;
						String sAttributeRanges = (String) MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", sType, "range", "|");
						if(UIUtil.isNullOrEmpty(sAttributeRanges))
						{
							sTypeAttributeSet += sType+":"+sAttribute;
						}
					}
					
					if(UIUtil.isNullOrEmpty(sReturnAttributes))
					{
						sReturnAttributes += sTypeAttributeSet;
					}
					else
						sReturnAttributes += ","+sTypeAttributeSet;
				}
			}
		}
		else
		{
			String sType = sTypes;
			String sTypeAttributes = (String) MqlUtil.mqlCommand(context, "print type $1 select $2 dump $3", sType, "attribute", "|");
			if(UIUtil.isNotNullAndNotEmpty(sTypeAttributes))
			{
				if(sTypeAttributes.contains("|"))
				{
					StringList slAttributesOfType = FrameworkUtil.split(sTypeAttributes,"|");
					int slAttributesOfTypeSize = slAttributesOfType.size();
					for(int j = 0; j < slAttributesOfTypeSize; j++)
					{
						String sAttribute = slAttributesOfType.get(j);
						String sAttributeRanges = (String) MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", sAttribute, "range", "|");
						if(UIUtil.isNotNullAndNotEmpty(sAttributeRanges))
						{
							//System.out.println("count this statement..");
							if(UIUtil.isNullOrEmpty(sTypeAttributeSet))
								sTypeAttributeSet += sType + ":" + sAttribute; 
							else
								sTypeAttributeSet += "|"+ sAttribute;
						}
					}
				}
				else
				{
					String sAttribute = sTypeAttributes;
					String sAttributeRanges = (String) MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump $3", sAttribute, "range", "|");					
					if(UIUtil.isNotNullAndNotEmpty(sAttributeRanges))
					{							
						if(UIUtil.isNullOrEmpty(sTypeAttributeSet))
							sTypeAttributeSet += sType + ":" + sAttribute; 
					}
					
				}
				sReturnAttributes += sTypeAttributeSet;
			}		
		}
		Get All Range Attributes : END*/
		
		System.out.println(" sep 22 sReturnAttributes = "+sReturnAttributes);
		return sReturnAttributes;
	}
	
	//method to configure table and toolbar in the search page based on type selection
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String getTypeSpecificTableAndToolbar(Context context, String args[]) throws Exception
	{
		//System.out.println("**In method getTypeSpecificTableAndToolbar***");
		Locale locale = context.getLocale();
		Map PgmMap = (Map) JPO.unpackArgs(args);
		
		//Get Default Table and Toolbar for Search Page
		String sGenericTable = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Table.Type.Generic");
		String sGenericToolbar = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Toolbar.Type.Generic");
		String sGenericTableHeader = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.TableHeader.Type.Generic");
		String sReturnDefault = "table:"+sGenericTable+"|toolbar:"+sGenericToolbar+"|header:"+sGenericTableHeader;
		
		//Get Type Specific Table and Toolbar for Search Page
		String sPartSpecificTable = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Table.Type.Part");
		String sPartSpecificToolbar = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Toolbar.Type.Part");		
		String sPartSpecificTableHeader = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.TableHeader.Type.Part");
		String sReturnPartSpecific = "table:"+sPartSpecificTable+"|toolbar:"+sPartSpecificToolbar+"|header:"+sPartSpecificTableHeader;
		
		String sChangeSpecificTable = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Table.Type.Change");
		String sChangeSpecificToolbar = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Toolbar.Type.Change");		
		String sChangeSpecificTableHeader = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.TableHeader.Type.Change");
		String sReturnChangeSpecific = "table:"+sChangeSpecificTable+"|toolbar:"+sChangeSpecificToolbar+"|header:"+sChangeSpecificTableHeader;
		
		String sIssueSpecificTable = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Table.Type.Issue");
		String sIssueSpecificToolbar = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Toolbar.Type.Issue");		
		String sIssueSpecificTableHeader = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.TableHeader.Type.Issue");
		String sReturnIssueSpecific = "table:"+sIssueSpecificTable+"|toolbar:"+sIssueSpecificToolbar+"|header:"+sIssueSpecificTableHeader;
		
		//Added for ABB : 19357 : START
		String sDocumentSpecificTable = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Table.Type.Document");
		String sDocumentSpecificToolbar = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.Toolbar.Type.Part");		
		String sDocumentSpecificTableHeader = EnoviaResourceBundle.getProperty(context,"emxABBSupportCentralStringResource",locale,"ABB.RealTimeSearch.TableHeader.Type.Document");
		String sReturnDocumentSpecific = "table:"+sDocumentSpecificTable+"|toolbar:"+sDocumentSpecificToolbar+"|header:"+sDocumentSpecificTableHeader;
		//Added for ABB : 19357 : END
		
		
		//do not modify below hashmap entries
		Map mTableToolbarDecider = new HashMap();
		mTableToolbarDecider.put("Part", "0");
		mTableToolbarDecider.put("Change", "0");
		mTableToolbarDecider.put("Issue","0");
		mTableToolbarDecider.put("Generic", "0");
		// New entry for ABB : 19357
		mTableToolbarDecider.put("Document", "0");
		
		String sGetParentType = "";		
		String sTypePattern = (String) PgmMap.get("types");
		
		if(sTypePattern.contains("*"))
			return sReturnDefault;

		else {
				//System.out.println(" at line 625");
			// If Multiple Types Selected :: START			
			if(sTypePattern.contains(","))
			{
				StringList slTypesPattern = FrameworkUtil.split(sTypePattern, ",");
				int iSlTypesPatternSize = slTypesPattern.size();
				for(int i = 0; i < iSlTypesPatternSize; i++) {
					String sType = slTypesPattern.get(i);
					//System.out.println("sType = "+sType);
					
					if(!"Part".equals(sType) && !"Change".equals(sType) && !"DOCUMENTS".equals(sType) && !"Issue".equals(sType))
					{
						//sGetParentType = (String) MqlUtil.mqlCommand(context, "print type $1 select $2 dump",sType, "derived");
						sGetParentType = getParentType(context,sType);
						//System.out.println("sGetParentType = "+sGetParentType);
						if("Part".equals(sGetParentType)) {
							//System.out.println("updating part table as 1 :: sType = "+sType);
							mTableToolbarDecider.put("Part", "1");
							continue;
						}
						else if("Change".equals(sGetParentType)) {
							mTableToolbarDecider.put("Change", "1");
							continue;
						}
						else if("DOCUMENTS".equals(sGetParentType)) {
							mTableToolbarDecider.put("Document", "1");
							continue;
						}
						else if("Issue".equals(sGetParentType)) {
							mTableToolbarDecider.put("Issue", "1");
							continue;
						}						
						else {
							mTableToolbarDecider.put("Generic", "1");
							break;
						}											
					}
					else {
						if("Part".equals(sType)) {
							//System.out.println("else :: updating part table as 1 :: sType = "+sType);
							mTableToolbarDecider.put("Part", "1");
							continue;
						}
						else if("Change".equals(sType)) {
							mTableToolbarDecider.put("Change", "1");
							continue;
						}
						else if("DOCUMENTS".equals(sType)) {
							mTableToolbarDecider.put("Document", "1");
							continue;
						}
						else if("Issue".equals(sType)) {
							mTableToolbarDecider.put("Issue", "1");
							continue;
						}
						else {
							mTableToolbarDecider.put("Generic", "1");
							break;
						}					
					}
				}
				//System.out.println("Part val : "+(String)mTableToolbarDecider.get("Part"));
				//System.out.println("Change val : "+(String)mTableToolbarDecider.get("Change"));
				//System.out.println("Generic val : "+(String)mTableToolbarDecider.get("Generic"));
				//System.out.println("Issue val : "+(String)mTableToolbarDecider.get("Issue"));
				
				if("1".equals( (String)mTableToolbarDecider.get("Generic")))
					return sReturnDefault;
				else {
					int iPartEntry = Integer.parseInt( (String)mTableToolbarDecider.get("Part") );
					int iChangeEntry = Integer.parseInt((String)mTableToolbarDecider.get("Change"));
					int iIssueEntry = Integer.parseInt((String)mTableToolbarDecider.get("Issue"));
					//New Entry for ABB:: 19357
					int iDocumentEntry = Integer.parseInt((String)mTableToolbarDecider.get("Document"));
					
					int sum = iPartEntry + iChangeEntry + iIssueEntry + iDocumentEntry;
					if(sum > 1) 
						return sReturnDefault;
					else {
						if(iPartEntry == 1)
							return sReturnPartSpecific;
						else if(iChangeEntry == 1)
							return sReturnChangeSpecific;
						else if(iIssueEntry == 1)
							return sReturnIssueSpecific;
						//New Entry for ABB : 19357
						else if(iDocumentEntry == 1)
							return sReturnDocumentSpecific;
							
						}
				}				
			}					
			// If Multiple Types Selected :: END			
			
			// If Single Type Selected :: START
			else {
				//System.out.println("at line 720");
				//System.out.println("**BEfore if****");
				//System.out.println("sTypePattern==>"+sTypePattern);
				if(!"Part".equals(sTypePattern) && !"Change".equals(sTypePattern) && !"DOCUMENTS".equals(sTypePattern) && !"Issue".equals(sTypePattern))
				{
					//sGetParentType = (String) MqlUtil.mqlCommand(context, "print type $1 select $2 dump",sTypePattern, "derived");
					sGetParentType = getParentType(context,sTypePattern);
					//System.out.println("sGetParentType==>"+sGetParentType);
					if("Part".equals(sGetParentType)) 
						return sReturnPartSpecific;
					
					else if("Change".equals(sGetParentType)) 
						return sReturnChangeSpecific;

					else if("DOCUMENTS".equals(sGetParentType))
						return sReturnDocumentSpecific;
					else if("Issue".equals(sGetParentType))
						return sReturnIssueSpecific;
					else
						return sReturnDefault;		
					
				}
				else {
					//System.out.println("**In else");
					if("Part".equals(sTypePattern))
						return sReturnPartSpecific;
					else if("Change".equals(sTypePattern))
						return sReturnChangeSpecific;
					else if("DOCUMENTS".equals(sTypePattern))
						return sReturnDocumentSpecific;
					else if("Issue".equals(sTypePattern))
						return sReturnIssueSpecific;
					else
						return sReturnDefault;					
				}				
			}
			// If Single Type Selected :: END
		}
		return "";
	}

	public boolean hideEBOMToolbarForDocuments(Context context, String args[]) throws Exception
	{
		boolean bReturn = true;
		//System.out.println("ABBPartBase :: hideToolbarForDocuments :: START");
		Map programMap = (Map) JPO.unpackArgs(args);
		//System.out.println("Oct 20 :: programMap = "+programMap);
		String sTable = (String) programMap.get("table");
		if(!"ENCEngineeringView".equals(sTable) && !"APPRouteDocumentSummary".equals(sTable)) {

			String sTypePattern =  (String) programMap.get("type");
			System.out.println("Oct 8 :: sTypePattern = "+sTypePattern);
			StringList slTypeField = new StringList();
			
			if(UIUtil.isNotNullAndNotEmpty(sTypePattern))
			{
				if(sTypePattern.contains(","))
					slTypeField = FrameworkUtil.split(sTypePattern, ",");
				else
					slTypeField.add(sTypePattern);
				
				for(int i = 0; i < slTypeField.size(); i++)
				{
					String sTypeField = slTypeField.get(i);
					if(sTypeField.equals("DOCUMENTS"))
						bReturn = false;
					else {
						String sGetParentType = getParentType(context,sTypeField);
						//System.out.println("sGetParentType = "+sGetParentType);
						if("DOCUMENTS".equals(sGetParentType))
							bReturn = false;
					}	
				}
			}

		}
		return bReturn;
	}
	
	//Added for ABB #19359 
	public String getXmlRanges(Context context, String args[]) throws Exception {
		//System.out.println("getXmlRanges :: START ");
		String sForXMLAttribute = args[0];
		if(sForXMLAttribute.contains("ABB "))
			sForXMLAttribute = sForXMLAttribute.replaceAll("ABB ", "");
		
		System.out.println("sForXMLAttribute :: "+sForXMLAttribute);
		File xmlFile = new File("Element.xml");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.parse(xmlFile);
		
		NodeList listOfAttributes = doc.getElementsByTagName("Attribute");

		String strProduct = "";
		
		StringList slAttrXMLRanges = new StringList();
		
		for (int s = 0; s < listOfAttributes.getLength(); s++) {
			Element attrElement = (Element) listOfAttributes.item(s);
			NodeList attributeList = attrElement.getElementsByTagName("AttributeName");
			StringList sProductList = new StringList();
			for (int s1 = 0; s1 < attributeList.getLength(); s1++) 
			{
				Node firstRangeNode1 = attributeList.item(s1);
				Element firstRangeElement1 = (Element) firstRangeNode1;
				String strRangesList = firstRangeElement1.getFirstChild().getNodeValue().trim();
				//System.out.println("strRangesList :: "+strRangesList);
				if(strRangesList.equals(sForXMLAttribute)) {
					NodeList listOfProduct = attrElement.getElementsByTagName("Product");

						for (int s2 = 0; s2 < listOfProduct.getLength(); s2++) {
							StringList strListRanges = new StringList();
							Node firstProductNode = listOfProduct.item(s2);
							if (firstProductNode.getNodeType() == Node.ELEMENT_NODE) {
								Element firstProductElement = (Element) firstProductNode;
								NodeList firstNameList = firstProductElement.getElementsByTagName("Name");
								Element firstNameElement = (Element) firstNameList.item(0);

								NodeList textProductList = firstNameElement.getChildNodes();

								strProduct = ((Node) textProductList.item(0)).getNodeValue().trim();
								//System.out.println("strProduct :: "+strProduct);
								
								Element element = (Element) listOfProduct.item(s2);
								NodeList RangeList = element.getElementsByTagName("Range");
								String sProduct = "";
								for (int s3 = 0; s3 < RangeList.getLength(); s3++) {
									Node firstRangeNode = RangeList.item(s3);
									Element firstRangeElement = (Element) firstRangeNode;
									String sRangeValue = firstRangeElement.getFirstChild().getNodeValue().trim();
									if(!slAttrXMLRanges.contains(sRangeValue)) {
										slAttrXMLRanges.add(sRangeValue);
									}									
								} //end of range for loop
								
							} //end of product if loop
						}										
				}
			}
		}
		//System.out.println("slAttrXMLRanges :: "+slAttrXMLRanges);
		int iAttrXMLSize = slAttrXMLRanges.size();

		String sReturnAttrXMLRanges = "";
		for(int k = 0; k < iAttrXMLSize; k++) {
			sReturnAttrXMLRanges += slAttrXMLRanges.get(k);
			sReturnAttrXMLRanges += "|";
		}
		
		if(sReturnAttrXMLRanges.endsWith("|"))
			sReturnAttrXMLRanges = sReturnAttrXMLRanges.substring(0, sReturnAttrXMLRanges.length()-1);
		
		//System.out.println("sReturnAttrXMLRanges :: "+sReturnAttrXMLRanges);
		return sReturnAttrXMLRanges;		
	}
	
	public Map getPolicyStates(Context context, String args[]) throws Exception {
		Map mReturnPolicyStates = new HashMap();
		//Map mReturnDisplay
		String sTypesSelected = args[0];
		String sSelectedTypes = "";
		String sSelectedTypePolicy = "";
		String sStateActual = "";
		String sStateActualKey = "";
		String sStateDisplay = "";
		String sPolicyKey = "";
		String sPolicyDisplay = "";
		String sFrameworkStateKey = "";
		int iDocumentCounter = 0;
		int iPartCounter = 0;
		int iRawMaterialCounter = 0;
		
		System.out.println("getPolicyStates :: sTypesSelected :: "+sTypesSelected);
		//Multiple Types Selection :: START
		if(sTypesSelected.contains("|")) {
			StringList slSelectedTypes = FrameworkUtil.split(sTypesSelected, "|");
			for(int n = 0; n < slSelectedTypes.size(); n++) {
				sSelectedTypes = slSelectedTypes.get(n);
				StringList slStatesList = new StringList();
								
				if(sSelectedTypes.equals("Change Order")) {
					sSelectedTypePolicy = "Fast track Change|Cancelled";
					StringList slSelectedTypePolicies = new StringList();
					slSelectedTypePolicies.add("Fast track Change");
					slSelectedTypePolicies.add("Cancelled");
					for(int l = 0; l < 2; l++) {
						slStatesList = new StringList();
						String sPolicy = slSelectedTypePolicies.get(l);
						String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
						StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
						for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
						}
						if(sPolicy.equals("Fast track Change"))
							mReturnPolicyStates.put("Change Order", slStatesList);
						if(sPolicy.equals("Cancelled"))
							mReturnPolicyStates.put("Cancelled", slStatesList);

					}
					//mReturnPolicyStates.put("Change Order", slStatesList);
				}
				else if(sSelectedTypes.equals("Change Request")) {
					sSelectedTypePolicy = "Change Request|Cancelled";
					StringList slSelectedTypePolicies = new StringList();
					slSelectedTypePolicies.add("Change Request");
					slSelectedTypePolicies.add("Cancelled");
					for(int l = 0; l < 2; l++) {
						slStatesList = new StringList();
						String sPolicy = slSelectedTypePolicies.get(l);
						String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
						StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
						for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
						}
						if(sPolicy.equals("Change Request"))
							mReturnPolicyStates.put("Change Request", slStatesList);
						if(sPolicy.equals("Cancelled"))
							mReturnPolicyStates.put("Cancelled", slStatesList);
					}					
				}	
				else if(sSelectedTypes.equals("ABB Product Design Issue")) {
					sSelectedTypePolicy = "ABB Issue RnD Process|ABB Issue Support Process";
					StringList slSelectedTypePolicies = new StringList();
					slSelectedTypePolicies.add("ABB Issue RnD Process");
					slSelectedTypePolicies.add("ABB Issue Support Process");
					for(int l = 0; l < 2; l++) {
						slStatesList = new StringList();
						String sPolicy = slSelectedTypePolicies.get(l);
						String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
						StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
						for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sPolicyDisplay = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Policy."+sPolicyKey);
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
						}
						mReturnPolicyStates.put(sPolicyDisplay, slStatesList);
					}
					
				}
				else {
					if(sSelectedTypes.equals("DOCUMENTS") || "DOCUMENTS".equals(getParentType(context, sSelectedTypes))) {
						iDocumentCounter++;
						if(iDocumentCounter <= 1) {
							sSelectedTypePolicy = "ABB Common Document|ABB Reference Document";
							StringList slSelectedTypePolicies = new StringList();
							slSelectedTypePolicies.add("ABB Common Document");
							slSelectedTypePolicies.add("ABB Reference Document");
							for(int l = 0; l < 2; l++) {
								slStatesList = new StringList();
								String sPolicy = slSelectedTypePolicies.get(l);
								String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
								StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
								for(int m = 0; m < slPolicyStates.size(); m++) {
									sStateActual = slPolicyStates.get(m);
									sStateActualKey = sStateActual.replaceAll(" ", "_");
									sPolicyKey = sPolicy.replaceAll(" ","_");
									sPolicyDisplay = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Policy."+sPolicyKey);
									sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
									sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
									slStatesList.add(sStateDisplay);
								}
								mReturnPolicyStates.put(sPolicyDisplay, slStatesList);
							}
						}
					}
					
					else if("Part".equals(getParentType(context, sSelectedTypes))) {
					
						boolean bIsRawMaterial = checkIfRawMaterialForPolicy(context, sSelectedTypes);
						
						if(!bIsRawMaterial) {											
							iPartCounter++;
							if(iPartCounter <= 1)
							{
								sSelectedTypePolicy = "EC Part";
								String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sSelectedTypePolicy, "state", "|");
								StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
								for(int m = 0; m < slPolicyStates.size(); m++) {
									sStateActual = slPolicyStates.get(m);
									sStateActualKey = sStateActual.replaceAll(" ", "_");
									sPolicyKey = sSelectedTypePolicy.replaceAll(" ","_");
									sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
									sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
									slStatesList.add(sStateDisplay);
								}						
								mReturnPolicyStates.put("Part", slStatesList);
							}
						}
						else {
							iRawMaterialCounter++;
							if(iRawMaterialCounter <= 1)
							{
								sSelectedTypePolicy = "ABB Raw Material";
								String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sSelectedTypePolicy, "state", "|");
								StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
								for(int m = 0; m < slPolicyStates.size(); m++) {
									sStateActual = slPolicyStates.get(m);
									sStateActualKey = sStateActual.replaceAll(" ", "_");
									sPolicyKey = sSelectedTypePolicy.replaceAll(" ","_");
									sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
									sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
									slStatesList.add(sStateDisplay);
								}						
								mReturnPolicyStates.put("Raw Material", slStatesList);
							}							
						}
					}
				}
				//								
			}
		}//Multiple Types Selection :: END
		
		//Single Type Selection :: START
		else {
			sSelectedTypes = sTypesSelected;
			StringList slStatesList = new StringList();
			//
			if(sSelectedTypes.equals("Change Order")) {
				sSelectedTypePolicy = "Fast track Change|Cancelled";
				StringList slSelectedTypePolicies = new StringList();
				slSelectedTypePolicies.add("Fast track Change");
				slSelectedTypePolicies.add("Cancelled");
				for(int l = 0; l < 2; l++) {
					slStatesList = new StringList();
					String sPolicy = slSelectedTypePolicies.get(l);
					String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
					StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
					for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
					}
					if(sPolicy.equals("Fast track Change"))
						mReturnPolicyStates.put("Change Order", slStatesList);
					if(sPolicy.equals("Cancelled"))
						mReturnPolicyStates.put("Cancelled", slStatesList);					
				}
				//mReturnPolicyStates.put("Change Order", slStatesList);
			}
			else if(sSelectedTypes.equals("Change Request")) {
				sSelectedTypePolicy = "Change Request|Cancelled";
				StringList slSelectedTypePolicies = new StringList();
				slSelectedTypePolicies.add("Change Request");
				slSelectedTypePolicies.add("Cancelled");
				for(int l = 0; l < 2; l++) {
					slStatesList = new StringList();
					String sPolicy = slSelectedTypePolicies.get(l);
					String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
					StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
					for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
					}
					if(sPolicy.equals("Change Request"))
						mReturnPolicyStates.put("Change Request", slStatesList);
					if(sPolicy.equals("Cancelled"))
						mReturnPolicyStates.put("Cancelled", slStatesList);
				}				
			}	
			else if(sSelectedTypes.equals("ABB Product Design Issue")) {
				sSelectedTypePolicy = "ABB Issue RnD Process|ABB Issue Support Process";
				StringList slSelectedTypePolicies = new StringList();
				slSelectedTypePolicies.add("ABB Issue RnD Process");
				slSelectedTypePolicies.add("ABB Issue Support Process");
				for(int l = 0; l < 2; l++) {
					slStatesList = new StringList();
					String sPolicy = slSelectedTypePolicies.get(l);
					String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
					StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
					for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sPolicyDisplay = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Policy."+sPolicyKey);
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
					}
					mReturnPolicyStates.put(sPolicyDisplay, slStatesList);
				}
				
			}
			else {
				if(sSelectedTypes.equals("DOCUMENTS") || "DOCUMENTS".equals(getParentType(context, sSelectedTypes))) {
					sSelectedTypePolicy = "ABB Common Document|ABB Reference Document";
					StringList slSelectedTypePolicies = new StringList();
					slSelectedTypePolicies.add("ABB Common Document");
					slSelectedTypePolicies.add("ABB Reference Document");
					for(int l = 0; l < 2; l++) {
						slStatesList = new StringList();
						String sPolicy = slSelectedTypePolicies.get(l);
						String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sPolicy, "state", "|");
						StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
						for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sPolicy.replaceAll(" ","_");
							sPolicyDisplay = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Policy."+sPolicyKey);
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
						}
						mReturnPolicyStates.put(sPolicyDisplay, slStatesList);
					}									
				}
				
				else if("Part".equals(getParentType(context, sSelectedTypes))) {				
					boolean bIsRawMaterial = checkIfRawMaterialForPolicy(context, sSelectedTypes);
					
					if(!bIsRawMaterial) {
						sSelectedTypePolicy = "EC Part";
						String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sSelectedTypePolicy, "state", "|");
						StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
						for(int m = 0; m < slPolicyStates.size(); m++) {
								sStateActual = slPolicyStates.get(m);
								sStateActualKey = sStateActual.replaceAll(" ", "_");
								sPolicyKey = sSelectedTypePolicy.replaceAll(" ","_");
								sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
								sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
								slStatesList.add(sStateDisplay);
						}						
						mReturnPolicyStates.put("Part", slStatesList);
					}					
					else {
						sSelectedTypePolicy = "ABB Raw Material";
						String sPolicyStateData = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", sSelectedTypePolicy, "state", "|");
						StringList slPolicyStates = FrameworkUtil.split(sPolicyStateData, "|");
						for(int m = 0; m < slPolicyStates.size(); m++) {
							sStateActual = slPolicyStates.get(m);
							sStateActualKey = sStateActual.replaceAll(" ", "_");
							sPolicyKey = sSelectedTypePolicy.replaceAll(" ","_");
							sFrameworkStateKey = "emxFramework.State."+sPolicyKey+"."+sStateActualKey;
							sStateDisplay = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), sFrameworkStateKey);
							slStatesList.add(sStateDisplay);
						}						
						mReturnPolicyStates.put("Raw Material", slStatesList);							
					}					
				}
			}
			//			
		}//Single Type Selection :: END
		
		System.out.println("sep 29 :: mReturnPolicyStates :: "+mReturnPolicyStates);
		return mReturnPolicyStates;		
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean checkParentRawMaterialAndMNSISSoftware(Context context,String sType)throws Exception
	{
		boolean bReturn = false;
       String paType = "";     
        try{
			if(sType.equals("ABB Part Raw Material") || sType.equals("ABB MNS iS Software")) {
				bReturn = true;
				return bReturn;
			}
			else {
				BusinessType busType= new BusinessType(sType,new Vault("ABB vault"));
				paType = busType.getParent(context);
				System.out.println("paType = "+paType);
				
				if(paType != null && paType.length()>0 && (paType.equals(PropertyUtil.getSchemaProperty(context, "type_Part")) || paType.equals("Change") ||  paType.equals("DOCUMENTS") || paType.equals("Issue"))) {
					bReturn = false;
					return bReturn;
				}

				else if(paType != null && paType.length()>0 && (paType.equals("ABB Part Raw Material") || paType.equals("ABB MNS iS Software")) ) {
					bReturn = true;
					return bReturn;
				}
				
				else {
					bReturn = checkParentRawMaterialAndMNSISSoftware(context,paType);
				}			
			}

        }
		catch(Exception e) {
           throw e;
        }
        return bReturn;
	}


	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean checkIfRawMaterialForPolicy(Context context,String sType) throws Exception
	{
		boolean bReturn = false;
       String paType = "";     
        try{
			if(sType.equals("ABB Part Raw Material")) {
				bReturn = true;
				return bReturn;
			}
			else {
				BusinessType busType= new BusinessType(sType,new Vault("ABB vault"));
				paType = busType.getParent(context);
				System.out.println("paType = "+paType);
				
				if(paType != null && paType.length()>0 && (paType.equals(PropertyUtil.getSchemaProperty(context, "type_Part")) || paType.equals("Change") ||  paType.equals("DOCUMENTS") || paType.equals("Issue"))) {
					bReturn = false;
					return bReturn;
				}

				else if(paType != null && paType.length()>0 && paType.equals("ABB Part Raw Material") ) {
					bReturn = true;
					return bReturn;
				}
				
				else {
					bReturn = checkIfRawMaterialForPolicy(context,paType);
				}			
			}
        }
		catch(Exception e) {
           throw e;
        }
        return bReturn;
	}	
}