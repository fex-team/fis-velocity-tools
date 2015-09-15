package com.baidu.fis.web.controller;

import com.baidu.fis.util.MapCache;
import com.baidu.fis.util.RemoteMapCache;
import com.baidu.fis.velocity.bean.FeServerBean;
import com.baidu.fis.velocity.bean.UserFeServerMap;
import com.baidu.fis.velocity.dest.RouterMapManager;
import com.baidu.fis.web.util.ResponseData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yongqingdong on 2015/8/1.
 */
@Controller
@RequestMapping("/fismanage")
public class FisManageController {

    /**
     * 获取当前WEB的配置
     * @return
     */
    @RequestMapping("/config/data")
    @ResponseBody
    public ResponseData configDataGet(){

        RouterMapManager rmm = RouterMapManager.getInstance();
        Map<String, Object> dataMap = new HashMap<String, Object>();

        dataMap.put("feServerList", rmm.getFeServerList());

        return new ResponseData(ResponseData.Status.SUCCESS, dataMap);
    };

    @RequestMapping("/config/update")
    @ResponseBody
    public ResponseData configDataUpdate(UserFeServerMap baseBean){

        System.out.println("FIS config update: " + baseBean);

        RouterMapManager rmm = RouterMapManager.getInstance();
        Map<String, Object> dataMap = new HashMap<String, Object>();

        if(rmm.isEnabled()){
            // 开启FE Server管理

            // 1. Config Bean
            rmm.setEnabled(baseBean.getEnabled());

            List<FeServerBean> feServerBeanList = new ArrayList<FeServerBean>();
            for(FeServerBean feServerBean : baseBean.getFeServerList()){
                // 除去MatchStandard为空，且UserMatchList为0的剩余情况，加入到List
                boolean isMatchStandardEmpty = feServerBean.getMatchStandard() == null || "".equals(feServerBean.getMatchStandard().trim());
                boolean isUserMatchListEmpty = feServerBean.getUserMatchList() == null || feServerBean.getUserMatchList().size() == 0;
                if(!isMatchStandardEmpty || !isUserMatchListEmpty){
                    feServerBeanList.add(feServerBean);
                }
            }

            rmm.setFeServerList(feServerBeanList);
            rmm.convertFeServerToUserMap();

            // 2. Connect
            try{
                for(FeServerBean feServer : rmm.getFeServerList()){
                    MapCache inst = MapCache.getInstance();
                    String feServerId = feServer.getServerId();

                    if(inst instanceof RemoteMapCache){
                        RemoteMapCache remoteInst = (RemoteMapCache)inst;
                        remoteInst.initFeServerMap(feServerId);
                    }
                }
                // Success
                return new ResponseData(ResponseData.Status.SUCCESS, null);

            }catch (Exception e){
                e.printStackTrace();
                String msg = String.format("FIS config failed[%s]: %s\n", e.getClass().getName(), e.getMessage());
                System.err.format(msg);

                if(e instanceof FileNotFoundException){
                    msg = String.format("FIS 404 for URL:[%s]\n", e.getMessage());
                    System.err.format(msg);
                }

                // Failed
                return new ResponseData(ResponseData.Status.ERROR, msg);
            }
        }else{
            return new ResponseData(ResponseData.Status.ERROR, "WEB服务器Dest功能已关闭，无法开启");
        }
    };

}
