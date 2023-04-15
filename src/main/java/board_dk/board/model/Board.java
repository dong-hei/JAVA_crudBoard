package board_dk.board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //퍼포먼스는 시퀀스가 제일좋은데 관리하기 복잡하고 손이 많이가기때문에
    //일반적으론 Identity를 선언
    //Table은 퍼포먼스가 제일 안좋기떄문에 사용하지않는게 좋다.
    @NotNull
    @Size(min=2, max=30, message = "제목은 2자 이상 30자 이하입니다.")
    private String title;
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
//  referencedColumnName 사용자컬럼에 어떤 컬럼과 연결이 됐는지 (생략가능)
    private User user;
}

