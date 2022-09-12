package org.apache.cordova.niceid;

import com.google.gson.annotations.SerializedName;

public class QRCodeData {

    //사원번호(리셉션)
    @SerializedName("recp_reg_sid") public String loginId;
    //수진번호
    @SerializedName("hmeNo") public String patientNumber;
    //호출여부
    @SerializedName("recpCallYn") public String callYn;

}
