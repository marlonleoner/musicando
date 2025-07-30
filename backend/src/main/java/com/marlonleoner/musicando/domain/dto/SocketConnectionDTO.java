package com.marlonleoner.musicando.domain.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocketConnectionDTO {
    private String nickname;
    private String code;
    private String secret;

    public static SocketConnectionDTO fromNativeHeaders(Map<String, List<String>> headers) {
        SocketConnectionDTO dto = new SocketConnectionDTO();
        dto.setNickname(headers.get("nickname").toString());
        dto.setCode(headers.get("code").toString());
        dto.setSecret(headers.get("secret").toString());

        return dto;
    }
}
