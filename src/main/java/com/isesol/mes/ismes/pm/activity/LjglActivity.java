package com.isesol.mes.ismes.pm.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.isesol.mes.ismes.pm.constant.SqlConstant;
import com.isesol.mes.ismes.pm.constant.TableConstant;

public class LjglActivity {
	
	private Logger log4j = Logger.getLogger(LjglActivity.class);

	/**
	 * 零件管理页面初始化
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String index(Parameters parameters, Bundle bundle){
		String ljid = parameters.getString("ljid");
		
		return "ljgl";
	}
	
	/**
	 * table
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String table(Parameters parameters, Bundle bundle){

		String fields = new StringBuffer()
				.append("ljid").append(SqlConstant.COMMA)// 零件id
				.append("ljbh").append(SqlConstant.COMMA)// 零件编号
				.append("ljmc").append(SqlConstant.COMMA)// 零件名称
				.append("ljlbdm").append(SqlConstant.COMMA)// 零件类别
				.append("clbh").append(SqlConstant.COMMA)// 材料编号
				.append("dw").append(SqlConstant.COMMA)// 单位
				.append("clyd").append(SqlConstant.COMMA)// 材料硬度
				.append("ljtpid").append(SqlConstant.COMMA)// 零件图片ID			
				.append("ljxh").append(SqlConstant.COMMA)//零件项号
				.append("mpdh").append(SqlConstant.COMMA)//零件简码
				.append("jgzt").append(SqlConstant.COMMA)//加工状态
				.append("tzbb").append(SqlConstant.COMMA)//图纸版本
				.append("zxsl").append(SqlConstant.COMMA)//装箱数量		
				.append("ljms").toString();// 材料编号
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
		int pageSize = parameters.get("pageSize") == null ? 10 : parameters.getInteger("pageSize");

		Dataset dataset = Sys.query(TableConstant.零件管理表, fields, conditions.toString(), orderby, 
				(page - 1) * pageSize, pageSize,conditionValue.toArray());
		List<Map<String, Object>> list = dataset.getList();
		int totalPage = dataset.getTotal() % pageSize == 0 ? dataset.getTotal() / pageSize
				: dataset.getTotal() / pageSize + 1;
		
		bundle.put("rows", list);
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset.getCount());
		return "json:";
	
	}
	
	/**
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public String getLjbh(Parameters parameters, Bundle bundle){
		String ljbh = parameters.getString("ljbh");
		
		Dataset dataset = Sys.query(TableConstant.零件管理表, "ljid,ljmc", "ljbh = ? ", null, 
				new Object[]{ljbh});
		
		List<Map<String, Object>> list = dataset.getList();
		
		bundle.put("data", list);
		
		return "json:";
	}
	
	/**
	 * 20161110 add by maww 验证零件项号是否唯一
	 * @param parameters
	 * @param bundle
	 */
	public String getLjhx(Parameters parameters, Bundle bundle){
		String ljid = parameters.getString("ljid");
		String ljxh = parameters.getString("ljxh");
		Dataset dataset_valid = null;
		if (null == ljid || "".equals(ljid)){
			dataset_valid = Sys.query(TableConstant.零件管理表, "ljxh,ljid,ljtpid",  " ljxh = ? ", null, new Object[]{ljxh});	
		}else{
			dataset_valid = Sys.query(TableConstant.零件管理表, "ljxh,ljid,ljtpid",  " ljxh = ? and ljid <> ?", null, new Object[]{ljxh,ljid});
		}
		
	
		List<Map<String, Object>> list = dataset_valid.getList();
		
		bundle.put("data", list);
		
		return "json:";
	}
	
