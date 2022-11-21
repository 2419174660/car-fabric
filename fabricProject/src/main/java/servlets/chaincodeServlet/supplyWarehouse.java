package servlets.chaincodeServlet;

import client.ChannelClient;
import client.FabricClient;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Properties;

@WebServlet(name = "supplyWarehouse", value = "/supplyWarehouse")
public class supplyWarehouse extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();

            String networkConfigPath ="/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            String userConfigPath = request.getParameter("userConfigPath");

            String PurchaseCode = request.getParameter("PurchaseCode");
            String MaterialCode = request.getParameter("MaterialCode");
            String WarehouseReservoir = request.getParameter("WarehouseReservoir");
            String WarehousePosition = request.getParameter("WarehousePosition");
            String Datetime = request.getParameter("Datetime");

            //新加参数：选中的通道
            String SelectedChannel = request.getParameter("SelectedChannel");
            //String channelName = "supplychannel";
            String channelName = SelectedChannel;
            //从链码配置文件读取链码信息
            Properties chaincodeConfig = new Properties();
            String chaincodeConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/chaincode_properties/"+channelName+".chaincode.properties";
            InputStream chaincodeConfigStream = new BufferedInputStream(new FileInputStream(chaincodeConfigPath));
            chaincodeConfig.load(chaincodeConfigStream);
            String chaincodeName = chaincodeConfig.getProperty("chaincodeName");
            System.out.println(chaincodeName);
            //String chaincodeName = "supplymain";
            if(chaincodeName==null){
                System.out.println("找不到链码配置文件");
            }

            //调用的函数名称
            String funcName = "warehouse";
            //调用合约的参数
            String[] arguments = {PurchaseCode,MaterialCode,WarehouseReservoir,WarehousePosition,Datetime};

            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelName,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);

            Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(chaincodeName, funcName, arguments);

            JSONArray jsonArray = new JSONArray();
            for (ProposalResponse res : responses){
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
