package test;

import client.ChannelClient;
import client.FabricClient;
import user.UserContext;
import util.Util;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import org.hyperledger.fabric.sdk.*;

import java.util.Collection;

public class invokeChaincode {
    public static void main(String[] args) {
        try {
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();
            //网络配置，用于连接当前网络并获取通道实例
            String networkConfigPath = "src/main/resources/network_resources/network_connection/connection.json";
            /*************************************************supplychannel***************************************************/
            //通道配置
            String channelConfigPath = "src/main/resources/network_resources/channel_properties/supplychannel.properties";
            //链码名称
            String chaincodeName = "supplymain";
            //用户配置
            String userConfigPath = "src/main/resources/network_resources/userid_properties/org1.user1.properties";
            /*************************************************marketingchannel************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            //String chaincodeName = "marketingmain";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org4.user1.properties";

            /*************************************************servicechannel**************************************************/
            //String channelConfigPath = "src/main/resources/network_resources/channel_properties/servicechannel.properties";
            //String chaincodeName = "servicemain";
            //String userConfigPath = "src/main/resources/network_resources/userid_properties/org5.user1.properties";


            //调用的函数名称
            String funcName = "initLedger";
            //调用合约的参数
            //String[] arguments = {"450011111706", "401111116CDB","测试材料002", "Org1", "300", "330", "2021-11-15"};
            String[] arguments = {};
            //String[] arguments = {"2", "2", "3", "4", "5"};

            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelConfigPath,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);

            Collection<ProposalResponse> response = channelClient.sendTransactionProposal(chaincodeName, funcName ,arguments);
            for(ProposalResponse res:response){
                System.out.println("Peer Name: "+res.getPeer().getName());
                System.out.println("invoke status: "+res.getStatus());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
