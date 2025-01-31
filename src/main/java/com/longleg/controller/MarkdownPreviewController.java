package com.longleg.controller;

import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Controller
public class MarkdownPreviewController {

    @GetMapping("/markdown")
    public String getMarkdownPreview(Model model) throws Exception {
        // 마크다운 파일 읽기
        Path filePath = new ClassPathResource("static/TODO.md").getFile().toPath();
        String markdown = Files.readString(filePath);

        // 확장 기능 활성화: Task List (체크박스)
        Parser parser = Parser.builder()
                .extensions(Arrays.asList(TaskListExtension.create()))
                .build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(Arrays.asList(TaskListExtension.create()))
                .build();

        // 마크다운 -> HTML 변환
        String htmlContent = renderer.render(parser.parse(markdown));

        // HTML을 모델에 추가
        model.addAttribute("content", htmlContent);
        return "markdown"; // markdown-preview.html 템플릿 반환
    }
}