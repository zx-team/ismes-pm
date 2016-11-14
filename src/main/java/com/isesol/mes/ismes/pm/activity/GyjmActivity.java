package com.isesol.mes.ismes.pm.activity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.pm.constant.SqlConstant;
import com.isesol.mes.ismes.pm.constant.TableConstant;

import net.sf.json.JSONObject;

/**
 * 工艺建模
 * 
 * @author Think
 *
 */
public class GyjmActivity {

	private Logger log4j = Logger.getLogger(GyjmActivity.class);

	public String partsIndex(Parameters parameters, Bundle bundle) {
		
		return "ljlb";
	}

	/**
	 * table
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String partsTable(Parameters parameters, Bundle bundle) {
		String[] models = { TableConstant.零件管理表, TableConstant.工艺表 };
		String join = new StringBuffer().append(TableConstant.零件管理表).append(SqlConstant.LEFT_JOIN)
				.append(TableConstant.工艺表).append(SqlConstant.ON).append(TableConstant.零件管理表).append(SqlConstant.PERIOD)
				.append("ljid").append(SqlConstant.EQUALS).append(TableConstant.工艺表).append(SqlConstant.PERIOD)
				.append("ljid").toString();

		String fields = new StringBuffer().append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljid")
				.append(SqlConstant.COMMA)// 零件id
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljbh").append(SqlConstant.COMMA)// 零件编号
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljmc").append(SqlConstant.COMMA)// 零件名称
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("ljlbdm").append(SqlConstant.COMMA)// 零件类别
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("clbh").append(SqlConstant.COMMA)// 材料编号
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("mpdh").append(SqlConstant.COMMA)// 材料编号
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("tzbb").append(SqlConstant.COMMA)// 材料编号
				.append(TableConstant.零件管理表).append(SqlConstant.PERIOD).append("jgzt").append(SqlConstant.COMMA)// 材料编号
				.append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("gyid").append(SqlConstant.COMMA)// 工艺id
				.append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("jmztdm").toString();// 建模状态

		StringBuffer conditions = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		String ljbh = parameters.getString("ljbh");
		if(StringUtils.isNotBlank(ljbh)){
			conditions = conditions.append(" and ljbh like ? ");
			conditionValue.add("%" + ljbh + "%");
		}
		String ljmc = parameters.getString("ljmc");
		if(StringUtils.isNotBlank(ljmc)){
			conditions = conditions.append(" and ljmc like ? ");
			conditionValue.add("%" + ljmc + "%");
		}
		String ljlb = parameters.getString("ljlb");
		if(StringUtils.isNotBlank(ljlb)){
			conditions = conditions.append(" and ljlbdm = ? ");
			conditionValue.add( ljlb );
		}
		String clbh = parameters.getString("clbh");
		if(StringUtils.isNotBlank(clbh)){
			conditions = conditions.append(" and clbh like ? ");
			conditionValue.add("%" + clbh + "%");
		}
		
		String orderby = TableConstant.零件管理表 + SqlConstant.PERIOD + "ljbh";

		int page = parameters.get("page") == null ? 1 : parameters.getInteger("page");
		int pageSize = parameters.get("pageSize") == null ? 100 : parameters.getInteger("pageSize");

		Dataset dataset = Sys.query(models, join, fields, conditions.toString(), orderby, 
				(page - 1) * pageSize, pageSize,conditionValue.toArray());
		List<Map<String, Object>> list = dataset.getList();
		for (Map<String, Object> map : list) {
			if (map.get("jmztdm") == null) {
				map.put("jmztdm", "10");
			}
		}
		int totalPage = dataset.getTotal() % pageSize == 0 ? dataset.getTotal() / pageSize
				: dataset.getTotal() / pageSize + 1;
		bundle.put("rows", list);
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset.getTotal());
		return "json:";
	}
	
	public String getGyid(Parameters parameters, Bundle bundle){
		String ljid = parameters.getString("ljid");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("gyztdm", "10");
		map.put("ljid", ljid);
		map.put("jmztdm", "10");
		Sys.insert(TableConstant.工艺表, map);
		String gyid = map.get("gyid").toString();
		bundle.put("gyid", gyid);
		return "json:gyid";
	}

	/**
	 * 工艺建模的图形编辑器
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gyjmIndex(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		if (StringUtils.isBlank(ljid)) {
			bundle.setError("初始化页面，零件id不能为空");
			return "gyjm";
		}
		String gyid = parameters.getString("gyid");
		if (StringUtils.isBlank(gyid)) {
			bundle.setError("初始化页面，工艺id不能为空");
			return "gyjm";
		}
		bundle.put("ljid", ljid);
		bundle.put("gyid", gyid);
		
		//加载零件基本信息
		Bundle partsinfo_bundle = Sys.callModuleService("pm", "partsInfoService", parameters);
		if (partsinfo_bundle == null) {
			bundle.setError("根据零件id："+ljid+";;查询零件信息出现异常");
			return "gyjm";
		}
		Map<String,Object> partsMap = (Map<String, Object>) partsinfo_bundle.get("partsInfo");
		bundle.put("partsInfo", partsMap);
		
		//加载图形编辑器
		String nodes = "{    \"circle\": {        \"type\": \"circle\",        \"label\": \"工序\",        \"description\": \"工艺工序\",        \"image\": \"/ismes-web/pm/resource/circle\",        \"attributes\": [            {                \"name\": \"name\",                \"label\": \"工序名称\",                \"default\": \"\",                \"type\": \"text\",                \"validate\": {                    \"required\": true                }            },            {                \"name\": \"gxxh\",                \"label\": \"工序序号\",                \"type\": \"text\",                \"validate\": {                    \"required\": true                }            }        ]    } }";
		bundle.put("nodes", nodes);
		bundle.put("toolBtns", "[\"cursor\",\"direct\",\"|\",\"circle\",\"|\",\"group\"]");
		try {
			InputStream inputStream = Sys.readFile("/gygx/" + ljid + "_" + gyid);
			BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer tStringBuffer = new StringBuffer();
			String sTempOneLine = new String("");
			while ((sTempOneLine = tBufferedReader.readLine()) != null) {
				tStringBuffer.append(sTempOneLine);
				bundle.put("data", JSONObject.fromObject(tStringBuffer.toString()));
			}
		} catch (Exception e) {
			log4j.info(e.getMessage());
		}
		return "gyjm";
	}

	/**
	 * 图形编辑器  保存信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gyjmSave(Parameters parameters, Bundle bundle) {
		if (parameters.get("data") == null) {
			bundle.put("msg", "数据为空");
			return "json:msg";
		}
		String gyid = parameters.getString("gyid");
		String ljid = parameters.getString("ljid");

		if (StringUtils.isBlank(gyid) || StringUtils.isBlank(ljid)) {
			bundle.put("msg", "传入的参数不合法");
			return "json:msg";
		}
		
		Dataset dataset_gy = Sys.query(TableConstant.工艺表, "jmztdm", " gyid = ? ", null, new Object[]{gyid});
		if("30".equals(dataset_gy.getMap().get("jmztdm"))){
			bundle.setError( "该工艺已经发布,不能发进行修改！");
			bundle.put("message", "该工艺已经发布,不能发进行修改！");
			return "json:message";
		}

//		//已经存在的工序id
		Dataset set = Sys.query(TableConstant.工艺工序管理表, "gxid", "  gyid = ?  ", null, new Object[] { gyid });
		List<Map<String, Object>> list = set.getList();
		Set<String> gxSet = new HashSet<String>();
		StringBuffer sb = new StringBuffer();
		String conditon = "";
		for (Map<String, Object> m : list) {
			gxSet.add( m.get("gxid").toString() );
			sb = sb.append("'") .append(m.get("gxid").toString())  .append("',");
		}
		if(sb.toString().endsWith(",")){
			sb.deleteCharAt(sb.length() - 1);
			conditon = "gxid in (" + sb.toString() + ")";
		}else{
			conditon = " 1 = 0 ";
		}

		// 删除工艺工序信息
		Sys.delete(TableConstant.工艺工序管理表, " gyid = ? ", new Object[] { gyid });

		// 保存进数据库
		// 读取业务数据
		Map<String, Map<String, Object>> gxMap = new HashMap<String, Map<String, Object>>();
		Map<String, Object> map = JSONObject.fromObject(parameters.get("data"));
		Map<String, Object> nodes = (Map<String, Object>) map.get("nodes");

		// 读取节点数据 维护工序信息表
		Iterator itnode = nodes.entrySet().iterator();
		while (itnode.hasNext()) {
			Map<String, Object> node = new HashMap<String, Object>();
			Map.Entry entry = (Map.Entry) itnode.next();
			Map<String, Object> nodeValue = (Map<String, Object>) entry.getValue();
			Map<String, Object> attributes = (Map<String, Object>) nodeValue.get("attributes");
			node.put("gxmc", attributes.get("name"));
			//node.put("gxbh", attributes.get("gxbh"));
			node.put("gxxh", attributes.get("gxxh"));
			
			String nodeCode = entry.getKey().toString();
			Dataset dataset = Sys.query(TableConstant.工序信息表, "gxid", " txbjqjdmc = ? "
					+ "and " + conditon, null , new Object[]{nodeCode});
			if(dataset.getCount() > 0 ){
				String gxid = dataset.getMap().get("gxid").toString();
				Sys.update(TableConstant.工序信息表, node, " gxid = ? ", gxid);
				node.put("gxid", gxid );
				gxSet.remove(gxid);
			}else{
				node.put("txbjqjdmc", entry.getKey().toString());
				//
				Dataset dataset_ljbh = Sys.query(TableConstant.零件管理表, "ljbh", " ljid = ? ", null , new Object[]{ljid});
				if(dataset_ljbh != null && MapUtils.isNotEmpty(dataset_ljbh.getMap())){
					String ljbh = dataset_ljbh.getMap().get("ljbh").toString().trim();
					long sequence = Sys.getNextSequence("ljid_"+ljid);
					String sequence_str = String.valueOf(sequence);
					sequence_str = sequence_str.length() == 1 ? "0" + sequence_str : sequence_str ;
					node.put("gxbh", ljbh + sequence_str);
				}
				Sys.insert(TableConstant.工序信息表, node);
			}
			gxMap.put(entry.getKey().toString(), node);
		}
		
		//把工艺 不存在的工序 删除
		for (String gx_string : gxSet) {
			Sys.delete(TableConstant.工序信息表, " gxid = ? ", new Object[] { gx_string });
		}
		
		// //读取连线数据 维护 前序后续表
		List<Map<String,Object>> gygxList = new ArrayList<Map<String,Object>>();	
		for(Entry<String, Map<String, Object>> e : gxMap.entrySet()){
			Map<String,Object> entry_map = e.getValue();
			String gxid = entry_map.get("gxid").toString();
			Map<String, Object> gygxMap = new HashMap<String, Object>();
			gygxMap.put("gyid", gyid);
			gygxMap.put("gxid", gxid);
			gygxList.add(gygxMap);
		}
		Sys.insert("pm_gygxglb", gygxList);
		
		
		Map<String, Object> lines = (Map<String, Object>) map.get("lines");
		Iterator itline = lines.entrySet().iterator();

		List<Map<String,Object>> gygx_List = new ArrayList<Map<String,Object>>();
		List<Object[]> valueList = new ArrayList<Object[]>();
		while (itline.hasNext()) {
			Map.Entry entry = (Map.Entry) itline.next();
			Map<String, Object> lineValue = (Map<String, Object>) entry.getValue();
			String fromValue = (String) lineValue.get("from");
			String qxid = gxMap.get(fromValue).get("gxid").toString();
			String toValue = (String) lineValue.get("to");
			String gxid = gxMap.get(toValue).get("gxid").toString();

			Map<String, Object> gygxMap = new HashMap<String, Object>();
			gygxMap.put("qxid", qxid);
			gygx_List.add(gygxMap);
			valueList.add(new Object[]{gyid,gxid});
		}
		if(CollectionUtils.isNotEmpty(gygx_List)){
			Sys.update(TableConstant.工艺工序管理表, gygx_List, " gyid = ? and gxid = ? ", valueList);
		}
		
		Map<String,Object> m = new HashMap<String, Object>();
		m.put("jmztdm", "20");
		Sys.update(TableConstant.工艺表, m, " gyid = ? ", new Object[]{gyid});
		
		// 保存到本地
		ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(
				parameters.get("data").toString().getBytes());
		Sys.saveFile("/gygx/" + ljid + "_" + gyid, tInputStringStream);

		bundle.put("msg", "OK");
		return "json:msg";
	}

	/**
	 * 工艺发布
	 * @param parameters
	 * @param bundle
	 */
	public String gyfb(Parameters parameters, Bundle bundle){
		String gyid = parameters.getString("gyid");
		String message = "";
		if(StringUtils.isBlank(gyid)){
			message = "工艺id为空，请联系管理员！";
			bundle.setError(message);
			bundle.put("message", message);
			return "json:message";
		}
		Dataset dataset_gy = Sys.query(TableConstant.工艺表, "jmztdm", " gyid = ? ", null, new Object[]{gyid});
		if(dataset_gy == null || MapUtils.isEmpty(dataset_gy.getMap())){
			message = "根据工艺id " + gyid + " 查询工艺信息为空，请联系管理员！";
			bundle.setError(message);
			bundle.put("message", message);
			return "json:message";
		}
		if("30".equals(dataset_gy.getMap().get("jmztdm"))){
			message = "该工艺已经发布,不能再次发布！";
			bundle.setError(message);
			bundle.put("message", message);
			return "json:message";
		}
		
		
		String [] models = {TableConstant.工序信息表, TableConstant.工艺工序管理表,TableConstant.工艺表};
		String join = new StringBuffer()
				
				.append(TableConstant.工艺工序管理表).append(SqlConstant.JOIN).append(TableConstant.工艺表)
				.append(SqlConstant.ON).append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("gyid")
				.append(SqlConstant.EQUALS)
				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("gyid")
				
				.append(SqlConstant.JOIN).append(TableConstant.工序信息表).append(SqlConstant.ON)
				.append(TableConstant.工艺工序管理表).append(SqlConstant.PERIOD).append("gxid").append(SqlConstant.EQUALS)
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").toString();
		
		String fields = new StringBuffer()
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxid").append(SqlConstant.COMMA)//工序id
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxxh").append(SqlConstant.COMMA)//工序序号
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxbh").append(SqlConstant.COMMA)//工序编号
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("gxmc").append(SqlConstant.COMMA)//工序名称
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("jgjp").append(SqlConstant.COMMA)//加工节拍
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("zbsj").append(SqlConstant.COMMA)//准备时间
				.append(TableConstant.工序信息表).append(SqlConstant.PERIOD).append("jgfs").append(SqlConstant.COMMA)
				.append(TableConstant.工艺表).append(SqlConstant.PERIOD).append("ljid").toString();//零件ID
		
		String conditions = TableConstant.工艺表 +".gyid = ? ";
		String orderby = TableConstant.工序信息表 + ".gxbh";
		Dataset dataset = Sys.query(models, join, fields, conditions, orderby, new Object[]{gyid});
		
		if(dataset == null || CollectionUtils.isEmpty(dataset.getList())){
			message ="工序信息不存在，不能发布！";
			bundle.setError(message);
			bundle.put("message", message);
			return "json:message";
		}
		
//		StringBuffer sbMessage = new StringBuffer();
		List<Map<String,Object>> list = dataset.getList();
		Integer ljid = (Integer) list.get(0).get("ljid");
		Integer gxsl = list.size();//工序数量，sendActivity 使用。add by yangfan
//		for(int i = 0 ; i < list.size() ; i ++){
//			Map<String,Object> map = list.get(i);
//			if(map.get("gxxh") == null || StringUtils.isBlank(map.get("gxxh").toString())
//					|| map.get("gxmc") == null || StringUtils.isBlank(map.get("gxmc").toString())){
//				message ="工序名称或者工序序号为空，不能发布！";
//				bundle.setError(message);
//				bundle.put("message", message);
//				return "json:message";
//			}
//			String gxid = map.get("gxid").toString();
//			String gxmc = map.get("gxmc").toString();
//			StringBuffer thisSb = new StringBuffer(gxmc + ":"); 
//			boolean flag = false;
//			//工步信息
//			Dataset dataset_gb = Sys.query(TableConstant.工步信息表, "gbid,gbxh", " gxid = ? ", "gbxh", new Object[]{gxid});
//			if(dataset_gb.getCount() == 0 ){
//				thisSb = thisSb.append("工步信息不能为空！");
//				flag = true;
//			}
//			//设备类型
//			Dataset dataset_nlz = Sys.query(TableConstant.工序设备类型关联表, "sblxid,glid", " gxid = ? ", null , new Object[]{gxid});
//			if(dataset_nlz.getCount() == 0 ){
//				thisSb = thisSb.append("设备类型不能为空！");
//				flag = true;
//			}
//			
//			//物料
//			Dataset dataset_wl = Sys.query(TableConstant.工序物料关联表, "wlid,wlsl,glid,gxid,wlqfdm", " gxid = ? and wlqfdm = ? ",
//					null , new Object[]{gxid,"30"});
//			if(dataset_wl.getCount() == 0 ){
//				thisSb = thisSb.append("物料信息不能为空！");
//				flag = true;
//			}
//			
//			//夹具
//			Dataset dataset_jj = Sys.query(TableConstant.工序物料关联表, "wlid,wlsl,glid,gxid,wlqfdm", " gxid = ? and wlqfdm = ? ",
//					null , new Object[]{gxid,"10"});
//			if(dataset_jj.getCount() == 0 ){
//				thisSb = thisSb.append("夹具信息不能为空！");
//				flag = true;
//			}
//			
//			//量具
//			Dataset dataset_lj = Sys.query(TableConstant.工序物料关联表, "wlid,wlsl,glid,gxid,wlqfdm", " gxid = ? and wlqfdm = ? ",
//					null , new Object[]{gxid,"20"});
//			if(dataset_lj.getCount() == 0 ){
//				thisSb = thisSb.append("量具信息不能为空！");
//				flag = true;
//			}
//			
//			if(flag){
//				sbMessage = sbMessage.append(thisSb).append("</br>");
//			}
//		}
//		if(StringUtils.isNotBlank(sbMessage.toString())){
//			message = sbMessage.toString();
//			bundle.setError(message);
//			bundle.put("message", message);
//			return "json:message";
//		}
		
		//修改工艺状态
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("jmztdm", "30");
		Sys.update(TableConstant.工艺表, map, " gyid = ? ", new Object[]{gyid});
		bundle.put("message", "OK");
		
		//send activity
		//查询零件基本信息	
		Dataset dataset_lj = Sys.query(TableConstant.零件管理表, "ljbh,ljmc", "  ljid = ?  ", null, new Object[] { ljid });
		
		List<Map<String, Object>> lj_list = dataset_lj.getList();
		Object ljbh = lj_list.get(0).get("ljbh");//零件编号
		Object ljmc = lj_list.get(0).get("ljmc");//零件名称
		
		String activityType = "4"; //动态任务
		String[] roles = new String[] { "PRODUCTION_ROLE" };//产品管理角色
		String templateId = "gyfb_tp";
		Map<String, Object> data = new HashMap<String, Object>();
		
		//查询零件图片
		parameters.set("ljid", ljid);
		Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", parameters);
		Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
				
		data.put("ljid", ljid);//零件id
		data.put("ljbh", ljbh);//零件编号
		data.put("ljmc", ljmc);//零件名称
		data.put("gyid", gyid);//工艺ID
		data.put("ljtp", ljtp);//零件图片
		data.put("gxsl", gxsl);//工序数量
		data.put("userid", Sys.getUserIdentifier());//操作人id
		data.put("username", Sys.getUserName());//操作人名称
		sendActivity(activityType, templateId, true, roles, null, null, data);
		
		return "json:message";
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
