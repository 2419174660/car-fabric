package servlets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "getAllChannelServlet", value = "/getChannel")
public class getAllChannelServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            //通过mspid获取本组织节点信息
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            String[] channels = Util.getOrgChannels();
            Set<String> set = new HashSet<>();
            for (String ch : channels) {
                if(ch!=null){
                    set.add(ch);
                }
            }
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(set));
            System.out.println(jsonArray);
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            //返回该组织所有的节点数组
            out.println(jsonArray);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
