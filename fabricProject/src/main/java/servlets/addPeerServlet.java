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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "addPeerServlet", value = "/addPeer")
public class addPeerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            //获取数据
            String userConfigPath = request.getParameter("userpath");
            String channelName = request.getParameter("channelName");
            String peerName = request.getParameter("peerName");
            String mspid = request.getParameter("mspid");
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            //通道名转化为通道配置文件路径

            //获取通道
            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            //Channel channel = fabclient.connectToNetwork(networkConfigPath,channelName,userConfigPath);
            Channel channel = fabclient.getChannelFromConfig(channelName);
            //将peer name转化为真正的peer
            Set<Peer> peers = Util.getOrgPeers(userContext, mspid, networkConfigPath);
            Peer selectPeer = null;

            if(!peerName.equals("all")){//没有选择全部节点
                Iterator it = peers.iterator();
                while(it.hasNext()){
                    Peer p = (Peer)it.next();
                    if(p.getName().equals(peerName)){
                        selectPeer = p;
                    }
                }
                channel.joinPeer(selectPeer);
            }else {//选择加入全部节点
                Iterator it = peers.iterator();
                while(it.hasNext()){
                    channel.joinPeer((Peer)it.next());
                }
            }
            fabclient.saveChannelAsConfig(channel);

            //转化为array展示到页面（查看通道内节点）
            Collection<Peer> channelpeers = channel.getPeers();
            JSONArray jsonArray = new JSONArray();
            Iterator it = channelpeers.iterator();
            while(it.hasNext()){
                Peer p = (Peer)it.next();
                jsonArray.add(p.getName());
            }

            //返回通道中节点名称
            out.println(jsonArray);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            //返回通道中节点名称
            PrintWriter out = response.getWriter();
            out.flush();
            out.close();
        }
    }
}
