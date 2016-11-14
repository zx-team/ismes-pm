package com.isesol.mes.ismes.pm.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.pm.constant.SqlConstant;
import com.isesol.mes.ismes.pm.constant.TableConstant;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 生产过程Activity
 * @author wangxu
 *
 */
public class ScgcActivity {
	
	private Logger log4j = Logger.getLogger(ScgcActivity.class);
	
	/**
	 * 进入生产过程页面的默认方法
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String index(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		String fields = new StringBuffer().append("ljid").append(SqlConstant.COMMA)// 零件id
				.append("ljbh").append(SqlConstant.COMMA)// 零件编号
				.append("ljmc").append(SqlConstant.COMMA)// 零件名称
				.append("ljlbdm").append(SqlConstant.COMMA)// 零件类别
				.append("clbh").append(SqlConstant.COMMA)// 材料编号
				.append("dw").append(SqlConstant.COMMA)// 单位
				.append("clyd").append(SqlConstant.COMMA)// 材料硬度
				.append("ljtpid").append(SqlConstant.COMMA)// 零件图片ID
				.append("ljxh").append(SqlConstant.COMMA)// 零件项号
				.append("mpdh").append(SqlConstant.COMMA)// 零件简码
				.append("jgzt").append(SqlConstant.COMMA)// 加工状态
				.append("tzbb").append(SqlConstant.COMMA)// 图纸版本
				.append("zxsl").append(SqlConstant.COMMA)// 装箱数量
				.append("ljms").toString();// 材料编号
		Map<String, Object> map = Sys.query(TableConstant.零件管理表, fields, "ljid=?", null, null, ljid).getMap();
		bundle.put("cpmc", map.get("ljmc"));
		bundle.put("th", map.get("ljbh"));
		bundle.put("cl", map.get("clbh"));
		bundle.put("tzbb", map.get("tzbb"));
		bundle.put("ljid", ljid);
		return "scgcgl";
	}
	
	/**
	 * 工序组列表查询
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gxzTable(Parameters parameters, Bundle bundle) {
		String scgcid = parameters.getString("scgcid");
		if (scgcid == null) {
			return "json:";
		}
		Dataset dataset = Sys.query("pm_gxz", "gxzid, gxzxh, gxids, gxzbh, gxzmc, scgcid, gxids", "scgcid = ?", null, "gxzxh asc", scgcid);
		List<Map<String, Object>> rows = dataset.getList();
		for (Map<String, Object> row : rows) {
			List<Map<String, Object>> datas = Sys.query("pm_gxzjgdyglb", "jgdyid", "gxzid = ?", null, "glid asc", row.get("gxzid")).getList();
			String jgdymc = "";
			if (!CollectionUtils.isEmpty(datas)) {
				for (Map<String, Object> data : datas) {
					Parameters p = new Parameters();
					p.set("jgdyid", data.get("jgdyid"));
					Bundle b = Sys.callModuleService("em", "emservice_jgdyById", p);
					Map<String, Object> d = (Map<String, Object>) b.get("data");
					jgdymc += "," + (String)d.get("jgdymc");
				}
				if (!"".equals(jgdymc)) {
					row.put("sxjgdy", jgdymc.substring(1));
				}
			} else {
				row.put("sxjgdy", "");
			}
		}
		bundle.put("rows", rows);
		return "json:";
	}
	
	/**
	 * 初始化修改页面
	 * @param parameters
	 * @param bundle
	 */
	public String initGxz(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("gxzid");
		Map<String, Object> data = Sys
				.query("pm_gxz", "gxzid, gxzxh, gxzbh, gxzmc, scgcid, gxids", "gxzid = ?", null, null, gxzid).getMap();
		bundle.put("data", data);
		return "json:";
	}
	
	/**
	 * 保存工序组
	 * @param parameters
	 * @param bundle
	 */
	public void saveGxz(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("gxzid");
		String gxzxh = parameters.getString("gxzxh");
		String gxzbh = parameters.getString("gxzbh");
		String gxzmc = parameters.getString("gxzmc");
		String scgcid = parameters.getString("scgcid");
		String gxids = parameters.getString("gxids");
		Map<String, Object> row = Maps.newHashMap();
		row.put("gxzxh", gxzxh);
		row.put("gxzbh", gxzbh);
		row.put("gxzmc", gxzmc);
		row.put("scgcid", scgcid);
		row.put("gxids", gxids);
		if (StringUtils.isEmpty(gxzid)) {
			// 添加
			Sys.insert("pm_gxz", row);
		} else {
			// 修改
			row.put("gxzid", gxzid);
			Sys.update("pm_gxz", row, "gxzid = ?", gxzid);
		}
	}
	/**
	 * 删除工序组
	 * @param parameters
	 * @param bundle
	 */
	public void deleteGxz(Parameters parameters, Bundle bundle) {
		// 删除工序组
		String gxzid = parameters.getString("gxzid");
		Sys.delete("pm_gxz", "gxzid = ?", gxzid);
		// 删除工序组和加工单元的关联数据
		Sys.delete("pm_gxzjgdyglb", "gxzid = ?", gxzid);
	}
	
	/**
	 * 加工单元列表查询
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String jgdyTable(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("gxzid");
		if (gxzid == null) {
			return "json:";
		}
		// 根据工序组id查询加工单元
		Dataset dataset = Sys.query("pm_gxzjgdyglb", "glid,jgdyid,jgsj,jgjp,zbsj", "gxzid=?", null, "glid asc", gxzid);
		List<Map<String, Object>> rows = dataset.getList();
		if (CollectionUtils.isNotEmpty(rows)) {
			for (Map<String, Object> row : rows) {
				Parameters p = new Parameters();
				p.set("jgdyid", row.get("jgdyid"));
				Bundle b = Sys.callModuleService("em", "emservice_jgdyById", p);
				if (b != null) {
					Map<String, Object> data = (Map<String, Object>) b.get("data");
					row.put("jgdybh", data.get("jgdybh"));
					row.put("jgdymc", data.get("jgdymc"));
				}
			}
		}
		bundle.put("rows", rows);
		return "json:";
	}
	
	/**
	 * 加工单元检索
	 * @param parameters
	 * @param bundle
	 */
	public String jgdyEdit(Parameters parameters, Bundle bundle) {
		String jgdybh = parameters.getString("query");
		String gxids = parameters.getString("gxids");
		if (StringUtils.isNotBlank(jgdybh)) {
			Parameters p = new Parameters();
			p.set("jgdybh", jgdybh);
			Bundle b = Sys.callModuleService("em", "emservice_jgdyBySblxids", p);
			if (b == null) {
				return "json:list";
			}
			List<Map<String, Object>> rows = (List<Map<String, Object>>) b.get("data");
			for (Map<String, Object> row : rows) {
				row.put("label", row.get("jgdybh"));
				row.put("value", row.get("jgdybh"));
				row.put("title", row.get("jgdybh"));
			}
			bundle.put("list", rows);
		}
		
		return "json:list";
//		String gxids = parameters.getString("gxids").replaceAll("\"", "'").replaceAll("[", "").replaceAll("]", "");
//		Map<String, Object> data = Sys
//				.query("pm_gxz", "gxzid, gxzxh, gxzbh, gxzmc, scgcid, gxids", "gxzid = ?", null, null, gxids).getMap();
	}
	
