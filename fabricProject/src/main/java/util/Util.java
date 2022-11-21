package util;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import client.FabricClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import user.CAEnrollment;
import user.UserContext;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

public class Util {
    //通过用户身份配置文件，生成一个userContext
    public static UserContext createUserContext(String userConfigPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
        Properties userConfig = new Properties();
        InputStream userConfigStream = new BufferedInputStream(new FileInputStream(userConfigPath));
        userConfig.load(userConfigStream);

        //certPath：证书地址；pkPath：密钥地址
        String pkPath = userConfig.getProperty("userPrivkeyPath");
        String certPath = userConfig.getProperty("userCertPath");
        String userName = userConfig.getProperty("userName");
        String mspId = userConfig.getProperty("mspId");
        UserContext userContext = new UserContext();
        Enrollment enrollment = getEnrollment(pkPath, certPath);
        userContext.setEnrollment(enrollment);
        userContext.setMspId(mspId);
        userContext.setName(userName);

        return userContext;
    }

    public static void writeUserContext(UserContext userContext) throws Exception {
        String directoryPath = "users/" + userContext.getAffiliation();
        String filePath = directoryPath + "/" + userContext.getName() + ".ser";
        File directory = new File(directoryPath);
        if (!directory.exists())
            directory.mkdirs();

        FileOutputStream file = new FileOutputStream(filePath);
        ObjectOutputStream out = new ObjectOutputStream(file);

        //序列化
        out.writeObject(userContext);

        out.close();
        file.close();
    }

    public static UserContext readUserContext(String affiliation, String username) throws Exception {
        String filePath = "users/" + affiliation + "/" + username + ".ser";
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream fileStream = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileStream);

            UserContext uContext = (UserContext) in.readObject();

