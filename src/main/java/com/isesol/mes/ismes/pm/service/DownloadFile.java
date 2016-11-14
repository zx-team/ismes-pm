package com.isesol.mes.ismes.pm.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.pm.constant.TableConstant;

public class DownloadFile {
	/**
	 * 下载程序
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String downloadFiles(Parameters parameters, Bundle bundle) {
		String wjid = parameters.getString("fid");
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
}
