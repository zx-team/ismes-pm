package com.isesol.mes.ismes.pm.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;

public class CxglActivity {
	private SimpleDateFormat sdf_time = new SimpleDateFormat("yyyyMMddHHmmss");
	/**
	 * 跳转程序管理页面
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String query_cxgl(Parameters parameters, Bundle bundle) {
		return "pm_cxxxgl";
	}
	
	public String table_cxxxgl(Parameters parameters, Bundle bundle){
		
		List<Map<String, Object>> ljxx_list = null;
		List<Map<String, Object>> gxxx_list = null;
		List<Map<String, Object>> sbxx_list = null;
		
		String query_ljbh = parameters.getString("query_ljbh"); // 零件编号 
		String query_ljmc = parameters.getString("query_ljmc"); // 零件名称 
		String query_gxbh = parameters.getString("query_gxbh"); // 工序编号
		String query_gxmc = parameters.getString("query_gxmc"); // 工序名称
		String query_cxmc = parameters.getString("query_cxmc"); // 程序名称
		String query_sbbh = parameters.getString("query_sbbh"); // 设备编号
		String query_sbmc = parameters.getString("query_sbmc"); // 设备名称
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(query_ljbh) || StringUtils.isNotBlank(query_ljmc)) 
		{
			parameters.set("ljbh",query_ljbh);
			parameters.set("ljmc",query_ljmc);
			Bundle ljBundle = Sys.callModuleService("pm", "queryLjxxByparam",parameters);
			ljxx_list = (List<Map<String, Object>>)ljBundle.get("ljxx");
			
			if(ljxx_list != null && ljxx_list.size() > 0){
				StringBuffer ljids = new StringBuffer();
				ljids.append("(");
				for(int i = 0, len = ljxx_list.size(); i < len; i++){
					ljids.append("\'");
					ljids.append(ljxx_list.get(i).get("ljid"));
					ljids.append("\'");
					if(i != (len - 1)){
						ljids.append(",");
					}
				}
				ljids.append(")");
				con = con + " and ljid in " + ljids.toString();
			} else {
				con = con + " and ljid = -1 ";
			}
		}
		if(StringUtils.isNotBlank(query_gxbh) || StringUtils.isNotBlank(query_gxmc)) 
		{
			String _con = "";
			List<Object> _val = new ArrayList<Object>();
			boolean flag = false;
			if(StringUtils.isNotBlank(query_gxbh)) 
			{
				_con = "gxbh like '%"+query_gxbh+"%'";
				flag = true;
			}
			if(StringUtils.isNotBlank(query_gxmc)) 
			{
				if(flag){
					_con += " and ";
				}
				_con += " gxmc like '%"+query_gxmc+"%'";
			}
			
			Dataset dataset_gxxx = Sys.query("pm_gxxxb","gxid,gxbh,gxmc",  _con, null, new Object[]{});
			gxxx_list = dataset_gxxx.getList();
			
			if(gxxx_list != null && gxxx_list.size() > 0){
				StringBuffer gxids = new StringBuffer();
				gxids.append("(");
				for(int i = 0, len = gxxx_list.size(); i < len; i++){
					gxids.append("\'");
					gxids.append(gxxx_list.get(i).get("gxid"));
					gxids.append("\'");
					if(i != (len -1)){
						gxids.append(",");
					}
				}
				gxids.append(")");
				con = con + " and gxid in " + gxids.toString();
			} else {
				con = con + " and gxid = -1 ";
			}
			
		}
		if(StringUtils.isNotBlank(query_cxmc))
		{
			con = con + " and cxmc like ? ";
			val.add("%"+query_cxmc+"%");
		}
		if(StringUtils.isNotBlank(query_sbbh) || StringUtils.isNotBlank(query_sbmc))
		{
			parameters.set("sbbh", query_sbbh);
			parameters.set("sbmc", query_sbmc);
			Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxxInfoBySbbh",parameters);
			sbxx_list = (List<Map<String, Object>>)sbBundle.get("sbxxList");
			
			if(sbxx_list != null && sbxx_list.size() > 0){
				StringBuffer sbids = new StringBuffer();
				sbids.append("(");
				for(int i = 0, len = sbxx_list.size(); i < len; i++){
					sbids.append("\'");
					sbids.append(sbxx_list.get(i).get("sbid"));
					sbids.append("\'");
					if(i != (len -1)){
						sbids.append(",");
					}
				}
				sbids.append(")");
				con = con + " and sbid in " + sbids.toString();
			} else {
				con = con + " and sbid = -1 ";
			}
		}
			
		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());
		
		Dataset datasetcxgl = Sys.query("pm_cxxxb","cxid,cxmc,sbid,ljid,gxid", con, null, (page-1)*pageSize, pageSize,val.toArray());
		
		List<Map<String, Object>> cxxx_list = datasetcxgl.getList();
		
		if(ljxx_list == null || ljxx_list.size() == 0){
			Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljmc,ljlbdm,ljtpid,clbh,ljbh,mpdh,fjdid,dw,zxsl,ljms", "1=1", null, new Object[]{});
			ljxx_list = dataset_ljxx.getList();
		}
		
		if(gxxx_list == null || gxxx_list.size() == 0){
			Dataset dataset_gxxx = Sys.query("pm_gxxxb","gxid,gxxh,gxbh,gxlxdm,gxmc", "1=1", null, new Object[]{});
			gxxx_list = dataset_gxxx.getList();
		}
		
		if(sbxx_list == null || sbxx_list.size() == 0){
			Bundle sbBundle = Sys.callModuleService("em", "emservice_all_sb",parameters);
			sbxx_list = (List<Map<String, Object>>)sbBundle.get("sbxxlist");
		}
		
		//整合零件信息
		for(int i = 0, len = cxxx_list.size(); i < len; i++){
			for(int j = 0, _len = ljxx_list.size(); j < _len; j++){
				if(cxxx_list.get(i).get("ljid").equals(ljxx_list.get(j).get("ljid"))){
					cxxx_list.get(i).put("ljbh", ljxx_list.get(j).get("ljbh"));
					cxxx_list.get(i).put("ljmc", ljxx_list.get(j).get("ljmc"));
				}
			}
		}
		
		//整合工序信息
		for(int i = 0, len = cxxx_list.size(); i < len; i++){
			for(int j = 0, _len = gxxx_list.size(); j < _len; j++){
				if(cxxx_list.get(i).get("gxid").equals(gxxx_list.get(j).get("gxid"))){
					cxxx_list.get(i).put("gxbh", gxxx_list.get(j).get("gxbh"));
					cxxx_list.get(i).put("gxmc", gxxx_list.get(j).get("gxmc"));
				}
			}
		}
		
		//整合设备信息
		for(int i = 0, len = cxxx_list.size(); i < len; i++){
			for(int j = 0, _len = sbxx_list.size(); j < _len; j++){
				if(cxxx_list.get(i).get("sbid").equals(sbxx_list.get(j).get("sbid"))){
					cxxx_list.get(i).put("sbbh", sbxx_list.get(j).get("sbbh"));
					cxxx_list.get(i).put("sbmc", sbxx_list.get(j).get("sbmc"));
				}
			}
		}
		
		bundle.put("rows", cxxx_list);
		int totalPage = datasetcxgl.getTotal()%pageSize==0?datasetcxgl.getTotal()/pageSize:datasetcxgl.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", datasetcxgl.getTotal());
		
		return "json:";
	}
	
	
	/**查询程序列表
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception 
	 */
	public String table_cxgl(Parameters parameters, Bundle bundle) throws Exception {

		String query_ljbh = parameters.getString("query_ljbh"); //  
		String query_gxbh = parameters.getString("query_gxbh"); //  
		String query_cxmc = parameters.getString("query_cxmc"); // 
		String query_cxmczd = parameters.getString("query_cxmczd"); // 
		String query_cxlx = parameters.getString("query_cxlx"); // 	   
		String query_cxlb = parameters.getString("query_cxlb");// 
		String query_cxzt = parameters.getString("query_cxzt");//
		String query_xfls = parameters.getString("query_xfls");//下发程序/历史程序标识
		String sortName = parameters.getString("sortName");  // 排序字段
		String sortOrder = parameters.getString("sortOrder");// 排序方式 asc  desc
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(query_ljbh)) 
		{
			con = con + " and ljbh like ? ";
			val.add("%"+query_ljbh+"%");
		}
		if(StringUtils.isNotBlank(query_gxbh)) 
		{
			con = con + " and gxbh like ? ";
			val.add("%"+query_gxbh+"%");
		}
		if(StringUtils.isNotBlank(query_cxmc))
		{
			con = con + " and cxmc like ? ";
			val.add("%"+query_cxmc+"%");
		}
		if(StringUtils.isNotBlank(query_cxmczd))
		{
			con = con + " and cxmczd like ? ";
			val.add("%"+query_cxmczd+"%");
		}
		if(StringUtils.isNotBlank(query_cxlx))
		{
			con = con + " and cxlx = ? ";
			val.add(query_cxlx);
		}
		if(StringUtils.isNotBlank(query_cxlb))
		{
			con = con + " and cxlb = ? ";
			val.add(query_cxlb);
		}
		if(StringUtils.isNotBlank(query_cxzt))
		{
			con = con + " and yxbz = ? ";
			val.add(query_cxzt);
		}
		if(StringUtils.isNotBlank(query_xfls))
		{
			if("0".equals(query_xfls))
			{
				con = con + " and sbid is null and zxbz= '1' ";
			}else if("1".equals(query_xfls))
			{
				con = con + " and sbid is not null and yxbz = '10' ";
			}else if("2".equals(query_xfls))
			{
				con = con + " and sbid is null  ";
			}
		}
		
