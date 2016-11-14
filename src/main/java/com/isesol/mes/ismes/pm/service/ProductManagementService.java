package com.isesol.mes.ismes.pm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.pm.constant.ModelState;
import com.isesol.mes.ismes.pm.constant.SqlConstant;
import com.isesol.mes.ismes.pm.constant.TableConstant;

public class ProductManagementService {
	
	private Logger log4j = Logger.getLogger(ProductManagementService.class);
	
	/**
	 * 接口 根据零件id查询零件信息
	 */
	public void queryPartsInfoByCode(Parameters parameters, Bundle bundle) {
		String ljid = parameters.get("ljid").toString();
		Dataset dataSet = Sys.query("pm_ljglb",
				"ljid,ljbh,"//零件图号
				+ "ljmc,"//零件名称
				+ "ljlbdm,"//零件类别代码
				+ "dw,"//单位
				+ "clbh,"//材料规格
				+ "clyd,"//材料硬度
				+ "clyddw,"//材料硬度单位
				+ "mpdh,"//零件简码
				+ "ljtpid,"//零件图片ID
				+ "fjdid,"//父节点
				+ "ljms,"//零件描述
				+ "ljxh,"//零件项号
				+ "zxsl,"//装箱数量
				+ "cpzl,"//成品重量(kg)
				+ "dezl,"//定额重量(kg)
				+ "tzbb,"//图纸版本
				+ "cpcd,"//成品长度(mm)
				+ "jgzt",//加工状态
				"ljid = ?", null, null, ljid);
		Map<String, Object> partsInfoMap = dataSet.getMap();
		partsInfoMap.put("url", Sys.getAbsoluteUrl("/service/pic_display?wjid="+partsInfoMap.get("ljtpid")));
		if(partsInfoMap.get("jgzt") != null && !"".equals(partsInfoMap.get("jgzt").toString())){
			String jgzt = partsInfoMap.get("jgzt").toString();
			String jgzt_str = "";
			if("10".equals(jgzt)){
				jgzt_str = "-0";
			}
			if("20".equals(jgzt)){
				jgzt_str = "-2";
			}
			if("30".equals(jgzt)){
				jgzt_str = "-0,-2";
			}
			partsInfoMap.put("jgzt_str",jgzt_str);
		}
		bundle.put("partsInfo", partsInfoMap);
	}
	
