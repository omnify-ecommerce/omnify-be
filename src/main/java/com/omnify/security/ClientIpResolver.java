package com.omnify.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

//Class chiu trach nhiem lay dia chi IP cua nguoi dung
@Component
public class ClientIpResolver {
    public String resolve(HttpServletRequest request){
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()){
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
