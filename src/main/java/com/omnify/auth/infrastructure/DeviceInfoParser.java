package com.omnify.auth.infrastructure;

import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

//Class nay co nhiem vu doc chuoi user-agent cua HTTP request doi chuyen no thanh ten thiet bi de doc VD
//Mozilla/5.0 ... Chrome/... Windows NT... thanh Chome on Window10
@Component
public class DeviceInfoParser {

    private final Parser uaParser = new Parser();

    public String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }
        try {
            //trich xuat thong tin cua client vab browser tu userAgent tu 2 ham extractbrowser va os viet o duoi
            Client client = uaParser.parse(userAgent);
            String browser = extractBrowser(client);
            String os = extractOs(client);

            if (browser == null && os == null) {
                return "Unknown device";
            }
            if (os == null) {
                return browser;
            }
            if (browser == null) {
                return os;
            }
            return browser + " on " + os;
        } catch (Exception e) {
            return "Unknown device";
        }
    }

    private String extractBrowser(Client client) {
        //neu khong xac dinh duoc browser hay client tra ve other thi tra ve null
        if (client.userAgent == null
            || client.userAgent.family == null
            || "Other".equals(client.userAgent.family)) {
            return null;
        }
        return client.userAgent.family;
    }

    private String extractOs(Client client) {
        //neu khong xac dinh duoc os hay client tra ve other thi tra ve null
        if (client.os == null || client.os.family == null || "Other".equals(client.os.family)) {
            return null;
        }
        //neu duoc thi noi them version cua os vao
        StringBuilder sb = new StringBuilder(client.os.family);
        if (client.os.major != null) {
            sb.append(' ').append(client.os.major);
            if (client.os.minor != null && !"0".equals(client.os.minor)) {
                sb.append('.').append(client.os.minor);
            }
        }
        return sb.toString();
    }
}