package servlets.chaincodeServlet;

import client.ChannelClient;
import client.FabricClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/******暂时未实现相应界面*****/

@WebServlet(name = "marketingSale",value = "/marketingSale")
public class marketingSale extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();

            response.setContentType("text/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            String VinCode = request.getParameter("VinCode");
            String SaleCode = request.getParameter("SaleCode");
            String ClientCode = request.getParameter("ClientCode");
            String DealerCode = request.getParameter("DealerCode");
            String Price = request.getParameter("Price");
            String OwnerName = request.getParameter("OwnerName");
            String OwnerPhone = request.getParameter("OwnerPhone");
            String Datetime = request.getParameter("Datetime");

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);


            //属于marketingchannel的组织用户才能调用成功
            String userConfigPath = request.getParameter("userConfigPath");
            String networkConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/network_connection/connection.json";
            String channelConfigPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/network_resources/channel_properties/marketingchannel.properties";
            String chaincodeName = "marketingmain";
            //调用的函数名称
            String funcName = "sale";
            //调用合约的参数
            String[] arguments = {VinCode,SaleCode,ClientCode,DealerCode,Price,OwnerName,OwnerPhone,Datetime};

            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.connectToNetwork(networkConfigPath,channelConfigPath,userConfigPath);
            ChannelClient channelClient = fabclient.createChannelClient(channel);

            Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(chaincodeName, funcName, arguments);

            Map<String,String> map = new HashMap<>();
            for(ProposalResponse res:responses){
                map.put(res.getPeer().getName(),res.getStatus().toString());
            }

            out.println(map);
            out.flush();
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
