package com.interview.prep.api;

import com.interview.prep.content.ContentLoader;
import com.interview.prep.content.ContentLoader.LoadResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
class AdminController {

    private final ContentLoader loader;

    AdminController(ContentLoader loader) {
        this.loader = loader;
    }

    @PostMapping("/reload-content")
    public LoadResult reload() {
        return loader.reload();
    }
}
