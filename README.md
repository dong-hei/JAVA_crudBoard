# JAVA_crudBoard

구조 
★★config -----------------------------------------------------------------------------------------------------------------------------------------------------
☆WebSecurityConfig(SpringFramework)

DataSource Autowired

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() //보안체크 하지않는다. 테스트용이기때문에 보안체크 해제한것 실제로 이러면 보안에 위험하다.
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/","account/register","/css/**","/api/**").permitAll()
                        //이 경로는 승인이 필요없다
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/account/login")
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll());
//어떤 요청이던 인증된
// 폼 부터는 로그인 성공해야된다.
        return http.build();
    }
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery(
                        "select username,password,enabled "
                        + "from user "
                        + "where username = ? ")
//                끝에 띄어쓰기 해줘야 where절과 붙지않는다.
                .authoritiesByUsernameQuery("select u.username, r.name "
                        + "from user_role ur inner join user u on ur.user_id = u.id "
                        + "inner join role r on ur.role_id = r.id "
                        + "where u.username = ? ");
    } // Authetication 로그인 처리
    //authoritie 권한 처리

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } //안전하게 암호화 할수있는 스프링 코드
}

테스트용 임시 유저 만들기
@Bean
public UserDetailsService userDetailsService() {
UserDetails user =
User.withDefaultPasswordEncoder()
.username("user")
.password("password")
.roles("USER")
.build();
return new InMemoryUserDetailsManager(user);
}

★★Controller -----------------------------------------------------------------------------------------------------------------------------------------------------
☆AccountController
로그인,등록페이지 mapping

☆BoardApiController
보드 API 컨트롤러로써 RestAPI 통신을 위함 (Spring doc참고)

☆BoardController
BoardRepository,BoardService,BoardValidator Autowired

 @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 3) Pageable pageable,@RequestParam(required = false, defaultValue = "") String searchText){
        //@PageableDefault 페이지네이션 사이즈를 안줬을때 디폴트 값을 지정해준다
//        Page<Board> boards = boardRepository.findAll(PageRequest.of(0,20));
        //Page로 리턴 받고 PageRequest.of(1,20) 는 PageNation
//        Page<Board> boards = boardRepository.findAll(pageable);
        //PageNation을 동적으로 적용될수있게끔 도와주는 코드
        Page<Board> boards = boardRepository.findByTitleContainingOrContentContaining(searchText, searchText, pageable);
        int startPage = Math.max(1,boards.getPageable().getPageNumber() -4);
        int endPage = Math.max(boards.getTotalPages() ,boards.getPageable().getPageNumber() +4);
        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage", endPage);
        //PageNation start 맨 앞페이지 end 맨 뒤페이지 지정

        model.addAttribute("boards",boards);
        //jpa
        return "board/list";
    }

☆HomeController
홈 mapping

☆UserApiController

UserRepository,UserMapper Autowired 

UserApiController를 
User API 컨트롤러로써 RestAPI 통신을 위함 (Spring doc참고)
(User db를 사용할때 querydsl or querydslCustom,jdbc,mybatis를 설정할수있도록 코드가 짜여져있음)

★★model(vo) -----------------------------------------------------------------------------------------------------------------------------------------------------
☆Board
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

☆Role
User의 역할 vo

☆User
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

★★repository -----------------------------------------------------------------------------------------------------------------------------------------------------
☆BoardRepository,CustomizedUserRepository
jpa 사용을 위한 interface

☆CustomizedUserRepositoryImpl 
  @Override
    public List<User> findByUsernameCustom(String username) {
//        원하는 쿼리를 작성하기위해 qUser선언
        QUser qUser = QUser.user;
        JPAQuery<?> query = new JPAQuery<Void>(em);
        List<User> users = query.select(qUser)
                .from(qUser)
                .where(qUser.username.contains(username))
                .fetch();
        return users;
    }

☆UserRepository 
querydsl을 사용하기위한 interface

★★service  -----------------------------------------------------------------------------------------------------------------------------------------------------
☆BoardSvc
BoardRepository Autowired

save 메소드에 board에 필요한 정보를 집어넣는다.

☆UserSvc
UserRepository,PasswordEncoder Autowired

save 메소드에 user에 필요한 정보를 집어넣는다.

☆validator
validation에 통과하지못할때 출력되는 정보를 설정한다.
(Spring Doc 참조)


★★resources  -----------------------------------------------------------------------------------------------------------------------------------------------------

☆mapper
user 쿼리

☆static/css
css 파일

★template
☆account
로그인,등록페이지 html 파일

☆board
폼,목록페이지 html 파일

☆fragment
공통으로 보여지는 html 파일

☆index.html
메인 페이지 html 파일