	/**根据ID查询零件信息
	 * @param parameters
	 * @param bundle
	 * @return
	 * @update 2016/8/8 杨帆 ：增加单位字段
	 */
	public void query_ljxx(Parameters parameters, Bundle bundle) {
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljmc,ljlbdm,ljtpid,clbh,ljbh,mpdh,fjdid,dw,zxsl,ljms ", "ljid in "+parameters.get("val_lj").toString(), null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	public void query_gxgdgl(Parameters parameters, Bundle bundle) {
		Dataset dataset_ljxx = Sys.query("pm_gygxglb","gygxglid,gyid,gxid,qxid", "ljid in "+parameters.get("val_lj").toString(), null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	public void query_gygx(Parameters parameters, Bundle bundle) {
		Dataset dataset_gygx = Sys.query("pm_gygxglb","gygxglid,gyid,gxid,qxid", "gxid in "+parameters.get("val_gx").toString(), null, new Object[]{});
		
		List<Map<String, Object>> gygx = dataset_gygx.getList();
		
		String val_gy = "(";
		for (int i = 0; i < gygx.size(); i++) {
			if(i!=0)
			{
				val_gy = val_gy +",";
			}
			val_gy += "'" +gygx.get(i).get("gyid")+"'";
		}
		val_gy = val_gy +")";
		
		Dataset dataset_gyxx = Sys.query(new String[]{"pm_gygxglb","pm_gxxxb"}, " pm_gxxxb left join pm_gygxglb on pm_gygxglb.gxid = pm_gxxxb.gxid ","pm_gygxglb.gyid,sum(jgfs)  zjgsj", "gyid in "+val_gy, "gyid",null,new Object[]{});
		List<Map<String, Object>> gyxx = dataset_gyxx.getList();
//		Dataset dataset_gyxx1 = Sys.query(new String[]{"pm_gygxglb","pm_gxxxb"}, " pm_gxxxb left join pm_gygxglb on pm_gygxglb.gxid = pm_gxxxb.gxid ","pm_gygxglb.gyid ,jgfs", "gyid in "+val_gy, null,new Object[]{});
//		List<Map<String, Object>> gyxx1 = dataset_gyxx1.getList();
//		for (int i = 0; i < gyxx.size(); i++) {
//			gyxx.get(i).put("zjgsj",0);
//			for (int j = 0; j < gyxx1.size(); j++) {
//				gyxx.get(i).put("zjgsj",Integer.parseInt(gyxx.get(i).get("zjgsj").toString())+ Integer.parseInt(gyxx1.get(j).get("jgfs").toString()));
//			}
//		}
		
		Dataset dataset_gxxx = Sys.query(new String[]{"pm_gygxglb","pm_gxxxb"}, " pm_gxxxb left join pm_gygxglb on pm_gygxglb.gxid = pm_gxxxb.gxid ","pm_gxxxb.gxid ,jgfs,qxid,gyid", "gyid in "+val_gy, null,new Object[]{});
		List<Map<String, Object>> gxxx = dataset_gxxx.getList();
		for (int i = 0; i < gxxx.size(); i++) {
			for (int j = 0; j < gyxx.size(); j++) {
				
				if (gxxx.get(i).get("gyid").toString().equals(gyxx.get(j).get("gyid").toString())) {
					gxxx.get(i).put("zjgsj",gyxx.get(j).get("zjgsj"));
					gxxx.get(i).put("percent_jd",""+(Math.round((Integer.parseInt(gxxx.get(i).get("jgfs").toString())*100)/Integer.parseInt(gxxx.get(i).get("zjgsj").toString()))/100.0));
					gxxx.get(i).put("jgsl",0);
					gxxx.get(i).put("wcsl",0);
				}
			}
		}
		bundle.put("gxxx", gxxx);
	}

	/**根据编号查询零件信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_ljxx_by_ljbh(Parameters parameters, Bundle bundle) {
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljbh,ljmc,ljlbdm,clbh,mpdh,ljtpid,fjdid,ljms", "ljbh like '%"+parameters.get("query").toString()+"%'", null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	/**根据编号查询零件项号
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_ljxx_by_ljxh(Parameters parameters, Bundle bundle) {
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljbh,ljmc,ljlbdm,clbh,mpdh,ljtpid,fjdid,ljms,ljxh", "ljxh like '%"+parameters.get("query").toString()+"%'", null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	/**
	 * 根据条件查询零件信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_ljxx_by_param(Parameters parameters, Bundle bundle) {
		String ljbh = parameters.get("ljbh").toString();
		String ljmc = parameters.get("ljmc").toString();
		String con = "";
		boolean flag = false;
		if(StringUtils.isNotBlank(ljbh)) 
		{
			con = "ljbh like '%"+ljbh+"%'";
			flag = true;
		}
		if(StringUtils.isNotBlank(ljmc)) 
		{
			if(flag){
				con += " and ";
			}
			con += " ljmc like '%"+ljmc+"%'";
		}
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljbh,ljmc,ljlbdm,clbh,mpdh,ljtpid,fjdid,ljms", con, null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	
	/**通过工序ID查询工歩信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gbxx(Parameters parameters, Bundle bundle) {
		Dataset dataset_gbxx = Sys.query("pm_gbxxb","gbid,gbxh,gxid,djtzbh,djid,jggcms,qxzj_min,qxzj_max,zzzs,jgl,dw,dpxhid", "gxid = ? ",
				null,"gbxh", new Object[]{parameters.get("gxid").toString()});
		bundle.put("gbxx", dataset_gbxx.getList());
	}
	
	/**通过工序ID查询程序信息（只查询最新版本、主程序）废弃了不要用
	 * @param parameters
	 * @param bundle
	 */
	public void query_cxxx(Parameters parameters, Bundle bundle) {
		Dataset dataset_cxxx = Sys.query("pm_cxgl","cxid,cxmczd,cxdbb,cxxbb,cxlx,cxlb,yggh,sbid,sbzbb,yxbz,ljid,gxid,scsj,zxbz,cxwjid", "zxbb = 1 and cxlb = '10' and gxid = ? ", null, new Object[]{parameters.get("gxid").toString()});
		bundle.put("cxxx", dataset_cxxx.getList());
	}
	
	/**
	 * 根据零件id查询零件的工序列表信息
	 * 一个零件有一个有效的工艺
	 * 一个工艺有多个工序
	 * 工序按照前序后续排列
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxxx_by_ljid(Parameters parameters, Bundle bundle){
		String [] models = {TableConstant.零件管理表,TableConstant.工序信息表,
				TableConstant.工艺工序管理表,TableConstant.工艺表};
		String join = new StringBuffer()
				.append(TableConstant.零件管理表).append(SqlConstant.JOIN).append(TableConstant.工艺表)
				.append(SqlConstant.ON).append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljid")
				.append(SqlConstant.EQUALS).append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("ljid")
				
				.append(SqlConstant.JOIN).append(TableConstant.工艺工序管理表).append(SqlConstant.ON)
				.append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("gyid").append(SqlConstant.EQUALS)
				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("gyid")
				
				.append(SqlConstant.JOIN).append(TableConstant.工序信息表).append(SqlConstant.ON)
				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("gxid").append(SqlConstant.EQUALS)
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").toString();
		
		String fields = new StringBuffer()
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").append(SqlConstant.COMMA)//工序id
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxxh").append(SqlConstant.COMMA)//工序序号
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxbh").append(SqlConstant.COMMA)//工序编号
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxmc").append(SqlConstant.COMMA)//工序名称
//				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("jgjp").append(SqlConstant.COMMA)//加工节拍
//				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("zbsj").append(SqlConstant.COMMA)//准备时间
//				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("cxmc").append(SqlConstant.COMMA)//程序名称
				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("qxid").toString();//.append(SqlConstant.COMMA)//前序
		
		//String conditions = TableConstant.零件管理表 +".ljid = ? ";
		StringBuffer condition = new StringBuffer(" 1= 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		boolean flag = true;
		String orderby = null;
		String ljid = parameters.getString("ljid");
		if(StringUtils.isNotBlank(ljid)){
			condition = condition.append(" and " +  TableConstant.零件管理表 +".ljid = ? ");
			condition = condition.append(" and " + TableConstant.工艺表 + ".gyztdm = '10' and " + TableConstant.工艺表 + ".jmztdm = '" + ModelState.已发布 + "' ");
			conditionValue.add(ljid);
			flag = false;
		}
		String gyid = parameters.getString("gyid");
		if(StringUtils.isNotBlank(gyid)){
			condition = condition.append(" and " +  TableConstant.工艺表 +".gyid = ? ");
			conditionValue.add(gyid);
			flag = false;
		}
		
		if(flag){
			condition = condition.append(" and  1 = 0 ");
		}
		
		Dataset dataset = Sys.query(models, join, fields, condition.toString(), orderby, conditionValue.toArray());
		//工序信息
		List<Map<String,Object>> listGx = dataset.getList();
		if(CollectionUtils.isEmpty(listGx)){
			bundle.put("gxList", null);
			return;
		}
		LinkedList<Map<String,Object>> linkedList = new LinkedList<Map<String,Object>>();
		int num = 0;
		while(true){
			for(Map<String,Object> map : listGx){
				//先加前序为空的
				if(CollectionUtils.isEmpty(linkedList)){
					if(map.get("qxid") == null || "".equals(map.get("qxid").toString())){
						linkedList.add(map);
						continue;
					}
				}else{
					if(map.get("qxid") == null || "".equals(map.get("qxid").toString())){
						continue;
					}
					String qxid = map.get("qxid").toString();
					if(linkedList.getLast().get("gxid").toString().equals(qxid)){
						linkedList.add(map);
						continue;
					}
				}
			}
			if(linkedList.size() == listGx.size()){
				break;
			}
			num ++ ;
			if(num == 1000){
				break;
			}
		}
		bundle.put("gxList", linkedList);
	}
	
	/**根据零件编号、零件名称和零件类别，查询零件信息 
	 * @param parameters
	 * @param bundle
	 * @return
	 * 
	 */
	public void query_ljxxbybhmc(Parameters parameters, Bundle bundle) {
		String query_ljbh = parameters.getString("query_ljbh"); // 零件编号
		String query_ljmc = parameters.getString("query_ljmc"); // 零件名称
		String query_ljlb = parameters.getString("query_ljlb"); // 零件类别
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(query_ljbh)) 
		{
			con = con + " and ljbh like ? ";
			val.add("%"+query_ljbh+"%");
		}
		if(StringUtils.isNotBlank(query_ljmc))
		{
			con = con + " and ljmc like ? ";
			val.add("%"+query_ljmc+"%");
		}
		if(StringUtils.isNotBlank(query_ljlb))
		{
			con = con + " and ljlbdm = ? ";
			val.add(query_ljlb);
		}
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljbh,ljmc,ljlbdm,dw,clbh,clyd,mpdh,ljtpid,fjdid,ljms", con, null, val.toArray());
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	/**根据零件编号、零件名称和零件类别，查询零件信息 
	 * @param parameters
	 * @param bundle
	 * @return
	 * 
	 */
	public void query_ljxxbybhxh(Parameters parameters, Bundle bundle) {
		String query_ljbh = parameters.getString("query_ljbh"); // 零件编号
		String query_ljmc = parameters.getString("query_ljmc"); // 零件名称
		String query_ljxh = parameters.getString("query_ljxh"); // 零件名称
		String query_ljlb = parameters.getString("query_ljlb"); // 零件类别
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(query_ljbh)) 
		{
			con = con + " and ljbh like ? ";
			val.add("%"+query_ljbh+"%");
		}
		if(StringUtils.isNotBlank(query_ljxh)) 
		{
			con = con + " and ljxh like ? ";
			val.add("%"+query_ljxh+"%");
		}
		if(StringUtils.isNotBlank(query_ljmc))
		{
			con = con + " and ljmc like ? ";
			val.add("%"+query_ljmc+"%");
		}
		if(StringUtils.isNotBlank(query_ljlb))
		{
			con = con + " and ljlbdm = ? ";
			val.add(query_ljlb);
		}
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljbh,ljxh,ljmc,ljlbdm,dw,clbh,clyd,mpdh,ljtpid,fjdid,ljms", con, null, val.toArray());
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	/**
	 * 通过能力组id  查询 工序信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxxx_by_nlzid(Parameters parameters, Bundle bundle){
		String nlzid = parameters.getString("nlzid");
		if(StringUtils.isBlank(nlzid)){
			bundle.put("", null);
			return;
		}
		String [] models = {TableConstant.工序信息表,TableConstant.工序能力组关联表};
		String join = new StringBuffer()
				.append(TableConstant.工序信息表).append(SqlConstant.JOIN).append(TableConstant.工序能力组关联表)
				.append(SqlConstant.ON).append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid")
				.append(SqlConstant.EQUALS).append(TableConstant.工序能力组关联表).append(SqlConstant.PERIOD).append("gxid")
				.toString();
		
		String fields = new StringBuffer()
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").toString();
		
		String conditions = TableConstant.工序能力组关联表 +".nlzid = ? ";
		String orderby = null;
		Dataset dataset = Sys.query(models, join, fields, conditions, orderby, new Object[]{nlzid});
		List<Map<String,Object>> list = dataset.getList();
		bundle.put("gxList", list);
	}
	
	/**根据设备ID 或者 工序ID查询
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	public void query_cxglbysbgx(Parameters parameters, Bundle bundle) throws Exception {
		String sbid = parameters.getString("sbid"); //  
		String gxid = parameters.getString("gxid"); //  
		String zxbz = parameters.getString("zxbz"); // 
		String con = "";
		if("1".equals(zxbz))
		{
			con = "zxbz = '1' and ";
		}
		Dataset datasetcxgl = null;
		List<Map<String, Object>> cxxx = new ArrayList<Map<String,Object>>();
		if (StringUtils.isNotBlank(sbid)) {
			datasetcxgl = Sys.query("pm_cxgl","cxid,cxmc,cxmczd,cxdbb,cxxbb,cxlx,cxlb,yggh,sbid,sbzbb,yxbz,ljid,gxid,scsj,zxbz,cxwjid", con + "yxbz = '10' and cxlb='10' and sbid = ?", null, new Object[]{sbid});
			cxxx = datasetcxgl.getList();
		}
		if (cxxx.size()<=0&&StringUtils.isNotBlank(gxid)) {
			datasetcxgl = Sys.query("pm_cxgl","cxid,cxmc,cxmczd,cxdbb,cxxbb,cxlx,cxlb,yggh,sbid,sbzbb,yxbz,ljid,gxid,scsj,zxbz,cxwjid", con + "yxbz = '10' and cxlx='10' and cxlb='10' and gxid = ?", null, new Object[]{gxid});
			cxxx = datasetcxgl.getList();
			for (int i = 0; i < cxxx.size(); i++) {
				cxxx.get(i).put("cxbb", "V"+cxxx.get(i).get("cxdbb")+"."+cxxx.get(i).get("cxxbb"));
			} 
		}else{
			for (int i = 0; i < cxxx.size(); i++) {
				cxxx.get(i).put("cxbb", "V"+cxxx.get(i).get("cxdbb")+"."+cxxx.get(i).get("cxxbb")+"."+cxxx.get(i).get("sbzbb"));
			} 
		}
		bundle.put("cxxx", cxxx);
	}
	
	/**根据设备ID 或者 工序ID查询
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	public void query_cxgl(Parameters parameters, Bundle bundle) throws Exception {
		String sbid = parameters.getString("sbid"); //  
		String gxid = parameters.getString("gxid"); //  
		String zxbz = parameters.getString("zxbz"); // 
		String cxlx = parameters.getString("cxlx"); //  
		StringBuffer sb = new StringBuffer(" yxbz = '10' ");
		List<Object> valueList1 = new ArrayList<Object>();
		List<Object> valueList2 = new ArrayList<Object>();
		if(StringUtils.isNotBlank(zxbz)){
			sb = sb.append(" and zxbz = ? ");
			valueList1.add(zxbz);
			valueList2.add(zxbz);
		}
		if(StringUtils.isNotBlank(cxlx)){
			sb = sb.append(" and cxlx = ? ");
			valueList1.add(cxlx);
			valueList2.add(cxlx);
		}
		Dataset datasetcxgl = null;
		List<Map<String, Object>> cxxx = new ArrayList<Map<String,Object>>();
		if (StringUtils.isNotBlank(sbid)) {
			valueList1.add(sbid);
			datasetcxgl = Sys.query("pm_cxgl","cxid,cxmc,cxmczd,cxdbb,cxxbb,cxlx,cxlb,yggh,"
					+ "sbid,sbzbb,yxbz,ljid,gxid,scsj,zxbz,cxwjid", sb.toString() + " and sbid = ? ",
					null,valueList1.toArray());
			cxxx = datasetcxgl.getList();
		}
		if (cxxx.size()<=0&&StringUtils.isNotBlank(gxid)) {
			sb = sb.append(" and gxid = ? ");
			valueList2.add(gxid);
			datasetcxgl = Sys.query("pm_cxgl","cxid,cxmc,cxmczd,cxdbb,"
					+ "cxxbb,cxlx,cxlb,yggh,sbid,sbzbb,yxbz,ljid,gxid,scsj,zxbz,cxwjid", sb.toString(), null,valueList2.toArray());
			cxxx = datasetcxgl.getList();
			for (int i = 0; i < cxxx.size(); i++) {
				cxxx.get(i).put("cxbb", "V"+cxxx.get(i).get("cxdbb")+"."+cxxx.get(i).get("cxxbb"));
			} 
		}else{
			for (int i = 0; i < cxxx.size(); i++) {
				cxxx.get(i).put("cxbb", "V"+cxxx.get(i).get("cxdbb")+"."+cxxx.get(i).get("cxxbb")+"."+cxxx.get(i).get("sbzbb"));
			} 
		}
		if(CollectionUtils.isNotEmpty(cxxx)){
			for(Map<String, Object> m : cxxx ){
				if(m.get("cxwjid") != null && StringUtils.isNotBlank(m.get("cxwjid").toString())){
					Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjlb,wjdx", " wjid = ? ", 
							null, new Object[]{m.get("cxwjid").toString()});
					Map<String,Object> map = dataset.getMap();
					File file = new File((String)map.get("wjmc"),null,
							Sys.readFile((String)map.get("wjlj")),
							(String)map.get("wjlb"),Long.valueOf(map.get("wjdx").toString()));
					m.put("cxwj", file);
				}
			}
		}
		bundle.put("cxxx", cxxx);
	}
	
	/**
	 * 通过工序id 得到使用物料的list
	 * @param parameters
	 * @param bundle
	 */
	public void wlListByGxid(Parameters parameters, Bundle bundle){
		String gxid = parameters.getString("gxid");
		String wlqfdm = parameters.getString("wlqfdm");
		if(StringUtils.isBlank(gxid)){
			log4j.info("工序id ==" + gxid +";");
			bundle.put("wllist", null);
			return;
		}
		String condition = " gxid = ? and wlqfdm = ? ";
		Object[] params = new Object[] { gxid, wlqfdm };
		if(StringUtils.isBlank(wlqfdm)){
			condition = " gxid = ? ";
			params = new Object[] { gxid };
		}
		
		Dataset dataset = Sys.query(TableConstant.工序物料关联表, "wlid,glid,gxid,wlqfdm,wlsl", condition,
				null , params);
		//物料id s		
		List<Map<String,Object>> wlList = dataset.getList();
		bundle.put("wllist", wlList);
	}
	
	@SuppressWarnings("unchecked")
	public void insertFile(Parameters parameters, Bundle bundle){
		Map<String, Object> map =  (Map<String, Object>) parameters.get("infoMap");
		Sys.insert("pm_file_info", map);
		bundle.put("wjid", map.get("wjid"));
	}
	
	/**
	 * 通过工序id  查询 能力id
	 * @param parameters
	 * @param bundle
	 */
	public void query_nlzids_by_gxid(Parameters parameters, Bundle bundle) {
		String gxid = parameters.getString("gxid");
		if (StringUtils.isBlank(gxid)) {
			bundle.put("", null);
			return;
		}
		Dataset dataset = Sys.query(TableConstant.工序能力组关联表, "nlzid", TableConstant.工序能力组关联表 + ".gxid = ? ", null, new Object[] { gxid });
		List<Map<String, Object>> list = dataset.getList();
		bundle.put("nlzIds", list);
	}
	
	/**
	 * @param parameters
	 * @param bundle
	 */
	public void query_fileInfo(Parameters parameters, Bundle bundle) {
		Dataset dataset_fileInfo = Sys.query("pm_file_info","wjdx, wjlj, wjid, wjmc, wjlb", "wjid = ? ", null, new Object[]{parameters.get("wjid")});
		bundle.put("fileInfo", dataset_fileInfo.getList());
		bundle.put("fileInfoMap", dataset_fileInfo.getMap());
	}
	/** 查询工序信息
	 * @param parameters
	 * @param bundle
	 * @author Yang Fan
	 */
	public void query_gxxx(Parameters parameters, Bundle bundle) {
		Dataset dataset = Sys.query("pm_gxxxb","gxbh, gxmc, gxlxdm,fjsm,zlbj", "gxid = ? ", null, new Object[]{parameters.get("gxid")});
		bundle.put("gxxx", dataset.getMap());
	}
	
	/** 查询工序组信息
	 * @param parameters
	 * @param bundle
	 * @author Yang Fan
	 */
	public void query_gxzxx(Parameters parameters, Bundle bundle) {
		Dataset dataset = Sys.query("pm_gxz","gxzid, gxzxh, gxzbh,gxzmc,scgcid,gxids", "gxzid = ? ", null, new Object[]{parameters.get("gxid")});
		bundle.put("gxxx", dataset.getMap());
	}
	public void query_gxzxxval(Parameters parameters, Bundle bundle) {
		Dataset dataset = Sys.query("pm_gxz","gxzid, gxzxh, gxzbh,gxzmc,scgcid,gxids", "gxzid in "+parameters.get("val_gxz"), null, new Object[]{});
		bundle.put("gxxx", dataset.getMap());
	}
	
	/** 查询工序组信息
	 * @param parameters
	 * @param bundle
	 * @author Yang Fan
	 */
	public void query_gxzxx_new(Parameters parameters, Bundle bundle) {
		Dataset dataset = Sys.query("pm_gxz","gxzid, gxzxh, gxzbh,gxzmc,scgcid,gxids", "gxzid = ? ", null, new Object[]{parameters.get("gxzid")});
		bundle.put("gxxx", dataset.getMap());
	}
	
	/** 根据工序id， 查询物料id
	 * @param parameters
	 * @param bundle
	 * @author Yang Fan
	 */
	public void query_wlxx(Parameters parameters, Bundle bundle) {
		Dataset dataset = Sys.query("pm_gxwlglb","wlid, wlqfdm", "gxid = ? and wlqfdm = '30'", null, new Object[]{parameters.get("gxid")});
		bundle.put("wlxx", dataset.getMap());
	}
	
	/**
	 * 图片关联
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String pic_display(Parameters parameters, Bundle bundle) {
		String wjid = parameters.getString("wjid");
		if (StringUtils.isNotBlank(wjid) && !"null".equals(wjid)) {
			Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjlb,wjdx", " wjid = ? ", 
					null, new Object[]{wjid});
			Map<String,Object> map = dataset.getMap();
			if (map != null) {
				File file_display = new File((String)map.get("wjmc"),null,
						Sys.readFile((String)map.get("wjlj")),
						(String)map.get("wjlb"),Long.valueOf(map.get("wjdx").toString()));
				bundle.put("file_display", file_display);
			} else {
				bundle.put("file_display", null);
			}
		} else {
			bundle.put("file_display", null);
		}
		return "file:file_display";
	}
	
	public void query_ljxxFile(Parameters parameters, Bundle bundle) {
		
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljtpid,ljbh,ljmc", "ljid = ? ", null, new Object[]{parameters.get("ljid").toString()});
		bundle.put("cxxx", dataset_ljxx.getList());
		
		Map<String, Object> map_lj  = new HashMap<String, Object>();
		if(dataset_ljxx.getList().size()>0){
			map_lj = dataset_ljxx.getList().get(0);
			map_lj.put("ljbh", dataset_ljxx.getList().get(0).get("ljbh"));
			map_lj.put("ljmc", dataset_ljxx.getList().get(0).get("ljmc"));
			map_lj.put("url", Sys.getAbsoluteUrl("/service/pic_display?wjid="+map_lj.get("ljtpid")));
		}
		bundle.put("ljtpxx", map_lj);
	}
	
	/**
	 * 根据参数信息查询工序信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxxx_by_param(Parameters parameters, Bundle bundle) {

		String gxid = parameters.getString("gxid"); // 工序id
		String gxxh = parameters.getString("gxxh"); // 工序序号
		String gxmc = parameters.getString("gxmc"); // 工序名称
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(gxid)) 
		{
			con = con + " and gxid = ? ";
			val.add(gxid);
		}
		if(StringUtils.isNotBlank(gxxh))
		{
			con = con + " and gxxh = ? ";
			val.add(gxxh);
		}
		if(StringUtils.isNotBlank(gxmc))
		{
			con = con + " and gxmc like ? ";
			val.add("%"+gxmc+"%");
		}
		Dataset dataset_gxxx = Sys.query(TableConstant.工序信息表,
				"gxid,gxxh,gxbh,gxlxdm,gxmc,zjfs,fjsm,gxtzid,gyzdwjid,zlbj,txbjqjdmc", con, null, val.toArray());
		bundle.put("gxxx", dataset_gxxx.getMap());
		bundle.put("gxxxList", dataset_gxxx.getList());
	
	}
	
	/**
	 * 通过工序组ID查询工序ID
	 * @param parameters
	 * @param bundle
	 */
	public void queryGxxxByGxzid(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("gxzid"); // 工序组id
		//通过工序组id获取工序id
		Dataset dataset_gxzxx = Sys.query("pm_gxz", "gxzid, gxids, gxzmc", "gxzid = ?", null, new Object[]{gxzid});
		List<Map<String, Object>> gxid_list = dataset_gxzxx.getList();
		if(gxid_list.size() > 0){
			String gxids_str = gxid_list.get(0).get("gxids").toString();
			gxids_str = gxids_str.replace("[", "").replace("]", "").replace("\"", "'");
			String gxids = "(" + gxids_str + ")";
			Dataset dataset_gxxx = Sys.query("pm_gxxxb", "gxid,gxxh, gxmc,gxbh", "gxid in " + gxids, "gxbh", new Object[]{});
			List<Map<String, Object>> list = dataset_gxxx.getList();
			for(Map<String, Object> map : list){
				map.put("gxzmc", gxid_list.get(0).get("gxzmc"));
			}
			bundle.put("gxxxlist", list);			
		} else {
			return;
		}
	}
	
	/**
	 * 查询工序组
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxz_by_param(Parameters parameters, Bundle bundle){

		String gxzid = parameters.getString("gxzid"); // 工序组id
		String gxzxh = parameters.getString("gxzxh"); // 工序组序号
		String gxzbh = parameters.getString("gxzbh"); // 工序组编号
		String gxzmc = parameters.getString("gxzmc"); // 工序组名称
		String scgcid = parameters.getString("scgcid"); // 生产过程id
		String gxid = parameters.getString("gxid"); // 工序id
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(gxzid)) 
		{
			con = con + " and gxzid = ? ";
			val.add(gxzid);
		}
		if(StringUtils.isNotBlank(gxzxh))
		{
			con = con + " and gxzxh = ? ";
			val.add(gxzxh);
		}
		if(StringUtils.isNotBlank(gxzbh))
		{
			con = con + " and gxzbh like ? ";
			val.add("%"+gxzbh+"%");
		}
		if(StringUtils.isNotBlank(gxzmc))
		{
			con = con + " and gxzmc like ? ";
			val.add("%"+gxzmc+"%");
		}
		if(StringUtils.isNotBlank(scgcid)) 
		{
			con = con + " and scgcid = ? ";
			val.add(scgcid);
		}
		Dataset dataset_gxzxx = Sys.query(TableConstant.工序组,
				"gxzid,gxzxh,gxzbh,gxzmc,scgcid,gxids", con, null, val.toArray());
		List<Map<String,Object>> list = dataset_gxzxx.getList();
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		if(StringUtils.isNotBlank(gxid))
		{
			//list
			for(Map<String,Object> m : list){
				Map<String,Object> mm = checkGxid(m, gxid);
				if(mm != null){
					returnList.add(mm);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(returnList)){
			bundle.put("gxz", returnList.get(0));
		}else{
			bundle.put("gxz", null);
		}
		bundle.put("gxzList", returnList);
	}
	
	private Map<String,Object> checkGxid(Map<String,Object> m ,String gxid){
		if(m.get("gxids") == null || StringUtils.isBlank(m.get("gxids").toString())){
			return null;
		}
		String gxids = m.get("gxids").toString();
		if(gxids.contains("[")){
			gxids = gxids.replace("[", "");
		}
		if(gxids.contains("]")){
			gxids = gxids.replace("]", "");
		}
		List<String> gxidList = Arrays.asList(gxids.split(","));
		if(gxidList.contains("\"" + gxid + "\"")){
			return m;
		}
		return null;
	}
	
	/**
	 * 通过工序组ids查询工序组信息
	 */
	public void query_gxz_byid(Parameters parameters, Bundle bundle){
		Dataset dataset_gxzxx = Sys.query(TableConstant.工序组,
				"gxzid,gxzbh,gxzmc,gxids", "gxzid in " + parameters.get("gxzids"), "gxids", new Object[]{});
		bundle.put("gxzlist", dataset_gxzxx.getList());
	}
	
	/**
	 * 通过加工单元id  查询工序组信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxz_by_jgdyid(Parameters parameters, Bundle bundle){
		String jgdyid = parameters.getString("jgdyid");
		if(StringUtils.isBlank(jgdyid)){
			bundle.put("", null);
			return;
		}
		String [] models = {TableConstant.工序组, TableConstant.工序组加工单元关联表};
		String join = new StringBuffer()
				.append(TableConstant.工序组).append(SqlConstant.JOIN).append(TableConstant.工序组加工单元关联表)
				.append(SqlConstant.ON).append(TableConstant.工序组).append(SqlConstant.PERIOD).append("gxzid")
				.append(SqlConstant.EQUALS).append(TableConstant.工序组加工单元关联表).append(SqlConstant.PERIOD).append("gxzid")
				.toString();
		
		String fields = new StringBuffer()
				.append(TableConstant.工序组).append(SqlConstant.PERIOD).append("gxzid").append(SqlConstant.COMMA)
				.append(TableConstant.工序组).append(SqlConstant.PERIOD).append("gxzmc").toString();
		
		String conditions = TableConstant.工序组加工单元关联表 +".jgdyid = ? ";
		String orderby = null;
		Dataset dataset = Sys.query(models, join, fields, conditions, orderby, new Object[] { jgdyid });
		List<Map<String,Object>> list = dataset.getList();
		bundle.put("gxzList", list);
	}
	
	/**
	 * 根据零件ID和加工状态查询工序组信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxzList_by_ljid_jgzt(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		String jgzt = parameters.getString("jgzt");
		// 查询符合条件的工序组
		String[] models = { TableConstant.生产过程, TableConstant.工序组 };
		String join = new StringBuffer()
				.append(TableConstant.生产过程).append(SqlConstant.JOIN).append(TableConstant.工序组)
				.append(SqlConstant.ON).append(TableConstant.生产过程).append(SqlConstant.PERIOD).append("scgcid")
				.append(SqlConstant.EQUALS).append(TableConstant.工序组).append(SqlConstant.PERIOD).append("scgcid")
				.toString();
		String conditions = "ljid = ? and ljjgzt = ?";
		String fields =  "gxzid,gxzmc,gxids";
		String orderby = "gxzxh asc";
		Dataset dataset = Sys.query(models, join, fields, conditions, orderby, new Object[] { ljid, jgzt });
		List<Map<String,Object>> gxzList = dataset.getList();
		if (CollectionUtils.isNotEmpty(gxzList)) {
			// 查询相关加工单元列表
			for (Map<String,Object> row : gxzList) {
				List<Map<String, Object>> jgdyList = Sys.query(TableConstant.工序组加工单元关联表, "jgdyid", "gxzid = ?", null,
						new Object[] { row.get("gxzid") }).getList();
				String jgdyids = "";
				for (Map<String, Object> r : jgdyList) {
					jgdyids += "," + r.get("jgdyid");
				}
				if (!"".equals(jgdyids)) {
					row.put("jgdyids", jgdyids.substring(1));
				}
			}
		}
		bundle.put("gxzList", gxzList);
	}
	
	/**
	 * 根据工序组ID和加工单元ID获取加工节拍和准备时间
	 * @param parameters
	 * @param bundle
	 */
	public void query_time_by_gxzid_jgdyid(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("gxzid");
		String jgdyid = parameters.getString("jgdyid");
		Dataset dataset = Sys.query(TableConstant.工序组加工单元关联表, "jgjp,zbsj,jgsj", "gxzid=? and jgdyid=?", null, new Object[] { gxzid, jgdyid });
		bundle.put("jgjp", MapUtils.isEmpty(dataset.getMap()) ? 0 : dataset.getMap().get("jgjp"));
		bundle.put("zbsj", MapUtils.isEmpty(dataset.getMap()) ? 0 : dataset.getMap().get("zbsj"));
		bundle.put("jgsj", MapUtils.isEmpty(dataset.getMap()) ? 0 : dataset.getMap().get("jgsj"));
	}
	
	/**
	 * 根据各个参数查询程序信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_cxxxb_by_param(Parameters parameters, Bundle bundle){

		String cxid = parameters.getString("cxid"); // 程序id
		String cxmc = parameters.getString("cxmc"); // 程序名称 like
		String cxmc_eq = parameters.getString("cxmc_eq"); // 程序名称 eq
		String sbid = parameters.getString("sbid"); // 设备id
		String ljid = parameters.getString("ljid"); // 零件id
		String gxid = parameters.getString("gxid"); // 工序id
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(cxid)) {
			con = con + " and cxid = ? ";
			val.add(cxid);
		}
		if(StringUtils.isNotBlank(cxmc)){
			con = con + " and cxmc like ? ";
			val.add("%"+cxmc+"%");
		}
		if(StringUtils.isNotBlank(cxmc_eq)) {
			con = con + " and cxmc = ? ";
			val.add(cxmc_eq);
		}
		if(StringUtils.isNotBlank(sbid)) {
			con = con + " and sbid = ? ";
			val.add(sbid);
		}
		if(StringUtils.isNotBlank(ljid)) {
			con = con + " and ljid = ? ";
			val.add(ljid);
		}
		if(StringUtils.isNotBlank(gxid)) {
			con = con + " and gxid = ? ";
			val.add(gxid);
		}
		
		Dataset dataset_cxxx = Sys.query("pm_cxxxb",
				"cxid,cxmc,sbid,ljid,gxid", con, null, val.toArray());
		bundle.put("cxxx", dataset_cxxx.getMap());
		bundle.put("cxxxList", dataset_cxxx.getList());
	
	}
	
	/**查询零件表信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_ljglb(Parameters parameters, Bundle bundle) {
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljbh,ljmc,ljlbdm,dw,clbh,clyd,clyddw,mpdh,ljtpid,fjdid,ljms,ljxh,zxsl,cpzl,dezl,tzbb,cpcd,jgzt", null, null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
	/**查询质检项信息，查询条件：零件ID、工序组ID
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_zjxbycon(Parameters parameters, Bundle bundle) {
		StringBuffer con = new StringBuffer(" ljid=? and gxzid=?");
		List params = new ArrayList();
		params.add(parameters.getInteger("ljid"));	// 零件ID
		params.add(parameters.getInteger("gxzid"));	// 工序组ID
		if (null != parameters.get("zjbzlx") && !"".equals(parameters.get("zjbzlx").toString().trim())){
			con.append(" and zjbzlx=?");
			params.add(parameters.get("zjbzlx").toString().trim());
		}
		if (null != parameters.get("gyyq") && !"".equals(parameters.get("gyyq").toString().trim())){
			con.append(" and gyyq like ?");
			params.add("%"+parameters.get("gyyq").toString().trim()+"%");
		}
		if (null != parameters.get("yxsx") && !"".equals(parameters.get("yxsx").toString().trim())){
			con.append(" and yxsx >= ?");
			params.add(Double.parseDouble(parameters.get("yxsx").toString().trim()));
		}
		if (null != parameters.get("yxxx") && !"".equals(parameters.get("yxxx").toString().trim())){
			con.append(" and yxxx <= ?");
			params.add(Double.parseDouble(parameters.get("yxxx").toString().trim()));
		}
				
		Dataset dataset_zjx = Sys.query(TableConstant.工序质检项录入,"gxzjxlrid,zjxh,ljid,gxzid,zjbzlx,jyxm,gyyq,yxsx,yxxx,swjsfj,zjjsfj,zjsfj,xjsfj,hxsfj", con.toString(), null, params.toArray());
		bundle.put("zjx", dataset_zjx.getList());
	}
	
	/**
	 * 质检标准类型
	 * 目测、游标卡板、游标卡尺、投影仪、外径千分卡板、外径千分尺、中心孔测量仪、总测量仪等
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void searchZjbzlx(Parameters parameters, Bundle bundle){
		List<Map<String,Object>> result = null;
		Dataset dataset = Sys.query("pm_sjzdb", "label,title,pinyin1,pinyin2", " type= ?", null, 
				new Object[]{"zjbzlx"});
		if(CollectionUtils.isEmpty(dataset.getList())){
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			Map<String,Object> map1 = new HashMap<String,Object>();
			map1.put("label", "目测");
			map1.put("title", "目测");
			map1.put("pinyin1", "muce");
			map1.put("pinyin2", "mc");
			list.add(map1);
			Map<String,Object> map2 = new HashMap<String,Object>();
			map2.put("label", "游标卡尺（卡板）");
			map2.put("title", "游标卡尺（卡板）");
			map2.put("pinyin1", "youbiaokachikaban");
			map2.put("pinyin2", "ybkckb");
			list.add(map2);
			Map<String,Object> map3 = new HashMap<String,Object>();
			map3.put("label", "游标卡尺");
			map3.put("title", "游标卡尺");
			map3.put("pinyin1", "youbiaokachi");
			map3.put("pinyin2", "ybkc");
			list.add(map3);
			Map<String,Object> map4 = new HashMap<String,Object>();
			map4.put("label", "投影仪");
			map4.put("title", "投影仪");
			map4.put("pinyin1", "tongyingyi");
			map4.put("pinyin2", "tyy");
			list.add(map4);
			Map<String,Object> map5 = new HashMap<String,Object>();
			map5.put("label", "外径千分尺（卡板）");
			map5.put("title", "外径千分尺（卡板）");
			map5.put("pinyin1", "waijingqianfenchikaban");
			map5.put("pinyin2", "wjqfckb");
			list.add(map5);
			Map<String,Object> map6 = new HashMap<String,Object>();
			map6.put("label", "外径千分尺");
			map6.put("title", "外径千分尺");
			map6.put("pinyin1", "waijingqianfenchi");
			map6.put("pinyin2", "wjqfc");
			list.add(map6);
			Map<String,Object> map7 = new HashMap<String,Object>();
			map7.put("label", "中心孔测量仪");
			map7.put("title", "中心孔测量仪");
			map7.put("pinyin1", "zhongxinkongceliangyi");
			map7.put("pinyin2", "zxkcly");
			list.add(map7);
			Map<String,Object> map8 = new HashMap<String,Object>();
			map8.put("label", "总测量仪");
			map8.put("title", "总测量仪");
			map8.put("pinyin1", "zongceliangyi");
			map8.put("pinyin2", "zcly");
			list.add(map8);
			
			result = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> item : list){
				result.add(item);
			}
		}else{
			Dataset dataset_query = Sys.query("pm_sjzdb", "label,title,pinyin1,pinyin2", " type= ?"
					, null, 
					new Object[]{"zjbzlx"});
			result = dataset_query.getList();
		}
		bundle.put("zjxm", result);
	}
	/** 查询工序组信息
	 * @param parameters
	 * @param bundle
	 * @author Yang Fan
	 */
	public void query_gxzxx_new1(Parameters parameters, Bundle bundle) {
		String param = " 1=1 ";
		Dataset dataset = Sys.query("pm_gxz","gxzid, gxzxh, gxzbh,gxzmc,scgcid,gxids", param, null, new Object[]{});
		bundle.put("gxxx", dataset.getList());
	}
	public void query_ljxxByljbh(Parameters parameters, Bundle bundle) {
		String param = " 1=1 ";
		if(null!=parameters.getString("ljbh")&&!"".equals(parameters.getString("ljbh"))){
			param+=" and ljbh = '"+parameters.getString("ljbh")+"'";
		}
		Dataset dataset_ljxx = Sys.query("pm_ljglb","ljid,ljmc,ljlbdm,ljtpid,clbh,ljbh,mpdh,fjdid,dw,zxsl,ljms ", param, null, new Object[]{});
		bundle.put("ljxx", dataset_ljxx.getList());
	}
}
