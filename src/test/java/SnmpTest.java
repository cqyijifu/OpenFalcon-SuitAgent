/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-12 14:14 创建
 */

import com.yiji.falcon.agent.plugins.util.SNMPHelper;
import com.yiji.falcon.agent.vo.snmp.IfStatVO;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author guqiu@yiji.com
 */
public class SnmpTest {

    private Snmp snmp;
    private UserTarget target;

    {
        try {
            snmp = new Snmp(new DefaultUdpTransportMapping());
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            snmp.listen();

            String accessName = "yijifu";
            String accessPassword = "cqyijifu";
            String accessAddress = "192.168.56.254";
            String transport = "udp";
            String port = "161";

            UsmUser user = new UsmUser(
                    new OctetString(accessName),
                    AuthMD5.ID, new OctetString(accessPassword),
                    PrivDES.ID, new OctetString(accessPassword));

            snmp.getUSM().addUser(new OctetString(accessName), user);

            target = new UserTarget();
            target.setSecurityName(new OctetString(accessName));
            target.setVersion(SnmpConstants.version3);
            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            target.setAddress(GenericAddress.parse(transport + ":" + accessAddress + "/" + port));
            target.setTimeout(1500);
            target.setRetries(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void test() throws IOException {
//        List<PDU> pduList = SNMPHelper.snmpWalk(snmp, target, ".1.3.6.1.2.1.31.1.1.1.1.0.1");
//        for (PDU pdu : pduList) {
//            System.out.println(pdu);
//        }

//        System.out.println(SNMPHelper.snmpGet(snmp, target, ".1.3.6.1.2.1.31.1.1.1.1.1"));

//        System.out.println("设备名称:" + SNMPHelper.snmpGet(snmp, target, "1.3.6.1.2.1.1.5.0"));

//        System.out.println("系统描述 : " + SNMPHelper.snmpGet(snmp, target, "1.3.6.1.2.1.1.1.0"));

        PDU pdu = SNMPHelper.snmpGet(snmp, target, "1.3.6.1.2.1.1.1.0.1.1.1");
        System.out.println(pdu);

    }

    @org.junit.Test
    public void h3cCPU() throws IOException {
        List<PDU> pduList = SNMPHelper.snmpWalk(snmp, target, "1.3.6.1.4.1.25506.2.6.1.1.1.1.6");
    }

    @org.junit.Test
    public void ifStatPrint() throws IOException {
        String ifNameOid = "1.3.6.1.2.1.31.1.1.1.1";
        String ifHCInOid = "1.3.6.1.2.1.31.1.1.1.6";
        String ifHCOutOid = "1.3.6.1.2.1.31.1.1.1.10";
        String ifHCInPktsOid = "1.3.6.1.2.1.31.1.1.1.7";
        String ifHCOutPktsOid = "1.3.6.1.2.1.31.1.1.1.11";
        String ifOperStatusOid = "1.3.6.1.2.1.2.2.1.8";
        String ifHCInBroadcastPktsOid = "1.3.6.1.2.1.31.1.1.1.9";
        String ifHCOutBroadcastPktsOid = "1.3.6.1.2.1.31.1.1.1.13";
        String ifHCInMulticastPktsOid = "1.3.6.1.2.1.31.1.1.1.8";
        String ifHCOutMulticastPktsOid = "1.3.6.1.2.1.31.1.1.1.12";

        // 忽略的接口，如Nu匹配ifName为*Nu*的接口
        List<String> ignoreIfName = Arrays.asList("Nu", "NU", "Vlan", "Vl", "LoopBack");

        List<PDU> ifNameList = SNMPHelper.snmpWalk(snmp, target, ifNameOid);
        List<PDU> ifInList = SNMPHelper.snmpWalk(snmp,target,ifHCInOid);
        List<PDU> ifOutList = SNMPHelper.snmpWalk(snmp,target,ifHCOutOid);
        List<PDU> ifHCInPktsList = SNMPHelper.snmpWalk(snmp,target,ifHCInPktsOid);
        List<PDU> ifHCOutPktsList = SNMPHelper.snmpWalk(snmp,target,ifHCOutPktsOid);
        List<PDU> ifOperStatusList = SNMPHelper.snmpWalk(snmp,target,ifOperStatusOid);
        List<PDU> ifHCInBroadcastPktsList = SNMPHelper.snmpWalk(snmp,target,ifHCInBroadcastPktsOid);
        List<PDU> ifHCOutBroadcastPktsList = SNMPHelper.snmpWalk(snmp,target,ifHCOutBroadcastPktsOid);
        List<PDU> ifHCInMulticastPktsList = SNMPHelper.snmpWalk(snmp,target,ifHCInMulticastPktsOid);
        List<PDU> ifHCOutMulticastPktsList = SNMPHelper.snmpWalk(snmp,target,ifHCOutMulticastPktsOid);

        List<IfStatVO> statVOs = new ArrayList<>();

        for (PDU pdu : ifNameList) {
            VariableBinding ifName = pdu.get(0);
            boolean check = true;
            for (String ignore : ignoreIfName) {
                if(ifName.getVariable().toString().contains(ignore)){
                    check = false;
                    break;
                }
            }
            if (check) {
                int index = Integer.parseInt(ifName.getOid().toString().replace(ifNameOid, "").replace(".",""));
                IfStatVO statVO = new IfStatVO();
                statVO.setIfName(ifName.getVariable().toString());
                statVO.setIfIndex(index);
                statVO.setIfHCInBroadcastPkts(ifHCInBroadcastPktsList.get(index).get(0).getVariable().toString());
                statVO.setIfHCInMulticastPkts(ifHCInMulticastPktsList.get(index).get(0).getVariable().toString());
                statVO.setIfHCInOctets(ifInList.get(index).get(0).getVariable().toString());
                statVO.setIfHCOutOctets(ifOutList.get(index).get(0).getVariable().toString());
                statVO.setIfHCInUcastPkts(ifHCInPktsList.get(index).get(0).getVariable().toString());
                statVO.setIfHCOutUcastPkts(ifHCOutPktsList.get(index).get(0).getVariable().toString());
                statVO.setIfOperStatus(ifOperStatusList.get(index).get(0).getVariable().toString());
                statVO.setIfHCOutBroadcastPkts(ifHCOutBroadcastPktsList.get(index).get(0).getVariable().toString());
                statVO.setIfHCOutMulticastPkts(ifHCOutMulticastPktsList.get(index).get(0).getVariable().toString());
                statVO.setTime(new Date());

                statVOs.add(statVO);
            }
        }

        for (IfStatVO statVO : statVOs) {
            System.out.println(statVO);
        }

    }
}
