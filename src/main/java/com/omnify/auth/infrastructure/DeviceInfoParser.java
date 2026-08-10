package com.omnify.auth.infrastructure;

import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;


@Component
public class DeviceInfoParser {

    private final Parser uaParser = new Parser();

    public String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }
        try {
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
        if (client.userAgent == null
                || client.userAgent.family == null
                || "Other".equals(client.userAgent.family)) {
            return null;
        }
        return client.userAgent.family;
    }

    private String extractOs(Client client) {
        if (client.os == null || client.os.family == null || "Other".equals(client.os.family)) {
            return null;
        }
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