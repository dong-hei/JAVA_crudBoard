package board_dk.board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private Boolean enabled;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL , orphanRemoval = true)
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
//    (EAGER 기본값 OTO,MTO)조회할때 사용자 정보를 가져올꺼냐 (LAZY 기본값 OTM,MTM) 사용할때 조회할꺼냐
    private List<Board> boards = new ArrayList<>();
//    CascadeType.Remove 외래키가 있어도 게시글과 유저 모두 삭제할수있다.
// orphanRemoval = true 부모가 없는 데이터는 다 지운다.
//    @OneToOne
//    ex. user - user_detail
//
//    @OneToMany
//            (한개의사용자라 여러개의 게시글 작성가능)
//    ex. user - board
//
//    @ManyToOne
//            (여러개의 게시글은 하나의 사용자에 의해 작성)
//    ex. board - user
//
//    @ManyToMany
//            (하나의 사용자는 여러가지 권한을 가질수있다)
//    ex. user - role
}
