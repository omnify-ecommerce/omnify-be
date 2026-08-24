package com.omnify.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//class nay luu tru cac cau hinh lien quan den thong tin nguoi gui email cua ung dung
//duoc bind tu bong qua pp.mail ben application.yml
@Component
@ConfigurationProperties(prefix = "omnify.mail")
@Getter
@Setter
public class MailProperties {
    //dia chi email duoc dung lam nguoi gui
    private String fromAddress;
    //ten hien thi cua nguoi gui
    private String fromName;


}