            in.close();
            fileStream.close();
            return uContext;
        }

        return null;
    }

    public static CAEnrollment getEnrollment(String keyFolderPath, String certFolderPath)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
        PrivateKey key = null;
        String certificate = null;
        InputStream isKey = null;
        BufferedReader brKey = null;

        try {

            isKey = new FileInputStream(keyFolderPath);
            brKey = new BufferedReader(new InputStreamReader(isKey));
            StringBuilder keyBuilder = new StringBuilder();

            for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
                if (line.indexOf("PRIVATE") == -1) {
                    keyBuilder.append(line);
                }
            }

            certificate = new String(Files.readAllBytes(Paths.get(certFolderPath)));

            byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("EC");
            key = kf.generatePrivate(keySpec);
        } finally {
            isKey.close();
            brKey.close();
        }

        CAEnrollment enrollment = new CAEnrollment(key, certificate);
        return enrollment;
    }


    public static void cleanUp() {
        String directoryPath = "users";
        File directory = new File(directoryPath);
        deleteDirectory(directory);
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        // either file or an empty directory
        Logger.getLogger(Util.class.getName()).log(Level.INFO, "Deleting - " + dir.getName());
        return dir.delete();
    }

    //读取json文件
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //写入json文件
    public static boolean writeFile(String filePath, String sets) {
        FileWriter fw;
        try {
            fw = new FileWriter(filePath);
            PrintWriter out = new PrintWriter(fw);
            out.write(sets);
            out.println();
            fw.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //获取已创建的channel
    public static String[] getOrgChannels() throws InvalidArgumentException, IOException, ClassNotFoundException {
        String blockPath = "/Users/zhaoyuting/IdeaProjects/fabricProject/src/main/resources/channel_resources";
        File file = new File(blockPath);
        if (file.isDirectory()){
            //获取所有channel名
            File[] files = file.listFiles();
            String[] channelsName = new String[files.length+1];
            for (int i = 0; i < files.length; i++){
                if(files[i].getName().indexOf("block")!=-1){
                    String channelName = files[i].getName();
                    channelsName[i] = channelName.substring(0,channelName.length()-6);
                }
            }
            return channelsName;
        }else {
            return null;
        }
    }



    //传入peer名称创建peer
    public static Peer createPeer(UserContext userContext,String org, String peer) throws InvalidArgumentException, CryptoException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        FabricClient fc = new FabricClient(userContext);
        String s = Util.readJsonFile("src/main/resources/network_resources/networkPeer.json");
        JSONObject jobj = JSON.parseObject(s);
        JSONArray orgjson = jobj.getJSONArray(org);
        for(int i=0;i<orgjson.size();i++){
            JSONObject peerjson = (JSONObject)orgjson.get(i);
            String peername = (String) peerjson.get("name");
            String peerurl = (String) peerjson.get("url");
            if(peername.equals(peer)){
                return fc.getInstance().newPeer(peername,peerurl);
            }
        }
        return null;
    }

    //将新建的组织节点信息写入json文件（管理员手动配置）


    //通过组织名称获得该组织所有节点集合
    public static Set<Peer> getOrgPeers(UserContext userContext, String orgName, String networkConfigPath) throws InvalidArgumentException, CryptoException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Set<Peer> peerSet = new HashSet<>();
        FabricClient fabclient = new FabricClient(userContext);
        //从网络配置文件connection.json中获取组织节点信息
        String str = Util.readJsonFile(networkConfigPath);
        JSONObject jsonObject = JSON.parseObject(str);
        JSONObject organizations = jsonObject.getJSONObject("organizations");
        JSONObject peers = jsonObject.getJSONObject("peers");
        //获取组织节点名称
        JSONArray orgPeers = organizations.getJSONObject(orgName).getJSONArray("peers");
        for(int i=0;i < orgPeers.size();i++){
            String peerName = (String) orgPeers.get(i);
            String peerUrl = (String)peers.getJSONObject(peerName).get("url");
            Peer p = fabclient.getInstance().newPeer(peerName, peerUrl);
            peerSet.add(p);
        }

        return peerSet;
    }

    public static X509Certificate readX509Certificate(final Path certificatePath) throws IOException, CertificateException {
        try (Reader certificateReader = Files.newBufferedReader(certificatePath, StandardCharsets.UTF_8)) {
            return Identities.readX509Certificate(certificateReader);
        }
    }

    public static PrivateKey getPrivateKey(final Path privateKeyPath) throws IOException, InvalidKeyException, IOException {
        try (Reader privateKeyReader = Files.newBufferedReader(privateKeyPath, StandardCharsets.UTF_8)) {
            return Identities.readPrivateKey(privateKeyReader);
        }
    }

    /**
     * 将字节码转换为json
     */
    public static JSONObject getDecodeData(byte[] data) throws ClientProtocolException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://127.0.0.1:7059/protolator/decode/common.Config");
        httppost.setEntity(new ByteArrayEntity(data));

        HttpResponse response = httpclient.execute(httppost);
        return JSONObject.parseObject(EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")));
    }

    /**
     * 将json转换为字节码
     */
    public static byte[] getEncodeData(String jsonData) throws ClientProtocolException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://127.0.0.1:7059/protolator/encode/common.Config");
        httppost.setEntity(new StringEntity(jsonData));

        HttpResponse response = httpclient.execute(httppost);
        return EntityUtils.toByteArray(response.getEntity());
    }

    /**
     * 从文件读取数据
     */
    public static String getDataFromFile(String filePath) {
        // 创建一个文件对象
        File file = new File(filePath);

        // 使用字节流对象读入内存
        try {
            InputStream fileIn = new FileInputStream(file);
            //DataInputStream in = new DataInputStream(fileIn);

            // 使用缓存区读入对象
            BufferedInputStream in = new BufferedInputStream(fileIn);

            // 读取数据
            String content = "";
            byte[] data = new byte[10240];
            int flag = 0;
            while((flag = in.read(data)) != -1) {
                content += new String(data, 0, flag);
            }

            return content;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算差异获得修改配置字节码
     */
    public static byte[] updateFromConfigs(byte[] original, byte[] updated, String channelName) throws ClientProtocolException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://127.0.0.1:7059/configtxlator/compute/update-from-configs");
        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("original", original, ContentType.APPLICATION_OCTET_STREAM, "original.proto")
                .addBinaryBody("updated", updated, ContentType.APPLICATION_OCTET_STREAM, "updated.proto")
                .addBinaryBody("channel", channelName.getBytes()).build();

        httppost.setEntity(multipartEntity);
        HttpResponse response = httpclient.execute(httppost);
        return EntityUtils.toByteArray(response.getEntity());
    }

}
