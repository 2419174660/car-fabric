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

@WebServlet(name = "getOrgPeerServlet", value = "/getOrgPeer")
public class getOrgPeerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            //通过mspid获取本组织节点信息
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            String mspid = request.getParameter("mspid");
            String userConfigPath = request.getParameter("userConfigPath");
            UserContext userContext = Util.createUserContext(userConfigPath);
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";

            //从connection.json中获取本组织节点信息
            Set<Peer> orgPeers = Util.getOrgPeers(userContext, mspid, networkConfigPath);
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(orgPeers));
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
