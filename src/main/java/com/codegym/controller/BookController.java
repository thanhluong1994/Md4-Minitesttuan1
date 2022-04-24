package com.codegym.controller;


import com.codegym.model.Book;
import com.codegym.model.BookForm;
import com.codegym.model.Category;
import com.codegym.service.book.IBookService;
import com.codegym.service.category.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller
public class BookController {
    @Autowired
    private IBookService bookService;

    @Autowired
    private ICategoryService categoryService;

    @Value("${file-upload}")
    private String fileUpload;

    @ModelAttribute("category")
    public Iterable<Category> categories(){
        return categoryService.findAll();
    }

    @GetMapping("/books")
    public ModelAndView listBook(@RequestParam("search") Optional<String> search,Pageable pageable) {
        Page<Book> books;
        if (search.isPresent()) {
            books = bookService.findAllByName(search.get(), pageable);
        } else {
            books = bookService.findAll(pageable);
        }
        ModelAndView modelAndView = new ModelAndView("/book/list");
        modelAndView.addObject("books", books);
        return modelAndView;
    }
//Them sach
    @GetMapping("/create-book")
    public ModelAndView showCreateForm(){
        ModelAndView modelAndView=new ModelAndView("/book/create");
        modelAndView.addObject("bookForm",new BookForm());
        return modelAndView;
    }

    @PostMapping("create-book")
    public ModelAndView saveBook(BookForm bookForm){
        MultipartFile multipartFile=bookForm.getAvatar();
        String fileName= multipartFile.getOriginalFilename();
        String name=bookForm.getName();
        String author=bookForm.getAuthor();
        int price=bookForm.getPrice();
        Category category=bookForm.getCategory();
        try {
            FileCopyUtils.copy(bookForm.getAvatar().getBytes(),new File(fileUpload+fileName));
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        Book book=new Book(name,price,author,fileName,category);
        bookService.save(book);
        ModelAndView modelAndView=new ModelAndView("/book/create");
        modelAndView.addObject("bookForm",new BookForm());
        modelAndView.addObject("message","New book created successfully!");
        return modelAndView;
    }
 //Sua sach
    @GetMapping("/edit-book/{id}")
    public ModelAndView showEditForm(@PathVariable Long id){
        Optional<Book> book=bookService.findById(id);
        if (book.isPresent()){
            ModelAndView modelAndView=new ModelAndView("/book/edit");
            modelAndView.addObject("book",book.get());
            return modelAndView;
        }else {
            ModelAndView modelAndView=new ModelAndView("/book/error.404");
            return modelAndView;
        }
    }
    @PostMapping("/edit-book")
    public ModelAndView updateBook(@ModelAttribute("book") Book book){
        bookService.save(book);
        ModelAndView modelAndView=new ModelAndView("/book/edit");
        modelAndView.addObject("book",book);
        modelAndView.addObject("message","Book updated successfully!");
        return modelAndView;
    }
//Xoa sach
    @GetMapping("/delete-book/{id}")
    public ModelAndView showDeleteForm(@PathVariable Long id){
        Optional<Book> book=bookService.findById(id);
        if (book.isPresent()){
            ModelAndView modelAndView=new ModelAndView("/book/delete");
            modelAndView.addObject("book",book.get());
            return modelAndView;
        }else {
            ModelAndView modelAndView=new ModelAndView("/book/error.404");
            return modelAndView;
        }
    }


    @PostMapping("/delete-book")
    public String deleteBook(@ModelAttribute("book") Book book){
        bookService.remove(book.getId());
        return "redirect:books";
    }
}
