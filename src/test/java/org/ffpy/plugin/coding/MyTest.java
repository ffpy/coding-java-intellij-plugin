package org.ffpy.plugin.coding;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

public class MyTest {

    @Test
    public void test() throws Exception {
        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Interchange_ContainerMsg>\n" +
                "    <InterchangeHeader>\n" +
                "        <MessageType>JZXJK</MessageType>\n" +
                "        <!--消息类型 JZXJK：集装箱接口 登记反馈-->\n" +
                "        <VersionNum>1.0</VersionNum>\n" +
                "        <!--版本号-->\n" +
                "        <InterchangeSender>YPI</InterchangeSender>\n" +
                "        <InterchangeRecipient>HSL</InterchangeRecipient>\n" +
                "        <!--接收方代码-->\n" +
                "        <PreparationDateTime>2017-09-29 00:15:00</PreparationDateTime>\n" +
                "        <!--报文时间-->\n" +
                "        <InterchangeControlRef>00000000000001</InterchangeControlRef>\n" +
                "        <!--报文参考号-->\n" +
                "        <AccessPassword>DJVt9m0qr8D9Urw6ofNCwGW6s959wHub</AccessPassword>\n" +
                "        <!--令牌号-->\n" +
                "    </InterchangeHeader>\n" +
                "    <MessageList>\n" +
                "        <Message>\n" +
                "            <!--集装箱登记可以有多条-->\n" +
                "            <MessageHeader>\n" +
                "                <FunctionCode>A</FunctionCode>\n" +
                "                <!--功能码 A:新增，C：取消-->\n" +
                "            </MessageHeader>\n" +
                "            <ContainerDetail>\n" +
                "                <ContainerNo>EMCU9893895</ContainerNo>\n" +
                "                <!--集装箱号，必须输入-->\n" +
                "                <BillShippingNum></BillShippingNum>\n" +
                "                <!--装提单号，可选输入-->\n" +
                "                <BookingNo></BookingNo>\n" +
                "                <!--订舱号，可选输入-->\n" +
                "                <CntrStatus>XF</CntrStatus>\n" +
                "                <!--集装箱状态 必须输入 XF：出口重柜，IF：进口重柜，EM-空箱-->\n" +
                "                <CustomerRefNo></CustomerRefNo>\n" +
                "                <!--客户业务参考号-->\n" +
                "            </ContainerDetail>\n" +
                "            <StorageStatus></StorageStatus>\n" +
                "            <!--登记或取消结果 S：成功，F：失败，E：录入时是已存在，取消时是不存在 ,N:数据不完整，必填项没填-->\n" +
                "        </Message>\n" +
                "        <ReturnCode>0</ReturnCode>\n" +
                "        <!--报文处理结果代码 Return Code 0：成功1：失败2：访问口令错误3：令牌超期 4：跟踪查询超期 5: 报文内容为空或者有必填项为空 6：功能代码错误-->\n" +
                "        <ReturnMessage>成功</ReturnMessage>\n" +
                "        <!--报文处理结果描述-->\n" +
                "    </MessageList>\n" +
                "</Interchange_ContainerMsg>\n";
        Document doc = DocumentHelper.parseText(text);
        show(doc.getRootElement());
    }

    private void show(Element el) {
        el.elements().forEach(this::show);
    }
}
