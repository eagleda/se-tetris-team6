package tetris.network.protocol;

import java.io.Serializable;

// GameMessage의 payload로 사용될 데이터 객체 예시
// record는 Java 14+ 부터 사용 가능하며, 자동으로 직렬화 관련 메소드들을 구현해줍니다.
// Java 구버전의 경우 private final 필드와 getter, equals, hashCode, toString을 가진 클래스로 만들면 됩니다.
public record PlayerInput(InputType inputType) implements Serializable {
    private static final long serialVersionUID = 1L;
}
