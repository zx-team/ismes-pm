package com.isesol.mes.ismes.pm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.pm.constant.TableConstant;

public class GxService {
	/**
	 * 图片关联
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String pic_display(Parameters parameters, Bundle bundle) {
		String wjid = parameters.getString("wjid");
		if (StringUtils.isNotBlank(wjid)) {
			Dataset dataset = Sys.query(TableConstant.附件文件表, "wjid,wjmc,wjlj,wjlb,wjdx", " wjid = ? ", null,
					new Object[] { wjid });
			Map<String, Object> map = dataset.getMap();
			if (map != null) {
				File file_display = new File((String) map.get("wjmc"), null, Sys.readFile((String) map.get("wjlj")),
						(String) map.get("wjlb"), Long.valueOf(map.get("wjdx").toString()));
				bundle.put("file_display", file_display);
			} else {
				bundle.put("file_display", null);
			}
		} else {
			bundle.put("file_display", null);
		}
		return "file:file_display";
	}

	public void query_gxxxFile(Parameters parameters, Bundle bundle) {

		String fieldsStr = "zlbj,gxid,gxxh,gxbh,gxlxdm,gxmc,zjfs,jgfs,jgjp,zbsj,fjsm,gxtzid,gyzdwjid,gxmc";
		Dataset gxDataset = Sys.query(TableConstant.工序信息表, fieldsStr ,
				" gxid = ? ", null, new Object[]{parameters.getString("gxid")});
		bundle.put("cxxx", gxDataset.getList());

		Map<String, Object> map_gx = new HashMap<String, Object>();
		if (gxDataset.getList().size() > 0) {
			map_gx = gxDataset.getList().get(0);
			map_gx.put("url", Sys.getAbsoluteUrl("/service/pic_display?wjid=" + map_gx.get("gxtzid")));
		}
		bundle.put("gxtpxx", map_gx);
	}
	public void query_gxzxxFile(Parameters parameters, Bundle bundle) {
		
		String fieldsStr = "zlbj,gxid,gxxh,gxbh,gxlxdm,gxmc,zjfs,jgfs,jgjp,zbsj,fjsm,gxtzid,gyzdwjid,gxmc";
		Dataset gxDataset = Sys.query(TableConstant.工序信息表, fieldsStr ,
				"  gxid in  "+ parameters.get("gxids") , null, new Object[]{});
		List<Map<String, Object>> gxtp = (List<Map<String, Object>>) gxDataset.getList();
		for (int i = 0; i < gxtp.size(); i++) {
			gxtp.get(i).put("url", Sys.getAbsoluteUrl("/service/pic_display?wjid=" +gxtp.get(i).get("gxtzid")));
		}
		bundle.put("gxtp", gxtp);
		
	}
	
	public void table_zjxx(Parameters parameters, Bundle bundle) {
		String gxzid = parameters.getString("s_gxzid");
		String s_fzj = parameters.getString("s_fzj");
		String s_jylx = parameters.getString("s_jylx");
		List<Object> val = new ArrayList<Object>();
		String con = "  gxzid = ?";
		val.add(gxzid);
		if("10".equals(s_fzj)) {
			con = con + " and  zjsfj = '10' ";
			if("10".equals(s_jylx)||"30".equals(s_jylx)) {
				con = con + " and  swjsfj = '10' ";
			}else if("20".equals(s_jylx)){
				con = con + " and  xjsfj = '10' ";
			}
		}else if("20".equals(s_fzj)){
			con = con + " and  zjysfj = '10' ";
			if("10".equals(s_jylx)||"30".equals(s_jylx)) {
				con = con + " and  swjsfj = '10' ";
			}else if("20".equals(s_jylx)){
				con = con + " and  xjsfj = '10' ";
			}
		}else if("30".equals(s_fzj)){
			con = con + " and  hxsfj = '10' ";
		}
		
		Dataset zjxxDataset = Sys.query(TableConstant.工序质检项录入, "gxzjxlrid,zjxh,ljid,gxzid,zjbzlx,jyxm,gyyq,yxsx,yxxx,swjsfj,zjjsfj,zjsfj,xjsfj,hxsfj",
				con, "zjxh", val.toArray());
		bundle.put("zjxx", zjxxDataset.getList());
	}
}
