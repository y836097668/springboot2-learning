package com.sherlocky.springboot2.shirojwt.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: zhangcx
 * @date: 2019/10/21 16:49
 */
@RestController
@RequestMapping("/index")
public class IndexController extends BaseController {

    @RequestMapping("")
    public String index() {
        return "首页~";
    }
}
