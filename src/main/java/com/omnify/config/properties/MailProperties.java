package com.omnify.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

//class nay luu tru cac cau hinh lien quan den thong tin nguoi gui email cua ung dung
//duoc bind tu bong qua pp.mail ben application.yml
@ConfigurationProperties(prefix = "omnify.mail")
public record MailProperties(
    //dia chi email duoc dung lam nguoi gui
    String fromAddress,
    //ten hien thi cua nguoi gui
    String fromName
) {
}