	/**
	 * 新增/修改 零件信息
	 * @param parameters
	 * @param bundle
	 */
	public void saveLjxx(Parameters parameters, Bundle bundle){
		Map<String,Object> map = new HashMap<String, Object>();
		String ljbh = parameters.getString("ljbh_modal");
		if(ljbh != null){
			ljbh = ljbh.trim();
		}
		map.put("ljbh", ljbh);
		String ljmc = parameters.getString("ljmc_modal");
		map.put("ljmc", ljmc);
		String ljlbdm = parameters.getString("ljlb_modal");
		map.put("ljlbdm", ljlbdm);
		String clbh = parameters.getString("clbh_modal");
		map.put("clbh", clbh);
		String dw = parameters.getString("dw_modal");
		map.put("dw", dw);
		String clyd = parameters.getString("clyd_modal");
		map.put("clyd", clyd);
		String clyddw = parameters.getString("clyddw_modal");
		map.put("clyddw", clyddw);
		String mpdh = parameters.getString("mpdh_modal");
		map.put("mpdh", mpdh);
		String ljms = parameters.getString("ljms_modal");
		map.put("ljms", ljms);
		
		String ljxh = parameters.getString("ljxh_modal");
		map.put("ljxh", ljxh);
		Integer zxsl = parameters.getInteger("zxsl_modal");
		map.put("zxsl", zxsl);
		Double cpzl_double = parameters.getDouble("cpzl_modal");
		if(cpzl_double != null){
			map.put("cpzl", new BigDecimal(cpzl_double));
		}
		Double dezl_double = parameters.getDouble("dezl_modal");
		if(dezl_double != null){
			map.put("dezl", new BigDecimal(dezl_double));
		}
		Integer cpcd = parameters.getInteger("cpcd_modal");
		map.put("cpcd", cpcd);
		//String jgzt = parameters.getString("jgzt_modal");
		String jgzt_hidden = parameters.getString("jgzt_hidden");
		map.put("jgzt", jgzt_hidden);
		String tzbb = parameters.getString("tzbb_modal");
		map.put("tzbb", tzbb);
		
		File ljtpid_file = parameters.getFile("ljtpid_modal");
		
		String ljid = parameters.getString("ljid_modal");		
		if(StringUtils.isBlank(ljid)){
			Sys.insert(TableConstant.零件管理表, map);
			ljid = map.get("ljid").toString();
			String ljtpid = saveFile(ljtpid_file,ljid , "");
			map.clear();
			if(StringUtils.isNotBlank(ljtpid)){
				map.put("ljtpid", ljtpid);
				Sys.update(TableConstant.零件管理表, map, " ljid = ? ", new Object[]{ljid});
			}
		}else{
			Dataset dataset = Sys.query(TableConstant.零件管理表, "ljtpid",  " ljid = ? ", null, new Object[]{ljid});
			String deleteFileInfoId = "";
			if(dataset != null && MapUtils.isNotEmpty(dataset.getMap()) && dataset.getMap().get("ljtpid") != null){
				deleteFileInfoId = dataset.getMap().get("ljtpid").toString();
			}
			String ljtpid = saveFile(ljtpid_file,ljid , deleteFileInfoId);
			if(StringUtils.isNotBlank(ljtpid)){
				map.put("ljtpid", ljtpid);
			}
			Sys.update(TableConstant.零件管理表, map, " ljid = ? ", new Object[]{ljid});
		}
				
	}
	
	private String saveFile(File file,String ljid,String deleteFileInfoId){
		if(file == null){
			return "";
		}
		String path = "/ljtp/"+ ljid ;
		Map<String,Object> fileMap = new HashMap<String,Object>();
		fileMap.put("wjlj", path);
		fileMap.put("wjmc", file.getName());
		fileMap.put("wjdx", file.getSize());
		fileMap.put("wjlb", file.getContentType());		
		Sys.saveFile(path, file.getInputStream());
		
		Sys.insert(TableConstant.附件文件表, fileMap);
		if(StringUtils.isNotBlank(deleteFileInfoId)){
			Sys.delete(TableConstant.附件文件表, " wjid = ? ", deleteFileInfoId.toString());
		}
		return fileMap.get("wjid").toString();
	}
	
