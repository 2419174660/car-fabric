package servlets;

import client.FabricClient;
import config.Config;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import user.UserContext;
import util.Util;

import java.io.PrintWriter;

@WebServlet(name = "createChannelServlet", value = "/createChannel")
public class createChannelServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try{
            CryptoSuite.Factory.getCryptoSuite();
            Util.cleanUp();

            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            //获取数据
            String userConfigPath = request.getParameter("userpath");
            String channelConfigPath = request.getParameter("channelpath");
            //创建通道
            UserContext userContext = Util.createUserContext(userConfigPath);
            FabricClient fabclient = new FabricClient(userContext);
            Channel channel = fabclient.constructChannel(channelConfigPath);
            if(channel == null){//channel为空
                System.out.println("对不起，通道已存在或当前用户没有操作权限");
            }else{
                //将管理员组织节点加入通道中
                channel.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_0,Config.ADMIN_PEER_0_URL));
                channel.joinPeer(fabclient.getInstance().newPeer(Config.ADMIN_PEER_1,Config.ADMIN_PEER_1_URL));
                fabclient.saveChannelAsConfig(channel);
                //返回通道相关信息
                out.println(channel.toString());
                System.out.println(channel.toString());
            }
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
