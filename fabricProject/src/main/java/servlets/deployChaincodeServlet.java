package servlets;


import client.ChannelClient;
import client.FabricClient;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "deployChaincodeServlet", value = "/deployChaincode")
public class deployChaincodeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            String userConfigPath = request.getParameter("curUserConfPath");
            String channelName = request.getParameter("channelName");
            String peerName = request.getParameter("peerName");
            String mspid = request.getParameter("mspid");
            String chaincodePath = request.getParameter("chaincodePath");
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";

            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelName,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);

            //将peer name转化为真正的peer
            Collection<Peer> peers = channel.getPeers();
            //获取组织所有节点名
            Set<Peer> orgPeers = Util.getOrgPeers(userContext, mspid, networkConfigPath);
            Collection<String> orgPeerName = new HashSet<>();
            Iterator<Peer> it = orgPeers.iterator();
            while(it.hasNext()){
                orgPeerName.add(it.next().getName());
            }
            //从通道中获取选择的节点
            Collection<Peer> mypeers = new HashSet<>();
            if(peerName.equals("all")){//选择全部节点
                Iterator<Peer> all = peers.iterator();
                while(all.hasNext()){
                    Peer p = all.next();
                    if(orgPeerName.contains(p.getName())){
                        mypeers.add(p);
                    }
                }
            }else{
                Iterator<Peer> one = peers.iterator();
                while(one.hasNext()){
                    Peer p = one.next();
                    if(p.getName().equals(peerName)){
                        mypeers.add(p);
                    }
                }
            }
            Collection<ProposalResponse> result = channelClient.deployChainCode(mypeers,chaincodePath);
            //实例化链码
            channelClient.instantiateChainCode(mypeers, chaincodePath);
            JSONArray jsonArray = new JSONArray();
            for (ProposalResponse res : result){
                JSONObject jsonObject = new JSONObject();
                if (res.getStatus() == ProposalResponse.Status.SUCCESS) {
                    jsonObject.put("name",res.getPeer().getName());
                    jsonObject.put("url",res.getPeer().getUrl());
                    jsonObject.put("msg","SUCCESS");
                }else{
                    jsonObject.put("name",res.getPeer().getName());
                    jsonObject.put("url",res.getPeer().getUrl());
                    jsonObject.put("msg",res.getMessage());
                }
                jsonArray.add(jsonObject);
            }

                response.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = response.getWriter();
                out.println(jsonArray);
                out.flush();
                out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