	/**
	 * 保存加工单元
	 * @param parameters
	 * @param bundle
	 */
	public void saveJgdy(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("gxzid");
		String data = parameters.getString("data");
		List<Map<String, Object>> insertRows = Lists.newArrayList();
		List<Map<String, Object>> updateRows = Lists.newArrayList();
		JSONArray jsonArray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonArray.size(); i++) {
			Map<String, Object> row = Maps.newHashMap();
			JSONObject obj = jsonArray.getJSONObject(i);
			row.put("gxzid", gxzid);
			row.put("jgdyid", obj.getString("jgdyid"));
			row.put("jgsj", obj.getString("jgsj"));
			row.put("jgjp", obj.getString("jgjp"));
			row.put("zbsj", obj.getString("zbsj"));
			if ("edit".equals(obj.getString("oper"))) {
				row.put("glid", obj.getString("glid"));
				updateRows.add(row);
			} else {
				insertRows.add(row);
			}
		}
		if (!insertRows.isEmpty()) {
			Sys.insert("pm_gxzjgdyglb", insertRows);
		}
		if (!updateRows.isEmpty()) {
			for (Map<String, Object> row : updateRows) {
				Sys.update("pm_gxzjgdyglb", row, "glid=?", row.get("glid"));
			}
		}
	}
	
	/**
	 * 保存生产过程
	 * @param parameters
	 * @param bundle
	 */
	public void saveScgc(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		String data = parameters.getString("data");
		List<Map<String, Object>> insertRows = Lists.newArrayList();
		List<Map<String, Object>> updateRows = Lists.newArrayList();
		JSONArray jsonArray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonArray.size(); i++) {
			Map<String, Object> row = Maps.newHashMap();
			JSONObject obj = jsonArray.getJSONObject(i);
			row.put("ljid", ljid);
			row.put("scgcmc", obj.getString("scgcmc"));
			row.put("ljjgzt", obj.getString("ljjgzt"));
			if ("edit".equals(obj.getString("oper"))) {
				row.put("scgcid", obj.getString("scgcid"));
				updateRows.add(row);
			} else {
				insertRows.add(row);
			}
		}
		if (!insertRows.isEmpty()) {
			Sys.insert("pm_scgc", insertRows);
		}
		if (!updateRows.isEmpty()) {
			for (Map<String, Object> row : updateRows) {
				Sys.update("pm_scgc", row, "scgcid=?", row.get("scgcid"));
			}
		}
	}
	
	public String table_scgc(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		if (ljid == null) {
			return "json:";
		}
		Dataset dataset = Sys.query("pm_scgc", "scgcid, scgcmc, ljjgzt", "ljid = ?", null, "scgcid asc", ljid);
		List<Map<String, Object>> rows = dataset.getList();
		bundle.put("rows", rows);
		return "json:";
	}
	
	public void del_scgc(Parameters parameters, Bundle bundle) {
		String scgcid = parameters.getString("scgcid");
		Sys.delete("pm_scgc", "scgcid = ?", scgcid);
		// 查询相关工序组
		List<Map<String, Object>> list = Sys.query("pm_gxz", "gxzid", "scgcid=?", null, new Object[] { scgcid })
				.getList();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> row : list) {
				// 删除关联的工序组
				Parameters p = new Parameters();
				Bundle b = new Bundle();
				p.set("gxzid", row.get("gxzid"));
				this.deleteGxz(p, b);
			}
		}
	}
	
	public void del_jgdy(Parameters parameters, Bundle bundle) {
		// 加工单元工序组关联ID
		String glid = parameters.getString("glid");
		Sys.delete("pm_gxzjgdyglb", "glid = ?", glid);
	}
	
	public String gxTable(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		if (ljid == null) {
			return "json:";
		}
		// 根据零件id获取工序信息
		Parameters p = new Parameters();
		p.set("ljid", ljid);
		Bundle b = Sys.callModuleService("pm", "query_gxxx_by_ljid_service", p);
		List<Map<String, Object>> gxList = (List<Map<String, Object>>) b.get("gxList");
		if (gxList == null || gxList.isEmpty()) {
			bundle.put("error", "零件没有对应的工序信息"); 
			return "json:";
		}
		List<Map<String, Object>> rows = Lists.newArrayList();
		for (Map<String, Object> gx : gxList) {
			Map<String, Object> row = Maps.newHashMap();
			// 工序ID
			String gxid = String.valueOf(gx.get("gxid"));
			// 工序名称
			String gxmc = String.valueOf(gx.get("gxmc"));
			// 工序编号
			String gxbh = String.valueOf(gx.get("gxbh"));
			// 工序序号
			String gxxh = String.valueOf(gx.get("gxxh"));
			
			row.put("gxid", gxid);
			row.put("gxmc", gxmc);
			row.put("gxbh", gxbh);
			row.put("gxxh", gxxh);
			rows.add(row);
		}
		bundle.put("rows", rows);
		return "json:";
	}
	
	/**
	 * 添加工序   初始化页面
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String addGxIndex(Parameters parameters, Bundle bundle){
		//工序id
		String gxid = parameters.getString("gxid");
		bundle.put("gxid", gxid);
		String fieldsStr = "zlbj,gxid,gxxh,gxbh,gxlxdm,gxmc,zjfs,jgfs,jgjp,zbsj,fjsm,gxtzid,gyzdwjid,gxmc";
		Dataset gxDataset = Sys.query(TableConstant.工序信息表, fieldsStr ,
				" gxid = ? ", null, new Object[]{gxid});
		Map<String,Object> gxMap = gxDataset.getMap();
		bundle.put("gxxx", gxMap);
		if(gxMap.get("zlbj") != null){
			bundle.put("zlbj", new String[]{ gxMap.get("zlbj").toString()});
		}
		//工艺指导文件
		if(gxMap.get("gyzdwjid") != null){
			Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjdx,wjlb", 
					 " wjid  = ? ", null, new Object[]{gxMap.get("gyzdwjid").toString()});
			
			Map<String,Object> gyzdwjMap = dataset.getMap();
			bundle.put("gyzdwj", gyzdwjMap);
			
			String url = Sys.getAbsoluteUrl("/gx/pic_display?wjid=" + gxMap.get("gyzdwjid").toString());
			bundle.put("gyzdwj_url", url);
		}
		//工序图纸
		if(gxMap.get("gxtzid") != null){
			Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjdx,wjlb", 
					 " wjid  = ? ", null, new Object[]{gxMap.get("gxtzid").toString()});
			
			Map<String,Object> gxtzMap = dataset.getMap();
			bundle.put("gxtz", gxtzMap);
			
			String url = Sys.getAbsoluteUrl("/gx/pic_display?wjid=" + gxMap.get("gxtzid").toString());
			bundle.put("gxtz_url", url);
		}
		
		//工艺id
		String gyid = parameters.getString("gyid");
		bundle.put("gyid", gyid);
		//根据工艺id得到零件信息
		String [] models = {TableConstant.零件管理表,TableConstant.工艺表};
		String join = new StringBuffer()
				.append(TableConstant.零件管理表).append(SqlConstant.JOIN).append(TableConstant.工艺表)
				.append(SqlConstant.ON).append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljid")
				.append(SqlConstant.EQUALS).append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("ljid")
				.toString();
		
		String fields = new StringBuffer()
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljid").append(SqlConstant.COMMA)//零件id
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljbh").append(SqlConstant.COMMA)//零件编号
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljmc").append(SqlConstant.COMMA)//零件名称
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("clyd").append(SqlConstant.COMMA)//材料硬度
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("clbh").toString();//材料编号
		
		String conditions = TableConstant.工艺表 +".gyid = ? ";
		String orderby = null;
		Dataset ljDataset = Sys.query(models, join, fields, conditions, orderby, new Object[]{gyid});
		Map<String,Object> ljMap = ljDataset.getMap();
		bundle.put("ljxx", ljMap);
		
		return "gxtj";
	}
	
	public String pic_display(Parameters parameters, Bundle bundle) {
		String wjid = parameters.getString("wjid");
		if (StringUtils.isNotBlank(wjid)) {
			Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjlb,wjdx", " wjid = ? ", 
					null, new Object[]{wjid});
			Map<String,Object> map = dataset.getMap();
			File file_display = new File((String)map.get("wjmc"),null,
					Sys.readFile((String)map.get("wjlj")),
					(String)map.get("wjlb"),Long.valueOf(map.get("wjdx").toString()));
			bundle.put("file_display", file_display);
		} else {
			bundle.put("file_display", null);
		}
		return "file:file_display";
	}
	
	/**
	 * 工步信息  表格
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gbxxTable(Parameters parameters, Bundle bundle){
		
		String gxid = parameters.getString("gxid");
		Dataset dataset = Sys.query(TableConstant.工步信息表, "gbid,gbxh,gxid,djtzbh,djid,jggcms,qxzj_min,qxzj_max,qxsud,"
				+ "zzzs,qxshend,jgl", " gxid = ? ", "gbxh", new Object[]{gxid});
		List<Map<String,Object>> gbList = dataset.getList();
		if(CollectionUtils.isNotEmpty(gbList)){
			for(Map<String,Object> m : gbList){
				if(m.get("djid") == null || StringUtils.isEmpty(m.get("djid").toString())){
					continue;
				}
				String wlid = m.get("djid").toString();
				parameters.set("wlid", wlid);
				Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
				if(b != null && b.get("materielInfo") != null){
					Map<String,Object> m_wl =  (Map<String, Object>) b.get("materielInfo") ;
					m.put("wlbh", m_wl.get("wlbh"));
				}
			}
		}
		bundle.put("rows", gbList);
		bundle.put("totalPage", 1);
		bundle.put("currentPage", 1);
		bundle.put("records", dataset.getCount());
		return "json:";
	}
	
	/**
	 * 工步  刀具信息模糊查询
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String wlEdit(Parameters parameters, Bundle bundle){
		String query = (String) parameters.get("query");
		parameters.set("query_wlbh", query);
		parameters.set("query_wlzt", "10");
		Bundle b = Sys.callModuleService("mm", "mmservice_wlxxkc", parameters);
		List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("wlxx");
		for (int i = 0; i < list.size(); i++) {
			list.get(i).put("label", list.get(i).get("wlbh"));
			list.get(i).put("value", list.get(i).get("wlid"));
			list.get(i).put("title", list.get(i).get("wlbh"));
		}
		
		bundle.put("data", list);
		return "json:data";
	}
	
	public String wlSelect(Parameters parameters, Bundle bundle){
		Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
		Map<String,Object> data = (Map<String, Object>) b.get("materielInfo");
		bundle.put("data", data);
		return "json:data";
	}
	
	/**
	 * 能力组的table
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String nlzTable(Parameters parameters, Bundle bundle){
		
		String gxid = parameters.getString("gxid");
		Dataset dataset = Sys.query(TableConstant.工序能力组关联表, "nlzid,glid", " gxid = ? ", null , new Object[]{gxid});
		//能力组id s		
		List<Map<String,Object>> nlzList = dataset.getList();
		Map<String,String> glidMap = new HashMap<String, String>();
		if(CollectionUtils.isNotEmpty(nlzList)){
			for(Map<String,Object> m : nlzList){
				String nlzid = m.get("nlzid").toString();
				String glid = m.get("glid").toString();
				glidMap.put(nlzid, glid);
			}
		}
		parameters.set("nlzids", nlzList);
		parameters.set("conditionNullReturnNull", true);
		Bundle b = Sys.callModuleService("em", "emservice_nlzList", parameters);
		List<Map<String,Object>> returnList = (List<Map<String, Object>>) b.get("nlzList");
		if(CollectionUtils.isNotEmpty(returnList)){
			for(Map<String,Object> m : returnList){
				String nlzid = m.get("nlzid").toString();
				m.put("glid", glidMap.get(nlzid));
				m.put("gxid", gxid);
			}
		}
		bundle.put("rows", returnList);
		bundle.put("totalPage", 1);
		bundle.put("currentPage", 1);
		bundle.put("records", 100);
		return "json:";
	}
	
	/**
	 * 能力组模糊匹配
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String nlzEdit(Parameters parameters, Bundle bundle){
		String param = (String) parameters.get("term");
		parameters.set("nlzbh", param);
		Bundle b = Sys.callModuleService("em", "emservice_nlzList", parameters);
		List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("nlzList");
		List<Map<String,String>> data = new ArrayList<Map<String,String>>();
		for(Map<String,Object> map : list){
			Map<String,String> m = new HashMap<String, String>();
			m.put("label", map.get("nlzbh").toString());
			m.put("value", map.get("nlzid").toString());
			data.add(m);
		}
		bundle.put("data", data);
		return "json:data";
	}
	
	public String nlzSelect(Parameters parameters, Bundle bundle){
		Bundle b = Sys.callModuleService("em", "emservice_nlzList", parameters);
		Map<String,Object> map = (Map<String, Object>) b.get("nlz");
		bundle.put("data", map);
		return "json:data";
	}
	
	/**
	 * 物料的table
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String wlTable(Parameters parameters, Bundle bundle){
		
		String gxid = parameters.getString("gxid");
		String wlqfdm = parameters.getString("wlqfdm");
		if(StringUtils.isBlank(gxid) || StringUtils.isBlank(wlqfdm) ){
			log4j.info("工序id ==" + gxid +";;;物料区分代码 ==" + wlqfdm);
			return "json:";
		}
		
		Dataset dataset = Sys.query(TableConstant.工序物料关联表, "wlid,wlsl,glid,gxid,wlqfdm", " gxid = ? and wlqfdm = ? ",
				null , new Object[]{gxid,wlqfdm});
		//物料id s		
		List<Map<String,Object>> wlList = dataset.getList();
		parameters.set("wlids", wlList);
		Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
		//物料 info
		List<Map<String,Object>> wlInfoList = (List<Map<String, Object>>) b.get("materielInfoList");
		if(CollectionUtils.isNotEmpty(wlInfoList)){
			for(Map<String,Object> map1 : wlInfoList){
				if(CollectionUtils.isEmpty(wlList)){
					break;
				}
				for(Map<String,Object> map2 : wlList){
					if(map1.get("wlid").toString().equals(map2.get("wlid").toString())){
						map1.put("wlsl", map2.get("wlsl").toString());
						map1.put("glid", map2.get("glid").toString());
						map1.put("wlqfdm",wlqfdm);
					}
				}
			}
		}
		bundle.put("rows", wlInfoList);
		bundle.put("totalPage", 1);
		bundle.put("currentPage", 1);
		bundle.put("records", 100);
		return "json:";
	}
	
	/**
	 * 工序基本信息
	 * @param parameters
	 * @param bundle
	 */
	public String saveBasicGxxx(Parameters parameters, Bundle bundle){
		Map<String,Object> returnMap = new HashMap<String, Object>();
		String gxid = parameters.getString("basicinfo-gxid");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("gxmc", parameters.get("basicinfo-gxmc"));
		map.put("gxxh", parameters.get("basicinfo-gxxh"));
		map.put("gxbh", parameters.get("basicinfo-gxbh"));
		map.put("gxlxdm", parameters.get("basicinfo-gxlx"));
		map.put("zjfs", parameters.get("basicinfo-zjfs"));
		map.put("zlbj", this.handleNullValue(parameters, "basicinfo-bj",Integer.class,0));
		map.put("jgfs", this.handleNullValue(parameters, "basicinfo-jgfs",Integer.class,0));
		map.put("jgjp", this.handleNullValue(parameters, "basicinfo-jgjp",Integer.class,0));
		map.put("zbsj", this.handleNullValue(parameters, "basicinfo-zbsj",Integer.class,0));
		map.put("fjsm", parameters.get("basicinfo-fjsm"));
		
		
		String fieldsStr = "gyzdwjid,gxtzid";
		Dataset gxDataset = Sys.query(TableConstant.工序信息表, fieldsStr ,
				" gxid = ? ", null, new Object[]{gxid});
		Map<String,Object> fileMap = gxDataset.getMap();
		//工艺指导文件
		File fileGyzdwj = parameters.getFile("basicinfo-gyzdwj");
		String gyzdwjid = saveFile(fileGyzdwj,fileMap.get("gyzdwjid"));
		if(StringUtils.isNotBlank(gyzdwjid)){
			map.put("gyzdwjid", gyzdwjid);
			returnMap.put("gyzdwjid", gyzdwjid);
		}
		//如果质量必检则  工艺指导文件必须上传
		if("10".equals(map.get("zlbj").toString())  && StringUtils.isBlank(gyzdwjid)){
			Dataset set = Sys.query(TableConstant.工序信息表, "gyzdwjid", " gxid = ? ", null , new Object[]{gxid});
			Map<String,Object> m = set.getMap();
			if( m.get("gyzdwjid") == null || StringUtils.isBlank(m.get("gyzdwjid").toString())){
				returnMap.put("message", "若质量必检则，则工艺指导文件必须上传");
				bundle.put("data", returnMap);
				return "json:data";
			}
		}
		//工序图纸
		File fileGxtz = parameters.getFile("basicinfo-gxtzid");
		String gxtzid = saveFile(fileGxtz,fileMap.get("gxtzid"));
		if(StringUtils.isNotBlank(gxtzid)){
			map.put("gxtzid", gxtzid);
			
			String url = Sys.getAbsoluteUrl("/gx/pic_display?wjid=" + gxtzid);
			returnMap.put("gxtz_url", url);
		}
		
		Sys.update(TableConstant.工序信息表, map, " gxid = ? ", new Object[]{gxid});
		
		saveNewWlxx(parameters);
		
		bundle.put("data", returnMap);
		return "json:data";
	}
	
	public void saveNewWlxx(Parameters parameters){
		//保存的时候，生成 物料信息
		Map<String,Object> wlMap = new HashMap<String, Object>();
		String gxid = parameters.getString("basicinfo-gxid");
		String ljmc = this.handleNullValue(parameters, "hidden-ljmc",String.class,"").toString();
		String ljid = this.handleNullValue(parameters, "hidden-ljid",String.class,"").toString();
		String ljbh = this.handleNullValue(parameters, "hidden-ljbh",String.class,"").toString();
		String gxmc = this.handleNullValue(parameters, "basicinfo-gxmc",String.class,"").toString();
		String gxbh = this.handleNullValue(parameters, "basicinfo-gxbh",String.class,"").toString();
		//		编号规则：BCP_零件号_工序号_序列号（自动增加）
		//		名称规则：零件名称_工序名
		//
		if(StringUtils.isBlank(gxbh) || StringUtils.isBlank(gxmc)){
			return;
		}
		Dataset dataset = Sys.query(TableConstant.工序物料关联表, 
				"glid,wlid", " gxid = ? and wlqfdm = '40' ", null, new Object[]{gxid});
		Map<String,Object> glMap = dataset.getMap();
		if(MapUtils.isEmpty(glMap)){
			String dw = "";
			//使用物料
			Dataset sywl_dataset = Sys.query(TableConstant.工序物料关联表, 
					"glid,wlid", " gxid = ? and wlqfdm = '30' ", null, new Object[]{gxid});
			//物料id
			if(sywl_dataset != null && MapUtils.isNotEmpty(sywl_dataset.getMap()) 
					&& sywl_dataset.getMap().get("wlid") != null){
				String sywlid = sywl_dataset.getMap().get("wlid").toString();
				Parameters p = new Parameters();
				p.set("wlid",sywlid);
				Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", p);
				if(b!= null &&  b.get("materielInfo") != null){
					Map<String,Object> qxinfo = (Map<String, Object>) b.get("materielInfo");
					dw = qxinfo.get("wldwdm").toString();
				}
			}
			
			String wlbh = "BCP_" + gxbh + "_"  + Sys.getNextSequence(gxid);
			wlMap.put("wlbh", wlbh);
			String wlmc = ljmc + "_" + gxmc;
			wlMap.put("wlmc", wlmc);
			//物料类别   半成品
			wlMap.put("wllbdm", "50");
			//物料类型   自制件
			wlMap.put("wllxdm", "20");
			//状态
			wlMap.put("wlzt", "10");
			
			wlMap.put("wldwdm", dw);
			
			parameters.set("wlxx", wlMap);
			Bundle b = Sys.callModuleService("mm", "insertWlxxService", parameters);
			String scwlid = b.get("wlid").toString();
			
			//保存工序  和  生成物料的关系
			Map<String,Object> m = new HashMap<String, Object>();
			m.put("gxid", gxid);
			m.put("wlid", scwlid);
			m.put("wlqfdm", "40");
			Sys.insert(TableConstant.工序物料关联表, m);
		}
	}
	
	private String saveFile(File file,Object deleteFileInfoId){
		if(file == null){
			return "";
		}
		String path = "/gxtj/"+ Sys.getUserIdentifier() +"/" + new Date().getTime() +"."+file.getSuffix();
		Map<String,Object> fileMap = new HashMap<String,Object>();
		fileMap.put("wjlj", path);
		fileMap.put("wjmc", file.getName());
		fileMap.put("wjdx", file.getSize());
		fileMap.put("wjlb", file.getContentType());		
		Sys.saveFile(path, file.getInputStream());
		
		Sys.insert(TableConstant.附件文件表, fileMap);
		if(deleteFileInfoId != null){
			Sys.delete(TableConstant.附件文件表, " wjid = ? ", deleteFileInfoId.toString());
		}
		return fileMap.get("wjid").toString();
	}
	
	private Object handleNullValue(Parameters parameters,String str,Class<?> clazz,Object defaulValue){
		if(defaulValue == null){
			defaulValue = "";
		}
		String value = StringUtils.isBlank(parameters.getString(str)) 
				? defaulValue.toString() : parameters.getString(str);
		try{
			if(clazz.isInstance(Double.class)){
				return Double.valueOf(value);
			}
			if(clazz.isInstance(Integer.class)){
				return Integer.valueOf(value);
			}
			if(clazz.isInstance(String.class)){
				return String.valueOf(value);
			}
		}catch(Exception e){
			log4j.info(str + "转换成"+ clazz.getName() +"类别，出现异常");
			log4j.error(e);
		}
		return value;
	}
	
	private Object handleNullValue(JSONObject jsonObject,String str,Class<?> clazz,Object defaulValue){
		if(defaulValue == null){
			defaulValue = "";
		}
		String value = StringUtils.isBlank(jsonObject.getString(str)) 
				? defaulValue.toString() : jsonObject.getString(str);
		try{
			if(clazz.isInstance(Double.class)){
				return Double.valueOf(value);
			}
			if(clazz.isInstance(Integer.class)){
				return Integer.valueOf(value);
			}
			if(clazz.isInstance(String.class)){
				return String.valueOf(value);
			}
		}catch(Exception e){
			log4j.info(str + "转换成"+ clazz.getName() +"类别，出现异常");
			log4j.error(e);
		}
		return value;
	}
	
	public String  download(Parameters parameters, Bundle bundle){
		String fjid = parameters.getString("wjid");
		if(StringUtils.isBlank(fjid)){
			return "file:";
		}
		Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjdx,wjlb", 
				 " wjid = ? ", null, new Object[]{fjid});
		Map<String,Object> gyzdwjMap = dataset.getMap();
		
		File data = new File((String)gyzdwjMap.get("wjmc"),null,
				Sys.readFile((String)gyzdwjMap.get("wjlj")),
				(String)gyzdwjMap.get("wjlb"),Long.valueOf(gyzdwjMap.get("wjdx").toString()));
		bundle.put("data", data);
		return "file:data";
	}
	
	/**
	 * 工序--工步信息
	 * @param parameters
	 * @param bundle
	 */
	public void saveGbxx(Parameters parameters, Bundle bundle){
		//得到当前所有行  并为行设置序号
		String rowids = parameters.getString("rowids");
		JSONArray rowids_jsonarray = JSONArray.fromObject(rowids);
		Map<String,String> xhMap = new HashMap<String, String>();
		for (int i = 0; i < rowids_jsonarray.size(); i++) {
			xhMap.put((String) rowids_jsonarray.get(i), i + 1 + "");
		}
		
		//得到编辑了的工步信息，按照id区分   进行编辑和新增
		String gxid = parameters.getString("gxid");
		String data = parameters.getString("data");
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		List<Object[]> updateConditionValue = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> gbMap = new HashMap<String, Object>();	
			String gbid = (String) jsonarray.getJSONObject(i).get("gbid");
			String xh = xhMap.get(gbid);
			xhMap.remove(gbid);
			gbMap.put("gbxh", xh);
			gbMap.put("gxid", gxid);
			gbMap.put("djtzbh", jsonarray.getJSONObject(i).get("djtzbh"));
			gbMap.put("djid", jsonarray.getJSONObject(i).get("djid"));
			gbMap.put("jggcms", jsonarray.getJSONObject(i).get("jggcms"));
			gbMap.put("qxzj_max", this.handleNullValue(jsonarray.getJSONObject(i), "qxzj_max",Double.class,0));
			gbMap.put("qxzj_min", this.handleNullValue(jsonarray.getJSONObject(i), "qxzj_min",Double.class,0));
			gbMap.put("qxsud", this.handleNullValue(jsonarray.getJSONObject(i), "qxsud",Double.class,0));
			gbMap.put("zzzs", this.handleNullValue(jsonarray.getJSONObject(i), "zzzs",Double.class,0));
			gbMap.put("qxshend", this.handleNullValue(jsonarray.getJSONObject(i), "qxshend",Double.class,0));
			gbMap.put("jgl", this.handleNullValue(jsonarray.getJSONObject(i), "jgl",Double.class,0));
			if("add".equals(jsonarray.getJSONObject(i).get("oper"))){
				insertList.add(gbMap);
			}
			if("edit".equals(jsonarray.getJSONObject(i).get("oper"))){
				updateConditionValue.add(new Object[]{gbid});
				updateList.add(gbMap);
			}
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			Sys.insert(TableConstant.工步信息表, insertList);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			Sys.update(TableConstant.工步信息表, updateList, " gbid = ? ", updateConditionValue);
		}
		
		updateList.clear();
		updateConditionValue.clear();
		//更新 xhMap 中   没有编辑和新增的工步信息的序号
		for(Entry<String, String> entry : xhMap.entrySet()){
			Map<String, Object> gbMap = new HashMap<String, Object>();	
			gbMap.put("gbxh", entry.getValue());
			updateConditionValue.add(new Object[]{entry.getKey()});
			updateList.add(gbMap);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			Sys.update(TableConstant.工步信息表, updateList, " gbid = ? ", updateConditionValue);
		}
		
		//删除 删除的工步信息
		List<Object[]> deleteCoditionValues = new ArrayList<Object[]>();
		String delete_rowids = parameters.getString("delete_rowids");
		JSONArray delete_rowids_jsonarray = JSONArray.fromObject(delete_rowids);
		for (int i = 0; i < delete_rowids_jsonarray.size(); i++) {
			String gbid = (String) delete_rowids_jsonarray.get(i);
			deleteCoditionValues.add(new Object[]{gbid});
		}
		if(CollectionUtils.isNotEmpty(deleteCoditionValues)){
			Sys.delete(TableConstant.工步信息表, " gbid = ? ", deleteCoditionValues);
		}
	}
	
	/**
	 * 工序--能力组保存
	 * @param parameters
	 * @param bundle
	 */
	public void saveNlz(Parameters parameters, Bundle bundle){
		//查询一下 当前已经有了的能力组
//		String gxid = parameters.getString("gxid");
		//修改/添加的数据
		String data = parameters.getString("data");
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		List<Object[]> updateConditionValue = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> nlzglMap = new HashMap<String, Object>();	
			String glid = (String) jsonarray.getJSONObject(i).get("glid");
			String gxid = (String) jsonarray.getJSONObject(i).get("gxid");
			nlzglMap.put("gxid", gxid);
			String nlzid = (String) jsonarray.getJSONObject(i).get("nlzid");
			nlzglMap.put("nlzid", nlzid);
			
			if("add".equals(jsonarray.getJSONObject(i).get("oper"))){
				insertList.add(nlzglMap);
			}
			if("edit".equals(jsonarray.getJSONObject(i).get("oper"))){
				updateConditionValue.add(new Object[]{glid});
				updateList.add(nlzglMap);
			}
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			Sys.insert(TableConstant.工序能力组关联表, insertList);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			Sys.update(TableConstant.工序能力组关联表, updateList, " glid = ? ", updateConditionValue);
		}
		
		//删除 删除的工序能力组关联信息
		List<Object[]> deleteCoditionValues = new ArrayList<Object[]>();
		String delete_rowids = parameters.getString("delete_rowids");
		JSONArray delete_rowids_jsonarray = JSONArray.fromObject(delete_rowids);
		for (int i = 0; i < delete_rowids_jsonarray.size(); i++) {
			String glid = (String) delete_rowids_jsonarray.get(i);
			deleteCoditionValues.add(new Object[]{glid});
		}
		if(CollectionUtils.isNotEmpty(deleteCoditionValues)){
			Sys.delete(TableConstant.工序能力组关联表, " glid = ? ", deleteCoditionValues);
		}
	
	}
	
	/**
	 * 工序--物料 保存
	 * @param parameters
	 * @param bundle
	 */
	public void saveWl(Parameters parameters, Bundle bundle){
		String gxid = parameters.getString("gxid");
		//修改/添加的数据
		String data = parameters.getString("data");
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		List<Object[]> updateConditionValue = new ArrayList<Object[]>();
		
		boolean sywl_flag = false;
		JSONArray jsonarray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> wlglMap = new HashMap<String, Object>();	
			String glid = (String) jsonarray.getJSONObject(i).get("glid");
			String wlid = (String) jsonarray.getJSONObject(i).get("wlid");
			wlglMap.put("wlid", wlid);
			wlglMap.put("gxid", gxid);
			String wlqfdm = (String) jsonarray.getJSONObject(i).get("wlqfdm");
			wlglMap.put("wlqfdm", wlqfdm);
			sywl_flag = "30".equals(wlqfdm);
			int wlsl = jsonarray.getJSONObject(i).getInt("wlsl");
			wlglMap.put("wlsl", wlsl);
			
			if("add".equals(jsonarray.getJSONObject(i).get("oper"))){
				insertList.add(wlglMap);
			}
			if("edit".equals(jsonarray.getJSONObject(i).get("oper"))){
				updateConditionValue.add(new Object[]{glid});
				updateList.add(wlglMap);
			}
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			Sys.insert(TableConstant.工序物料关联表, insertList);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			Sys.update(TableConstant.工序物料关联表, updateList, " glid = ? ", updateConditionValue);
		}
		
		//删除 删除的工序物料关联表
		List<Object[]> deleteCoditionValues = new ArrayList<Object[]>();
		String delete_rowids = parameters.getString("delete_rowids");
		JSONArray delete_rowids_jsonarray = JSONArray.fromObject(delete_rowids);
		for (int i = 0; i < delete_rowids_jsonarray.size(); i++) {
			String glid = (String) delete_rowids_jsonarray.get(i);
			deleteCoditionValues.add(new Object[]{glid});
		}
		if(CollectionUtils.isNotEmpty(deleteCoditionValues)){
			Sys.delete(TableConstant.工序物料关联表, " glid = ? ", deleteCoditionValues);
		}
	
		//如果是使用物料，修改生成物料的单位
		if(sywl_flag){
			//查询生成物料
			Dataset scwl_dataset = Sys.query(TableConstant.工序物料关联表, 
					"glid,wlid", " gxid = ? and wlqfdm = '40' ", null, new Object[]{gxid});
			Map<String,Object> glMap = scwl_dataset.getMap();
			if(MapUtils.isNotEmpty(glMap)){
				Dataset sywl_dataset = Sys.query(TableConstant.工序物料关联表, 
						"glid,wlid", " gxid = ? and wlqfdm = '30' ", null, new Object[]{gxid});
				String dwdm = "";
				String sywlid = sywl_dataset.getMap().get("wlid").toString();
				Parameters p = new Parameters();
				p.set("wlid",sywlid);
				Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", p);
				if(b!= null &&  b.get("materielInfo") != null){
					Map<String,Object> sywlinfo = (Map<String, Object>) b.get("materielInfo");
					dwdm = sywlinfo.get("wldwdm").toString();
				}
				//Parameters updateParameters = new Parameters();
				Map<String,Object> updateMap = new HashMap<String, Object>();
				updateMap.put("wldwdm", dwdm);
				p.set("wlxx", updateMap);
				p.set("condition", " wlid = ? ");
				List<Object> conditionValues = new ArrayList<Object>();
				conditionValues.add(glMap.get("wlid").toString());
				p.set("conditionValues", conditionValues);
				Sys.callModuleService("mm", "updateWlxxService", p);
			}
				
		}
	}
	
	
	/**
	 * 工序类表
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gxIndex(Parameters parameters, Bundle bundle){
		
		String gyid = parameters.getString("gyid");
		bundle.put("gyid", gyid);
		String ljid = parameters.getString("ljid");
		bundle.put("ljid", ljid);
		return "gxlb";
	}
	
//	/**
//	 * 工序列表 表格
//	 * @param parameters
//	 * @param bundle
//	 * @return
//	 */
//	public String gxTable(Parameters parameters, Bundle bundle){
//
//		String gyid = parameters.getString("gyid");
//		
////		String [] models = {TableConstant.工序信息表,TableConstant.工艺工序管理表,TableConstant.工艺表};
////		String join = new StringBuffer()
////				.append(TableConstant.工艺表).append(SqlConstant.JOIN)
////				.append(TableConstant.工艺工序管理表).append(SqlConstant.ON)
////				.append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("gyid").append(SqlConstant.EQUALS)
////				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("gyid")
////				
////				.append(SqlConstant.JOIN).append(TableConstant.工序信息表).append(SqlConstant.ON)
////				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("gxid").append(SqlConstant.EQUALS)
////				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").toString();
////		
////		String fields = new StringBuffer()
////				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").append(SqlConstant.COMMA)//工序id
////				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxxh").append(SqlConstant.COMMA)//工序序号
////				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxbh").append(SqlConstant.COMMA)//工序编号
////				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxmc")//工序名称
////				.toString();
////		
////		String conditions = TableConstant.工艺表 +".gyid = ? ";
////		String orderby = null;
////		Dataset dataset = Sys.query(models, join, fields, conditions, orderby, new Object[]{gyid});		
//		
//		Bundle b = Sys.callModuleService("pm", "query_gxxx_by_ljid_service", parameters);
//		List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("gxList");
//		
//		if(CollectionUtils.isNotEmpty(list)){
//			for(Map<String,Object> map : list){
//				String gxid = map.get("gxid").toString();
//				//能力组
//				Dataset datasetNlz = Sys.query(TableConstant.工序能力组关联表, "nlzid,glid", 
//						" gxid = ? ", null , new Object[]{gxid});
//				List<Map<String,Object>> nlzList = datasetNlz.getList();
//				parameters.set("nlzids", nlzList);
//				parameters.set("conditionNullReturnNull", "true");
//				Bundle b_nlz_bundle = Sys.callModuleService("em", "emservice_nlzList", parameters);
//				String nlzbh = "";
//				String nlzmc = "";
//				if(b_nlz_bundle!=null){
//					List<Map<String,Object>> returnList = (List<Map<String, Object>>) b_nlz_bundle.get("nlzList");
//					if(CollectionUtils.isNotEmpty(returnList)){
//						for(Map<String,Object> m : returnList){
//							String nlzbhStr = m.get("nlzbh").toString();
//							String nlzmcStr = m.get("nlzmc").toString();
//							
//							nlzbh = nlzbh + nlzbhStr + ";";
//							nlzmc = nlzmc + nlzmcStr + ";";
//						}
//					}
//				}
//				map.put("nlzbh", nlzbh);
//				map.put("nlzmc", nlzmc);
//				
//				//程序
//				Dataset datasetCx = Sys.query(TableConstant.程序管理, "cxmc,cxmczd", 
//						" gxid = ? and cxlb = '10' and yxbz = '10' and zxbz = '1' ", null , new Object[]{gxid});
//				Map<String,Object> mapCx = datasetCx.getMap();
//				Object cxmc = "";
//				Object cxmczd = "";
//				if(MapUtils.isNotEmpty(mapCx)){
//					cxmc = mapCx.get("cxmc");
//					cxmczd = mapCx.get("cxmczd");
//				}
//				map.put("cxmc", cxmc);
//				map.put("cxmczd", cxmczd);
//				
//				//刀具
//				Dataset datasetGb = Sys.query(TableConstant.工步信息表,"djid"," gxid = ? ",null, new Object[]{gxid});
//				List<Map<String,Object>> djidList = datasetGb.getList();
//				List<Map<String,Object>> wlidList = new ArrayList<Map<String,Object>>();
//				if(CollectionUtils.isNotEmpty(djidList)){
//					for(Map<String,Object> m : djidList){
//						Map<String,Object> mParam = new HashMap<String, Object>();
//						mParam.put("wlid", m.get("djid"));
//						wlidList.add(mParam);
//					}
//				}
//				parameters.set("wlids", wlidList);
//				Bundle b_dj_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
//				String djmc = "";
//				if(b_dj_bundle != null){
//					List<Map<String,Object>> djwlInfoList = (List<Map<String, Object>>) b_dj_bundle.get("materielInfoList");
//					if(CollectionUtils.isNotEmpty(djwlInfoList)){
//						for(Map<String,Object> m : djwlInfoList){
//							djmc = djmc + m.get("wlmc").toString() + ";";
//						}
//					}
//				}
//				map.put("djmc", djmc);
//				
//				//量具
//				Dataset ljdataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
//						" gxid = ? and wlqfdm = '20' ", null, new Object[]{gxid});
//				List<Map<String,Object>> ljList = ljdataset.getList();
//				parameters.set("wlids", ljList);
//				String ljmc = "";
//				Bundle b_lj_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
//				if(b_lj_bundle != null){
//					List<Map<String,Object>> ljmcInfoList = (List<Map<String, Object>>) b_lj_bundle.get("materielInfoList");
//					if(CollectionUtils.isNotEmpty(ljmcInfoList)){
//						for(Map<String,Object> m : ljmcInfoList){
//							ljmc = ljmc + m.get("wlmc").toString() + ";";
//						}
//					}
//				}
//				map.put("ljmc", ljmc);
//				
//				//夹具
//				Dataset jjdataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
//						" gxid = ? and wlqfdm = '10' ", null, new Object[]{gxid});
//				List<Map<String,Object>> jjList = jjdataset.getList();
//				parameters.set("wlids", jjList);
//				Bundle b_jj_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
//				String jjmc = "";
//				if(b_jj_bundle != null){
//					List<Map<String,Object>> jjmcInfoList = (List<Map<String, Object>>) b_jj_bundle.get("materielInfoList");
//					if(CollectionUtils.isNotEmpty(jjmcInfoList)){
//						for(Map<String,Object> m : jjmcInfoList){
//							jjmc = jjmc + m.get("wlmc").toString() + ";";
//						}
//					}
//				}
//				map.put("jjmc", jjmc);
//				
//				//使用物料 
//				Dataset sswldataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
//						" gxid = ? and wlqfdm = '30' ", null, new Object[]{gxid});
//				List<Map<String,Object>> sswlList = sswldataset.getList();
//				parameters.set("wlids", sswlList);
//				String sywlbh = "";
//				Bundle b_sywl_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
//				if(b_sywl_bundle != null){
//					List<Map<String,Object>> sywlmcInfoList = (List<Map<String, Object>>) b_sywl_bundle.get("materielInfoList");
//					if(CollectionUtils.isNotEmpty(sywlmcInfoList)){
//						for(Map<String,Object> m : sywlmcInfoList){
//							sywlbh = sywlbh + m.get("wlbh").toString() + ";";
//						}
//					}
//				}
//				map.put("sywlbh", sywlbh);
//				
//				//生成物料
//				Dataset scswldataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
//						" gxid = ? and wlqfdm = '40' ", null, new Object[]{gxid});
//				List<Map<String,Object>> scswList = scswldataset.getList();
//				parameters.set("wlids", scswList);
//				Bundle b_scwl_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
//				String scwlbh = "";
//				if(b_scwl_bundle != null){
//					List<Map<String,Object>> scwlmcInfoList = (List<Map<String, Object>>) b_scwl_bundle.get("materielInfoList");
//					if(CollectionUtils.isNotEmpty(scwlmcInfoList)){
//						for(Map<String,Object> m : scwlmcInfoList){
//							scwlbh = scwlbh + m.get("wlbh").toString() + ";";
//						}
//					}
//				}
//				map.put("scwlbh", scwlbh);
//			}
//		}
//		
//		bundle.put("rows", list);
//		bundle.put("totalPage", 100);
//		bundle.put("currentPage", 1);
//		bundle.put("totalRecord", 100);
//		return "json:";
//	
//	}
	
	/**
	 * 工序质检项录入index
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gxzjxlr(Parameters parameters, Bundle bundle) {
		//零件id
		String ljid = parameters.getString("ljid");
		bundle.put("ljid", ljid);
		//工序组id
		String gxzid = parameters.getString("gxzid");
		bundle.put("gxzid", gxzid);
		//加工状态
		String jgzt = parameters.getString("jgzt");
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
		bundle.put("jgzt", jgzt_str);
		// 根据零件id获取工序信息
		Parameters p = new Parameters();
		p.set("ljid", ljid);
		Bundle b = Sys.callModuleService("pm", "partsInfoService", p);
		if(b!= null){
			Map<String, Object> map = (Map<String, Object>) b.get("partsInfo");
			//产品名称
			bundle.put("cpmc", map.get("ljmc"));
			//图号
			bundle.put("th", map.get("ljbh"));
			//材料
			bundle.put("cl", map.get("clbh"));
		}
		
		Dataset dataset = Sys.query(TableConstant.工序组, "gxzid,gxzxh,gxzbh,gxzmc",
					" gxzid = ? ", null, new Object[]{gxzid});
		if(dataset != null && MapUtils.isNotEmpty(dataset.getMap())){
			//工序组 编号
			bundle.put("gxzbh", dataset.getMap().get("gxzbh"));
			//工序组 序号
			bundle.put("gxzxh", dataset.getMap().get("gxzxh"));
			//工序组 名称
			bundle.put("gxzmc", dataset.getMap().get("gxzmc"));
		}
		
		return "gxzjxlr";
	}
	
	/**
	 * 质检标准类型
	 * 目测、游标卡板、游标卡尺、投影仪、外径千分卡板、外径千分尺、中心孔测量仪、总测量仪等
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String searchZjbzlx(Parameters parameters, Bundle bundle){
		String param = parameters.getString("query");
		if(param != null){
			param = param.trim();
		}
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		Dataset dataset = Sys.query("pm_sjzdb", "label,title,pinyin1,pinyin2", " type= ?", null, 
				new Object[]{"zjbzlx"});
		List<Map<String,Object>> list = dataset.getList();
		if(CollectionUtils.isEmpty(list)){
			list = new ArrayList<Map<String,Object>>();
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
			map8.put("label", "总长测量仪");
			map8.put("title", "总长测量仪");
			map8.put("pinyin1", "zongchangceliangyi");
			map8.put("pinyin2", "zccly");
			list.add(map8);
			
			result = new ArrayList<Map<String,Object>>();
		}
		for(Map<String,Object> item : list){
			if(StringUtils.isEmpty(param) 
					|| item.get("label").toString().startsWith(param)
					|| item.get("pinyin1").toString().startsWith(param.toLowerCase())
					|| item.get("pinyin2").toString().startsWith(param.toLowerCase())){
				result.add(item);
			}
		}
		bundle.put("data", result);
		return "json:data";
	}
	
	public String gxzjxlrTable(Parameters parameters, Bundle bundle){
		//零件id
		String ljid = parameters.getString("ljid");
		//工序组id
		String gxzid = parameters.getString("gxzid");
		
		Dataset dataset = Sys.query(TableConstant.工序质检项录入, "gxzjxlrid,zjxh,ljid,gxzid,zjbzlx,jyxm,gyyq,yxsx,yxxx,swjsfj,xjsfj,hxsfj,zjsfj,zjysfj",
				" ljid = ? and gxzid = ? ", "zjxh asc", new Object[]{ljid,gxzid});
		List<Map<String,Object>> list = dataset.getList();
		
		for(Map map : list){
			if (null==map.get("swjsfj") || "".equals(map.get("swjsfj").toString())){
				map.put("swjsfj", "00");
			}
			if (null==map.get("xjsfj") || "".equals(map.get("xjsfj").toString())){
				map.put("xjsfj", "00");
			}
			if (null==map.get("hxsfj") || "".equals(map.get("hxsfj").toString())){
				map.put("hxsfj", "00");
			}
			if (null==map.get("zjsfj") || "".equals(map.get("zjsfj").toString())){
				map.put("zjsfj", "00");
			}
			if (null==map.get("zjysfj") || "".equals(map.get("zjysfj").toString())){
				map.put("zjysfj", "00");
			}
		}
		bundle.put("rows", list);
		bundle.put("totalPage", 100);
		bundle.put("currentPage", 1);
		bundle.put("totalRecord", 100);
		return "json:";
	}
	
	/**
	 * 保存 工序质检项录入 表格
	 * @param parameters
	 * @param bundle
	 */
	public void saveGxzjxlrb(Parameters parameters, Bundle bundle) {
		
		String ljid = parameters.getString("ljid");
		String gxzid = parameters.getString("gxzid");
		//修改/添加的数据
		String data = parameters.getString("data");
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		List<Object[]> updateConditionValue = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();	
			String gxzjxlrid = (String) jsonarray.getJSONObject(i).get("gxzjxlrid");
			map.put("gxzjxlrid", gxzjxlrid);
			String zjbzlx = (String) jsonarray.getJSONObject(i).get("zjbzlx");
			map.put("zjbzlx", zjbzlx);
			String jyxm = (String) jsonarray.getJSONObject(i).get("jyxm");
			if(StringUtils.isBlank(jyxm)){
				jyxm = "0";
			}
			map.put("jyxm", jyxm);
			String gyyq = (String) jsonarray.getJSONObject(i).get("gyyq");
			map.put("gyyq", gyyq);
			String yxsx = (String) jsonarray.getJSONObject(i).get("yxsx");
			map.put("yxsx", yxsx);
			String yxxx = (String) jsonarray.getJSONObject(i).get("yxxx");
			map.put("yxxx", yxxx);
			String zjxh = (String) jsonarray.getJSONObject(i).get("zjxh");
			map.put("zjxh", zjxh);
			
			map.put("ljid", ljid);
			map.put("gxzid", gxzid);
			
			//工序质检增加四项是否检查
			String swjsfj = "00".equals((String) jsonarray.getJSONObject(i).get("swjsfj"))?null:(String) jsonarray.getJSONObject(i).get("swjsfj");
			map.put("swjsfj", swjsfj);
			String xjsfj = "00".equals((String) jsonarray.getJSONObject(i).get("xjsfj"))?null:(String) jsonarray.getJSONObject(i).get("xjsfj");
			map.put("xjsfj", xjsfj);
			String hxsfj = "00".equals((String) jsonarray.getJSONObject(i).get("hxsfj"))?null:(String) jsonarray.getJSONObject(i).get("hxsfj");
			map.put("hxsfj", hxsfj);
			String zjsfj = "00".equals((String) jsonarray.getJSONObject(i).get("zjsfj"))?null:(String) jsonarray.getJSONObject(i).get("zjsfj");
			map.put("zjsfj", zjsfj);
			String zjysfj = "00".equals((String) jsonarray.getJSONObject(i).get("zjysfj"))?null:(String) jsonarray.getJSONObject(i).get("zjysfj");
			map.put("zjysfj", zjysfj);
			
			if("add".equals(jsonarray.getJSONObject(i).get("oper"))){
				insertList.add(map);
			}
			if("edit".equals(jsonarray.getJSONObject(i).get("oper"))){
				updateConditionValue.add(new Object[]{gxzjxlrid});
				updateList.add(map);
			}
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			Sys.insert(TableConstant.工序质检项录入, insertList);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			Sys.update(TableConstant.工序质检项录入, updateList, " gxzjxlrid = ? ", updateConditionValue);
		}
		
		//删除 删除的工序能力组关联信息
		List<Object[]> deleteCoditionValues = new ArrayList<Object[]>();
		String delete_rowids = parameters.getString("delete_rowids");
		JSONArray delete_rowids_jsonarray = JSONArray.fromObject(delete_rowids);
		for (int i = 0; i < delete_rowids_jsonarray.size(); i++) {
			String gxzjxlrid = (String) delete_rowids_jsonarray.get(i);
			deleteCoditionValues.add(new Object[]{gxzjxlrid});
		}
		if(CollectionUtils.isNotEmpty(deleteCoditionValues)){
			Sys.delete(TableConstant.工序质检项录入, " gxzjxlrid = ? ", deleteCoditionValues);
		}
	
	}
}
