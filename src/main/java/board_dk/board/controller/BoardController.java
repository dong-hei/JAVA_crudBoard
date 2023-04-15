package board_dk.board.controller;

import board_dk.board.model.Board;
import board_dk.board.repository.BoardRepository;
import board_dk.board.service.BoardService;
import board_dk.board.validator.BoardValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardValidator boardValidator;

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

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id){
        if (id == null) {
        model.addAttribute("board",new Board());
        }else{
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board",board);
        }
        return "board/form";
    }

    @PostMapping("/form")
    public String postForm(@Valid Board board, BindingResult bindingResult, Authentication authentication){
     boardValidator.validate(board, bindingResult);
      if(bindingResult.hasErrors()){
          return "board/form";
      }
//       밑의 내용과 같다
//        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
//        사용자 user id를 담아서 게시글 게시시 인증된 회원 아이디로 저장
        boardService.save(username, board);

        boardRepository.save(board);
        return "redirect:/board/list";
    }
}
