package network;
import org.hyperledger.fabric.sdk.Orderer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrdererOrg {
    final String name = "OrdererOrg";
    Set<Orderer> orderers = new HashSet<>();
    Map<String, Orderer> ordererMap = new HashMap<>();
    Set<Orderer> freeOrderers = new HashSet<>();//当前可用的orderer（一个orderer当前时刻不能同时加入多个channel）

    public OrdererOrg(){}

    public void addOrderer(Orderer orderer){
        orderers.add(orderer);
        ordererMap.put(orderer.getName(),orderer);
        freeOrderers.add(orderer);
    }

    public Orderer getAFreeOrderer(){//获取一个当前空闲的orderer
        Iterator it = freeOrderers.iterator();
        if(it.hasNext()){
            Orderer orderer = (Orderer)it.next();
            freeOrderers.remove(orderer);
            return orderer;
        }else{
            Logger.getLogger(OrdererOrg.class.getName()).log(Level.INFO,
                    "none orderer can use");
            return null;
        }
    }


    public Orderer getOrderer(String name){
        return ordererMap.get(name);
    }

    public Set<Orderer> getOrderers(){
        return orderers;
    }
}
