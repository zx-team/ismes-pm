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

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.pm.constant.SqlConstant;
import com.isesol.mes.ismes.pm.constant.TableConstant;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GxActivity {
	
	private Logger log4j = Logger.getLogger(GxActivity.class);

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
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljid")	//零件id
				//20161105 add by maww 查询零件的“加工状态”
				.append(SqlConstant.COMMA).append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("jgzt").toString();//零件"加工状态"
		
		String conditions = TableConstant.工艺表 +".gyid = ? ";
		String orderby = null;
		Dataset ljDataset = Sys.query(models, join, fields, conditions, orderby, new Object[]{gyid});
		Map<String,Object> ljMap = ljDataset.getMap();
//		bundle.put("ljxx", ljMap);
		
		//加载零件基本信息
		Parameters p_partsinfo = new Parameters();
		p_partsinfo.set("ljid", ljMap.get("ljid"));
		Bundle partsinfo_bundle = Sys.callModuleService("pm", "partsInfoService", p_partsinfo);
		if (partsinfo_bundle == null) {
			bundle.setError("根据零件id："+ljMap.get("ljid")+";;查询零件信息出现异常");
			return "gyjm";
		}
		Map<String,Object> partsMap = (Map<String, Object>) partsinfo_bundle.get("partsInfo");
		//20161105 add by maww 设置零件的“加工状态”
		//partsMap.put("jgzt", ljMap.get("jgzt"));
		partsMap.put("jgzt", parameters.getString("jgzt"));
		
		bundle.put("partsInfo", partsMap);
		
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
		Dataset dataset = Sys.query(TableConstant.工步信息表, "gbid,gbxh,gxid,djtzbh,djid,jggcms,"
				+ "qxzj_min,qxzj_max,qxsud,zzzs,qxshend,jgl,"
				+ "dw,dpxhid,djsm", " gxid = ? ", "gbxh", new Object[]{gxid});
		List<Map<String,Object>> gbList = dataset.getList();
		if(CollectionUtils.isNotEmpty(gbList)){
			for(Map<String,Object> m : gbList){
				if(m.get("djid") != null && StringUtils.isNotEmpty(m.get("djid").toString())){
					String wlid = m.get("djid").toString();
					Parameters dj_parameter = new Parameters();
					dj_parameter.set("wlid", wlid);
					Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", dj_parameter);
					if(b != null && b.get("materielInfo") != null){
						Map<String,Object> m_wl =  (Map<String, Object>) b.get("materielInfo") ;
						m.put("djbh", m_wl.get("wlbh"));
					}
				}
				
				if(m.get("dpxhid") != null && StringUtils.isNotEmpty(m.get("dpxhid").toString())){
					String dpxhid = m.get("dpxhid").toString();
					Parameters dp_parameter = new Parameters();
					dp_parameter.set("wlid", dpxhid);
					Bundle b = Sys.callModuleService("mm", "materielInfoByWlidService", dp_parameter);
					if(b != null && b.get("materielInfo") != null){
						Map<String,Object> m_wl =  (Map<String, Object>) b.get("materielInfo") ;
//						m.put("sysm", m_wl.get("sysm"));
						m.put("wltm", m_wl.get("wltm"));
						m.put("dpxhbh", m_wl.get("wlbh"));
					}
				}
				
				if(m.get("wltm") == null || "".equals(m.get("wltm").toString())){
					m.put("wltm", "");
				}
//				if(m.get("sysm") == null || "".equals(m.get("sysm").toString())){
//					m.put("sysm", "");
//				}
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
	 * 设备类型的table
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String sblxTable(Parameters parameters, Bundle bundle){
		
		String gxid = parameters.getString("gxid");
		Dataset dataset = Sys.query(TableConstant.工序设备类型关联表, "sblxid,glid,gxid", " gxid = ? ", null , new Object[]{gxid});
		List<Map<String,Object>> gxsblxList = dataset.getList();
		
		Parameters p = new Parameters();
		p.set("sblxids", gxsblxList);
		p.set("conditionNullReturnNull", true);
		Bundle b = Sys.callModuleService("em", "query_sblx_service", p);
		
		List<Map<String,Object>> sblxList = (List<Map<String, Object>>) b.get("sblxList");
		
		for(Map<String,Object> gl_map : gxsblxList){
			for(Map<String,Object> sblx_map : sblxList){
				if(gl_map.get("sblxid").toString().equals(sblx_map.get("sblxid").toString())){
					gl_map.putAll(sblx_map);
				}
			}
		}
		
		bundle.put("rows", gxsblxList);
		bundle.put("totalPage", 1);
		bundle.put("currentPage", 1);
		bundle.put("records", 100);
		return "json:";
	}
	
	public String sblxEdit(Parameters parameters, Bundle bundle){
		String query = (String) parameters.get("query");
		parameters.set("sblxbh", query);
		Bundle b = Sys.callModuleService("em", "query_sblx_service", parameters);
		List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("sblxList");
		for (int i = 0; i < list.size(); i++) {
			list.get(i).put("label", list.get(i).get("sblxbh"));
			list.get(i).put("value", list.get(i).get("sblxid"));
			list.get(i).put("title", list.get(i).get("sblxbh"));
		}
		
		bundle.put("data", list);
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
		Map<String,Object> hashmap = (Map<String, Object>) parameters.get("wlxx");
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
		//20161105 add by maww 零件加工状态
		String ljjgzt = parameters.getString("hidden-ljjgzt");
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
			//20161105 modify by maww 修改物料的类型编号规则：BCP_+零件编号+加工状态+产生物料工序序号
			String wlbh = "BCP_" + gxbh + "_" + ljjgzt + "_" + Sys.getNextSequence(gxid);
			wlMap.put("wlbh", wlbh);
			String wlmc = ljmc + "_" + gxmc;
			wlMap.put("wlmc", wlmc);
			//物料类别   半成品
			wlMap.put("wllbdm", "70");
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
			gbMap.put("dw", jsonarray.getJSONObject(i).get("dw"));
			gbMap.put("djtzbh", jsonarray.getJSONObject(i).get("djtzbh"));
			gbMap.put("djid", jsonarray.getJSONObject(i).get("djid"));
			gbMap.put("dpxhid", jsonarray.getJSONObject(i).get("dpxhid"));
			gbMap.put("jggcms", jsonarray.getJSONObject(i).get("jggcms"));
			gbMap.put("qxzj_max", this.handleNullValue(jsonarray.getJSONObject(i), "qxzj_max",Double.class,0));
			gbMap.put("qxzj_min", this.handleNullValue(jsonarray.getJSONObject(i), "qxzj_min",Double.class,0));
			gbMap.put("qxsud", this.handleNullValue(jsonarray.getJSONObject(i), "qxsud",Double.class,0));
			gbMap.put("zzzs", this.handleNullValue(jsonarray.getJSONObject(i), "zzzs",Double.class,0));
			gbMap.put("qxshend", this.handleNullValue(jsonarray.getJSONObject(i), "qxshend",Double.class,0));
			gbMap.put("jgl", this.handleNullValue(jsonarray.getJSONObject(i), "jgl",Double.class,0));
			gbMap.put("djsm", this.handleNullValue(jsonarray.getJSONObject(i), "djsm",Integer.class,0));
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
			Dataset dataset = Sys.query(TableConstant.工步信息表, "gbid", " gbid = ? ", null, new Object[]{gbid});
			if(dataset.getMap() != null){
				deleteCoditionValues.add(new Object[]{gbid});
			}
		}
		if(CollectionUtils.isNotEmpty(deleteCoditionValues)){
			Sys.delete(TableConstant.工步信息表, " gbid = ? ", deleteCoditionValues);
		}
	}
	
	/**
	 * 工序--设备类型保存
	 * @param parameters
	 * @param bundle
	 */
	public void saveSblx(Parameters parameters, Bundle bundle){
		//修改/添加的数据
		String data = parameters.getString("data");
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		List<Object[]> updateConditionValue = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> sblxMap = new HashMap<String, Object>();	
			String glid = (String) jsonarray.getJSONObject(i).get("glid");
			String gxid = (String) jsonarray.getJSONObject(i).get("gxid");
			sblxMap.put("gxid", gxid);
			String sblxid = (String) jsonarray.getJSONObject(i).get("sblxid");
			sblxMap.put("sblxid", sblxid);
			
			if("add".equals(jsonarray.getJSONObject(i).get("oper"))){
				insertList.add(sblxMap);
			}
			if("edit".equals(jsonarray.getJSONObject(i).get("oper"))){
				updateConditionValue.add(new Object[]{glid});
				updateList.add(sblxMap);
			}
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			Sys.insert(TableConstant.工序设备类型关联表, insertList);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			Sys.update(TableConstant.工序设备类型关联表, updateList, " glid = ? ", updateConditionValue);
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
			Sys.delete(TableConstant.工序设备类型关联表, " glid = ? ", deleteCoditionValues);
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
		
		//加载零件基本信息
		Bundle partsinfo_bundle = Sys.callModuleService("pm", "partsInfoService", parameters);
		if (partsinfo_bundle == null) {
			bundle.setError("根据零件id："+ljid+";;查询零件信息出现异常");
			return "gxlb";
		}
		Map<String,Object> partsMap = (Map<String, Object>) partsinfo_bundle.get("partsInfo");
		bundle.put("partsInfo", partsMap);
		
		return "gxlb";
	}
	
	/**
	 * 工序列表 表格
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gxTable(Parameters parameters, Bundle bundle){

		String gyid = parameters.getString("gyid");
		
		Bundle b = Sys.callModuleService("pm", "query_gxxx_by_ljid_service", parameters);
		List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("gxList");
		
		if(CollectionUtils.isNotEmpty(list)){
			for(Map<String,Object> map : list){
				String gxid = map.get("gxid").toString();
				//设备类型
				Dataset datasetSblx = Sys.query(TableConstant.工序设备类型关联表,
						" glid,gxid,sblxid",
						" gxid = ? ", null , new Object[]{gxid});
				List<Map<String,Object>> gxsblxList = datasetSblx.getList();
				
				Parameters p_sb = new Parameters();
				p_sb.set("sblxids", gxsblxList);
				p_sb.set("conditionNullReturnNull", true);
				Bundle b_sblx = Sys.callModuleService("em", "query_sblx_service", p_sb);
				
				String sblxbhStr = "";
				String sblxmcStr = "";
				if(b_sblx!=null){
					List<Map<String,Object>> sblxList = (List<Map<String, Object>>) b_sblx.get("sblxList");
					if(CollectionUtils.isNotEmpty(sblxList)){
						for(Map<String,Object> m : sblxList){
							String sblxbh = m.get("sblxbh").toString();
							String sblxmc = m.get("sblxmc").toString();
							
							sblxbhStr = sblxbhStr + sblxbh + ";";
							sblxmcStr = sblxmcStr + sblxmc + ";";
						}
					}
				}
				map.put("sblxbh", sblxbhStr);
				map.put("sblxmc", sblxmcStr);
				
				//程序
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
				
				//刀具
				Dataset datasetGb = Sys.query(TableConstant.工步信息表,"djid,dpxhid"," gxid = ? ",null, new Object[]{gxid});
				List<Map<String,Object>> gbList = datasetGb.getList();
				List<Map<String,Object>> dtidList = new ArrayList<Map<String,Object>>();
				List<Map<String,Object>> dpidList = new ArrayList<Map<String,Object>>();
				if(CollectionUtils.isNotEmpty(gbList)){
					for(Map<String,Object> m : gbList){
						Map<String,Object> djParam = new HashMap<String, Object>();
						djParam.put("wlid", m.get("djid"));
						dtidList.add(djParam);
						
						Map<String,Object> dpParam = new HashMap<String, Object>();
						dpParam.put("wlid", m.get("dpxhid"));
						dpidList.add(dpParam);
					}
				}
				Parameters p_dt = new Parameters();
				p_dt.set("wlids", dtidList);
				Bundle b_dt_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p_dt);
				String djmc = "";
				if(b_dt_bundle != null){
					List<Map<String,Object>> djwlInfoList = (List<Map<String, Object>>) b_dt_bundle.get("materielInfoList");
					if(CollectionUtils.isNotEmpty(djwlInfoList)){
						for(Map<String,Object> m : djwlInfoList){
							djmc = djmc + m.get("wlmc").toString() + ";";
						}
					}
				}
				map.put("djmc", djmc);
				
				
				Parameters p_dp = new Parameters();
				p_dp.set("wlids", dpidList);
				Bundle b_dp_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p_dp);
				String dpmc = "";
				if(b_dp_bundle != null){
					List<Map<String,Object>> djwlInfoList = (List<Map<String, Object>>) b_dp_bundle.get("materielInfoList");
					if(CollectionUtils.isNotEmpty(djwlInfoList)){
						for(Map<String,Object> m : djwlInfoList){
							dpmc = dpmc + m.get("wlmc").toString() + ";";
						}
					}
				}
				map.put("dpmc", dpmc);
				
				//量具
				Dataset ljdataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
						" gxid = ? and wlqfdm = '20' ", null, new Object[]{gxid});
				List<Map<String,Object>> ljList = ljdataset.getList();
				Parameters p_lj = new Parameters();
				p_lj.set("wlids", ljList);
				String ljmc = "";
				Bundle b_lj_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p_lj);
				if(b_lj_bundle != null){
					List<Map<String,Object>> ljmcInfoList = (List<Map<String, Object>>) b_lj_bundle.get("materielInfoList");
					if(CollectionUtils.isNotEmpty(ljmcInfoList)){
						for(Map<String,Object> m : ljmcInfoList){
							ljmc = ljmc + m.get("wlmc").toString() + ";";
						}
					}
				}
				map.put("ljmc", ljmc);
				
				//夹具
				Dataset jjdataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
						" gxid = ? and wlqfdm = '10' ", null, new Object[]{gxid});
				List<Map<String,Object>> jjList = jjdataset.getList();
				Parameters p_jj = new Parameters();
				p_jj.set("wlids", jjList);
				Bundle b_jj_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p_jj);
				String jjmc = "";
				if(b_jj_bundle != null){
					List<Map<String,Object>> jjmcInfoList = (List<Map<String, Object>>) b_jj_bundle.get("materielInfoList");
					if(CollectionUtils.isNotEmpty(jjmcInfoList)){
						for(Map<String,Object> m : jjmcInfoList){
							jjmc = jjmc + m.get("wlmc").toString() + ";";
						}
					}
				}
				map.put("jjmc", jjmc);
				
				//使用物料 
				Dataset sswldataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
						" gxid = ? and wlqfdm = '30' ", null, new Object[]{gxid});
				List<Map<String,Object>> sswlList = sswldataset.getList();
				Parameters p_sywl = new Parameters();
				p_sywl.set("wlids", sswlList);
				String sywlbh = "";
				Bundle b_sywl_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p_sywl);
				if(b_sywl_bundle != null){
					List<Map<String,Object>> sywlmcInfoList = (List<Map<String, Object>>) b_sywl_bundle.get("materielInfoList");
					if(CollectionUtils.isNotEmpty(sywlmcInfoList)){
						for(Map<String,Object> m : sywlmcInfoList){
							sywlbh = sywlbh + m.get("wlbh").toString() + ";";
						}
					}
				}
				map.put("sywlbh", sywlbh);
				
				//生成物料
				Dataset scswldataset = Sys.query(TableConstant.工序物料关联表, "wlid", 
						" gxid = ? and wlqfdm = '40' ", null, new Object[]{gxid});
				List<Map<String,Object>> scswList = scswldataset.getList();
				Parameters p_scwl = new Parameters();
				p_scwl.set("wlids", scswList);
				Bundle b_scwl_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p_scwl);
				String scwlbh = "";
				if(b_scwl_bundle != null){
					List<Map<String,Object>> scwlmcInfoList = (List<Map<String, Object>>) b_scwl_bundle.get("materielInfoList");
					if(CollectionUtils.isNotEmpty(scwlmcInfoList)){
						for(Map<String,Object> m : scwlmcInfoList){
							scwlbh = scwlbh + m.get("wlbh").toString() + ";";
						}
					}
				}
				map.put("scwlbh", scwlbh);
			}
		}
		
		bundle.put("rows", list);
		bundle.put("totalPage", 100);
		bundle.put("currentPage", 1);
		bundle.put("totalRecord", 100);
		return "json:";
	
	}
}