		if(StringUtils.isNotBlank(sortName))
		{
			sortOrder = sortName + " "+sortOrder+" ";
		}else {
			sortOrder = "scsj desc";
		}
			
		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());
		Dataset datasetcxgl = Sys.query(new String[]{"pm_cxgl","pm_ljglb","pm_gxxxb","pm_file_info"}, "pm_cxgl left join pm_ljglb on pm_cxgl.ljid = pm_ljglb.ljid left join pm_gxxxb on pm_cxgl.gxid = pm_gxxxb.gxid left join pm_file_info on pm_file_info.wjid = pm_cxgl.cxwjid","cxid,cxmc,cxmczd,cxdbb,cxxbb,cxlx,cxlb,yggh,sbid,sbzbb,yxbz,pm_cxgl.ljid,pm_cxgl.gxid,scsj,zxbz,ljbh,gxbh,pm_cxgl.cxwjid,wjlj,wjmc,wjdx,wjlb", con, sortOrder, (page-1)*pageSize, pageSize,val.toArray());
		List<Map<String, Object>> cxxx = datasetcxgl.getList(true);
		for (int i = 0; i < cxxx.size(); i++) {
			cxxx.get(i).put("cxbb", "V"+cxxx.get(i).get("cxdbb")+"."+cxxx.get(i).get("cxxbb"));
			cxxx.get(i).put("sbbb", "V"+cxxx.get(i).get("cxdbb")+"."+cxxx.get(i).get("cxxbb")+"."+cxxx.get(i).get("sbzbb"));
		} 
		
		bundle.put("rows", cxxx);
		int totalPage = datasetcxgl.getTotal()%pageSize==0?datasetcxgl.getTotal()/pageSize:datasetcxgl.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", datasetcxgl.getTotal());
		return "json:";
	}


	
	/**插入程序信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String inster_cxxx(Parameters parameters, Bundle bundle) {
		String cxmc = parameters.getFile("add_cxwj").getName();
		String ContentType = parameters.getFile("add_cxwj").getContentType();
		long   wjdx = parameters.getFile("add_cxwj").getSize();
		String filetype = cxmc.substring((cxmc.lastIndexOf(".")),cxmc.length());
		Integer ljid = parameters.getInteger("add_ljid");
		Integer gxid = parameters.getInteger("add_gxbh");
//		String ljbh = parameters.getString("add_ljbh");
		String gxbh = parameters.getString("add_gxid");
		String  num = ""+Sys.getNextSequence("cx"+gxbh.replace("-", ""));
		if(num.length()==1)
		{
			num = "00"+num;
		}else if(num.length()==2){
			num = "0"+num;
		}
		String cxmczd = gxbh + "_" + num;
		String wjbcmc=cxmczd +filetype;
		String cxlx = parameters.getString("add_cxlx");
		String cxlb = parameters.getString("add_cxlb");
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("cxmc", cxmc);
		map.put("cxmczd", cxmczd);
		map.put("cxdbb", 1);
		map.put("cxxbb", 1);
		map.put("cxlx", cxlx);
		map.put("cxlb", cxlb);
		map.put("yggh", Sys.getUserIdentifier());
		map.put("yxbz", "10");
		map.put("ljid", ljid);
		map.put("gxid", gxid);
		map.put("scsj", new Date());
		map.put("zxbz", "1");
		map.put("wjlj", "/cxgl/"+wjbcmc);
		map.put("wjmc", wjbcmc);
		map.put("wjdx", wjdx);
		map.put("wjlb", ContentType);
		if("10".equals(cxlb))
		{
			Map<String, Object> mapup = new HashMap<String, Object>();
			mapup.put("cxlb", "20");
			Sys.update("pm_cxgl", mapup,  "gxid = ?  and yxbz='10' ",new Object[]{parameters.getInteger("add_gxbh")});
		}
		Sys.saveFile("/cxgl/"+wjbcmc, parameters.getFile("add_cxwj").getInputStream());
		Sys.insert("pm_file_info", map);
		map.put("cxwjid", map.get("wjid"));
		Sys.insert("pm_cxgl", map);
		
		
		String activityType = "4"; //动态任务
		String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
		String templateId = "cxsc_tp";
		Map<String, Object> data = new HashMap<String, Object>();
		// 查询零件信息
		Parameters parts = new Parameters();
		parts.set("ljid", ljid);
		Bundle ljBundle = Sys.callModuleService("pm", "partsInfoService", parts);
		if (ljBundle != null) {
			Map<String, Object> ljMap = (Map<String, Object>) ljBundle.get("partsInfo");
			if (ljMap != null) {
				data.put("ljbh", ljMap.get("ljbh"));// 零件编号
				data.put("ljmc", ljMap.get("ljmc"));// 零件名称
			}
		}
		// 查询工序信息 （工序编号）
		parts = new Parameters();
		parts.set("gxid", gxid);
		Bundle gxBundle = Sys.callModuleService("pm", "queryGxxxByGxid", parts);
		Map<String, Object> gxMap = (Map<String, Object>) gxBundle.get("gxxx");
		if (null != gxMap) {
			if (null !=  gxMap.get("gxbh") && StringUtils.isNotBlank( gxMap.get("gxbh").toString())) {
				data.put("gxbh", gxMap.get("gxbh"));// 工序编号
			}
			if (null != gxMap.get("gxmc") && StringUtils.isNotBlank(gxMap.get("gxmc").toString())) {
				data.put("gxmc", gxMap.get("gxmc"));// 工序名称
			}
		}
		
		//查询零件图片
		parameters.set("ljid", ljid);
		Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", parameters);
		Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
		String cxlxmc = "";
		if("10".equals(cxlx)){
			cxlxmc = "派发程序";
		}
		else if("20".equals(cxlx)){
			cxlxmc = "非派发程序";
		}
		data.put("cxlx", cxlxmc);//程序类型 
		String cxlbmc = "";
		if("10".equals(cxlx)){
			cxlbmc = "主程序";
		}
		else if("20".equals(cxlx)){
			cxlbmc = "子程序";
		}
		data.put("cxlb", cxlbmc);//程序类别
		data.put("cxmc", cxmc);//程序名称
		data.put("ljtp", ljtp);//零件图片
		data.put("userid", Sys.getUserIdentifier());//操作人标识
		data.put("username", Sys.getUserName());//操作人名称
		sendActivity(activityType, templateId, true, roles, null, null, data);
		
		
		return null;
	}
	
	/**下载程序
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String download_cxxx(Parameters parameters, Bundle bundle) {
		String cxmc = parameters.getString("cxmc");
		String wjlj = parameters.getString("wjlj");
		int    wjdx = parameters.getInteger("wjdx");
		String wjlb = parameters.getString("wjlb");
		File file_cxxx= new File(cxmc, null,Sys.readFile(wjlj) , wjlb,  wjdx);
		bundle.put("file_cxxx", file_cxxx);
		return "file:file_cxxx";
	}
	
	/**更新程序信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String update_cxxx(Parameters parameters, Bundle bundle) {
		String cxid = parameters.getString("edit_cxid");
		String cxlx = parameters.getString("edit_cxlx");
		String cxlb = parameters.getString("edit_cxlb");
		if("10".equals(cxlb))
		{
			Map<String, Object> mapup = new HashMap<String, Object>();
			mapup.put("cxlb", "20");
			Sys.update("pm_cxgl", mapup,  "gxid = ? and cxid != ? and yxbz='10' ",new Object[]{parameters.getString("edit_gxid"),cxid});
		}
		Map<String, Object> map = new HashMap<String, Object>(); 
		if (null!=parameters.getFile("edit_cxwj")) {
			map.put("zxbz", 0);
			map.put("yxbz", 20);
			Sys.update("pm_cxgl", map, "cxid = ? ",new Object[]{cxid});
			String cxmc = parameters.getFile("edit_cxwj").getName();
			String ljid = parameters.getString("edit_ljid");
			String gxid = parameters.getString("edit_gxid");
			String cxmczd = parameters.getString("edit_cxmczd");
			Integer cxdbb = parameters.getInteger("edit_cxdbb");
			Integer cxxbb = parameters.getInteger("edit_cxxbb");
			String filetype = cxmc.substring((cxmc.lastIndexOf(".")),cxmc.length());
			String wjbcmc=cxmczd + filetype;
			long   wjdx = parameters.getFile("edit_cxwj").getSize();
			String ContentType = parameters.getFile("edit_cxwj").getContentType();
			map.put("cxmc", cxmc);
			map.put("cxmczd", cxmczd);
			map.put("cxdbb", cxdbb);
			map.put("cxxbb", cxxbb+1);
			map.put("cxlx", cxlx);
			map.put("cxlb", cxlb);
			map.put("yggh", Sys.getUserIdentifier());
			map.put("yxbz", "10");
			map.put("ljid", ljid);
			map.put("gxid", gxid);
			map.put("scsj", new Date());
			map.put("zxbz", "1");
			map.put("wjlj", "/cxgl/"+wjbcmc);
			map.put("wjmc", wjbcmc);
			map.put("wjdx", wjdx);
			map.put("wjlb", ContentType);
			Sys.saveFile("/cxgl/"+wjbcmc, parameters.getFile("edit_cxwj").getInputStream());
			Sys.insert("pm_file_info", map);
			map.put("cxwjid", map.get("wjid"));
			Sys.insert("pm_cxgl", map);
		}else{
			
			map.put("cxlx", cxlx);
			map.put("cxlb", cxlb);
			Sys.update("pm_cxgl", map, "cxid = ? ",new Object[]{cxid});
		}
		
		
		return null;
	}
	
	public String check_cxxx(Parameters parameters, Bundle bundle) {
		String con = "";
		if (null!=parameters.get("cxid")) {
			con = con + "cxid != " + parameters.get("cxid")+" and ";
		}
		
		Dataset datasetcxgl = Sys.query("pm_cxgl","cxid", con+" cxlb = '10' and yxbz='10' and gxid = ?", null, new Object[]{parameters.get("gxid")});
		if (datasetcxgl.getList().size()>0) {
			bundle.put("data", true);
		}else{
			bundle.put("data", false);
		}
		return "json:data";
	}
	
	/**删除程序信息
	 * @param parameters
	 * @param bundle
	 */
	public void del_cxxx(Parameters parameters, Bundle bundle) {
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("yxbz", parameters.getString("yxbz"));
		try {
			Sys.update("pm_cxgl", map, "cxid = ? ",new Object[]{parameters.getString("cxid")});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getGxxxByID(Parameters parameters,Bundle bundle){
		
		String gxid = parameters.get("gxid").toString();
		if(StringUtils.isEmpty(gxid)){
			return "json:";
		}
		Dataset dataset = Sys.query("pm_gxxxb","gxmc","gxid = " + gxid,null,new Object[]{});
		Map<String, Object> map = dataset.getMap();
		bundle.put("data", map);
		return "json:";
	}
	
	/**根据零件ID查询工序下拉框
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gxxxSelect(Parameters parameters,Bundle bundle){
		if (null==parameters.get("ljid")) {
			return "json";
		}
		int ljid = Integer.parseInt(parameters.get("ljid").toString());
		Dataset datasetcxgl = Sys.query(new String[]{"pm_ljglb","pm_gyb","pm_gygxglb","pm_gxxxb"}, "pm_ljglb left join pm_gyb on pm_ljglb.ljid = pm_gyb.ljid left join pm_gygxglb on pm_gyb.gyid = pm_gygxglb.gyid left join pm_gxxxb on pm_gygxglb.gxid = pm_gxxxb.gxid ","pm_gxxxb.gxbh  ,pm_gxxxb.gxid ", "pm_gyb.gyztdm = '10' and pm_gxxxb.gxid is not null  and pm_ljglb.ljid = ?", null,new Object[]{ljid});
		List<Map<String, Object>> cxxx = datasetcxgl.getList();
		for (int i = 0; i < cxxx.size(); i++) {
			cxxx.get(i).put("label", cxxx.get(i).get("gxbh"));
			cxxx.get(i).put("value", cxxx.get(i).get("gxid"));
			cxxx.get(i).remove("gxid");
		}
		bundle.put("cxxx", cxxx);
		return "json:cxxx";
	}
	/** 根据零件编号模糊查询零件信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String ljxxSelect(Parameters parameters,Bundle bundle){
		Dataset datasetljxx = Sys.query("pm_ljglb","ljid,ljbh,ljmc,ljlbdm,clbh,mpdh,ljtpid,fjdid,ljms", "ljbh like '%"+parameters.get("query").toString()+"%'", null, new Object[]{});
		List<Map<String, Object>> list = datasetljxx.getList();
		for (int i = 0; i < list.size(); i++) {
			list.get(i).put("label", list.get(i).get("ljbh"));
			list.get(i).put("value", list.get(i).get("ljid"));
			list.get(i).put("title", list.get(i).get("ljbh"));
		}		
		bundle.put("list", list);
		return "json:list";
	}
	
	public String sbxxSelect(Parameters parameters,Bundle bundle){		
		String param = parameters.get("query").toString();
		parameters.set("sbbh", param);
		Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxxInfoBySbbh",parameters);
		List<Map<String, Object>> list = (List<Map<String, Object>>)sbBundle.get("sbxxList");
		for (int i = 0; i < list.size(); i++) {
			list.get(i).put("label", list.get(i).get("sbbh"));
			list.get(i).put("value", list.get(i).get("sbid"));
			list.get(i).put("title", list.get(i).get("sbbh"));
		}	
		bundle.put("list", list);
		return "json:list";
	}
	
	public String saveInfo(Parameters parameters,Bundle bundle){
		
		String cxid = parameters.get("cxid").toString();
		String cxmc = parameters.get("cxmc").toString();
		String ljid = parameters.get("ljid").toString();
		String gxid = parameters.get("gxid").toString();
		String sbid = parameters.get("sbid").toString();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cxmc", cxmc);
		map.put("ljid", ljid);
		map.put("gxid", gxid);
		map.put("sbid", sbid);
		int n = 0;
		
		//修改程序
		if(StringUtils.isNotBlank(cxid)){
			n = Sys.update("pm_cxxxb", map, "cxid = ?", new Object[]{cxid});	
		} 
		//添加程序
		else {
			n = Sys.insert("pm_cxxxb", map);	
		}
	
		bundle.put("data", "success");
		
		return "json:";
	}
	
	public String delInfo(Parameters parameters,Bundle bundle){
		
		String cxid = parameters.get("cxid").toString();
		
		int n = Sys.delete("pm_cxxxb", "cxid = ?", new Object[]{cxid});
		
		bundle.put("data", "success");
		
		return "json:";
	}
	
	private Bundle sendActivity(String type, String templateId, boolean isPublic, String[] roles, String[] users, String[] group,
			Map<String, Object> data) {
		String PARAMS_TYPE = "type";
		String PARAMS_TEMPLATE_ID = "template_id";
		String PARAMS_PUBLIC = "public";
		String PARAMS_ROLE = "role";
		String PARAMS_USER = "user";
		String PARAMS_GROUP = "group";
		String PARAMS_DATA = "data";
		String SERVICE_NAME = "activity";
		String METHOD_NAME = "send";
		Parameters parameters = new Parameters();
		parameters.set(PARAMS_TYPE, type);
		parameters.set(PARAMS_TEMPLATE_ID, templateId);
		if (isPublic)
			parameters.set(PARAMS_PUBLIC, "1");
		if (roles != null && roles.length > 0)
			parameters.set(PARAMS_ROLE, roles);
		if (users != null && users.length > 0)
			parameters.set(PARAMS_USER, users);
		if (group != null && group.length > 0)
			parameters.set(PARAMS_GROUP, group);
		if (data != null && !data.isEmpty())
			parameters.set(PARAMS_DATA, data);
		return Sys.callModuleService(SERVICE_NAME, METHOD_NAME, parameters);
	}
}