	/**
	 * 根据零件id查询零件信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String query_ljxxByid(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		String fields = new StringBuffer()
				.append("ljid").append(SqlConstant.COMMA)// 零件id
				.append("ljbh").append(SqlConstant.COMMA)// 零件图号
				.append("ljmc").append(SqlConstant.COMMA)// 零件名称
				.append("ljlbdm").append(SqlConstant.COMMA)// 零件类别
				.append("clbh").append(SqlConstant.COMMA)// 材料规格
				.append("dw").append(SqlConstant.COMMA)// 单位
				.append("clyd").append(SqlConstant.COMMA)// 材料硬度  
				.append("clyddw").append(SqlConstant.COMMA)// 材料硬度单位
				.append("ljtpid").append(SqlConstant.COMMA)// 零件图片id
				.append("mpdh").append(SqlConstant.COMMA)// 零件简码
				
				.append("ljxh").append(SqlConstant.COMMA)// 零件项号
				.append("zxsl").append(SqlConstant.COMMA)// 装箱数量
				.append("cpzl").append(SqlConstant.COMMA)// 成品重量
				.append("dezl").append(SqlConstant.COMMA)// 定额重量
				.append("tzbb").append(SqlConstant.COMMA)// 图纸版本
				.append("cpcd").append(SqlConstant.COMMA)// 成品长度
				.append("jgzt").append(SqlConstant.COMMA)// 加工状态
				
				.append("ljms").toString();// 零件描述
		
		Dataset dataset = Sys.query(TableConstant.零件管理表, fields, " ljid = ? ", null, new Object[]{ljid});
		Map<String,Object> map =  dataset.getMap();
		if(map.get("ljtpid") != null){
			String url = Sys.getAbsoluteUrl("/ljgl/pic_display?wjid="+map.get("ljtpid").toString());
			map.put("url", url);
		}
		if(map.get("clbh") != null && StringUtils.isNotBlank(map.get("clbh").toString())){
			Parameters p = new Parameters();
			p.set("query_wlgg_eq", map.get("clbh").toString());
			Bundle mp_bundle = Sys.callModuleService("mm", "mmservice_wlxxkc", p);
			if(mp_bundle != null && mp_bundle.get("wlxxMap") != null){
				String wlid = ((Map<String,Object>)mp_bundle.get("wlxxMap")).get("wlid").toString();
				map.put("wlid", wlid);
			}
		}
		bundle.put("data",map);
		return "json:data";
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
	
	public void delete_ljxxByid(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		Sys.delete(TableConstant.零件管理表,  " ljid = ? ",  new Object[]{ljid});
	}
	
	/**
	 * 毛坯规格 选择
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String mp_query_search(Parameters parameters, Bundle bundle){
		String term = parameters.getString("term");
		term = StringUtils.isNotEmpty(term) ? term.trim() : "";
		
		Parameters p = new Parameters();
		p.set("query_wlgg", term);
		p.set("query_wlzt", "10");//启用的
		p.set("query_wllbdm_ids", "'40','50','70'");
		
		Bundle mp_bundle = Sys.callModuleService("mm", "mmservice_wlxxkc", p);
		if(mp_bundle == null){
			bundle.setError("根据物料规格："+term+"查询物料出现异常");
			return "json:select_query";
		}
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> list = (List<Map<String, Object>>) mp_bundle.get("wlxx");
		if(CollectionUtils.isNotEmpty(list)){
			for(Map<String,Object> map : list){
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("label", map.get("wlgg"));
				m.put("value", map.get("wlid"));
				returnList.add(m);
			}
		}
		bundle.put("select_query", returnList.toArray());
		
		return "json:select_query";
	}
	
	public String mp_select(Parameters parameters, Bundle bundle){

		String wlid = parameters.getString("wlid");
		wlid = StringUtils.isNotEmpty(wlid) ? wlid.trim() : "";
		
		Parameters p = new Parameters();
		p.set("wlid", wlid);
		Bundle mp_bundle = Sys.callModuleService("mm", "materielInfoByWlidService", p);
		if(mp_bundle == null){
			bundle.setError("根据物料id："+wlid+"查询物料出现异常");
			return "json:wlgg";
		}
		Map<String,Object> wlxxMap = (Map<String, Object>) mp_bundle.get("materielInfo");
		bundle.put("wlxx", wlxxMap);
		
		return "json:wlxx";
	}
}
