package servlets;

import client.FabricClient;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "getChannelPeerServlet", value = "/getChannelPeer")
public class getChannelPeerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    /*
    获取某一通道内，属于某组织的节点信息
    * */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            String channelName = request.getParameter("channelName");
            String mspid = request.getParameter("mspid");
            String curUserConfPath = request.getParameter("curUserConfPath");
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";

            UserContext userContext = Util.createUserContext(curUserConfPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.getChannelFromConfig(channelName);
            JSONArray cahnnelPeers = new JSONArray();
            //获取通道内节点
            Collection<Peer> peers = channel.getPeers();
            //获取组织内节点
            Set<Peer> orgPeers = Util.getOrgPeers(userContext, mspid, networkConfigPath);
            Iterator<Peer> it = orgPeers.iterator();
            while(it.hasNext()){
                Peer p = it.next();
                if(peers.contains(p)){
                    cahnnelPeers.add(p.getName());
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println(cahnnelPeers);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
