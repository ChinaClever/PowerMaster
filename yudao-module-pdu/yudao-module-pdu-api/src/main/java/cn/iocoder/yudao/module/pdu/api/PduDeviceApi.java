package cn.iocoder.yudao.module.pdu.api;

import java.util.List;
import java.util.Map;

public interface PduDeviceApi {

    Map<String, String> getPositionByKeys(List<String> pduKeys);
}
