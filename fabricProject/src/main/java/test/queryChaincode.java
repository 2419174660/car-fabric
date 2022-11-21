package test;

import client.ChannelClient;
import client.FabricClient;
import user.UserContext;
import util.Util;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import net.sf.json.JSONArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class queryChaincode {
    public static void main(String[] args) {
        try {
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            //网络配置，用于连接当前网络并获取通道实例
            //String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            /*************************************************supplychannel***************************************************/
            //通道配置
            //String channelConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //链码名称
            //String chaincodeName = "supplymain";
            //String userConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/userid_properties/org1.user1.properties";
            /*************************************************marketingchannel************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            //String chaincodeName = "marketingmain";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org4.user1.properties";
            /*************************************************servicechannel**************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";
            //String chaincodeName = "servicemain";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org5.user1.properties";

            String carID = "CAR0";

            //属于supplychannel的组织用户才能调用成功
            String userConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/userid_properties/org1.user1.properties";
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            String channelConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/channel_properties/supplychannel.properties";
            String channelName = "supplychannel";
            String chaincodeName = "supplymain";
            //调用的函数名称
            String funcName = "queryCar";
            //调用合约的参数
            String[] arguments = {carID};


            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelName,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);

            Collection<ProposalResponse> responses = channelClient.queryByChainCode(chaincodeName, funcName, arguments);
            //Map<Peer, JSONArray> result = fabclient.getResponsePayload(responses);
            Map<Peer,JSONArray> result = new HashMap<>();
            for (ProposalResponse res : responses) {
                String str = new String(res.getChaincodeActionResponsePayload());
                JSONArray jsonArray = JSONArray.fromObject("["+str+"]");
                result.put(res.getPeer(),jsonArray);
                System.out.println("Peer name: "+res.getPeer().getName());
                System.out.println("get return: "+jsonArray);
            }
            //获取第一个值
            Iterator it = result.values().iterator();
            System.out.println(it.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
